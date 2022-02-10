package com.sellertool.auth_server.domain.workspace.repository;

import com.sellertool.auth_server.domain.workspace.entity.WorkspaceEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkspaceRepositoryCustom {
    List<WorkspaceEntity> qSelectOwnWorkspace(UUID userId);

    List<WorkspaceEntity> qSelectList(UUID workspaceId);
}
