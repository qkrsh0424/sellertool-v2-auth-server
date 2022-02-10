package com.sellertool.auth_server.domain.workspace.entity;

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
@Table(name = "workspace")
@DynamicInsert
public class WorkspaceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cid")
    private Integer cid;
    @Column(name = "id")
    @Type(type="uuid-char")
    private UUID id;
    @Column(name = "name")
    private String name;
    @Column(name = "master_id")
    @Type(type="uuid-char")
    private UUID masterId;
    @Column(name = "public_yn", columnDefinition = "n")
    private String publicYn;
    @Column(name = "delete_protection_yn", columnDefinition = "y")
    private String deleteProtectionYn;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
