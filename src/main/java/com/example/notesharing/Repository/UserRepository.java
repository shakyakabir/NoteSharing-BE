package com.example.notesharing.Repository;

import com.example.notesharing.modal.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;


public interface UserRepository extends JpaRepository<User, UUID> {


    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * Race-safe point spend (used when unlocking premium with points): the guarded UPDATE only
     * succeeds while the balance still covers the price, so a double-submit can't overdraw. Returns
     * the number of rows changed - 0 means "insufficient points".
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE User u SET u.pointBalance = u.pointBalance - :cost " +
            "WHERE u.email = :email AND u.pointBalance >= :cost")
    int trySpendPoints(@Param("email") String email, @Param("cost") int cost);
}
