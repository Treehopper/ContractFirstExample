package eu.hohenegger.client.cli;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ ClientConfigurationProperties.class })
public class ConsoleApplication implements ApplicationRunner {

    private static final int EXPECTED_CMD_COUNT = 1;

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

        final Set<String> optionNames = args.getOptionNames();
        
        final File workingDir = new File(System.getProperty("user.dir"));
        
        String optionValue = getMandatoryOptionValue(optionNames.iterator().next());
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
