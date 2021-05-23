package eu.hohenegger.client.cli;

import java.net.URI;

import javax.annotation.Nonnull;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cli-client", ignoreUnknownFields = false)
public class ClientConfigurationProperties {

    @Nonnull
    private URI backend;

    public URI getBackend() {
        return backend;
    }
    public void setBackend(URI backend) {
        this.backend = backend;
    }
}
