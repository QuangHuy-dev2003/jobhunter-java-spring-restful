package vn.hoidanit.jobhunter.domain.response.subscription;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.hoidanit.jobhunter.util.constant.SubscriptionStatusEnum;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDTO {
  private long postLimitID;
  private String planName;
  private SubscriptionStatusEnum status;
  private long timeRemainingInSeconds;

}
