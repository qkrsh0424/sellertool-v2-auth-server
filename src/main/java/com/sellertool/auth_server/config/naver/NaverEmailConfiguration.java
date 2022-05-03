package com.sellertool.auth_server.config.naver;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("naver.cloud.platform")
public class NaverEmailConfiguration {
    private String accessKey;
    private String secretKey;
    private String requestUrl;
    private String requestMailApi;

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

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getRequestMailApi() {
        return requestMailApi;
    }

    public void setRequestMailApi(String requestMailApi) {
        this.requestMailApi = requestMailApi;
    }
}
