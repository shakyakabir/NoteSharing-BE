package com.example.notesharing.modal;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_scores")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserScore {

    @Id
    private String email;

    @Builder.Default
    private long totalPoints = 0L;

    /*
     * NOTE: if you already have a `User` entity in the project, it's probably cleaner to add
     * a `totalPoints` column there instead of this standalone table - swap UserScoreService's
     * repository calls for your existing UserRepository and delete this class.
     */
}
