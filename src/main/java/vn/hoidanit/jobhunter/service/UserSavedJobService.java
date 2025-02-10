package vn.hoidanit.jobhunter.service;

import org.springframework.stereotype.Service;
import vn.hoidanit.jobhunter.domain.Job;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.UserSavedJob;
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
}
