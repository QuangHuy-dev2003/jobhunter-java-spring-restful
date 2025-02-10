package vn.hoidanit.jobhunter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import vn.hoidanit.jobhunter.domain.Job;
import vn.hoidanit.jobhunter.domain.Skill;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>,
                JpaSpecificationExecutor<Job> {

        List<Job> findBySkillsIn(List<Skill> skills);

        List<Job> findByLocation(String location);
        List<Job> findByLocationNotIn(List<String> locations);

        @Query(value = "SELECT j FROM Job j JOIN FETCH j.company ORDER BY RAND() LIMIT 6")
        List<Job> findRandomJobs();

        List<Job> findByCompanyIdIn(List<Long> companyIds);

}
