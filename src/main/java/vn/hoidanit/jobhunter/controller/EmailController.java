package vn.hoidanit.jobhunter.controller;

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

import vn.hoidanit.jobhunter.Util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.service.EmailService;
import vn.hoidanit.jobhunter.service.SubscriberService;

@RestController
@RequestMapping("/api/v1")
public class EmailController {
    private final EmailService emailService;
    private final SubscriberService subscriberService;

    public EmailController(EmailService emailService,
            SubscriberService subscriberService) {
        this.emailService = emailService;
        this.subscriberService = subscriberService;
    }

    @GetMapping("/email")
    @ApiMessage("Send simple email")
    // @Scheduled(cron = "*/10 * * * * *" )
    // @Transactional
    public String sendSimpleEmail() {
        // this.emailService.sendSimpleEmail();
        // this.emailService.sendEmailFromTemplateSync("quanghuydao17@gmail.com", "Test
        // Send Email", "job");
        this.subscriberService.sendSubscribersEmailJobs();
        return "ok";
    }
    // Send Email OTP

    @PostMapping("/email/send-otp")
    @ApiMessage("Send OTP email")
    public ResponseEntity<?> sendOtpEmail(@RequestParam String email) {
        try {
            if (!emailService.findByEmail(email)) {
                return ResponseEntity.badRequest()
                    .body(new HashMap<String, String>() {{
                        put("message", "Email không tồn tại trong hệ thống");
                    }});
            }

            emailService.sendOtpEmail(email);
            return ResponseEntity.ok()
                .body(new HashMap<String, String>() {{
                    put("message", "Mã OTP đã được gửi");
                }});
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new HashMap<String, String>() {{
                    put("message", "Lỗi khi gửi mã OTP");
                }});
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
                .body(new HashMap<String, String>() {{
                    put("message", "Mã OTP hợp lệ");
                }});
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new HashMap<String, String>() {{
                    put("message", "Mã OTP không hợp lệ hoặc đã hết hạn");
                }});
        }
    }
}
