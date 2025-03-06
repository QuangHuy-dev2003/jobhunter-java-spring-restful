package vn.hoidanit.jobhunter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.hoidanit.jobhunter.domain.ContactRequest;

@Repository
public interface ContactRequestRepository extends JpaRepository<ContactRequest, Long> {
  List<ContactRequest> findByStatus(String status);

  List<ContactRequest> findByStatusAndIsEmailSent(String status, Boolean isEmailSent);
}
