package vn.hoidanit.jobhunter.domain.response.payment;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlanSalesDTO {
 private Integer month;
 @NotBlank(message = "Plan name không được để trống")
 private String planName;
 private long totalSales;
}
