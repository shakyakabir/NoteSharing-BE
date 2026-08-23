package com.example.notesharing.modal;

import com.example.notesharing.Enum.Difficulty;
import com.example.notesharing.Enum.QuizMode;
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

    // --- NEW FIELDS ---

    @Enumerated(EnumType.STRING)
    private QuizMode mode;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    /** Max points obtainable for a perfect run at this quiz's difficulty. Only paid out for SOLO mode. */
    private int pointsPerCompletion;

    /** Optional - id of the notebook this quiz was generated from, if the user picked "Select from Library". */
    private String notebookId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
