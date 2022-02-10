package com.sellertool.auth_server.domain.workspace_member.repository;

import com.sellertool.auth_server.domain.workspace_member.entity.WorkspaceMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMemberEntity, Integer> {
    List<WorkspaceMemberEntity> findByWorkspaceId(UUID workspaceId);
}
