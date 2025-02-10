package vn.hoidanit.jobhunter.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "user_saved_jobs")
@Getter
@Setter
public class UserSavedJob {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne
  @JoinColumn(name = "job_id")
  private Job job;

  private Instant savedAt;
  private String status;

  @PrePersist
  public void handleBeforeCreate() {
    this.savedAt = Instant.now();
  }
}
