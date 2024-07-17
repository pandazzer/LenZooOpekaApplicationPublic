package OpekaLenZooApplication.OpekaLenZooApplication.CreateNewContract.Repository;

import OpekaLenZooApplication.OpekaLenZooApplication.Constants;
import OpekaLenZooApplication.OpekaLenZooApplication.CreateNewContract.Entety.CompanyFromDaData;
import OpekaLenZooApplication.OpekaLenZooApplication.CreateNewContract.Entety.CompanyShort;
import com.google.gson.Gson;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Component
public class RepositoryDaData {

    public CompanyShort getCorrectCompany(String inn) {
        String dataString = getDataFromINN(inn);

        Gson gson = new Gson();
        CompanyFromDaData companyFromDaData = gson.fromJson(dataString, CompanyFromDaData.class);
        return reduceInCompanyShort(companyFromDaData);
    }

    private String getDataFromINN(String inn) {
        try {
            URL url = new URL(Constants.urlDaData);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.addRequestProperty("Content-Type", "application/json");
            connection.addRequestProperty("Accept", "application/json");
            connection.addRequestProperty("Authorization", Constants.tokenDaData);
            connection.setDoOutput(true);
            OutputStream os = connection.getOutputStream();
            String body = "{ \"query\": \"" + inn + "\" }";
            os.write(body.getBytes());
            os.flush();
            os.close();
            InputStream is = connection.getInputStream();
            return streamToString(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private CompanyShort reduceInCompanyShort(CompanyFromDaData companyFromDaData) {
        CompanyFromDaData.SuggestionData data = companyFromDaData.getSuggestions()[0].getData();

        String fullCompanyName = data.getName().getFullWithOpf();
        String shortCompanyName = data.getName().getShortWithOpf();
        String ogrn = data.getOgrn();
        String okpo = data.getOkpo();
        String urAdress = data.getAddress().getValue();
        boolean isLegal = data.getType().equals("LEGAL");
        String name = "";
        String post = "";
        String kpp = null;

        if (isLegal) {
            CompanyFromDaData.Management management = data.getManagement();
            if (management != null) {
                name = management.getName();
                post = management.getPost();
            }
            kpp = data.getKpp();
        }
        String status = data.getState().getStatus();
        return new CompanyShort(name, post, fullCompanyName, shortCompanyName, kpp, ogrn,
                okpo, urAdress, isLegal, status);
    }

    private String streamToString(InputStream in) throws IOException {
        StringBuilder out = new StringBuilder();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1; ) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }
}
