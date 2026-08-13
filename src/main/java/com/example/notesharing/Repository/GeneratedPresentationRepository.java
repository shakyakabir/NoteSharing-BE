package com.example.notesharing.Repository;

import com.example.notesharing.modal.GeneratedPresentation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GeneratedPresentationRepository extends JpaRepository<GeneratedPresentation, UUID> {

    List<GeneratedPresentation> findByUserEmail(String userEmail);
}
