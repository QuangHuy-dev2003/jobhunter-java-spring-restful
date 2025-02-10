package vn.hoidanit.jobhunter.domain.response.postlimit;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PostLimitDTO {
  private long id;
  private String planName;
  private int maxPostsPerMonth;
  private double price;
  private String description;
}
