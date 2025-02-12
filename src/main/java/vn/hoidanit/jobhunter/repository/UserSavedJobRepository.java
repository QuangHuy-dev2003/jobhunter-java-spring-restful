package vn.hoidanit.jobhunter.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.hoidanit.jobhunter.domain.UserSavedJob;

@Repository
public interface UserSavedJobRepository extends JpaRepository<UserSavedJob, Long> {
  boolean existsByUserIdAndJobId(long userId, long jobId);
  boolean existsByUserIdAndJobIdAndStatus(long userId, long jobId, String status);

  UserSavedJob findByUserIdAndJobId(long userId, long jobId);

  List<UserSavedJob> findAllByUserId(long userId);
}
