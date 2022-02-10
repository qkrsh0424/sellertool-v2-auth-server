package com.sellertool.auth_server.domain.signup.service;

import com.sellertool.auth_server.domain.exception.dto.ConflictErrorException;
import com.sellertool.auth_server.domain.exception.dto.NotAllowedAccessException;
import com.sellertool.auth_server.domain.workspace.entity.WorkspaceEntity;
import com.sellertool.auth_server.domain.workspace.service.WorkspaceService;
import com.sellertool.auth_server.domain.signup.dto.SignupDto;
import com.sellertool.auth_server.domain.workspace.utils.WorkspaceStaticVariable;
import com.sellertool.auth_server.domain.workspace_member.entity.WorkspaceMemberEntity;
import com.sellertool.auth_server.domain.workspace_member.service.WorkspaceMemberService;
import com.sellertool.auth_server.domain.workspace_member.utils.WorkspaceMemberStaticVariable;
import com.sellertool.auth_server.domain.user.entity.UserEntity;
import com.sellertool.auth_server.domain.user.service.UserService;
import com.sellertool.auth_server.utils.DateTimeUtils;
import com.sellertool.auth_server.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.UUID;

@Service
public class SignupBusinessService {
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final WorkspaceService teamService;
    private final WorkspaceMemberService teamMemberService;

    @Autowired
    public SignupBusinessService(
            PasswordEncoder passwordEncoder,
            UserService userService,
            WorkspaceService teamService,
            WorkspaceMemberService teamMemberService
    ) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.teamService = teamService;
        this.teamMemberService = teamMemberService;
    }

    @Transactional
    public void signup(SignupDto signupDto) {
        String USERNAME = signupDto.getUsername();
        String PASSWORD = signupDto.getPassword();
        String PASSWORD_CHECK = signupDto.getPasswordCheck();
        String NICKNAME = signupDto.getNickname();

        if (userService.isDuplicatedUsername(USERNAME)) {
            throw new ConflictErrorException("아이디 중복 체크를 확인해 주세요.");
        }

        if (!PASSWORD.equals(PASSWORD_CHECK)) {
            throw new NotAllowedAccessException("패스워드 불일치. 잘못 된 접근 방식입니다.");
        }

        String SALT = UUID.randomUUID().toString();
        String ENC_PASSWORD = passwordEncoder.encode(PASSWORD + SALT);

        UUID USER_ID = UUID.randomUUID();
        UUID TEAM_ID = UUID.randomUUID();
        UUID TEAM_MEMBER_ID = UUID.randomUUID();

        UserEntity userEntity = UserEntity.builder()
                .id(USER_ID)
                .username(USERNAME)
                .password(ENC_PASSWORD)
                .nickname(NICKNAME)
                .salt(SALT)
                .roles(UserUtils.ROLE_USER)
                .allowedAccessCount(UserUtils.ALLOWED_ACCESS_COUNT_DEFAULT)
                .updatedAt(DateTimeUtils.getCurrentDateTime())
                .createdAt(DateTimeUtils.getCurrentDateTime())
                .build();

        WorkspaceEntity teamEntity = WorkspaceEntity.builder()
                .id(TEAM_ID)
                .name(userEntity.getNickname() + "의 셀러툴")
                .masterId(USER_ID)
                .publicYn(WorkspaceStaticVariable.PUBLIC_N)
                .deleteProtectionYn(WorkspaceStaticVariable.DELETE_PROTECTION_Y)
                .createdAt(DateTimeUtils.getCurrentDateTime())
                .updatedAt(DateTimeUtils.getCurrentDateTime())
                .build();

        WorkspaceMemberEntity teamMemberEntity = WorkspaceMemberEntity.builder()
                .id(TEAM_MEMBER_ID)
                .workspaceId(TEAM_ID)
                .userId(USER_ID)
                .grade(WorkspaceMemberStaticVariable.GRADE_MASTER)
                .createdAt(DateTimeUtils.getCurrentDateTime())
                .readPermissionYn(WorkspaceMemberStaticVariable.READ_PERMISSION_Y)
                .writePermissionYn(WorkspaceMemberStaticVariable.WRITE_PERMISSION_Y)
                .updatePermissionYn(WorkspaceMemberStaticVariable.UPDATE_PERMISSION_Y)
                .deletePermissionYn(WorkspaceMemberStaticVariable.DELETE_PERMISSION_Y)
                .build();

        userService.saveAndModify(userEntity);
        teamService.saveAndModify(teamEntity);
        teamMemberService.saveAndModify(teamMemberEntity);
    }

}
