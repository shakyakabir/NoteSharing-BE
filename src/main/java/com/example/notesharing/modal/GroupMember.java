package com.example.notesharing.modal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"group_id", "user_email"}
                )
        }
)
public class GroupMember {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private CollaborationGroup group;

    @Column(nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private String role; // OWNER / MEMBER

    @Column(nullable = false)
    private LocalDateTime joinedAt;
}
