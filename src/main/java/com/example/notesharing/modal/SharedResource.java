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
public class SharedResource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "community_id")
    @JsonIgnore
    private Community community;

    @Enumerated(EnumType.STRING)
    private SharedResourceType resourceType;

    private UUID resourceId;

    private String sharedByEmail;

    @ManyToOne
    @JoinColumn(name = "shared_by_id")
    @JsonIgnore
    private User sharedBy;

    private LocalDateTime sharedAt;
}
