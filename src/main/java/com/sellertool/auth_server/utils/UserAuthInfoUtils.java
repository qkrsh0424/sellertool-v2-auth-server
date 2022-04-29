package com.sellertool.auth_server.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class UserAuthInfoUtils {

    @Value("${naver.cloud.platform.request.mail.api}")
    private static String MAIL_REQUEST_API;

    @Value("${naver.cloud.platform.access}")
    private static String NAVER_CLOUD_PLATFORM_ACCESS_KEY;

    @Value("${naver.cloud.platform.secret}")
    private static String NAVER_CLOUD_PLATFORM_SECRET_KEY;


    @Value("${naver.cloud.platform.request.mail.api}")
    public void setMailRequestApi(String requestApi) {
        UserAuthInfoUtils.MAIL_REQUEST_API = requestApi;
    }

    @Value("${naver.cloud.platform.access}")
    public void setNaverCloudPlatformAccessKey(String accessKey) {
        UserAuthInfoUtils.NAVER_CLOUD_PLATFORM_ACCESS_KEY = accessKey;
    }

    @Value("${naver.cloud.platform.secret}")
    public void setNaverCloudPlatformSecretKey(String secretKey) {
        UserAuthInfoUtils.NAVER_CLOUD_PLATFORM_SECRET_KEY = secretKey;
    }

    public static HttpHeaders setApiRequestHeader(Long time) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-ncp-apigw-timestamp", time.toString());
        headers.set("x-ncp-iam-access-key", NAVER_CLOUD_PLATFORM_ACCESS_KEY);
        String sig = makeSignature(time);
        headers.set("x-ncp-apigw-signature-v2", sig);

        return headers;
    }

    public static String makeSignature(Long time) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        String newLine = "\n";
        String method = "POST";
        String space = " ";
        String url = MAIL_REQUEST_API;
        String accessKey = NAVER_CLOUD_PLATFORM_ACCESS_KEY;
        String secretKey = NAVER_CLOUD_PLATFORM_SECRET_KEY;

        String message = new StringBuilder()
                .append(method)
                .append(space)
                .append(url)
                .append(newLine)
                .append(time.toString())
                .append(newLine)
                .append(accessKey)
                .toString();

        SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(signingKey);

        byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
        String encodeBase64String = Base64.getEncoder().encodeToString(rawHmac);
        return encodeBase64String;
    }

    public static String generateAuthNumber() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }
}
