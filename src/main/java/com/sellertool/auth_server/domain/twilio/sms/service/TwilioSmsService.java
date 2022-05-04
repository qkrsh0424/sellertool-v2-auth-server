package com.sellertool.auth_server.domain.twilio.sms.service;

import com.sellertool.auth_server.config.twilio.TwilioSmsConfiguration;
import com.sellertool.auth_server.domain.twilio.sms.dto.TwilioSmsRequestDto;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TwilioSmsService {
    private final TwilioSmsConfiguration twilioSmsConfiguration;

    public void sendSms(TwilioSmsRequestDto smsRequestDto) {
        PhoneNumber sendPhoneNumber = new PhoneNumber(smsRequestDto.getSendPhoneNumber());
        PhoneNumber fromPhoneNumber = new PhoneNumber(twilioSmsConfiguration.getFromNumber());

        Message.creator(sendPhoneNumber, fromPhoneNumber, smsRequestDto.getSendMessage()).create();
    }

    public void sendSmsAsync(TwilioSmsRequestDto smsRequestDto) {
        PhoneNumber sendPhoneNumber = new PhoneNumber(smsRequestDto.getSendPhoneNumber());
        PhoneNumber fromPhoneNumber = new PhoneNumber(twilioSmsConfiguration.getFromNumber());

        Message.creator(sendPhoneNumber, fromPhoneNumber, smsRequestDto.getSendMessage()).createAsync();
    }
}
