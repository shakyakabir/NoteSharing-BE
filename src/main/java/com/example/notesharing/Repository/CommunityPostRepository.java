package com.example.notesharing.Repository;

import com.example.notesharing.modal.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, UUID> {

    List<CommunityPost> findByCommunityIdOrderByCreatedAtDesc(UUID communityId);
}
