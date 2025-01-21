package vn.hoidanit.jobhunter.domain;

import java.time.Instant;
import java.util.List;

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
    private String planName; // FREE, BASIC, PREMIUM

    private int maxPostsPerMonth;
    private double price;
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