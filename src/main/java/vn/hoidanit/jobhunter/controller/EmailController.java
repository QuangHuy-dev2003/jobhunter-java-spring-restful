package vn.hoidanit.jobhunter.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import vn.hoidanit.jobhunter.domain.ContactRequest;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.repository.ContactRequestRepository;
import vn.hoidanit.jobhunter.repository.UserRepository;
import vn.hoidanit.jobhunter.service.EmailService;
import vn.hoidanit.jobhunter.service.SubscriberService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1")
public class EmailController {

    private final EmailService emailService;
    private final SubscriberService subscriberService;
    private final ContactRequestRepository contactRequestRepository;
    private final UserRepository userRepository;

    public EmailController(EmailService emailService,
            SubscriberService subscriberService,
            ContactRequestRepository contactRequestRepository,
            UserRepository userRepository) {
        this.emailService = emailService;
        this.contactRequestRepository = contactRequestRepository;
        this.subscriberService = subscriberService;
        this.userRepository = userRepository;
    }

    @GetMapping("/email")
    @ApiMessage("Send simple email")
    // @Scheduled(cron = "*/30 * * * * *")
    // @Transactional
    public String sendSimpleEmail() {

        this.emailService.sendSimpleEmail();
        this.emailService.sendEmailFromTemplateSync("daoquanghuy17@gmail.com", "Test Send Email", "job", "Quang Huy",
                new Object());
        this.subscriberService.sendSubscribersEmailJobs();
        return "ok";
    }

