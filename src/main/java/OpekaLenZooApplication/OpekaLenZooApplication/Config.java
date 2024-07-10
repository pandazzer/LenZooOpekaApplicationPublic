package OpekaLenZooApplication.OpekaLenZooApplication;

import OpekaLenZooApplication.OpekaLenZooApplication.UpdateDB2.Repository.RepositoryDB;
import OpekaLenZooApplication.OpekaLenZooApplication.UpdateDB2.Repository.RepositoryGoogleSheet;
import OpekaLenZooApplication.OpekaLenZooApplication.UpdateDB2.ServiceDB;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.spring.SpringFxWeaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableAsync
public class Config {
    @Autowired
    RepositoryGoogleSheet repositoryGoogleSheet;
    @Autowired
    RepositoryDB repositoryDB;

    @Bean
    public ServiceDB serviceDb() {
        return new ServiceDB(repositoryGoogleSheet, repositoryDB);
    }

    @Bean
    public FxWeaver fxWeaver(ConfigurableApplicationContext applicationContext) {
        return new SpringFxWeaver(applicationContext);
    }
    @Bean
    public void checkENV() throws EnvironmentNullException {
        Map<String, String> envMap = new HashMap<>();
        envMap.put("passwordEmail", Constants.password);
        envMap.put("pathDB", Constants.pathDB);
        envMap.put("curatorsDirectoryPath", Constants.curatorsDirectoryPath);
        envMap.put("spreadsheetId", Constants.spreadsheetId);
        envMap.put("tokenDaData", System.getProperty("tokenDaData"));
        for (Map.Entry<String, String> env : envMap.entrySet()) {
            if (env.getValue() == null || env.getValue().isEmpty()) {
                throw new EnvironmentNullException(env.getKey() + " = null or empty");
            }
            System.out.println(env.getValue());
        }
    }
}
