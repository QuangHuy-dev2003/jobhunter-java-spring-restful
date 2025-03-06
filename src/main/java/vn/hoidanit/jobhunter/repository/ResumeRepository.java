package vn.hoidanit.jobhunter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.hoidanit.jobhunter.domain.Resume;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long>,
    JpaSpecificationExecutor<Resume> {

  @Query("SELECT r FROM Resume r WHERE r.job.company.id = :companyId")
  List<Resume> findAllByCompanyId(@Param("companyId") Long companyId);

  @Query("SELECT r, j, c, s.name " +
      "FROM Resume r " +
      "JOIN r.job j " +
      "JOIN j.company c " +
      "LEFT JOIN j.skills s " +
      "WHERE r.user.email = :email")
  List<Object[]> findResumeDetailsByUserEmail(@Param("email") String email);
}
