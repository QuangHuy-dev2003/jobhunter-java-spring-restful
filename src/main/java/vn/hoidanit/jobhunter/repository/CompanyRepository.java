package vn.hoidanit.jobhunter.repository;

import org.springframework.stereotype.Repository;

import vn.hoidanit.jobhunter.domain.Company;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    
}
