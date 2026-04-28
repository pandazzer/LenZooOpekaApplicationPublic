package OpekaLenZooApplication.OpekaLenZooApplication.CreateNewContract;

import OpekaLenZooApplication.OpekaLenZooApplication.CreateNewContract.Exeption.ExistFileException;
import OpekaLenZooApplication.OpekaLenZooApplication.CreateNewContract.Exeption.IncorrectException;
import OpekaLenZooApplication.OpekaLenZooApplication.UpdateDB2.Enteties.DataForContracts;
import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.RuleBasedNumberFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class HandlerDocx {

    private final RuleBasedNumberFormat nf = new RuleBasedNumberFormat(Locale.forLanguageTag("ru"),
            RuleBasedNumberFormat.SPELLOUT);
    protected final List<String> MONTH = new ArrayList<>();
    protected boolean ignoreExistFileException = false;
    protected DataForContracts dataForContracts;

    public HandlerDocx(){
        MONTH.add("");
        MONTH.add("январь");
        MONTH.add("февраль");
        MONTH.add("март");
        MONTH.add("апрель");
        MONTH.add("май");
        MONTH.add("июнь");
        MONTH.add("июль");
        MONTH.add("август");
        MONTH.add("сентябрь");
        MONTH.add("октябрь");
        MONTH.add("ноябрь");
        MONTH.add("декабрь");
    }

    abstract void replaceSampleAndSave() throws IOException, IncorrectException, ExistFileException;

    protected String getWordRuble(int ruble) {
        return String.format("%s (%s) %s",
                getSplitSumString(ruble),
                nf.format(ruble),
                getCorrectRuble(ruble));
    }

    protected String getCorrectRuble(int sum) {
        int number = sum % 100;
        if (number > 4 && number < 20) return "рублей";
        switch (number % 10) {
            case (1) -> {
                return "рубль";
            }
            case (2), (3), (4) -> {
                return "рубля";
            }
            default -> {
                return "рублей";
            }
        }
    }

    protected String getSplitSumString(int sum){
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        return decimalFormat.format(sum);
    }

    public void setIgnoreExistFileException(boolean ignoreExistFileException) {
        this.ignoreExistFileException = ignoreExistFileException;
    }

    public void setDataForContracts(DataForContracts dataForContracts) {
        this.dataForContracts = dataForContracts;
    }
}
