package OpekaLenZooApplication.OpekaLenZooApplication.Controllers;

import OpekaLenZooApplication.OpekaLenZooApplication.Constants;
import OpekaLenZooApplication.OpekaLenZooApplication.CreateNewContract.CreateDocxService;
import OpekaLenZooApplication.OpekaLenZooApplication.CreateNewContract.Exeption.ExistFileException;
import OpekaLenZooApplication.OpekaLenZooApplication.CreateNewContract.Exeption.IncorrectException;
import OpekaLenZooApplication.OpekaLenZooApplication.SortByComp.DeleteScanInDirectory;
import OpekaLenZooApplication.OpekaLenZooApplication.SortByComp.RenamePNGFiles;
import OpekaLenZooApplication.OpekaLenZooApplication.SortByComp.SortService;
import OpekaLenZooApplication.OpekaLenZooApplication.UpdateDB2.Exeption.NotFoundDataForContract;
import OpekaLenZooApplication.OpekaLenZooApplication.UpdateDB2.Exeption.UncorrectedVisitPerson;
import OpekaLenZooApplication.OpekaLenZooApplication.UpdateDB2.ServiceDB;
import OpekaLenZooApplication.OpekaLenZooApplication.zooMailing.ENUM.StatusBookkeeping;
import OpekaLenZooApplication.OpekaLenZooApplication.zooMailing.ENUM.StatusCurator;
import OpekaLenZooApplication.OpekaLenZooApplication.zooMailing.H2Repository;
import OpekaLenZooApplication.OpekaLenZooApplication.zooMailing.POJO.BookkeepingExist;
import OpekaLenZooApplication.OpekaLenZooApplication.zooMailing.POJO.CuratorsBookkeeping;
import OpekaLenZooApplication.OpekaLenZooApplication.zooMailing.ServiceMail;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

@Component
@FxmlView("/maket/12.fxml")
public class GenController {

    private final ObservableList<String> choiceBoxList = FXCollections
            .observableArrayList("Акты сверок", "Акты об оказании услуг", "Счета", "Счета-факутры");

    @FXML
    private TextArea blackListArea;

    @FXML
    private Button cr;

    @FXML
    private Button createCurratorsListButton;

    @FXML
    private TextArea curratorsArea;

    @FXML
    private TextArea dataArea;

    @FXML
    private TextField directoryMailFilesField;

    @FXML
    private TextField fileToPathTextField;

    @FXML
    private Button getDataButton;

    @FXML
    private TextArea logText;
    static TextArea staticLogText;

    @FXML
    private TextArea mailTextField;

    @FXML
    private Button renameButton;

    @FXML
    private ProgressBar renameProgBar;

    @FXML
    private Button sendButton;

    @FXML
    private ProgressBar sendMailProgressBar;

    @FXML
    private Button sortButton;

    @FXML
    private ProgressBar sortProgBar;

    @FXML
    private TextField subjectField;

    @FXML
    private TextField txtcr;

    @FXML
    private CheckBox updateDbCheck;
    @FXML
    private ChoiceBox<String> chBox;
    @FXML
    private Button clearLogs;
    @FXML
    private Button deleteScanButton;
    @FXML
    private Button createListKit;

    @Autowired
    ServiceDB serviceDB;
    @Autowired
    H2Repository repository;
    @Autowired
    ServiceMail serviceMail;
    @Autowired
    CreateDocxService createService;

    @FXML
    void initialize() {

        staticLogText = logText;
        chBox.setItems(choiceBoxList);
        renameButton.setOnAction(e -> handleRenameButton());
        sortButton.setOnAction(e -> handleSortButton());
        createListKit.setOnAction(e -> handleCreateListKit());
        createCurratorsListButton.setOnAction(e -> handleCuratorsListButton());
        sendButton.setOnAction(e -> handleSendButton());
        getDataButton.setOnAction(e -> handleGetDataButton());
        cr.setOnAction(e -> handleCreateButton());
        clearLogs.setOnAction(e -> logText.clear());
        deleteScanButton.setOnAction(e -> handleDeleteScan());

    }


    private void handleDeleteScan() {
        String path = fileToPathTextField.getText();
        if (path.equals("")) {
            showAlert("Нет пути!");
            return;
        }
        new Thread(new DeleteScanInDirectory(this, path)).start();
    }

    private void handleCreateButton() {
        try {
            createService.createContract(txtcr.getText());
            showAlert("Договор создан!");
        } catch (NotFoundDataForContract e) {
            showAlert("Ошибка создания договора" + e);
        } catch (ExistFileException e) {
            if (getYesNoButton(e.getMessage() + " Перезаписать?").get() == ButtonType.YES) {
                forceCreateContract();
            }
        } catch (IncorrectException | UncorrectedVisitPerson e) {
            showAlert(e.getMessage());
        }
    }

    private void forceCreateContract() {
        try {
            createService.setIgnoreExistFileException(true);
            createService.createContract(txtcr.getText());
            createService.setIgnoreExistFileException(false);
            showAlert("Договор создан!");
        } catch (ExistFileException ignored) {
        } catch (NotFoundDataForContract e) {
            throw new RuntimeException(e);
        } catch (IncorrectException | UncorrectedVisitPerson e) {
            showAlert(e.getMessage());
        }


    }

