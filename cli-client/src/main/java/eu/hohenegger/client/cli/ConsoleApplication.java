package eu.hohenegger.client.cli;

import static java.util.Optional.ofNullable;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;

import org.moduliths.Modulith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import eu.hohenegger.contract.client.internal.api.DevelopersApi;
import eu.hohenegger.contract.client.internal.impl.ApiClient;
import eu.hohenegger.contract.client.internal.impl.ApiException;
import eu.hohenegger.contract.client.internal.impl.ApiResponse;
import eu.hohenegger.contract.client.internal.model.Forecasts;
import eu.hohenegger.contract.client.internal.model.Wind;

@Modulith
@SpringBootApplication
@EnableConfigurationProperties({ ClientConfigurationProperties.class })
public class ConsoleApplication implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleApplication.class);

    private ClientConfigurationProperties properties;

    private ApplicationArguments args;

    private final DevelopersApi api;

    @Autowired
    public ConsoleApplication(ClientConfigurationProperties properties) {
        this.properties = properties;
        ApiClient apiClient = new ApiClient();
        try {
            apiClient.updateBaseUri(properties.getBackend()
                    .toURL()
                    .toString());
        } catch (MalformedURLException e) {
            LOGGER.error("Invalid URI: " + properties.getBackend(), e);
        }
        api = new DevelopersApi(apiClient);
    }

    public static void main(String[] args) {
        SpringApplication.run(ConsoleApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.args = args;
        final List<String> commands = args.getNonOptionArgs();
        final String command = commands.iterator().next();

        final Set<String> optionNames = args.getOptionNames();
        
        final File workingDir = new File(System.getProperty("user.dir"));
        
        String cityId = getMandatoryOptionValue("cityid");
        String appId = getMandatoryOptionValue("appid");

        ApiResponse<Forecasts> withHttpInfo;
        try {
            withHttpInfo = api.getForecastsWithHttpInfo(cityId, appId, "json", "metric", "en");
            if (withHttpInfo.getStatusCode() != 200) {
                LOGGER.error("Unexpected status: " + withHttpInfo.getStatusCode());
                return;
            }
            System.out.println(
                        "Date, " +
                        "Speed (meter/sec), " +
                        "Degrees, " +
                        "Gusts (meter/sec), " +
                        "Probability of precipitation, " +
                        "Rain (Rain volume for last 3 hours - mm), " +
                        "Snow (Snow volume for last 3 hours), "
                    );
            withHttpInfo.getData().getList().forEach( forecast -> {
                Wind wind = forecast.getWind();
                System.out.println(
                        String.format("%s, %.2f, %03d, %.2f, %.2f, %.2f, %.2f",
                                forecast.getDtTxt(),
                                wind.getSpeed(),
                                wind.getDeg(),
                                wind.getGust(),
                                forecast.getPop(),
                                (ofNullable(forecast.getRain()).isPresent()) ? forecast
                                        .getRain().get3h() : 0,
                                        (ofNullable(forecast.getSnow()).isPresent()) ? forecast
                                                .getSnow().get3h() : 0)
                        );
            });
        } catch (ApiException e) {
            LOGGER.error("Unable to fetch data", e);
        }
    }

    public String getMandatoryOptionValue(String optionName) {
        final List<String> values = args.getOptionValues(optionName);
        final String result = values.get(0);
        if (result.isEmpty()) {
            throw new IllegalArgumentException("No value found for option: " + optionName);
        }
        return result;
    }
}
