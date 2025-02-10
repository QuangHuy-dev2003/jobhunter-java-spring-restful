package vn.hoidanit.jobhunter.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import vn.hoidanit.jobhunter.util.constant.SubscriptionStatusEnum;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
public class Subscription {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  private int numberOfMonths;
  private Instant startDate;
  private Instant endDate;
  private Instant createdAt;
  private Instant updatedAt;

  @Enumerated(EnumType.STRING)
  private SubscriptionStatusEnum status;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @OneToOne
  @JoinColumn(name = "payment_id")
  @JsonIgnore
  private Payment payment;

  @ManyToOne
  @JoinColumn(name = "post_limit_id")
  private PostLimit postLimit;

  @PrePersist
  public void handleBeforeCreate() {
    this.createdAt = Instant.now();
    this.startDate = Instant.now();
    this.endDate = this.startDate.plusSeconds(numberOfMonths * 30 * 24 * 60 * 60L);
  }

  @PreUpdate
  public void handleBeforeUpdate() {
    this.updatedAt = Instant.now();
  }

  public String getPlanName() {
    String limitDescription = (postLimit != null) ? postLimit.getDescription() : "Default";
    return numberOfMonths + " tháng - " + limitDescription;
  }
}