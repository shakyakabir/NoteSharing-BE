package com.example.notesharing.Repository;
import com.example.notesharing.modal.CollaborationGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CollaborationGroupRepository
        extends JpaRepository<CollaborationGroup, UUID> {

    Optional<CollaborationGroup> findByShareCode(String shareCode);

    boolean existsByShareCode(String shareCode);

    java.util.List<CollaborationGroup> findByOwnerEmail(String email);
}