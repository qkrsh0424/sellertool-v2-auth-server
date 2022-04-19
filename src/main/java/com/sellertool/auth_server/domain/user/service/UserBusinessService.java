package com.sellertool.auth_server.domain.user.service;

import com.sellertool.auth_server.domain.exception.dto.AccessDeniedPermissionException;
import com.sellertool.auth_server.domain.exception.dto.InvalidUserAuthException;
import com.sellertool.auth_server.domain.exception.dto.NotMatchedFormatException;
import com.sellertool.auth_server.domain.user.dto.UserDto;
import com.sellertool.auth_server.domain.user.entity.UserEntity;
import com.sellertool.auth_server.domain.user.vo.UserVo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserBusinessService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Object getInfoOwn() {
        if (!userService.isLogin()) {
            throw new InvalidUserAuthException("로그인이 필요한 서비스 입니다.");
        }

        UUID USER_ID = userService.getUserId();
        UserVo userVo = UserVo.toVo(userService.searchUserByUserId(USER_ID));
        return userVo;
    }

    @Transactional(readOnly = true)
    public Object checkUsernameDuplicate(Map<String, Object> params) {
        Object usernameObj = params.get("username");
        Map<String, Object> resultData = new HashMap<>();

        if (usernameObj == null) {
            resultData.put("isEmpty", true);
            resultData.put("isDuplicated", false);
        }
        String USERNAME = usernameObj.toString();

        if (!userService.isDuplicatedUsername(USERNAME)) {
            resultData.put("isEmpty", false);
            resultData.put("isDuplicated", false);
        } else {
            resultData.put("isEmpty", false);
            resultData.put("isDuplicated", true);
        }
        return resultData;
    }

    /*
    로그인 체크
    자신의 요청인지 체크
    엔터티 불러오기
    업데이트 데이터 셋
     */
    @Transactional
    public void updateInfoOwn(UserDto userDto) {
        /*
        로그인 체크
         */
        if (!userService.isLogin()) {
            throw new InvalidUserAuthException("로그인이 필요한 서비스 입니다.");
        }
        UUID USER_ID = userService.getUserId();

        /*
        자신의 요청인지 체크
         */
        if (!userDto.getId().equals(USER_ID)) {
            throw new AccessDeniedPermissionException("잘못된 접근 방법 입니다.");
        }

        /*
        엔터티 불러오기
         */
        UserEntity userEntity = userService.searchUserByUserId(USER_ID);

        /*
        업데이트 데이터 셋 (영속성 업데이트)
         */
        userEntity.setNickname(userDto.getNickname());
        userEntity.setEmail(userDto.getEmail());
        userEntity.setName(userDto.getName());
        userEntity.setPhoneNumber(userDto.getPhoneNumber());

    }

    /*
    로그인 체크
    새 비밀번호, 새 비밀번호 확인 비교 체크
    새 비밀번호 형식 체크
    현재 비밀번호 체크
    새로운 솔트 부여
    패스워드 및 솔트 값 업데이트
     */
    @Transactional
    public void changePassword(Map<String, Object> password) {
        /*
        로그인 체크
         */
        if (!userService.isLogin()) {
            throw new InvalidUserAuthException("로그인이 필요한 서비스 입니다.");
        }
        UUID USER_ID = userService.getUserId();
        String currentPassword = password.get("currentPassword").toString();
        String newPassword = password.get("newPassword").toString();
        String checkPassword = password.get("checkPassword").toString();

        /*
        새 비밀번호, 새 비밀번호 확인 비교 체크
         */
        if(!newPassword.equals(checkPassword)){
            throw new NotMatchedFormatException("새 비밀번호를 확인해 주세요.");
        }

        /*
        새 비밀번호 형식 체크
         */
        boolean isContainNumbers = Pattern.compile("[0-9]").matcher(newPassword).find();
        boolean isContainAlphabets = Pattern.compile("[a-z]").matcher(newPassword).find();
        boolean isContainSymbols = Pattern.compile("[!@#$%^&*()\\-_=+\\\\\\/\\[\\]{};:\\`\"',.<>\\/?\\|~]").matcher(newPassword).find();
        if(!isContainNumbers || !isContainAlphabets || !isContainSymbols){
            throw new NotMatchedFormatException("새 비밀번호 형식을 확인해 주세요.");
        }

        /*
        현재 비밀번호 체크
         */
        UserEntity userEntity = userService.searchUserByUserId(USER_ID);
        String currentSalt = userEntity.getSalt();

        if(!passwordEncoder.matches(currentPassword + currentSalt, userEntity.getPassword())){
            throw new NotMatchedFormatException("현재 비밀번호를 확인해 주세요.");
        }

        String newSalt = UUID.randomUUID().toString();
        String encNewPassword = passwordEncoder.encode(newPassword + newSalt);

        /*
        패스워드 및 솔트 값 업데이트 (영속성 업데이트)
         */
        userEntity.setPassword(encNewPassword);
        userEntity.setSalt(newSalt);
    }
}
