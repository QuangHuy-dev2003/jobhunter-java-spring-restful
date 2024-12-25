package vn.hoidanit.jobhunter.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.reponse.ResultPaginationDTO;
import vn.hoidanit.jobhunter.repository.CompanyRepository;
import vn.hoidanit.jobhunter.repository.UserRepository;

@Service
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public CompanyService(CompanyRepository companyRepository,
            UserRepository userRepository) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    public Company handleCreateCompany(Company company) {
        return this.companyRepository.save(company);
    }

    public Optional<Company> findById(Long id) {
        return this.companyRepository.findById(id);
    }

    

    public ResultPaginationDTO fetchAllCompanies(Specification<Company> spec, Pageable pageable) {
        Page<Company> comPage = this.companyRepository.findAll(pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();
        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());

        mt.setPages(comPage.getTotalPages());
        mt.setTotal(comPage.getTotalElements());

        rs.setMeta(mt);
        rs.setResult(comPage.getContent());

        return rs;
    }

    public void handleDeleteCompany(Long id) {
        Optional<Company> companyOptional = this.companyRepository.findById(id);
        if (companyOptional.isPresent()) {
            Company com = companyOptional.get();
            // fetch all user belong to this company
            List<User> users = this.userRepository.findByCompany(com);
            this.userRepository.deleteAll(users);
        }
        this.companyRepository.deleteById(id);
    }

    public Company handleUpdateCompany(Company postManCompany) {
        Optional<Company> company = this.companyRepository.findById(postManCompany.getId());
        if (company.isPresent()) {
            Company currentCompany = company.get();
            currentCompany.setName(postManCompany.getName());
            currentCompany.setDescription(postManCompany.getDescription());
            currentCompany.setAddress(postManCompany.getAddress());
            currentCompany.setLogo(postManCompany.getLogo());

            return this.companyRepository.save(currentCompany);
        }
        return null;
    }
}
