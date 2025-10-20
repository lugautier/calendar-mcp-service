package com.enterprise.calendar.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "microsoft.graph")
@Data
public class GraphProperties {

    private String tenantId;
    private String clientId;
    private String clientSecret;
    private String scope;
}
