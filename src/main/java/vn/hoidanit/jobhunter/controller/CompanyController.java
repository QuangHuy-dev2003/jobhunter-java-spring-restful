package vn.hoidanit.jobhunter.controller;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import vn.hoidanit.jobhunter.Util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.domain.reponse.ResultPaginationDTO;
import vn.hoidanit.jobhunter.service.CompanyService;

@RestController
@RequestMapping("/api/v1")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping("/companies")
    @ApiMessage("Create new company")
    public ResponseEntity<Company> createNewCompany(@Valid @RequestBody Company postManCompany) {
        Company company = this.companyService.handleCreateCompany(postManCompany);

        return ResponseEntity.status(HttpStatus.CREATED).body(company);
    }

    @GetMapping("/companies")
    @ApiMessage("Get all companies")
    public ResponseEntity<ResultPaginationDTO> getAllCompanies(
            @Filter Specification<Company> spec, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(this.companyService.fetchAllCompanies(spec, pageable));
    }

    @DeleteMapping("/companies/{id}")
    @ApiMessage("Delete company")
    public ResponseEntity<String> deleteCompany(@PathVariable("id") Long id) {
        this.companyService.handleDeleteCompany(id);
        return ResponseEntity.status(HttpStatus.OK).body("Company with id: " + id + " has been deleted");
    }

    @PutMapping("/companies")
    @ApiMessage("Update company")
    public ResponseEntity<Company> updateCompany(@Valid @RequestBody Company postManCompany) {
        Company company = this.companyService.handleUpdateCompany(postManCompany);
        return ResponseEntity.status(HttpStatus.OK).body(company);
    }

    @GetMapping("/companies/{id}")
    @ApiMessage("Fetch company by id")
    public ResponseEntity<Company> fetchCompanyById(@PathVariable("id") Long id) {
        Optional<Company> company = this.companyService.findById(id);
        return ResponseEntity.ok().body(company.get());
    }
}
