package com.sellertool.auth_server.config.twilio;

import com.twilio.Twilio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioSmsInitializer {
    private final TwilioSmsConfiguration twilioSmsConfiguration;

    @Autowired
    public TwilioSmsInitializer(TwilioSmsConfiguration twilioSmsConfiguration) {
        this.twilioSmsConfiguration = twilioSmsConfiguration;
        Twilio.init(twilioSmsConfiguration.getAccountSid(), twilioSmsConfiguration.getAuthToken());
    }
}
