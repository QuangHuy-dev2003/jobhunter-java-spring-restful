package vn.hoidanit.jobhunter.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.hoidanit.jobhunter.domain.PostLimit;

public interface PostLimitRepository extends JpaRepository<PostLimit, Long> {
    Optional<PostLimit> findById(long id);
}
