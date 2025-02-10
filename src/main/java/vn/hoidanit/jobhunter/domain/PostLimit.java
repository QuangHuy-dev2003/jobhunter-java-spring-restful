package vn.hoidanit.jobhunter.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "post_limits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostLimit {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(unique = true)
  @NotBlank(message = "Plan name không được để trống")
  private String planName;

  @NotNull
  @Min(5)
  private int maxPostsPerMonth;
  private double price;
  @NotBlank(message = "Description không được để trống")
  private String description;

  private Instant createdAt;
  private Instant updatedAt;

  public PostLimit(String planName, int maxPostsPerMonth, double price, String description) {
    this.planName = planName;
    this.maxPostsPerMonth = maxPostsPerMonth;
    this.price = price;
    this.description = description;
  }

  @OneToMany(mappedBy = "postLimit")
  @JsonIgnore
  private List<Subscription> subscriptions;

  @PrePersist
  public void handleBeforeCreate() {
    this.createdAt = Instant.now();
  }

  @PreUpdate
  public void handleBeforeUpdate() {
    this.updatedAt = Instant.now();
  }
}
