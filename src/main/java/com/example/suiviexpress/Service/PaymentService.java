package com.example.suiviexpress.Service;

import com.example.suiviexpress.DTO.*;
import com.example.suiviexpress.Entity.Payment;
import com.example.suiviexpress.Entity.PaymentMethod;
import com.example.suiviexpress.Entity.PaymentStatus;
import com.example.suiviexpress.Entity.User;
import com.example.suiviexpress.Repository.PaymentRepository;
import com.example.suiviexpress.Repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.success.url}")
    private String successUrl;

    @Value("${stripe.cancel.url}")
    private String cancelUrl;

    // Méthode pour récupérer l'utilisateur courant
    private User getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            throw new RuntimeException("Unauthorized: Authentication missing");
        }

        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        if (currentUser.isLocked()) {
            throw new RuntimeException("Account locked. Access denied.");
        }

        return currentUser;
    }

    @Transactional
    public PaymentResponseDTO createPayment(CreatePaymentDTO createPaymentDTO, Authentication authentication) {
        try {
            Stripe.apiKey = stripeSecretKey;

            // Récupérer l'utilisateur courant
            User currentUser = getCurrentUser(authentication);

            // Créer les paramètres pour Stripe
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount((long) (createPaymentDTO.getAmount() * 100)) // Stripe utilise les centimes
                    .setCurrency(createPaymentDTO.getCurrency().toLowerCase())
                    .setDescription(createPaymentDTO.getDescription())
                    .putMetadata("user_id", currentUser.getId().toString())
                    .putMetadata("user_email", currentUser.getEmail())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .setConfirm(false) // Nous confirmons côté client
                    .build();

            // Créer le Payment Intent Stripe
            PaymentIntent paymentIntent = PaymentIntent.create(params);

            // Sauvegarder le paiement en base de données
            Payment payment = Payment.builder()
                    .paymentIntentId(paymentIntent.getId())
                    .amount(createPaymentDTO.getAmount())
                    .currency(createPaymentDTO.getCurrency().toUpperCase())
                    .status(PaymentStatus.PENDING)
                    .paymentMethod(PaymentMethod.valueOf(createPaymentDTO.getPaymentMethod()))
                    .description(createPaymentDTO.getDescription())
                    .user(currentUser)
                    .clientSecret(paymentIntent.getClientSecret())
                    .metadata("User: " + currentUser.getEmail())
                    .build();

            paymentRepository.save(payment);

            return PaymentResponseDTO.builder()
                    .clientSecret(paymentIntent.getClientSecret())
                    .paymentIntentId(paymentIntent.getId())
                    .status(paymentIntent.getStatus())
                    .amount(createPaymentDTO.getAmount())
                    .currency(createPaymentDTO.getCurrency())
                    .redirectUrl(successUrl + "?payment_intent=" + paymentIntent.getId())
                    .build();

        } catch (StripeException e) {
            log.error("Erreur Stripe lors de la création du paiement: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la création du paiement Stripe", e);
        }
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getAllPayments(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        // Seul l'admin peut voir tous les paiements
        if (!currentUser.getRole().name().equals("ROLE_ADMIN")) {
            throw new RuntimeException("Access denied: Admins only");
        }

        return paymentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PaymentDTO getPaymentById(Long id, Authentication authentication) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé"));

        // Vérifier les permissions
        User currentUser = getCurrentUser(authentication);
        if (!currentUser.getRole().name().equals("ROLE_ADMIN") &&
                !payment.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied: You can only view your own payments");
        }

        return convertToDTO(payment);
    }

    @Transactional(readOnly = true)
    public PaymentDTO getPaymentByIntentId(String paymentIntentId, Authentication authentication) {
        Payment payment = paymentRepository.findByPaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé"));

        // Vérifier les permissions
        User currentUser = getCurrentUser(authentication);
        if (!currentUser.getRole().name().equals("ROLE_ADMIN") &&
                !payment.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied: You can only view your own payments");
        }

        return convertToDTO(payment);
    }

    @Transactional
    public PaymentDTO updatePayment(Long id, UpdatePaymentDTO updatePaymentDTO, Authentication authentication) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé"));

        // Vérifier les permissions
        User currentUser = getCurrentUser(authentication);
        if (!currentUser.getRole().name().equals("ROLE_ADMIN") &&
                !payment.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied: You can only update your own payments");
        }

        // Seuls les paiements en attente peuvent être modifiés
        if (!payment.getStatus().equals(PaymentStatus.PENDING)) {
            throw new RuntimeException("Seuls les paiements en attente peuvent être modifiés");
        }

        // Mettre à jour les champs modifiables
        if (updatePaymentDTO.getDescription() != null) {
            payment.setDescription(updatePaymentDTO.getDescription());
        }
        if (updatePaymentDTO.getAmount() != null) {
            payment.setAmount(updatePaymentDTO.getAmount());
        }

        Payment updatedPayment = paymentRepository.save(payment);
        return convertToDTO(updatedPayment);
    }

    @Transactional
    public void deletePayment(Long id, Authentication authentication) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé"));

        // Vérifier les permissions
        User currentUser = getCurrentUser(authentication);
        if (!currentUser.getRole().name().equals("ROLE_ADMIN") &&
                !payment.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied: You can only delete your own payments");
        }

        // Annuler le paiement Stripe si nécessaire
        if (payment.getStatus() == PaymentStatus.PENDING) {
            cancelStripePayment(payment.getPaymentIntentId());
        }

        paymentRepository.delete(payment);
    }

    @Transactional
    public void handlePaymentSuccess(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            Payment payment = paymentRepository.findByPaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new RuntimeException("Paiement non trouvé"));

            if ("succeeded".equals(paymentIntent.getStatus())) {
                payment.setStatus(PaymentStatus.SUCCEEDED);
                payment.setPaidAt(LocalDateTime.now());
                paymentRepository.save(payment);

                log.info("Paiement {} marqué comme réussi", paymentIntentId);
            }
        } catch (StripeException e) {
            log.error("Erreur lors de la confirmation du paiement: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la confirmation du paiement", e);
        }
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getMyPayments(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        return paymentRepository.findByUserId(currentUser.getId()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByStatus(PaymentStatus status, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        // Seul l'admin peut filtrer tous les paiements par statut
        if (!currentUser.getRole().name().equals("ROLE_ADMIN")) {
            throw new RuntimeException("Access denied: Admins only");
        }

        return paymentRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getMyPaymentsByStatus(PaymentStatus status, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        return paymentRepository.findByUserIdAndStatus(currentUser.getId(), status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private PaymentDTO convertToDTO(Payment payment) {
        return PaymentDTO.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .description(payment.getDescription())
                .paymentMethod(payment.getPaymentMethod().name())
                .userId(payment.getUser().getId())
                .userName(payment.getUser().getFirstName() + " " + payment.getUser().getLastName())
                .userEmail(payment.getUser().getEmail())
                .createdAt(payment.getCreatedAt())
                .paidAt(payment.getPaidAt())
                .paymentIntentId(payment.getPaymentIntentId())
                .build();
    }

    private void cancelStripePayment(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            if (!"canceled".equals(paymentIntent.getStatus())) {
                paymentIntent.cancel();
            }
        } catch (StripeException e) {
            log.error("Erreur lors de l'annulation du paiement Stripe: {}", e.getMessage());
        }
    }
}