package com.example.notesharing.Repository;

import com.example.notesharing.modal.SharedResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SharedResourceRepository extends JpaRepository<SharedResource, UUID> {

    List<SharedResource> findByCommunityId(UUID communityId);
}
