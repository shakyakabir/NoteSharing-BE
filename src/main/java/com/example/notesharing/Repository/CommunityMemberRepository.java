package com.example.notesharing.Repository;

import com.example.notesharing.modal.CommunityMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommunityMemberRepository extends JpaRepository<CommunityMember, UUID> {

    List<CommunityMember> findByCommunityId(UUID communityId);

    Optional<CommunityMember> findByCommunityIdAndUserEmail(UUID communityId, String userEmail);

    boolean existsByCommunityIdAndUserEmail(UUID communityId, String userEmail);
}
