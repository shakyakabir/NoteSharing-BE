package com.example.notesharing.Repository;

import com.example.notesharing.modal.OTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface OtpRepository  extends JpaRepository<OTP, UUID> {

    Optional<OTP> findByEmail(String email);

    OTP findTopByEmailOrderByIdDesc(String email);
    @Modifying
    @Query("DELETE FROM OTP o WHERE o.email = :email")
    void deleteByEmail(String email);
}
