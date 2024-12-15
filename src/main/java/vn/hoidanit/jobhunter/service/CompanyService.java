package vn.hoidanit.jobhunter.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.Company;
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

    public List<Company> fetchAllCompanies() {
        return this.companyRepository.findAll();
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
