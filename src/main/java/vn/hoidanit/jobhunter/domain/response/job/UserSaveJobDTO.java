package vn.hoidanit.jobhunter.domain.response.job;

import java.time.Instant;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.hoidanit.jobhunter.util.constant.LevelEnum;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSaveJobDTO {
  private CompanyUser company;
  private JobUser job;
  private List<SkillUser> skills;
  private String status;


  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  public static class CompanyUser {
    private long id;
    private String name;
    private String logo;
  }

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  public static class JobUser {
    private long id;
    private String name;
    private String location;
    private double salary;
    private LevelEnum level;
    private Instant startDate;
    private Instant endDate;
  }

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  public static class SkillUser {
    private long id;
    private String name;
  }
}