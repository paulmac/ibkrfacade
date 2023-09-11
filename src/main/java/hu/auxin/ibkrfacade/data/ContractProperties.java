package hu.auxin.ibkrfacade.data;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.data.redis")
public class ContractProperties {
    private String eurCash;
    private String eurCfd;
}
