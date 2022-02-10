package com.sellertool.auth_server.domain.workspace_member.service;

import com.sellertool.auth_server.domain.workspace_member.entity.WorkspaceMemberEntity;
import com.sellertool.auth_server.domain.workspace_member.repository.WorkspaceMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WorkspaceMemberService {
    private final WorkspaceMemberRepository workspaceMemberRepository;

    @Autowired
    public WorkspaceMemberService(
            WorkspaceMemberRepository workspaceMemberRepository
    ) {
        this.workspaceMemberRepository = workspaceMemberRepository;
    }

    public void saveAndModify(WorkspaceMemberEntity entity) {
        workspaceMemberRepository.save(entity);
    }

    public List<WorkspaceMemberEntity> searchListByWorkspaceId(UUID workspaceId) {
        return workspaceMemberRepository.findByWorkspaceId(workspaceId);
    }

    public boolean isAccessedWritePermissionOfWorkspace(UUID workspaceId, UUID userId){
        List<WorkspaceMemberEntity> workspaceMemberEntities = workspaceMemberRepository.findByWorkspaceId(workspaceId);

        List<WorkspaceMemberEntity> matchedMemberEntities = workspaceMemberEntities.stream().filter(r->r.getUserId().equals(userId)).collect(Collectors.toList());
        Optional<WorkspaceMemberEntity> workspaceMemberEntityOpt = matchedMemberEntities.stream().findFirst();

        if(!workspaceMemberEntityOpt.isPresent()){
            return false;
        }

        if(!workspaceMemberEntityOpt.get().getWritePermissionYn().equals("y")){
            return false;
        }

        return true;
    }

    public boolean isAccessedReadPermissionOfWorkspace(UUID workspaceId, UUID userId){
        List<WorkspaceMemberEntity> workspaceMemberEntities = workspaceMemberRepository.findByWorkspaceId(workspaceId);

        List<WorkspaceMemberEntity> matchedMemberEntities = workspaceMemberEntities.stream().filter(r->r.getUserId().equals(userId)).collect(Collectors.toList());
        Optional<WorkspaceMemberEntity> workspaceMemberEntityOpt = matchedMemberEntities.stream().findFirst();

        if(!workspaceMemberEntityOpt.isPresent()){
            return false;
        }

        if(!workspaceMemberEntityOpt.get().getReadPermissionYn().equals("y")){
            return false;
        }

        return true;
    }

    public boolean isAccessedUpdatePermissionOfWorkspace(UUID workspaceId, UUID userId) {
        List<WorkspaceMemberEntity> workspaceMemberEntities = workspaceMemberRepository.findByWorkspaceId(workspaceId);

        List<WorkspaceMemberEntity> matchedMemberEntities = workspaceMemberEntities.stream().filter(r->r.getUserId().equals(userId)).collect(Collectors.toList());
        Optional<WorkspaceMemberEntity> workspaceMemberEntityOpt = matchedMemberEntities.stream().findFirst();

        if(!workspaceMemberEntityOpt.isPresent()){
            return false;
        }

        if(!workspaceMemberEntityOpt.get().getUpdatePermissionYn().equals("y")){
            return false;
        }

        return true;
    }

    public boolean isAccessedDeletePermissionOfWorkspace(UUID workspaceId, UUID userId) {
        List<WorkspaceMemberEntity> workspaceMemberEntities = workspaceMemberRepository.findByWorkspaceId(workspaceId);

        List<WorkspaceMemberEntity> matchedMemberEntities = workspaceMemberEntities.stream().filter(r->r.getUserId().equals(userId)).collect(Collectors.toList());
        Optional<WorkspaceMemberEntity> workspaceMemberEntityOpt = matchedMemberEntities.stream().findFirst();

        if(!workspaceMemberEntityOpt.isPresent()){
            return false;
        }

        if(!workspaceMemberEntityOpt.get().getDeletePermissionYn().equals("y")){
            return false;
        }

        return true;
    }
}
