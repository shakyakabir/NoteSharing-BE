package com.example.notesharing.service;

import com.example.notesharing.DTO.Request.CreateGroupRequest;
import com.example.notesharing.DTO.Request.JoinGroupRequest;
import com.example.notesharing.Repository.CollaborationGroupRepository;
import com.example.notesharing.Repository.GroupMemberRepository;
import com.example.notesharing.modal.CollaborationGroup;
import com.example.notesharing.modal.GroupMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CollaborationGroupService {

    @Autowired
    private CollaborationGroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository memberRepository;


    // CREATE GROUP
    public CollaborationGroup createGroup(
            CreateGroupRequest request,
            String email
    ) {

        CollaborationGroup group =
                new CollaborationGroup();

        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setOwnerEmail(email);
        group.setShareCode(generateShareCode());
        group.setCreatedAt(LocalDateTime.now());

        CollaborationGroup savedGroup =
                groupRepository.save(group);


        // Add creator as OWNER
        GroupMember owner = new GroupMember();

        owner.setGroup(savedGroup);
        owner.setUserEmail(email);
        owner.setRole("OWNER");
        owner.setJoinedAt(LocalDateTime.now());

        memberRepository.save(owner);

        return savedGroup;
    }


    // JOIN GROUP
    public CollaborationGroup joinGroup(
            JoinGroupRequest request
    ) {

        CollaborationGroup group =
                groupRepository
                        .findByShareCode(request.getShareCode())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Invalid group code"
                                ));


        boolean alreadyMember =
                memberRepository
                        .existsByGroupIdAndUserEmail(
                                group.getId(),
                                request.getUserEmail()
                        );


        if (!alreadyMember) {

            GroupMember member =
                    new GroupMember();

            member.setGroup(group);
            member.setUserEmail(
                    request.getUserEmail()
            );
            member.setRole("MEMBER");
            member.setJoinedAt(
                    LocalDateTime.now()
            );

            memberRepository.save(member);
        }

        return group;
    }


    // GET MY GROUPS
    public List<GroupMember> getMyGroups(
            String email
    ) {

        return memberRepository
                .findByUserEmail(email);
    }


    // GET GROUP MEMBERS
    public List<GroupMember> getMembers(
            UUID groupId
    ) {

        return memberRepository
                .findByGroupId(groupId);
    }


    // GENERATE UNIQUE GROUP CODE
    private String generateShareCode() {

        String code;

        do {

            code = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 8)
                    .toUpperCase();

        } while (
                groupRepository
                        .existsByShareCode(code)
        );

        return code;
    }
}
