package com.example.notesharing.modal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GeneratedReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    private String userEmail;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @ManyToOne
    @JoinColumn(name = "note_id")
    @JsonIgnore
    private Note sourceNote;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String sourceContent;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    private String reportType;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
