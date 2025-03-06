package vn.hoidanit.jobhunter.domain;

import jakarta.persistence.Column;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "contact_requests")
public class ContactRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String position;
    private String email;
    private String phone;
    private String companyName;
    private String companyLocation;
    private String website;
    private String status;

    @Column(name = "is_email_sent", columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean isEmailSent = false;
    private LocalDateTime createdAt;
    private LocalDateTime emailSentAt;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

}
