package com.sellertool.auth_server.domain.user.service;

import com.sellertool.auth_server.domain.exception.dto.AccessDeniedPermissionException;
import com.sellertool.auth_server.domain.exception.dto.InvalidUserAuthException;
import com.sellertool.auth_server.domain.user.dto.UserDto;
import com.sellertool.auth_server.domain.user.entity.UserEntity;
import com.sellertool.auth_server.domain.user.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserBusinessService {
    private final UserService userService;

    @Autowired
    public UserBusinessService(
            UserService userService
    ) {
        this.userService = userService;
    }

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
        업데이트 데이터 셋
         */
        userEntity.setNickname(userDto.getNickname());
        userEntity.setEmail(userDto.getEmail());
        userEntity.setName(userDto.getName());
        userEntity.setPhoneNumber(userDto.getPhoneNumber());

    }
}
