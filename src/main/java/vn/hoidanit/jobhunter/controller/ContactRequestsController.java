package vn.hoidanit.jobhunter.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import vn.hoidanit.jobhunter.domain.ContactRequest;
import vn.hoidanit.jobhunter.domain.response.ResUserDTO;
import vn.hoidanit.jobhunter.service.ContactRequestsService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
public class ContactRequestsController {
    private final ContactRequestsService contactRequestsService;

    public ContactRequestsController(ContactRequestsService contactRequestsService) {
        this.contactRequestsService = contactRequestsService;
    }

    @GetMapping("/contact-requests")
    @ApiMessage("Get all contact requests")
    public ResponseEntity<?> getAllContactRequests() {
        List<ContactRequest> contactRequests = contactRequestsService.getAllContactRequests();
        return ResponseEntity.ok(contactRequests);
    }

    @GetMapping("/contact-requests/{id}")
    public ResponseEntity<?> getContactRequestById(@PathVariable("id") Long id) {
        ContactRequest contactRequest = contactRequestsService.findByContarctRequest(id);
        if (contactRequest != null) {
            return ResponseEntity.ok(contactRequest);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Contact request not found with id: " + id));
        }
    }

    @PutMapping("/contact-requests/{id}/status")
    @ApiMessage("Update contact request status")
    public ResponseEntity<?> updateContactRequestStatus(
            @PathVariable("id") Long id,
            @RequestBody Map<String, String> statusUpdate) {

        String newStatus = statusUpdate.get("status");
        if (newStatus == null || newStatus.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Status cannot be empty"));
        }

        ContactRequest updatedRequest = contactRequestsService.updateContactRequestStatus(id, newStatus);

        if (updatedRequest != null) {
            return ResponseEntity.ok(Map.of(
                    "message", "Status updated successfully",
                    "data", updatedRequest));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Contact request not found with id: " + id));
        }
    }

    @GetMapping("/contact-requests/approved")
    @ApiMessage("Get all approved contact requests")
    public ResponseEntity<?> getApprovedContactRequests() {
        List<ContactRequest> contactRequests = contactRequestsService.findByStatus("APPROVED");
        return ResponseEntity.ok(contactRequests);
    }

    @GetMapping("/contact-requests/check-email")
    @ApiMessage("Check if contact request email exists in users")
    public ResponseEntity<?> checkContactRequestEmail(@RequestParam("email") String email) {
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email cannot be empty"));
        }

        Optional<ResUserDTO> matchingUser = contactRequestsService.checkEmailMatchWithUser(email);

        if (matchingUser.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "exists", true,
                    "message", "Email matches with an existing user",
                    "userData", matchingUser.get()));
        } else {
            return ResponseEntity.ok(Map.of(
                    "exists", false,
                    "message", "Email does not match with any user"));
        }
    }
}
