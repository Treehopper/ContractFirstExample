package eu.hohenegger.client.cli;

import java.io.File;
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

import eu.hohenegger.contract.client.internal.api.DefaultApi;
import eu.hohenegger.contract.client.internal.impl.ApiClient;
import eu.hohenegger.contract.client.internal.model.Forecast;
import eu.hohenegger.contract.client.internal.model.Forecasts;
import eu.hohenegger.contract.client.internal.model.Wind;

@Modulith
@SpringBootApplication
@EnableConfigurationProperties({ ClientConfigurationProperties.class })
public class ConsoleApplication implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleApplication.class);

    private ClientConfigurationProperties properties;

    private ApplicationArguments args;

    @Autowired
    public ConsoleApplication(ClientConfigurationProperties properties) {
        this.properties = properties;
    }

    public static void main(String[] args) {
        SpringApplication.run(ConsoleApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.args = args;
        final List<String> commands = args.getNonOptionArgs();
        final String command = commands.iterator().next();
        if(!"wind".equals(command)) {
            return;
        }
        final Set<String> optionNames = args.getOptionNames();
        
        final File workingDir = new File(System.getProperty("user.dir"));
        String firstOption = optionNames.iterator().next();
        if(!"appid".equals(firstOption)) {
            return;
        }
        String optionValue = getMandatoryOptionValue(firstOption);

        LOGGER.info("Hello World: {} - {} - {} - {}" + command, optionValue, workingDir, properties.getBackend(), "");

        DefaultApi developersApi = new DefaultApi();
        Forecasts forecasts = developersApi.getForecasts("6556328", optionValue, "json", "metric", "en");
        for (Forecast forecast : forecasts.getList()) {
            Wind wind = forecast.getWind();
            LOGGER.info("Wind speed: {}" + wind.getSpeed(), "");
            LOGGER.info("Wind direction: {}" + wind.getDeg(), "");
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
