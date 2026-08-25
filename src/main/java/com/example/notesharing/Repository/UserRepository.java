package com.example.notesharing.Repository;

import com.example.notesharing.modal.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     * Paginated admin user search with optional filters. {@code search} matches username or email
     * (case-insensitive); {@code active} filters by account status; {@code premium} filters by whether
     * the user has an ACTIVE PREMIUM subscription (TRUE) or not (FALSE) - a user with no subscription
     * row counts as non-premium. Any argument may be null to skip that filter.
     */
    @Query("SELECT u FROM User u WHERE " +
            "(:search IS NULL OR LOWER(u.userName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:active IS NULL OR u.isActive = :active) " +
            "AND (:premium IS NULL " +
            "   OR (:premium = TRUE AND EXISTS (SELECT 1 FROM AiSubscription s WHERE s.userEmail = u.email " +
            "        AND s.plan = com.example.notesharing.Enum.SubscriptionPlan.PREMIUM " +
            "        AND s.status = com.example.notesharing.Enum.SubscriptionStatus.ACTIVE)) " +
            "   OR (:premium = FALSE AND NOT EXISTS (SELECT 1 FROM AiSubscription s WHERE s.userEmail = u.email " +
            "        AND s.plan = com.example.notesharing.Enum.SubscriptionPlan.PREMIUM " +
            "        AND s.status = com.example.notesharing.Enum.SubscriptionStatus.ACTIVE)))")
    Page<User> searchForAdmin(@Param("search") String search,
                              @Param("active") Boolean active,
                              @Param("premium") Boolean premium,
                              Pageable pageable);

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
