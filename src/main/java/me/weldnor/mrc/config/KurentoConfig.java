package me.weldnor.mrc.config;

import org.kurento.client.KurentoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class KurentoConfig {

    @Value("${endpoints.kurento}")
    private String kurentoWsUrl;

    @Bean
    public KurentoClient kurentoClient() {
        return KurentoClient.create(kurentoWsUrl);
    }
}
