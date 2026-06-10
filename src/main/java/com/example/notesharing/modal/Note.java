package com.example.notesharing.modal;

import com.example.notesharing.Enum.Visibility;
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
public class Note {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String title;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String content;
    @Enumerated(EnumType.STRING)
    private Visibility visibility;

    private String shareCode; // for friend sharing

    private String userEmail; // since you use email login

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
