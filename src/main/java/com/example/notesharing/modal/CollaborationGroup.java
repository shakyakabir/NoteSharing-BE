package com.example.notesharing.modal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
public class CollaborationGroup {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false, unique = true)
    private String shareCode;

    @Column(nullable = false)
    private String ownerEmail;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}