package com.sellertool.auth_server.domain.workspace.service;

import com.sellertool.auth_server.domain.user.service.UserService;
import com.sellertool.auth_server.domain.workspace.entity.WorkspaceEntity;
import com.sellertool.auth_server.domain.workspace.vo.WorkspaceVo;
import com.sellertool.auth_server.domain.workspace_member.entity.WorkspaceMemberEntity;
import com.sellertool.auth_server.domain.workspace_member.service.WorkspaceMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WorkspaceBusinessService {
    private final WorkspaceService workspaceService;
    private final WorkspaceMemberService workspaceMemberService;
    private final UserService userService;

    @Autowired
    public WorkspaceBusinessService(
            WorkspaceService workspaceService,
            WorkspaceMemberService workspaceMemberService,
            UserService userService
    ) {
        this.workspaceService = workspaceService;
        this.workspaceMemberService = workspaceMemberService;
        this.userService = userService;
    }

    public Object searchWorkspace(Map<String, Object> params) {
        if(!userService.isLogin()){
            return null;
        }

        UUID USER_ID = userService.getUserId();
        Object workspaceIdObj = params.get("workspaceId");
        UUID workspaceId = null;

        if(workspaceIdObj == null){
            return WorkspaceVo.toVo(workspaceService.searchOwnPrivateWorkspace(USER_ID));
        }

        try{
            workspaceId = UUID.fromString(workspaceIdObj.toString());
        }catch (IllegalArgumentException e){
            return WorkspaceVo.toVo(workspaceService.searchOwnPrivateWorkspace(USER_ID));
        }

        WorkspaceEntity workspaceEntity = workspaceService.searchWorkspaceOne(workspaceId);

        List<WorkspaceMemberEntity> workspaceMemberEntities = workspaceMemberService.searchListByWorkspaceId(workspaceId);
        List<UUID> memberIds = workspaceMemberEntities.stream().map(r->r.getUserId()).collect(Collectors.toList());

        if(memberIds.contains(USER_ID)){
            return WorkspaceVo.toVo(workspaceEntity);
        }

        return WorkspaceVo.toVo(workspaceService.searchOwnPrivateWorkspace(USER_ID));
    }
}
