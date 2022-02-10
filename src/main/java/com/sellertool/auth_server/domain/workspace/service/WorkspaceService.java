package com.sellertool.auth_server.domain.workspace.service;

import com.sellertool.auth_server.domain.workspace.entity.WorkspaceEntity;
import com.sellertool.auth_server.domain.workspace.repository.WorkspaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class WorkspaceService {
    private final WorkspaceRepository workspaceRepository;

    @Autowired
    public WorkspaceService(
            WorkspaceRepository workspaceRepository
    ) {
        this.workspaceRepository = workspaceRepository;
    }

    public void saveAndModify(WorkspaceEntity workspaceEntity) {
        workspaceRepository.save(workspaceEntity);
    }

    public WorkspaceEntity searchOwnPrivateWorkspace(UUID userId) {
        return workspaceRepository.qSelectOwnWorkspace(userId).stream().findFirst().orElse(null);
    }

    public WorkspaceEntity searchWorkspaceOne(UUID workspaceId){
        List<WorkspaceEntity> workspaceEntities = workspaceRepository.qSelectList(workspaceId);
        return workspaceEntities.stream().findFirst().orElse(null);
    }
}
