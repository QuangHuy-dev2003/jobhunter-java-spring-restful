package vn.hoidanit.jobhunter.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.domain.User;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    User findByEmail(String email);

    Optional<User> findById(Long id);

    boolean existsByEmail(String email);

    User findByRefreshTokenAndEmail(String token, String email);

    List<User> findByCompany(Company company);

    @Query("SELECT u.company.id FROM User u WHERE u.id = :userId AND u.role.name = :roleName")
    List<Long> findCompanyIdsByUserIdAndRoleName(long userId, String roleName);


    @Query("SELECT u.postCount FROM User u WHERE u.id = :id")
    Long getPostCountById(@Param("id") long id);
}
