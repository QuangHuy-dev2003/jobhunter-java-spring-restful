package vn.hoidanit.jobhunter.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.hoidanit.jobhunter.domain.Subscription;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.util.constant.SubscriptionStatusEnum;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
  List<Subscription> findByStatus(SubscriptionStatusEnum status);
  List<Subscription> findByPostLimitId(long postLimitId);


  @Query("SELECT s FROM Subscription s WHERE s.user = :user " +
      "AND s.status = :status " +
      "AND (s.endDate IS NULL OR s.endDate > :now) " +
      "ORDER BY s.createdAt DESC")
  Optional<Subscription> findActiveSubscription(
      @Param("user") User user,
      @Param("status") SubscriptionStatusEnum status,
      @Param("now") Instant now
  );

  @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId " +
      "AND s.status = 'ACTIVE' " +
      "AND (s.endDate IS NULL OR s.endDate > :now) " +
      "ORDER BY s.createdAt DESC")
  Optional<Subscription> findActiveSubscriptionByUserId(
      @Param("userId") long userId,
      @Param("now") Instant now
  );
}
