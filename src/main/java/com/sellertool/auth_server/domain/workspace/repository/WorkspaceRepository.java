package com.sellertool.auth_server.domain.workspace.repository;

import com.sellertool.auth_server.domain.workspace.entity.WorkspaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkspaceRepository extends JpaRepository<WorkspaceEntity, Integer>, WorkspaceRepositoryCustom {
}
