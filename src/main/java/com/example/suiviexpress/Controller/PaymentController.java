package com.example.suiviexpress.Controller;

import com.example.suiviexpress.DTO.*;
import com.example.suiviexpress.Entity.PaymentStatus;
import com.example.suiviexpress.Service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponseDTO> createPayment(
            @Valid @RequestBody CreatePaymentDTO createPaymentDTO,
            Authentication authentication) {
        PaymentResponseDTO response = paymentService.createPayment(createPaymentDTO, authentication);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PaymentDTO>> getAllPayments(Authentication authentication) {
        List<PaymentDTO> payments = paymentService.getAllPayments(authentication);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/my-payments")
    public ResponseEntity<List<PaymentDTO>> getMyPayments(Authentication authentication) {
        List<PaymentDTO> payments = paymentService.getMyPayments(authentication);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDTO> getPaymentById(
            @PathVariable Long id,
            Authentication authentication) {
        PaymentDTO payment = paymentService.getPaymentById(id, authentication);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/intent/{paymentIntentId}")
    public ResponseEntity<PaymentDTO> getPaymentByIntentId(
            @PathVariable String paymentIntentId,
            Authentication authentication) {
        PaymentDTO payment = paymentService.getPaymentByIntentId(paymentIntentId, authentication);
        return ResponseEntity.ok(payment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentDTO> updatePayment(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePaymentDTO updatePaymentDTO,
            Authentication authentication) {
        PaymentDTO updatedPayment = paymentService.updatePayment(id, updatePaymentDTO, authentication);
        return ResponseEntity.ok(updatedPayment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(
            @PathVariable Long id,
            Authentication authentication) {
        paymentService.deletePayment(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{paymentIntentId}/success")
    public ResponseEntity<Void> handlePaymentSuccess(@PathVariable String paymentIntentId) {
        paymentService.handlePaymentSuccess(paymentIntentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByStatus(
            @PathVariable PaymentStatus status,
            Authentication authentication) {
        List<PaymentDTO> payments = paymentService.getPaymentsByStatus(status, authentication);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/my-payments/status/{status}")
    public ResponseEntity<List<PaymentDTO>> getMyPaymentsByStatus(
            @PathVariable PaymentStatus status,
            Authentication authentication) {
        List<PaymentDTO> payments = paymentService.getMyPaymentsByStatus(status, authentication);
        return ResponseEntity.ok(payments);
    }
}