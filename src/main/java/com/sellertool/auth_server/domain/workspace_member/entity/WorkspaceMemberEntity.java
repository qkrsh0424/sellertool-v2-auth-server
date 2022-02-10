package com.sellertool.auth_server.domain.workspace_member.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "workspace_member")
@DynamicInsert
public class WorkspaceMemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cid")
    private Integer cid;
    @Column(name = "id")
    @Type(type = "uuid-char")
    private UUID id;
    @Column(name = "workspace_id")
    @Type(type = "uuid-char")
    private UUID workspaceId;
    @Column(name = "user_id")
    @Type(type = "uuid-char")
    private UUID userId;
    @Column(name = "grade")
    private String grade;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "read_permission_yn", columnDefinition = "y")
    private String readPermissionYn;
    @Column(name = "write_permission_yn", columnDefinition = "y")
    private String writePermissionYn;
    @Column(name = "update_permission_yn", columnDefinition = "n")
    private String updatePermissionYn;
    @Column(name = "delete_permission_yn", columnDefinition = "n")
    private String deletePermissionYn;
}
