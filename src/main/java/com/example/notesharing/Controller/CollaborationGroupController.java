package com.example.notesharing.Controller;
import com.example.notesharing.DTO.Request.CreateGroupRequest;
import com.example.notesharing.DTO.Request.JoinGroupRequest;
import com.example.notesharing.modal.CollaborationGroup;
import com.example.notesharing.modal.GroupMember;
import com.example.notesharing.service.CollaborationGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
public class CollaborationGroupController {

    @Autowired
    private CollaborationGroupService groupService;


    // CREATE GROUP
    @PostMapping
    public CollaborationGroup createGroup(
            @RequestBody CreateGroupRequest request,
            @RequestParam String email
    ) {

        return groupService.createGroup(
                request,
                email
        );
    }


    // JOIN GROUP
    @PostMapping("/join")
    public CollaborationGroup joinGroup(
            @RequestBody JoinGroupRequest request
    ) {

        return groupService.joinGroup(request);
    }


    // GET MY GROUPS
    @GetMapping
    public List<GroupMember> getMyGroups(
            @RequestParam String email
    ) {

        return groupService.getMyGroups(email);
    }


    // GET GROUP MEMBERS
    @GetMapping("/{groupId}/members")
    public List<GroupMember> getMembers(
            @PathVariable UUID groupId
    ) {

        return groupService.getMembers(groupId);
    }
}