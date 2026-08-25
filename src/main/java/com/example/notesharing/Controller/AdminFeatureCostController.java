package com.example.notesharing.Controller;

import com.example.notesharing.DTO.AiFeatureConfigDTO;
import com.example.notesharing.DTO.Request.FeatureCostItemRequest;
import com.example.notesharing.service.AiFeatureConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin "AI Credit Costs" screen: read the per-feature configs and bulk-save the edited costs /
 * premium gating. Saved costs become the value {@code AiCreditPolicy.costOf} enforces.
 */
@RestController
@RequestMapping("/api/admin/ai-credit-costs")
public class AdminFeatureCostController {

    @Autowired
    private AiFeatureConfigService aiFeatureConfigService;

    @GetMapping
    public List<AiFeatureConfigDTO> list() {
        return aiFeatureConfigService.list();
    }

    @PutMapping
    public List<AiFeatureConfigDTO> bulkUpdate(@RequestBody List<FeatureCostItemRequest> items) {
        return aiFeatureConfigService.bulkUpdate(items);
    }
}
