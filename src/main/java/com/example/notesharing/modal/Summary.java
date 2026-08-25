package com.example.notesharing.modal;

import com.example.notesharing.Enum.SharedResourceType;
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
@Table(name = "summaries")
public class Summary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String userEmail;

    private String title;

    @ManyToOne
    @JoinColumn(name = "note_id")
    private Note note;

    @Column(columnDefinition = "TEXT")
    private String summaryContent;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
