package vn.hoidanit.jobhunter.domain.response.payment;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class ResPaymentDTO {
  private Long id;
  private String paymentRef;
  private Double totalPrice;
  private String transferContent;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String paymentStatus;
  private String paymentMethod;
  private UserDto user;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UserDto {
    private Long id;
    private String email;
    private String name;
  }

  @Override
  public String toString() {
    return "ResPaymentDTO{" +
        "id=" + id +
        ", paymentRef='" + paymentRef + '\'' +
        ", totalPrice=" + totalPrice +
        ", transferContent='" + transferContent + '\'' +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        ", paymentStatus='" + paymentStatus + '\'' +
        ", paymentMethod='" + paymentMethod + '\'' +
        ", user=" + user +
        '}';
  }
}
