package com.sellertool.auth_server.utils;

import com.sellertool.auth_server.domain.exception.dto.NotMatchedFormatException;

import java.util.regex.Pattern;

public class DataFormatUtils {

    public static void checkPhoneNumberFormat(String number) {
        boolean isPhoneNumberFormat = Pattern.compile("^01(?:0|1|[6-9])([0-9]{3,4})([0-9]{4})$").matcher(number).find();

        if (!isPhoneNumberFormat) {
            throw new NotMatchedFormatException("전화번호 형식이 올바르지 않습니다.");
        }
    }

    public static void checkEmailFormat(String email) {
        boolean isEmailAddressFormat = Pattern.compile("^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@([0-9a-zA-Z]+\\.)+[0-9a-zA-Z]{2,8}$").matcher(email).find();

        if (!isEmailAddressFormat) {
            throw new NotMatchedFormatException("이메일 형식이 올바르지 않습니다.");
        }
    }
}
