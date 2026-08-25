package com.example.notesharing.Controller;

import com.example.notesharing.DTO.Request.RewardItemRequest;
import com.example.notesharing.modal.RewardItem;
import com.example.notesharing.service.RewardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Admin Point Shop CRUD. Returns {@link RewardItem} entities directly, consistent with the
 * self-service {@code RewardController}; the frontend formats the numeric fields into its display
 * strings ({@code "5,000 pts"}, {@code "Unlimited"}, ...). {@code getAllRewards} lists every status
 * (the self-service list shows ACTIVE only).
 */
@RestController
@RequestMapping("/api/admin/rewards")
public class AdminRewardController {

    @Autowired
    private RewardService rewardService;

    @GetMapping
    public List<RewardItem> list() {
        return rewardService.getAllRewards();
    }

    @PostMapping
    public RewardItem create(@RequestBody RewardItemRequest request) {
        return rewardService.createReward(request);
    }

    @PutMapping("/{id}")
    public RewardItem update(@PathVariable UUID id, @RequestBody RewardItemRequest request) {
        return rewardService.updateReward(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        rewardService.deleteReward(id);
    }
}
