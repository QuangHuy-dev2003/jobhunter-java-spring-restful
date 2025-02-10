package vn.hoidanit.jobhunter.controller;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import vn.hoidanit.jobhunter.domain.Job;
import vn.hoidanit.jobhunter.domain.Skill;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.domain.response.job.ResCreateJobDTO;
import vn.hoidanit.jobhunter.domain.response.job.ResUpdateJobDTO;
import vn.hoidanit.jobhunter.service.JobService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping("/jobs")
    @ApiMessage("Create a job")
    public ResponseEntity<ResCreateJobDTO> create(@Valid @RequestBody Job job) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.jobService.create(job));
    }

    @PutMapping("/jobs")
    @ApiMessage("Update a job")
    public ResponseEntity<ResUpdateJobDTO> update(@Valid @RequestBody Job job) throws IdInvalidException {
        Optional<Job> currentJob = this.jobService.fetchJobById(job.getId());
        if (!currentJob.isPresent()) {
            throw new IdInvalidException("Job not found");
        }

        return ResponseEntity.ok()
                .body(this.jobService.update(job, currentJob.get()));
    }

    @DeleteMapping("/jobs/{id}")
    @ApiMessage("Delete a job by id")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) throws IdInvalidException {
        Optional<Job> currentJob = this.jobService.fetchJobById(id);
        if (!currentJob.isPresent()) {
            throw new IdInvalidException("Job not found");
        }
        this.jobService.delete(id);
        return ResponseEntity.ok().body(null);
    }

    @GetMapping("/jobs/{id}")
    @ApiMessage("Get a job by id")
    public ResponseEntity<Job> getJob(@PathVariable("id") long id) throws IdInvalidException {
        Optional<Job> currentJob = this.jobService.fetchJobById(id);
        if (!currentJob.isPresent()) {
            throw new IdInvalidException("Job not found");
        }

        return ResponseEntity.ok().body(currentJob.get());
    }

    @GetMapping("/jobs")
    @ApiMessage("Get job with pagination")
    public ResponseEntity<ResultPaginationDTO> getAllJob(
            @Filter Specification<Job> spec,
            Pageable pageable) {

        return ResponseEntity.ok().body(this.jobService.fetchAll(spec, pageable));
    }

    @GetMapping("/jobs/skill/{skillId}")
    @ApiMessage("Get jobs by skill ID")
    public ResponseEntity<List<Job>> getJobsBySkillId(@PathVariable("skillId") long skillId) {
        List<Job> jobs = this.jobService.fetchJobsBySkillId(skillId);
        return ResponseEntity.ok().body(jobs);
    }

    @GetMapping("/jobs/location/{location}")
    @ApiMessage("Get jobs by location")
    public ResponseEntity<List<Job>> getJobsByLocation(@PathVariable("location") String location) {
        List<Job> jobs = this.jobService.fetchJobsByLocation(location);
        return ResponseEntity.ok().body(jobs);
    }


    @GetMapping("/jobs/skills/{skillId}")
    @ApiMessage("Get jobs by skill ID")
    public ResponseEntity<List<Job>> getALLJobsBySkillId(@PathVariable("skillId") long skillId) {
        List<Job> jobs = this.jobService.fetchAllJobsBySkillId(skillId);
        return ResponseEntity.ok().body(jobs);
    }

    @GetMapping("/jobs/random")
    @ApiMessage("Get 6 random jobs")
    public ResponseEntity<List<Job>> getRandomJobs() {
        List<Job> jobs = this.jobService.fetchRandomJobs();
        return ResponseEntity.ok().body(jobs);
    }

    @GetMapping("/jobs/hr/{userId}")
    @ApiMessage("Get jobs by HR user ID")
    public ResponseEntity<List<Job>> getJobsByHRUserId(@PathVariable("userId") long userId) {
        List<Job> jobs = this.jobService.fetchJobsByHRUser(userId);
        return ResponseEntity.ok().body(jobs);
    }

    // Count Job
    @GetMapping("/jobs/count")
    @ApiMessage("Count jobs")
    public ResponseEntity<Long> countJobs() {
        Long count = this.jobService.countJobs();
        return ResponseEntity.ok().body(count);
    }
}
