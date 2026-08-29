package com.example.notesharing.Controller;

import com.example.notesharing.DTO.PageResponse;
import com.example.notesharing.DTO.PaymentHistoryDTO;
import com.example.notesharing.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin eSewa payment history. Gated to ADMIN by the {@code /api/admin/**} security rule. Returns a
 * raw {@link PageResponse}, consistent with the other admin controllers.
 */
@RestController
@RequestMapping("/api/admin/payments")
public class AdminPaymentController {

    @Autowired
    private AdminService adminService;

    @GetMapping
    public PageResponse<PaymentHistoryDTO> list(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return adminService.paymentHistory(page, size);
    }
}
