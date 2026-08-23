package com.example.notesharing.Repository;
import com.example.notesharing.modal.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupMemberRepository
        extends JpaRepository<GroupMember, UUID> {

    boolean existsByGroupIdAndUserEmail(
            UUID groupId,
            String userEmail
    );

    Optional<GroupMember> findByGroupIdAndUserEmail(
            UUID groupId,
            String userEmail
    );
    List<GroupMember> findByUserEmail(String userEmail);

    List<GroupMember> findByGroupId(UUID groupId);
}