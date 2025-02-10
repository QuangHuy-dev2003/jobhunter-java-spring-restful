package vn.hoidanit.jobhunter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.hoidanit.jobhunter.service.UserSavedJobService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;


@RestController
@RequestMapping("/api/v1")
public class UserSavedJobController {
  private final UserSavedJobService userSavedJobService;

  public UserSavedJobController(UserSavedJobService userSavedJobService) {
    this.userSavedJobService = userSavedJobService;
  }

  @PostMapping("/user-saved-jobs")
  @ApiMessage("Save a job for a user")
  public ResponseEntity<?> saveJob(@RequestParam long userId, @RequestParam long jobId) {
    System.out.println("Received request to save job: userId=" + userId + ", jobId=" + jobId);
    boolean saved = userSavedJobService.saveJob(userId, jobId);
    if (saved) {
      return ResponseEntity.status(HttpStatus.OK).body(saved);
    } else {
      return ResponseEntity.badRequest().body(saved);
    }
  }

  //Check user_id xem đang đăng kí job_id nào
  @GetMapping("/user-saved-jobs/check")
  @ApiMessage("Check if a user has saved a job")
  public ResponseEntity<?> checkSavedJob(@RequestParam long userId, @RequestParam long jobId) {
    boolean saved = userSavedJobService.checkSavedJob(userId, jobId);
    if (saved) {
      return ResponseEntity.ok().body(saved);
    } else {
      return ResponseEntity.ok().body(saved);
    }
  }

  @PutMapping("/user-saved-jobs/toggle")
  @ApiMessage("Toggle the status of a saved job")
  public ResponseEntity<?> toggleSavedJob(@RequestParam long userId, @RequestParam long jobId) {
    boolean toggled = userSavedJobService.toggleJobStatus(userId, jobId);
    if (toggled) {
      return ResponseEntity.ok().body(toggled);
    } else {
      return ResponseEntity.badRequest().body(toggled);
    }
  }
}
