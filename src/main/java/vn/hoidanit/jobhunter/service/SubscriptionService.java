package vn.hoidanit.jobhunter.service;

import jakarta.transaction.Transactional;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vn.hoidanit.jobhunter.domain.Payment;
import vn.hoidanit.jobhunter.domain.PostLimit;
import vn.hoidanit.jobhunter.domain.Subscription;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.response.subscription.SubscriptionDTO;
import vn.hoidanit.jobhunter.repository.PaymentRepository;
import vn.hoidanit.jobhunter.repository.PostLimitRepository;
import vn.hoidanit.jobhunter.repository.SubscriptionRepository;
import vn.hoidanit.jobhunter.util.constant.SubscriptionStatusEnum;

@Service
public class SubscriptionService {

  private final SubscriptionRepository subscriptionRepository;


  public SubscriptionService(SubscriptionRepository subscriptionRepository
      ) {
    this.subscriptionRepository = subscriptionRepository;

  }

  @Transactional
  public Subscription createSubscription(Payment payment) {
    try {
      // Kiểm tra và expire subscription cũ
      expireActiveSubscription(payment.getUser());

      // Parse số tháng từ transferContent
      int numberOfMonths = extractNumberOfMonths(payment.getTransferContent());

      // Tạo subscription mới
      Subscription subscription = new Subscription();
      subscription.setUser(payment.getUser());
      subscription.setPayment(payment);
      subscription.setPostLimit(payment.getPostLimit());
      subscription.setStatus(SubscriptionStatusEnum.ACTIVE);

      // Set số tháng và thời gian
      if ("FREE".equals(payment.getPostLimit().getPlanName())) {
        subscription.setNumberOfMonths(1000000);
      } else {
        subscription.setNumberOfMonths(numberOfMonths);
      }

      System.out.println("Created new subscription: " +
          "User=" + payment.getUser().getEmail() +
          ", Plan=" + payment.getPostLimit().getPlanName() +
          ", Months=" + numberOfMonths);

      return subscriptionRepository.save(subscription);

    } catch (Exception e) {
      String errorMsg = "Error creating subscription for user: " +
          payment.getUser().getEmail() + ", Error: " + e.getMessage();
      System.err.println(errorMsg);
      e.printStackTrace();
      throw new RuntimeException(errorMsg, e);
    }
  }

  // logic expire subscription cũ
  private void expireActiveSubscription(User user) {
    Optional<Subscription> activeSubscription = subscriptionRepository
        .findActiveSubscription(user, SubscriptionStatusEnum.ACTIVE, Instant.now());

    activeSubscription.ifPresent(oldSub -> {
      oldSub.setStatus(SubscriptionStatusEnum.EXPIRED);
      subscriptionRepository.save(oldSub);
      System.out.println("Expired old subscription for user: " + user.getEmail());
    });
  }

  //  logic parse số tháng
  private int extractNumberOfMonths(String transferContent) {
    if (transferContent == null || transferContent.isEmpty()) {
      System.out.println("Transfer content is empty, using default months: 1");
      return 1;
    }

    try {
      String decodedContent = URLDecoder.decode(transferContent, StandardCharsets.UTF_8);
      System.out.println("Decoded transfer content: " + decodedContent);

      Pattern pattern = Pattern.compile("Thời hạn (\\d+) tháng");
      Matcher matcher = pattern.matcher(decodedContent);

      if (matcher.find()) {
        int months = Integer.parseInt(matcher.group(1));
        if (isValidSubscriptionMonths(months)) {
          return months;
        }
        System.out.println("Invalid number of months: " + months + ", using default: 1");
      } else {
        System.out.println("Could not find months in content: " + decodedContent);
      }
    } catch (Exception e) {
      System.err.println("Error parsing months from content: " + e.getMessage());
    }

    return 1; // Mặc định 1 tháng nếu có lỗi
  }

  // logic validate số tháng
  private boolean isValidSubscriptionMonths(int months) {
    return months == 1 || months == 3 || months == 6 || months == 12;
  }

  public boolean isSubscriptionActive(User user) {
    Optional<Subscription> subscription = subscriptionRepository.findActiveSubscription(
        user,
        SubscriptionStatusEnum.ACTIVE,
        Instant.now()
    );
    return subscription.isPresent();
  }

  public Optional<Subscription> getCurrentSubscription(User user) {
    return subscriptionRepository.findActiveSubscription(
        user,
        SubscriptionStatusEnum.ACTIVE,
        Instant.now()
    );
  }

  @Scheduled(cron = "0 0 0 * * ?")
  public void checkExpiredSubscriptions() {
    Instant now = Instant.now();
    List<Subscription> activeSubscriptions = subscriptionRepository.findByStatus(
        SubscriptionStatusEnum.ACTIVE);

    activeSubscriptions.forEach(subscription -> {
      if (!"FREE".equals(subscription.getPostLimit().getPlanName())
          && subscription.getEndDate() != null
          && subscription.getEndDate().isBefore(now)) {
        subscription.setStatus(SubscriptionStatusEnum.EXPIRED);
        subscriptionRepository.save(subscription);
      }
    });
  }

  public SubscriptionDTO getUserSubscriptionStatus(long userId) {
//    Instant now = Instant.parse("2025-04-17T08:04:04.312375Z");
    Instant now = Instant.now();
    Optional<Subscription> activeSubscription = subscriptionRepository.findActiveSubscriptionByUserId(userId, now);

    if (activeSubscription.isPresent()) {
      Subscription subscription = activeSubscription.get();
      long timeRemainingInSeconds = subscription.getEndDate() != null
          ? ChronoUnit.SECONDS.between(now, subscription.getEndDate())
          : 0;

      long postLimitID = subscription.getPostLimit().getId();  // ID gói
      String planName = subscription.getPlanName();  // Tên gói
      SubscriptionStatusEnum status = subscription.getStatus();  // Trạng thái gói

      return new SubscriptionDTO(postLimitID,planName, status, timeRemainingInSeconds);
    }

    // Nếu không có gói đăng ký hoặc gói đã hết hạn
    return new SubscriptionDTO(
        0L, // postLimitId cho gói FREE
        "FREE",
        SubscriptionStatusEnum.ACTIVE,
        Long.MAX_VALUE // Thời gian không giới hạn cho gói FREE
    );
  }
}
