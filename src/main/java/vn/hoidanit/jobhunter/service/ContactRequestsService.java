package vn.hoidanit.jobhunter.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.ContactRequest;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.response.ResUserDTO;
import vn.hoidanit.jobhunter.repository.ContactRequestRepository;
import vn.hoidanit.jobhunter.repository.UserRepository;

@Service
public class ContactRequestsService {
    private final ContactRequestRepository contactRequestRepository;
    private final UserRepository userRepository;

    public ContactRequestsService(ContactRequestRepository contactRequestRepository,
            UserRepository userRepository) {
        this.contactRequestRepository = contactRequestRepository;
        this.userRepository = userRepository;
    }

    public List<ContactRequest> getAllContactRequests() {
        return contactRequestRepository.findAll();
    }

    public ContactRequest findByContarctRequest(Long id) {
        Optional<ContactRequest> optional = contactRequestRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        } else {
            return null;
        }
    }

    public ContactRequest updateContactRequestStatus(Long id, String status) {
        Optional<ContactRequest> optional = contactRequestRepository.findById(id);
        if (optional.isPresent()) {
            ContactRequest contactRequest = optional.get();
            contactRequest.setStatus(status);
            return contactRequestRepository.save(contactRequest);
        } else {
            return null;
        }
    }

    public List<ContactRequest> findByStatus(String status) {
        return contactRequestRepository.findByStatus(status);
    }

    public List<ContactRequest> findByStatusAndIsEmailSent(String status, Boolean isEmailSent) {
        return contactRequestRepository.findByStatusAndIsEmailSent(status, isEmailSent);
    }

    // Cập nhật trnajg thái email đã gửi
    public ContactRequest updateEmailSentStatus(Long id, boolean emailSent) {
        ContactRequest contactRequest = contactRequestRepository.findById(id).orElse(null);
        if (contactRequest != null) {
            contactRequest.setIsEmailSent(emailSent);
            if (emailSent) {
                contactRequest.setEmailSentAt(LocalDateTime.now());
            }
            return contactRequestRepository.save(contactRequest);
        }
        return null;
    }

    public Optional<ResUserDTO> checkEmailMatchWithUser(String email) {
        Optional<User> userOptional = userRepository.findUserWithCompanyAndRoleByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            ResUserDTO resUserDTO = new ResUserDTO();
            resUserDTO.setId(user.getId());
            resUserDTO.setEmail(user.getEmail());
            resUserDTO.setName(user.getName());
            resUserDTO.setPhoneNumber(user.getPhoneNumber());

            if (user.getCompany() != null) {
                ResUserDTO.CompanyUser companyUser = new ResUserDTO.CompanyUser();
                companyUser.setId(user.getCompany().getId());
                companyUser.setName(user.getCompany().getName());
                companyUser.setLogo(user.getCompany().getLogo());
                resUserDTO.setCompany(companyUser);
            }

            if (user.getRole() != null) {
                ResUserDTO.RoleUser roleUser = new ResUserDTO.RoleUser();
                roleUser.setId(user.getRole().getId());
                roleUser.setName(user.getRole().getName());
                resUserDTO.setRole(roleUser);
            }

            return Optional.of(resUserDTO);
        }

        return Optional.empty();
    }
}
