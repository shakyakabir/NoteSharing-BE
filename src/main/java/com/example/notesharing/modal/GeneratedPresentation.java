package com.example.notesharing.modal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GeneratedPresentation {

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

    private String theme;

    private String templateName;

    private String exportFilePath;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "presentation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("slideOrder ASC")
    @Builder.Default
    private List<PresentationSlide> slides = new ArrayList<>();
}
