package vn.hoidanit.jobhunter.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.hoidanit.jobhunter.domain.Payment;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.response.payment.PlanSalesDTO;
import vn.hoidanit.jobhunter.util.constant.PaymentStatusEnum;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
  Optional<Payment> findByPaymentRef(String paymentRef);
  @Query("SELECT p FROM Payment p WHERE p.paymentStatus = :status")
  List<Payment> findByPaymentStatus(@Param("status") PaymentStatusEnum status);
  List<Payment> findByPostLimitId(Long postLimitId);
  List<Payment> findByUser(User user);

  List<Payment> findByTotalPriceGreaterThan(double amount);

  @Query("SELECT p FROM Payment p WHERE p.transferContent LIKE %:keyword%")
  List<Payment> searchByTransferContent(@Param("keyword") String keyword);

  @Query("SELECT p FROM Payment p WHERE p.user.id = :userId AND p.paymentStatus = :status")
  List<Payment> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") PaymentStatusEnum status);

  void deleteByPaymentRef(String paymentRef);

  boolean existsByPaymentRef(String paymentRef);

    @Query(value = """
          SELECT months.month AS month, pl.plan_name AS planName, COALESCE(COUNT(p.id), 0) AS totalSales
          FROM (
              SELECT 1 AS month UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION
              SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION
              SELECT 9 UNION SELECT 10 UNION SELECT 11 UNION SELECT 12
          ) AS months
          CROSS JOIN post_limits pl
          LEFT JOIN payments p ON MONTH(p.created_at) = months.month 
                               AND YEAR(p.created_at) = :year 
                               AND p.post_limit_id = pl.id 
                               AND p.payment_status = 'PAYMENT_SUCCEED'
          GROUP BY months.month, pl.plan_name
          ORDER BY months.month, pl.plan_name
          """, nativeQuery = true)
    List<Object[]> findMonthlyPlanSalesData(@Param("year") int year);

    // count total_price with payment_status = 'PAYMENT_SUCCEED'
  @Query("SELECT SUM(p.totalPrice) FROM Payment p WHERE p.paymentStatus = :status")
  Double getTotalPriceByPaymentStatus(@Param("status") PaymentStatusEnum status);
}
