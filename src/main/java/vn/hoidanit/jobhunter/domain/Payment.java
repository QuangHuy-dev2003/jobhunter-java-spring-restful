package vn.hoidanit.jobhunter.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import vn.hoidanit.jobhunter.Util.constant.PaymentMethodEnum;
import vn.hoidanit.jobhunter.Util.constant.PaymentStatusEnum;


@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true)
    private String paymentRef;

    private double totalPrice;
    private String transferContent;
    private Instant createdAt;
    private Instant updatedAt;

    @Enumerated(EnumType.STRING)
    private PaymentStatusEnum paymentStatus;

    @Enumerated(EnumType.STRING)
    private PaymentMethodEnum paymentMethod;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(mappedBy = "payment")
    @JsonIgnore
    private Subscription subscription;

    @ManyToOne
    @JoinColumn(name = "post_limit_id")
    private PostLimit postLimit;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedAt = Instant.now();
    }
}
