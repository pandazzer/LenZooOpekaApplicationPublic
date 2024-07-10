package OpekaLenZooApplication.OpekaLenZooApplication;

public interface Constants {
    String pathDB = "jdbc:h2:file:/H2DataBase/db";
    ///DBtest/dbtest - test
    ///H2DataBase/db - prod
    String curatorsDirectoryPath = "Z:/Опека/Опекуны";
    //Z:/Опека/ОпекуныТест - test
    //Z:/Опека/Опекуны - prod
    String spreadsheetId = "19IJPlFe8AR8p02SnhIyTup029njfG7ZEJAWAYUN8R7A";
    //1UvNOnXH-wlaL9QHEW82uHBX2rX_VCejm9N95h9SNYhE - test
    //19IJPlFe8AR8p02SnhIyTup029njfG7ZEJAWAYUN8R7A - prod
    String password = System.getProperty("passwordEmail");
    String tokenDaData = "Token " + System.getProperty("tokenDaData");
    String NEW_CONTRACTS_PATH = "Z:/Опека/Договоры/";
    String login = "opeka@spbzoo.ru";
    String userDB = "sa";
    String passDB = "password";
    String CREDENTIALS_FILE_PATH = "TokenV2/token.json";
    String exampleUrFace = "SampleDocx/Спонсорский ПАКЕТ_2024.docx";
    String exampleInFace = "SampleDocx/Опека ПАКЕТ_2024.docx";
    String iconPath = "/gif/icon.jpg";
    String loadWindowXMLpath = "/maket/loadView.fxml";
    String nameApplication = "Опека";
    String tessPath = "./Tess4J/tessdata";
    String urlDaData = "https://suggestions.dadata.ru/suggestions/api/4_1/rs/findById/party";
    String APPLICATION_NAME = "Application Name";

    String FILE_ALREADY_EXIST = "Файл уже существует";
    String VisitPersonExeption = "Некорректное имя подписанта";
    String NOT_MAIL_MESSAGE = "Почта не найдена в базе";
    String EMPTY_FIELD = "Пустое поле";
}

