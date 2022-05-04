package com.sellertool.auth_server.config.naver;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("app.cloud.naver.platform")
public class NaverEmailConfiguration {
    private String accessKey;
    private String secretKey;
    private String mailRequestUrl;
    private String mailApiUri;

    public NaverEmailConfiguration() {
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getMailRequestUrl() {
        return mailRequestUrl;
    }

    public void setMailRequestUrl(String mailRequestUrl) {
        this.mailRequestUrl = mailRequestUrl;
    }

    public String getMailApiUri() {
        return mailApiUri;
    }

    public void setMailApiUri(String mailApiUri) {
        this.mailApiUri = mailApiUri;
    }
}
