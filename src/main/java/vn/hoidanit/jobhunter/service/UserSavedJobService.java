package vn.hoidanit.jobhunter.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import vn.hoidanit.jobhunter.domain.Job;
import vn.hoidanit.jobhunter.domain.Skill;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.UserSavedJob;
import vn.hoidanit.jobhunter.domain.response.job.UserSaveJobDTO;
import vn.hoidanit.jobhunter.repository.JobRepository;
import vn.hoidanit.jobhunter.repository.UserRepository;
import vn.hoidanit.jobhunter.repository.UserSavedJobRepository;

@Service
public class UserSavedJobService {

  private final UserSavedJobRepository userSavedJobRepository;
  private UserRepository userRepository;
  private JobRepository jobRepository;

  public UserSavedJobService(UserSavedJobRepository userSavedJobRepository,
      UserRepository userRepository, JobRepository jobRepository) {
    {
      this.userSavedJobRepository = userSavedJobRepository;
      this.userRepository = userRepository;
      this.jobRepository = jobRepository;
    }
  }

  public boolean saveJob(long userId, long jobId) {
    UserSavedJob userSavedJob = userSavedJobRepository.findByUserIdAndJobId(userId, jobId);

    if (userSavedJob != null) {
      // Bản ghi đã tồn tại, cập nhật status
      userSavedJob.setStatus("true");
    } else {
      // Bản ghi chưa tồn tại, tạo mới
      User user = userRepository.findById(userId).orElse(null);
      Job job = jobRepository.findById(jobId).orElse(null);

      if (user == null || job == null) {
        return false;
      }

      userSavedJob = new UserSavedJob();
      userSavedJob.setUser(user);
      userSavedJob.setJob(job);
      userSavedJob.setStatus("true");
    }

    try {
      userSavedJobRepository.save(userSavedJob);
      System.out.println("Job saved successfully.");
      return true;
    } catch (Exception e) {
      System.out.println("Error saving job: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  public boolean checkSavedJob(long userId, long jobId) {
    return userSavedJobRepository.existsByUserIdAndJobIdAndStatus(userId, jobId, "true");
  }

  public boolean toggleJobStatus(long userId, long jobId) {
    UserSavedJob userSavedJob = userSavedJobRepository.findByUserIdAndJobId(userId, jobId);
    if (userSavedJob != null) {
      // Toggle the status
      userSavedJob.setStatus(userSavedJob.getStatus().equals("true") ? "false" : "true");
      userSavedJobRepository.save(userSavedJob);
      return true;
    }
    return false;
  }

  public List<UserSavedJob> getSavedJobs(long userId) {
    return userSavedJobRepository.findAllByUserId(userId);
  }

  public UserSaveJobDTO convertToDTO(UserSavedJob userSavedJob) {
    UserSaveJobDTO dto = new UserSaveJobDTO();

    // Chuyển đổi Company
    UserSaveJobDTO.CompanyUser company = new UserSaveJobDTO.CompanyUser();
    company.setId(userSavedJob.getJob().getCompany().getId());
    company.setName(userSavedJob.getJob().getCompany().getName());
    company.setLogo(userSavedJob.getJob().getCompany().getLogo());
    dto.setCompany(company);

    // Chuyển đổi Job
    UserSaveJobDTO.JobUser job = new UserSaveJobDTO.JobUser();
    job.setId(userSavedJob.getJob().getId());
    job.setName(userSavedJob.getJob().getName());
    job.setLocation(userSavedJob.getJob().getLocation());
    job.setSalary(userSavedJob.getJob().getSalary());
    job.setLevel(userSavedJob.getJob().getLevel());
    job.setStartDate(userSavedJob.getJob().getStartDate());
    dto.setJob(job);

    // Chuyển đổi Skill
    if (userSavedJob.getJob().getSkills() != null && !userSavedJob.getJob().getSkills().isEmpty()) {
      List<UserSaveJobDTO.SkillUser> skillUsers = userSavedJob.getJob().getSkills().stream()
          .map(skill -> {
            UserSaveJobDTO.SkillUser skillUser = new UserSaveJobDTO.SkillUser();
            skillUser.setId(skill.getId());
            skillUser.setName(skill.getName());
            return skillUser;
          })
          .collect(Collectors.toList());
      dto.setSkills(skillUsers);
    } else {
      dto.setSkills(new ArrayList<>());
    }

    // Đặt trạng thái
    dto.setStatus(userSavedJob.getStatus());

    return dto;
  }
}
