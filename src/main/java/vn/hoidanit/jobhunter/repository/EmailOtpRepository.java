package vn.hoidanit.jobhunter.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.hoidanit.jobhunter.domain.EmailOTP;

@Repository
public interface  EmailOtpRepository extends JpaRepository<EmailOTP, Long>{
  Optional<EmailOTP> findByEmailAndOtpCodeAndIsUsedFalseAndExpiryTimeAfter(
      String email,
      String otpCode,
      LocalDateTime now
  );

  @Query("DELETE FROM EmailOTP e WHERE e.expiryTime < :now OR e.isUsed = true")
  @Modifying
  void deleteExpiredOtp(@Param("now") LocalDateTime now);
}
