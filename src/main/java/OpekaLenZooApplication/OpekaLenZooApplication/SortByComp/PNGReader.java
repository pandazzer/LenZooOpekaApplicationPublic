package OpekaLenZooApplication.OpekaLenZooApplication.SortByComp;


import OpekaLenZooApplication.OpekaLenZooApplication.Constants;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.io.File;

public class PNGReader {
    private final String path;
    private String text;

    public PNGReader(String path) {
        this.path = path;
        this.text = getTextFromPngFile();
    }


    public String getTextFromPngFile() {
        Tesseract tesseract = new Tesseract();
        String result = null;
        try {
            tesseract.setDatapath(Constants.tessPath);
            tesseract.setLanguage("rus");
            result = tesseract.doOCR(new File(path));
        } catch (TesseractException e) {
            e.printStackTrace();
        }
        return result.toLowerCase();
    }

    public String getNameCompanyActs() {
        int begin = text.indexOf("заказчик:");
        int start = text.indexOf(" ", begin);
        int end = text.indexOf("\n", start);
        return text.substring(start, end);
    }

    public String getNameCompanySchetFacture() {
        int begin = text.indexOf("покупатель:");
        int start = text.indexOf(" ", begin);
        int end = text.indexOf("\n", start);
        return text.substring(start, end);
    }

    public String getNameCompanySchet() {
        int begin = text.indexOf("заказчик");
        int start = text.indexOf(" ", begin);
        int end = text.indexOf("плательщик", start);
        String result = text.substring(start, end);
        if (result.contains(" инн ")) {
            return result.substring(0, result.indexOf(" инн "));
        }
        return text.substring(start, end);
    }

    public String getNameCompanyActSverki() {
        int begin = text.indexOf("\n", text.indexOf("между"));
        int start = text.indexOf(" ", begin);
        int end = text.indexOf("\n", start);
        return text.substring(start, end);
    }
}
