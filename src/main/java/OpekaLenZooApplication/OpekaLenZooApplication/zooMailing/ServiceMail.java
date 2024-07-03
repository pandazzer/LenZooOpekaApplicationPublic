package OpekaLenZooApplication.OpekaLenZooApplication.zooMailing;


import OpekaLenZooApplication.OpekaLenZooApplication.Constants;
import OpekaLenZooApplication.OpekaLenZooApplication.Controllers.GenController;
import OpekaLenZooApplication.OpekaLenZooApplication.zooMailing.ENUM.StatusBookkeeping;
import OpekaLenZooApplication.OpekaLenZooApplication.zooMailing.ENUM.StatusCurator;
import OpekaLenZooApplication.OpekaLenZooApplication.zooMailing.POJO.BookkeepingExist;
import OpekaLenZooApplication.OpekaLenZooApplication.zooMailing.POJO.CuratorsBookkeeping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.File;
import java.util.*;

@Service
public class ServiceMail {
    @Autowired
    private H2Repository repository;
    private final Logger log = LogManager.getLogger();
    private List<CuratorsBookkeeping> foundCorrectCurators;
    private String[] bookkeepingList;
    private String subject;
    private String text;
    private HashSet<String> blackList;

    @Async
    public void startService(GenController genController) {
        SmtpSendMessage sendMessage = new SmtpSendMessage();
        repository.addColumn(List.of(bookkeepingList));
        int correctSendCount = 0;
        int alreadySendCount = 0;
        double sumCount = 0;
        int allFilesCount = foundCorrectCurators.size();
        for (CuratorsBookkeeping curatorsBookkeeping : foundCorrectCurators) {
            sumCount++;
            if (curatorsBookkeeping.status() != StatusCurator.OK) continue;
            String name = curatorsBookkeeping.curator().getName();
            String address = curatorsBookkeeping.mailAddress();
            List<File> listFilePath = getListFilePath(curatorsBookkeeping);
            if (!listFilePath.isEmpty()) {
                try {
                    sendMessage.send(address, subject, text, listFilePath);
                    log.warn(name + " -> ");
                    listFilePath.stream().map(File::getName).forEach(log::warn);
                    genController.addLogText(name + " -> ");
                    listFilePath.stream().map(File::getName).forEach(genController::addLogText);
                    setSendCorrect(name, curatorsBookkeeping.bookkeeping());
                    correctSendCount++;
                } catch (MessagingException e) {
                    genController.addLogText(String.format(name + " -> "));
                    genController.addLogText(String.format("%-50s - Ошибка отправки!!!", address));
                    log.warn(String.format("%-50s - Ошибка отправки!!!", address));
                    throw new RuntimeException(e);
                }
                log.warn(String.format("%-50s - Отправка завершена", address));
            }
            genController.setSendMailProgressBar(sumCount / allFilesCount);
        }
        genController.addLogText(String.format("%d/%d - успешно отправлено%n%d - уже было отправлено"
                , correctSendCount
                , allFilesCount
                , alreadySendCount));
    }

    private List<File> getListFilePath(CuratorsBookkeeping curatorsBookkeeping) {
        List<File> listFiles = new ArrayList<>();
        for (BookkeepingExist bookkeeping : curatorsBookkeeping.bookkeeping()) {
            if (bookkeeping.status() == StatusBookkeeping.ALREADY_SEND) continue;
            File curBookkeeping = new File(curatorsBookkeeping.curator().getPath() + "\\" + bookkeeping.bookkeeping());
            for (File file : Objects.requireNonNull(curBookkeeping.listFiles())) {
                if (file.getName().equals("Thumbs.db")) {
                    continue;
                }
                listFiles.add(file);
            }
        }
        return listFiles;
    }

    private void setSendCorrect(String name, List<BookkeepingExist> bookkeepingList) {
        for (BookkeepingExist bookkeeping : bookkeepingList) {
            repository.setBooleanTrueWithColumn(name, bookkeeping.bookkeeping().replace("\\", "_"));
        }
    }

    private void findCorrectCurators() {
        foundCorrectCurators = new ArrayList<>();
        StatusCurator statusCurator;
        for (File curatorDir : Objects.requireNonNull(new File(Constants.curatorsDirectoryPath).listFiles())) {

            String path = curatorDir.getName();
            List<BookkeepingExist> bookkeepingExistList = new ArrayList<>();
            if (!blackList.isEmpty() && blackList.contains(curatorDir.getName())) continue;

            StatusBookkeeping statusBookkeeping;
            for (String bookkeeping : bookkeepingList) {
                statusBookkeeping = StatusBookkeeping.OK;
                File curBookkeeping = new File(curatorDir.getPath() + "/" + bookkeeping);
                if (curBookkeeping.exists()) {

                    if (repository.isSend(path, bookkeeping.replace("\\", "_"))) {
                        statusBookkeeping = StatusBookkeeping.ALREADY_SEND;
                    }
                    bookkeepingExistList.add(new BookkeepingExist(bookkeeping, statusBookkeeping));
                }
            }
            String email = null;
            statusCurator = StatusCurator.OK;
            try {
                email = repository.getMailByPath(path);
            } catch (NotMailException e) {
                statusCurator = StatusCurator.NO_MAIL;
            }
            if (!bookkeepingExistList.isEmpty()) {
                foundCorrectCurators.add(new CuratorsBookkeeping(curatorDir, bookkeepingExistList, email, statusCurator));
            }
        }
    }

    public void setBookkeepingList(String[] bookkeepingList) {
        this.bookkeepingList = bookkeepingList;
    }

    public List<CuratorsBookkeeping> getFoundCorrectCurators(String[] bookkeepingList, HashSet<String> blackListSet) {
        setBlackList(blackListSet);
        setBookkeepingList(bookkeepingList);
        findCorrectCurators();
        return foundCorrectCurators;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setBlackList(HashSet<String> blackList) {
        this.blackList = blackList;
    }
}
