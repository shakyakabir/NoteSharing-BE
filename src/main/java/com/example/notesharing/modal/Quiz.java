package com.example.notesharing.modal;

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
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String userEmail;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String sourceText;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String questionsJson;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String answerKeyJson;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
