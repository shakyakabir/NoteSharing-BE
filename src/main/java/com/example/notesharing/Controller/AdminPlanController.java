package com.example.notesharing.Controller;

import com.example.notesharing.DTO.Request.PlanRequest;
import com.example.notesharing.DTO.SubscriptionPlanDTO;
import com.example.notesharing.service.SubscriptionPlanService;
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
 * Admin CRUD for subscription plans. DELETE is non-destructive when the plan is referenced
 * (soft-deactivate) and a hard delete otherwise (see {@code SubscriptionPlanService}).
 */
@RestController
@RequestMapping("/api/admin/plans")
public class AdminPlanController {

    @Autowired
    private SubscriptionPlanService subscriptionPlanService;

    @GetMapping
    public List<SubscriptionPlanDTO> list() {
        return subscriptionPlanService.listActive();
    }

    @PostMapping
    public SubscriptionPlanDTO create(@RequestBody PlanRequest request) {
        return subscriptionPlanService.create(request);
    }

    @PutMapping("/{id}")
    public SubscriptionPlanDTO update(@PathVariable UUID id, @RequestBody PlanRequest request) {
        return subscriptionPlanService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        subscriptionPlanService.delete(id);
    }
}
