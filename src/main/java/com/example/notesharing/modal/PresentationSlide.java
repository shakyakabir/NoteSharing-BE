package com.example.notesharing.modal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PresentationSlide {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    private String subtitle;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @ElementCollection
    @CollectionTable(name = "presentation_slide_bullets", joinColumns = @JoinColumn(name = "slide_id"))
    @Column(name = "bullet", columnDefinition = "TEXT")
    @OrderColumn(name = "bullet_order")
    @Builder.Default
    private List<String> bullets = new ArrayList<>();

    private Integer slideOrder;

    private String design; // theme name (kept for backward compat)

    private String slideType; // title/section/content/split/image_text/cards/comparison/timeline/... (from AI)

    // Raw JSON blobs — @JsonRawValue means these serialize as real JSON objects
    // to the frontend, not as escaped strings.
    @Lob
    @Column(name = "layout_json", columnDefinition = "LONGTEXT")
    @JsonRawValue
    private String layout;

    @Lob
    @Column(name = "visual_elements_json", columnDefinition = "LONGTEXT")
    @JsonRawValue
    private String visualElements;

    @Column(columnDefinition = "TEXT")
    @Lob
    private String imageUrl;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String imagePrompt;

    @ManyToOne
    @JoinColumn(name = "presentation_id")
    @JsonIgnore
    private GeneratedPresentation presentation;
}
