package vn.hoidanit.jobhunter.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vn.hoidanit.jobhunter.domain.Subscription;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.response.subscription.SubscriptionDTO;
import vn.hoidanit.jobhunter.service.SubscriptionService;
import vn.hoidanit.jobhunter.service.UserService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
public class SubscriptionController {
  private final SubscriptionService subscriptionService;
  private final UserService userService;

  public SubscriptionController(SubscriptionService subscriptionService,
      UserService userService) {
    this.subscriptionService = subscriptionService;
    this.userService = userService;
  }

  @GetMapping("/subscriptions/current")
  public ResponseEntity<?> getCurrentSubscription() {
    String userEmail = userService.getCurrentUserEmail();
    User user = userService.handleGetUserByUsername(userEmail);
    Optional<Subscription> subscription = subscriptionService.getCurrentSubscription(user);

    return ResponseEntity.ok(Map.of(
        "code", "00",
        "message", "success",
        "data", subscription.orElse(null)));
  }

  @GetMapping("/subscriptions/check-status")
  public ResponseEntity<?> checkSubscriptionStatus() {
    String userEmail = userService.getCurrentUserEmail();
    User user = userService.handleGetUserByUsername(userEmail);
    boolean isActive = subscriptionService.isSubscriptionActive(user);

    return ResponseEntity.ok(Map.of(
        "code", "00",
        "message", "success",
        "data", Map.of(
            "isActive", isActive)));
  }

  @GetMapping("/subscriptions/{userId}")
  @ApiMessage("Check subscription by user_id.")
  public ResponseEntity<?> getUserSubscriptionStatus(@PathVariable("userId") long userId) {
    SubscriptionDTO subscriptionDTO = subscriptionService.getUserSubscriptionStatus(userId);

    if (subscriptionDTO != null) {
      return ResponseEntity.ok(subscriptionDTO);
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of(
              "code", "01",
              "message", "User does not have an active subscription."));
    }
  }
}
