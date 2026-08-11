package com.example.notesharing.Repository;

import com.example.notesharing.modal.Community;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommunityRepository extends JpaRepository<Community, UUID> {

    List<Community> findByActiveTrue();
}
