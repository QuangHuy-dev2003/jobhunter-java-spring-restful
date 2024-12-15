package vn.hoidanit.jobhunter.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.domain.DTO.Meta;
import vn.hoidanit.jobhunter.domain.DTO.ResultPaginationDTO;
import vn.hoidanit.jobhunter.repository.CompanyRepository;

@Service
public class CompanyService {
    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Company handleCreateCompany(Company company) {
        return this.companyRepository.save(company);
    }

    public ResultPaginationDTO fetchAllCompanies(Specification<Company> spec, Pageable pageable) {
        Page<Company> comPage = this.companyRepository.findAll(pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        Meta mt = new Meta();
        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());

        mt.setPages(comPage.getTotalPages());
        mt.setTotal(comPage.getTotalElements());

        rs.setMeta(mt);
        rs.setResult(comPage.getContent());

        return rs;
    }

    public void handleDeleteCompany(Long id) {
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
