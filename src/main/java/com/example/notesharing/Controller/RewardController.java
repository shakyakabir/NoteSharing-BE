package com.example.notesharing.Controller;

import com.example.notesharing.DTO.PointBalanceDTO;
import com.example.notesharing.DTO.Request.PointTransactionRequest;
import com.example.notesharing.DTO.Request.RewardItemRequest;
import com.example.notesharing.modal.*;
import com.example.notesharing.service.RewardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class RewardController {

    @Autowired
    private RewardService rewardService;

    @GetMapping("/points/balance")
    public PointBalanceDTO getBalance(@RequestParam String email) {
        return rewardService.getBalance(email);
    }

    @PostMapping("/points/earn")
    public PointTransaction earnPoints(@RequestBody PointTransactionRequest request) {
        return rewardService.earnPoints(request);
    }

    @GetMapping("/points/transactions")
    public List<PointTransaction> getTransactions(@RequestParam String email) {
        return rewardService.getTransactions(email);
    }

    @PostMapping("/rewards")
    public RewardItem createReward(@RequestBody RewardItemRequest request) {
        return rewardService.createReward(request);
    }

    @GetMapping("/rewards")
    public List<RewardItem> getRewards() {
        return rewardService.getRewards();
    }

    @PostMapping("/rewards/{id}/redeem")
    public RewardPurchase redeemReward(@PathVariable UUID id,
                                       @RequestParam String email) {
        return rewardService.redeemReward(id, email);
    }

    @GetMapping("/rewards/purchases")
    public List<RewardPurchase> getPurchases(@RequestParam String email) {
        return rewardService.getPurchases(email);
    }
}