    @GetMapping("/email/check")
    @ApiMessage("Check if email exists")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email) {
        boolean exists = emailService.findByEmail(email);
        return ResponseEntity.ok().body(exists);
    }

    // Send Email OTP

    @PostMapping("/email/send-otp")
    @ApiMessage("Send OTP email")
    public ResponseEntity<?> sendOtpEmail(@RequestParam String email) {
        try {
            if (!emailService.findByEmail(email)) {
                return ResponseEntity.badRequest()
                        .body(new HashMap<String, String>() {
                            {
                                put("message", "Email không tồn tại trong hệ thống");
                            }
                        });
            }

            emailService.sendOtpEmail(email);
            return ResponseEntity.ok()
                    .body(new HashMap<String, String>() {
                        {
                            put("message", "Mã OTP đã được gửi");
                        }
                    });
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new HashMap<String, String>() {
                        {
                            put("message", "Lỗi khi gửi mã OTP");
                        }
                    });
        }
    }

    // Verify OTP

    @PostMapping("/email/verify-otp")
    @ApiMessage("Verify OTP code")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otpCode = request.get("otpCode");

        boolean isValid = emailService.verifyOtp(email, otpCode);
        if (isValid) {
            return ResponseEntity.ok()
                    .body(new HashMap<String, String>() {
                        {
                            put("message", "Mã OTP hợp lệ");
                        }
                    });
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new HashMap<String, String>() {
                        {
                            put("message", "Mã OTP không hợp lệ hoặc đã hết hạn");
                        }
                    });
        }
    }

    @PostMapping("/email/contact")
    @ApiMessage("Gửi form liên hệ")
    public ResponseEntity<?> submitContactForm(@RequestBody Map<String, String> request) {
        try {
            String fullName = request.get("fullName");
            String position = request.get("position");
            String email = request.get("email");
            String phone = request.get("phone");
            String companyName = request.get("companyName");
            String companyLocation = request.get("companyLocation");
            String website = request.get("website");

            // Chuyển đổi mã vị trí thành văn bản có thể đọc được
            String locationText = switch (companyLocation) {
                case "hcm" -> "Hồ Chí Minh";
                case "hn" -> "Hà Nội";
                case "dn" -> "Đà Nẵng";
                default -> "Khác";
            };

            // Lưu thông tin liên hệ vào database
            ContactRequest contactRequest = new ContactRequest();
            contactRequest.setFullName(fullName);
            contactRequest.setPosition(position);
            contactRequest.setEmail(email);
            contactRequest.setPhone(phone);
            contactRequest.setCompanyName(companyName);
            contactRequest.setCompanyLocation(locationText);
            contactRequest.setWebsite(website);
            contactRequest.setStatus("PENDING");
            contactRequest.setCreatedAt(LocalDateTime.now());
            contactRequestRepository.save(contactRequest);

            // Tạo map chứa dữ liệu form cho template
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("fullName", fullName);
            templateData.put("position", position);
            templateData.put("email", email);
            templateData.put("phone", phone);
            templateData.put("companyName", companyName);
            templateData.put("companyLocation", locationText);
            templateData.put("website", website);
            templateData.put("currentDate", java.time.LocalDate.now().toString());

            // Gửi email thông báo cho admin
            emailService.sendContactNotificationEmail(templateData);

            return ResponseEntity.ok()
                    .body(new HashMap<String, String>() {
                        {
                            put("message", "Yêu cầu liên hệ của bạn đã được gửi thành công");
                        }
                    });
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new HashMap<String, String>() {
                        {
                            put("message", "Đã xảy ra lỗi khi gửi yêu cầu liên hệ");
                        }
                    });
        }
    }

    @PostMapping("/email/recruiter-activation")
    @ApiMessage("Gửi email thông báo trở thành nhà tuyển dụng")
    public ResponseEntity<?> sendRecruiterActivationEmail(@RequestBody Map<String, Object> request) {
        try {
            // Lấy thông tin từ request
            String email = (String) request.get("email");
            String name = (String) request.get("name");
            String companyName = (String) request.get("companyName");
            String password = (String) request.get("password");
            Long contactRequestId = request.get("contactRequestId") != null
                    ? Long.valueOf(request.get("contactRequestId").toString())
                    : null;

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Email không được để trống"));
            }

            // Tạo map chứa dữ liệu cho template
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("email", email);
            templateData.put("name", name != null ? name : "người dùng");
            templateData.put("companyName", companyName != null ? companyName : "Công ty của bạn");
            templateData.put("password", password != null ? password : "12345678");

            User user = userRepository.findByEmail(email);

            // Gửi email thông báo kèm OTP
            boolean sent = emailService.sendRecruiterActivationEmail(templateData);

            if (sent) {
                // Cập nhật trạng thái đã gửi email cho contact request nếu có id
                if (contactRequestId != null) {
                    ContactRequest contactRequest = contactRequestRepository.findById(contactRequestId).orElse(null);
                    if (contactRequest != null) {
                        contactRequest.setIsEmailSent(true);
                        contactRequest.setEmailSentAt(LocalDateTime.now());

                        // Liên kết user nếu người dùng đã tồn tại
                        if (user != null) {
                            contactRequest.setUser(user);
                        }
                        contactRequestRepository.save(contactRequest);
                    }
                }

                return ResponseEntity.ok()
                        .body(Map.of("message", "Email thông báo và mã OTP đã được gửi thành công"));
            } else {
                return ResponseEntity.internalServerError()
                        .body(Map.of("message", "Có lỗi xảy ra khi gửi email"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Đã xảy ra lỗi: " + e.getMessage()));
        }
    }

    // kích hoạt tài khoản HR
    @PostMapping("/email/verify-otp-activate-hr")
    @ApiMessage("Xác thực OTP và kích hoạt tài khoản HR")
    public ResponseEntity<?> verifyOtpAndActivateHR(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String otpCode = request.get("otpCode");

            if (email == null || otpCode == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Email và mã OTP không được để trống"));
            }

            // Xác thực OTP
            boolean isValid = emailService.verifyOtp(email, otpCode);

            if (!isValid) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of("message", "Mã OTP không hợp lệ hoặc đã hết hạn"));
            }

            // Tìm user theo email
            User user = userRepository.findByEmail(email);

            if (user == null) {
                return ResponseEntity.notFound()
                        .build();
            }

            // Kiểm tra vai trò của người dùng
            boolean isHrRole = user.getRole() != null && user.getRole().getName().equals("HR");

            if (!isHrRole) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Tài khoản không có vai trò HR"));
            }

            // Kích hoạt tài khoản HR
            user.setIsHrActivated(true);
            userRepository.save(user);

            // Trả về thông tin thành công
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Xác thực OTP thành công và tài khoản HR đã được kích hoạt");
            response.put("email", email);
            response.put("isHrActivated", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Đã xảy ra lỗi: " + e.getMessage()));
        }
    }
}
