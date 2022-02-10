package com.sellertool.auth_server.domain.user.service;

import com.sellertool.auth_server.domain.exception.dto.InvalidUserAuthException;
import com.sellertool.auth_server.domain.user.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserBusinessService {
    private final UserService userService;
    @Autowired
    public UserBusinessService(
            UserService userService
    ){
        this.userService = userService;
    }

    public Object getInfoOwn(){
        if(!userService.isLogin()){
            throw new InvalidUserAuthException("로그인이 필요한 서비스 입니다.");
        }

        UUID USER_ID = userService.getUserId();
        UserVo userVo = UserVo.toVo(userService.searchUserByUserId(USER_ID));
        return userVo;
    }

    public Object checkUsernameDuplicate(Map<String, Object> params){
        Object usernameObj = params.get("username");
        Map<String, Object> resultData = new HashMap<>();

        if (usernameObj == null) {
            resultData.put("isEmpty", true);
            resultData.put("isDuplicated", false);
        }
        String USERNAME = usernameObj.toString();

        if(!userService.isDuplicatedUsername(USERNAME)){
            resultData.put("isEmpty", false);
            resultData.put("isDuplicated", false);
        }else{
            resultData.put("isEmpty", false);
            resultData.put("isDuplicated", true);
        }
        return resultData;
    }
}
