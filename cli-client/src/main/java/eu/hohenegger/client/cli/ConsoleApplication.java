package eu.hohenegger.client.cli;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
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
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
        if(commands.isEmpty()) {
            LOGGER.error("At least one command expected. Found none.");
            return;
        }
        final String command = commands.iterator().next();
        if(!"wind".equals(command)) {
            return;
        }
        final Set<String> optionNames = args.getOptionNames();
        if(optionNames.isEmpty()) {
            LOGGER.error("At least one option expected. Found none.");
            return;
        }
        
        final File workingDir = new File(System.getProperty("user.dir"));
        String firstOption = optionNames.iterator().next();
        if(!"appid".equals(firstOption)) {
            return;
        }
        String optionValue = getMandatoryOptionValue(firstOption);

        LOGGER.info("Hello World: {} - {} - {} - {}" + command, optionValue, workingDir, properties.getBackend(), "");

        DefaultApi developersApi = new DefaultApi();

        // Create an interceptor which catches requests and logs the info you want
        Interceptor logRequests= new Interceptor() {

            @Override
            public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request();

                    LOGGER.info("Sending request {} on {}{}", 
                        request.url(), chain.connection(), request.headers());

                    Response response = chain.proceed(request);

                    LOGGER.info("Received response for {} - {}",
                        response.request().url(), response.headers());

                    return response;
            }
        
        };

        OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(logRequests)
            .build();

        ApiClient apiClient = new ApiClient(client);
        apiClient.setBasePath(properties.getBackend().toString());
        developersApi.setApiClient(apiClient);
        Forecasts forecasts = developersApi.getForecasts("6556328", optionValue, "json", "metric", "en");
        Map<String, Set<Forecast>> forecastByDate = forecasts
            .getList()
            .stream()
            .collect(groupingBy(fc -> fc.getDtTxt().substring(0, 10), toSet()));
        for (String date : forecastByDate.keySet()) {
            LOGGER.info("Date: {}" + date, "");
            for (Forecast forecast : forecastByDate.get(date)) {
                Wind wind = forecast.getWind();
                LOGGER.info("Time: {}" + forecast.getDtTxt().substring(10), "");
                LOGGER.info("Wind speed: {}" + wind.getSpeed(), "");
                LOGGER.info("Wind direction: {}" + wind.getDeg(), "");
            }
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
