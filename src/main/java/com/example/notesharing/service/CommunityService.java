package com.example.notesharing.service;

import com.example.notesharing.DTO.Request.CommunityPostRequest;
import com.example.notesharing.DTO.Request.CommunityRequest;
import com.example.notesharing.DTO.Request.ShareResourceRequest;
import com.example.notesharing.Enum.CommunityRole;
import com.example.notesharing.Repository.*;
import com.example.notesharing.modal.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CommunityService {

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private CommunityMemberRepository memberRepository;

    @Autowired
    private CommunityPostRepository postRepository;

    @Autowired
    private SharedResourceRepository sharedResourceRepository;

    @Autowired
    private UserRepository userRepository;

    public Community createCommunity(CommunityRequest request) {
        if (request == null) {
            throw new RuntimeException("Community request is required");
        }
        User owner = findUser(request.getOwnerEmail());

        Community community = new Community();
        community.setName(required(request.getName(), "Community name is required"));
        community.setCategory(request.getCategory());
        community.setDescription(request.getDescription());
        community.setOwnerEmail(request.getOwnerEmail());
        community.setOwner(owner);
        community.setActive(true);
        community.setCreatedAt(LocalDateTime.now());
        community.setUpdatedAt(LocalDateTime.now());

        Community saved = communityRepository.save(community);
        addMember(saved, owner, CommunityRole.OWNER);
        return saved;
    }

    public List<Community> getCommunities() {
        return communityRepository.findByActiveTrue();
    }

    public Community getCommunity(UUID id) {
        return communityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Community not found"));
    }

    public CommunityMember joinCommunity(UUID communityId, String email) {
        Community community = getCommunity(communityId);
        User user = findUser(email);
        if (memberRepository.existsByCommunityIdAndUserEmail(communityId, email)) {
            return memberRepository.findByCommunityIdAndUserEmail(communityId, email)
                    .orElseThrow(() -> new RuntimeException("Member not found"));
        }
        return addMember(community, user, CommunityRole.MEMBER);
    }

    public void leaveCommunity(UUID communityId, String email) {
        CommunityMember member = memberRepository.findByCommunityIdAndUserEmail(communityId, email)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        if (member.getRole() == CommunityRole.OWNER) {
            throw new RuntimeException("Owner cannot leave community");
        }
        memberRepository.delete(member);
    }

    public List<CommunityMember> getMembers(UUID communityId) {
        return memberRepository.findByCommunityId(communityId);
    }

    public CommunityPost createPost(UUID communityId, CommunityPostRequest request) {
        if (request == null) {
            throw new RuntimeException("Post request is required");
        }
        Community community = getCommunity(communityId);
        User author = findUser(request.getAuthorEmail());
        requireMember(communityId, request.getAuthorEmail());

        CommunityPost post = new CommunityPost();
        post.setCommunity(community);
        post.setAuthorEmail(request.getAuthorEmail());
        post.setAuthor(author);
        post.setTitle(required(request.getTitle(), "Post title is required"));
        post.setTag(request.getTag());
        post.setContent(required(request.getContent(), "Post content is required"));
        post.setLikes(0);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        return postRepository.save(post);
    }

    public List<CommunityPost> getPosts(UUID communityId) {
        return postRepository.findByCommunityIdOrderByCreatedAtDesc(communityId);
    }

    public CommunityPost likePost(UUID postId) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setLikes(post.getLikes() + 1);
        post.setUpdatedAt(LocalDateTime.now());
        return postRepository.save(post);
    }

    public SharedResource shareResource(UUID communityId, ShareResourceRequest request) {
        if (request == null) {
            throw new RuntimeException("Share resource request is required");
        }
        Community community = getCommunity(communityId);
        User sharedBy = findUser(request.getSharedByEmail());
        requireMember(communityId, request.getSharedByEmail());

        SharedResource resource = new SharedResource();
        resource.setCommunity(community);
        resource.setResourceType(request.getResourceType());
        resource.setResourceId(request.getResourceId());
        resource.setSharedByEmail(request.getSharedByEmail());
        resource.setSharedBy(sharedBy);
        resource.setSharedAt(LocalDateTime.now());

        return sharedResourceRepository.save(resource);
    }

    public List<SharedResource> getSharedResources(UUID communityId) {
        return sharedResourceRepository.findByCommunityId(communityId);
    }

    private CommunityMember addMember(Community community, User user, CommunityRole role) {
        CommunityMember member = new CommunityMember();
        member.setCommunity(community);
        member.setUserEmail(user.getEmail());
        member.setUser(user);
        member.setRole(role);
        member.setJoinedAt(LocalDateTime.now());
        return memberRepository.save(member);
    }

    private void requireMember(UUID communityId, String email) {
        if (!memberRepository.existsByCommunityIdAndUserEmail(communityId, email)) {
            throw new RuntimeException("User is not a community member");
        }
    }

    private User findUser(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new RuntimeException(message);
        }
        return value;
    }
}