    private void handleGetDataButton() {
        for (String str : repository.getData()) {
            dataArea.appendText(str + "\n");
        }
    }

    private void handleSendButton() {
        String[] blackList = blackListArea.getText().split("\n");
        HashSet<String> blackListSet = new HashSet<>(Arrays.asList(blackList));
        if (isEmptyFieldsForHandleSendButton()) {
            showAlert(Constants.EMPTY_FIELD);
            return;
        }
        serviceMail.setSubject(subjectField.getText());
        serviceMail.setText(mailTextField.getText());
        serviceMail.setBlackList(blackListSet);
        serviceMail.setBookkeepingList(directoryMailFilesField.getText().split(","));
        serviceMail.startService(this);
    }

    private boolean isEmptyFieldsForHandleSendButton() {
        return subjectField.getText().isEmpty()
                || mailTextField.getText().isEmpty()
                || isEmptyFieldsForHandleCuratorsListButton();
    }

    private boolean isEmptyFieldsForHandleCuratorsListButton() {
        return directoryMailFilesField.getText().isEmpty();
    }

    private void handleCuratorsListButton() {
        curratorsArea.clear();
        updateDB();
        if (isEmptyFieldsForHandleCuratorsListButton()) {
            showAlert(Constants.EMPTY_FIELD);
            return;
        }
        appendCorrectCuratorsToArea();
        sendButton.setDisable(false);
    }

    private void updateDB() {
        if (!updateDbCheck.isSelected()) {
            serviceDB.start();
            updateDbCheck.setSelected(true);
        }
    }

    private void appendCorrectCuratorsToArea() {
        String[] bookkeepingList = directoryMailFilesField.getText().split(",");
        String[] blackList = Stream.of(blackListArea.getText().split("\n")).map(String::trim).toArray(String[]::new);
        HashSet<String> blackListSet = new HashSet<>(Arrays.asList(blackList));
        List<CuratorsBookkeeping> curatorsBookkeepingList = serviceMail.getFoundCorrectCurators(bookkeepingList, blackListSet);

        for (CuratorsBookkeeping curatorsBookkeeping : curatorsBookkeepingList) {
            String name = curatorsBookkeeping.curator().getName();
            if (Objects.requireNonNull(curatorsBookkeeping.status()) == StatusCurator.NO_MAIL) {
                curratorsArea.appendText("(0)-");
                showAlert(name + " " + Constants.NOT_MAIL_MESSAGE);
            }
            curratorsArea.appendText(name + "\n");
            for (BookkeepingExist bookkeepingExist : curatorsBookkeeping.bookkeeping()) {
                if (bookkeepingExist.status() == StatusBookkeeping.ALREADY_SEND) {
                    curratorsArea.appendText("      (1)-");
                }
                curratorsArea.appendText(bookkeepingExist.bookkeeping() + "\n");
            }
        }
    }

    private void handleCreateListKit() {
        curratorsArea.clear();
        appendNotKitCuratorsToArea();
    }

    private void appendNotKitCuratorsToArea() {
        List<String> bookkeepingList = List.of(directoryMailFilesField.getText().split(","));
        for (File curatorDir : Objects.requireNonNull(new File(Constants.curatorsDirectoryPath).listFiles())) {
            List<String> bookkeepingNotExist = new ArrayList<>();
            boolean isBookkeepingExist = false;
            int countExist = 0;
            for (String bookkeeping : bookkeepingList) {
                File curBookkeeping = new File(curatorDir.getPath() + "/" + bookkeeping);
                if (curBookkeeping.exists()) {
                    isBookkeepingExist = true;
                    countExist++;
                } else {
                    bookkeepingNotExist.add(bookkeeping.replace("\\", "!").split("!")[1]);
                }
            }
            if (isBookkeepingExist && countExist != bookkeepingList.size()) {
                curratorsArea.appendText(String.format("%s нет:" + bookkeepingNotExist + "\n", curatorDir.getName()));
            }
        }
    }

    private void handleSortButton() {
        String path = fileToPathTextField.getText();
        if (path.equals("")) {
            showAlert("Нет пути!");
            return;
        }
        new Thread(new SortService(this, path)).start();
    }

    private void handleRenameButton() {
        String path = fileToPathTextField.getText();
        String chBoxString = chBox.getValue();
        if (path.equals("") || chBoxString == null) {
            showAlert("Нет пути или формата документа!");
            return;
        }
        new Thread(new RenamePNGFiles(this, path, chBoxString)).start();
    }

    private void showAlert(String text) {
        new Alert(Alert.AlertType.INFORMATION, text).showAndWait();
    }

    private Optional<ButtonType> getYesNoButton(String text) {
        return new Alert(Alert.AlertType.WARNING
                , text
                , ButtonType.YES
                , ButtonType.NO)
                .showAndWait();
    }

    private void showWarning(String text) {
        new Alert(Alert.AlertType.WARNING, text).showAndWait();
    }

    public void setRenameProgBar(double prog) {
        renameProgBar.setProgress(prog);
    }

    public void addLogText(String log) {
        logText.appendText(log + "\n");
    }

    public void setSortProgBar(double progBar) {
        sortProgBar.setProgress(progBar);
    }

    public void setSendMailProgressBar(double progBar) {
        sendMailProgressBar.setProgress(progBar);
    }
}

