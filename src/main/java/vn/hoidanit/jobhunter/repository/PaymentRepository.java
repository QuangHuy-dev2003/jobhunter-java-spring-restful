package vn.hoidanit.jobhunter.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.hoidanit.jobhunter.domain.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentRef(String paymentRef);
}
