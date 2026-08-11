package com.example.notesharing.Controller;

import com.example.notesharing.DTO.Request.CommunityPostRequest;
import com.example.notesharing.DTO.Request.CommunityRequest;
import com.example.notesharing.DTO.Request.ShareResourceRequest;
import com.example.notesharing.modal.*;
import com.example.notesharing.service.CommunityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/communities")
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    @PostMapping
    public Community createCommunity(@RequestBody CommunityRequest request) {
        return communityService.createCommunity(request);
    }

    @GetMapping
    public List<Community> getCommunities() {
        return communityService.getCommunities();
    }

    @GetMapping("/{id}")
    public Community getCommunity(@PathVariable UUID id) {
        return communityService.getCommunity(id);
    }

    @PostMapping("/{id}/join")
    public CommunityMember joinCommunity(@PathVariable UUID id,
                                          @RequestParam String email) {
        return communityService.joinCommunity(id, email);
    }

    @DeleteMapping("/{id}/leave")
    public void leaveCommunity(@PathVariable UUID id,
                               @RequestParam String email) {
        communityService.leaveCommunity(id, email);
    }

    @GetMapping("/{id}/members")
    public List<CommunityMember> getMembers(@PathVariable UUID id) {
        return communityService.getMembers(id);
    }

    @PostMapping("/{id}/posts")
    public CommunityPost createPost(@PathVariable UUID id,
                                    @RequestBody CommunityPostRequest request) {
        return communityService.createPost(id, request);
    }

    @GetMapping("/{id}/posts")
    public List<CommunityPost> getPosts(@PathVariable UUID id) {
        return communityService.getPosts(id);
    }

    @PostMapping("/posts/{postId}/like")
    public CommunityPost likePost(@PathVariable UUID postId) {
        return communityService.likePost(postId);
    }

    @PostMapping("/{id}/resources")
    public SharedResource shareResource(@PathVariable UUID id,
                                        @RequestBody ShareResourceRequest request) {
        return communityService.shareResource(id, request);
    }

    @GetMapping("/{id}/resources")
    public List<SharedResource> getSharedResources(@PathVariable UUID id) {
        return communityService.getSharedResources(id);
    }
}
