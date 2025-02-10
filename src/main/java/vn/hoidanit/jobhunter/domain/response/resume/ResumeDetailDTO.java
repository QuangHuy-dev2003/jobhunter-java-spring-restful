package vn.hoidanit.jobhunter.domain.response.resume;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import lombok.Data;
@Data
public class ResumeDetailDTO {
  private String url;
  private String status;
  private Instant createdAt;
  private String level;
  private String location;
  private long jobId;
  private String jobName;
  private double salary;
  private String companyName;
  private String companyLogo;
  private Set<String> skillNames;


}