package com.example.notesharing.modal;

import com.example.notesharing.Enum.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)


    private UUID id;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String userName;
    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Builder.Default
    @Column(name="point_balance", columnDefinition = "INT DEFAULT 0")
    private int pointBalance =0;

    @Builder.Default
    @Column(name ="streak_days", columnDefinition = "INT DEFAULT 0")
    private int streakDays=0;
    @Builder.Default
    @Column(name = "ai_quota_used", columnDefinition = "INT DEFAULT 0")
    private int aiQuotaUsed = 0;

    @Column(name = "ai_quota_expires_at")
    private Instant aiQuotaExpires;

    @Builder.Default
    @Column(name = "is_active", columnDefinition = "BOOLEAN DEFAULT true")
    private boolean isActive=true;

    @Builder.Default
    @Column(name = "email_verified", columnDefinition = "BOOLEAN DEFAULT false")
    private boolean emailVerified = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "role", columnDefinition = "VARCHAR(20) DEFAULT 'USER'")
    private UserRole role = UserRole.USER;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;


    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

}
