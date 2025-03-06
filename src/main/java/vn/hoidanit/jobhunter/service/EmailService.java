package vn.hoidanit.jobhunter.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import vn.hoidanit.jobhunter.domain.EmailOTP;
import vn.hoidanit.jobhunter.repository.EmailOtpRepository;
import vn.hoidanit.jobhunter.repository.UserRepository;

@Service
public class EmailService {

    private final MailSender mailSender;
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    private final UserRepository userRepository;
    private final EmailOtpRepository emailOtpRepository;

    private static final long OTP_EXPIRY_MINUTES = 5;
    @Value("${spring.mail.username}")
    private String emailSender;

    public EmailService(MailSender mailSender,
            JavaMailSender javaMailSender,
            SpringTemplateEngine templateEngine,
            UserRepository userRepository,
            EmailOtpRepository emailOtpRepository) {
        this.mailSender = mailSender;
        this.javaMailSender = javaMailSender;
        this.userRepository = userRepository;
        this.templateEngine = templateEngine;
        this.emailOtpRepository = emailOtpRepository;
    }

    public void sendSimpleEmail() {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo("ads.hoidanit@gmail.com");
        msg.setSubject("Testing from Spring Boot");
        msg.setText("Hello World from Spring Boot Email");
        this.mailSender.send(msg);
    }

    public void sendEmailSync(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        // Prepare message using a Spring helper
        MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content, isHtml);
            this.javaMailSender.send(mimeMessage);
        } catch (MailException | MessagingException e) {
            System.out.println("ERROR SEND EMAIL: " + e);
        }
    }

    @Async
    public void sendEmailFromTemplateSync(
            String to,
            String subject,
            String templateName,
            String username,
            Object value) {

        Context context = new Context();
        context.setVariable("name", username);
        context.setVariable("jobs", value);

        String content = templateEngine.process(templateName, context);
        this.sendEmailSync(to, subject, content, false, true);
    }

    public boolean findByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // Tạo OTP
    public String generateOtp() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    public void sendOtpEmail(String email) {
        // Generate OTP
        String otpCode = generateOtp();

        // Save OTP to database
        EmailOTP emailOtp = new EmailOTP();
        emailOtp.setEmail(email);
        emailOtp.setOtpCode(otpCode);
        emailOtp.setExpiryTime(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        emailOtp.setUsed(false);
        emailOtpRepository.save(emailOtp);

        // Send email with OTP
        Context context = new Context();
        context.setVariable("otp", otpCode);
        context.setVariable("expiryMinutes", OTP_EXPIRY_MINUTES);

        String content = templateEngine.process("otp-email", context);
        this.sendEmailSync(email, "Mã xác thực OTP", content, false, true);
    }

    public boolean verifyOtp(String email, String otpCode) {
        Optional<EmailOTP> emailOtp = emailOtpRepository
                .findByEmailAndOtpCodeAndIsUsedFalseAndExpiryTimeAfter(
                        email,
                        otpCode,
                        LocalDateTime.now());

        if (emailOtp.isPresent()) {
            EmailOTP otp = emailOtp.get();
            otp.setUsed(true);
            emailOtpRepository.save(otp);
            return true;
        }

        return false;
    }

    // xóa OTP hết hạn
    // @Scheduled(fixedRate = 300000) // Chạy mỗi 5 phút (5 * 60 * 1000 ms)
    // @Transactional
    // public void cleanupExpiredOtp() {
    // try {
    // LocalDateTime now = LocalDateTime.now();
    // emailOtpRepository.deleteExpiredOtp(now);
    // System.out.println("Cleaned up expired OTP at: " + now);
    // } catch (Exception e) {
    // System.err.println("Error cleaning up expired OTP: " + e.getMessage());
    // }
    // }

    /**
     * Gửi email thông báo đến admin khi có form liên hệ được gửi đến
     */
    public void sendContactNotificationEmail(Map<String, Object> contactData) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message,
                    true,
                    StandardCharsets.UTF_8.name());

            // Cấu hình email gửi đến admin
            helper.setTo("quanghuydao17@gmail.com"); // Địa chỉ email của admin
            helper.setSubject("🔔 JobHunter - Có yêu cầu liên hệ mới từ khách hàng");

            // Xử lý template với dữ liệu form liên hệ
            Context context = new Context();
            context.setVariables(contactData);

            String htmlContent = templateEngine.process("contact-notification", context);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
        } catch (MessagingException e) {
            // Ghi log lỗi
            System.err.println("Lỗi khi gửi email thông báo liên hệ: " + e.getMessage());
        }
    }

    public boolean sendRecruiterActivationEmail(Map<String, Object> data) {
        try {
            String email = (String) data.get("email");

            // Tạo mã OTP mới
            String otpCode = generateOtp();
            // Lưu OTP vào database/cache với thời hạn
            saveOtp(email, otpCode);

            // Thêm OTP vào dữ liệu template
            data.put("otpCode", otpCode);

            // Sử dụng Thymeleaf để xử lý template
            Context context = new Context();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                context.setVariable(entry.getKey(), entry.getValue());
            }

            // Xử lý template và lấy nội dung HTML
            String htmlContent = templateEngine.process("recruiter-activation-email", context);

            // Gửi email với nội dung HTML
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(emailSender);
            helper.setTo(email);
            helper.setSubject("🎉 Chúc mừng! Bạn đã trở thành Nhà Tuyển Dụng của JobHunter");
            helper.setText(htmlContent, true);

            // Gửi email
            javaMailSender.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    private void saveOtp(String email, String otpCode) {
        EmailOTP emailOtp = new EmailOTP();
        emailOtp.setEmail(email);
        emailOtp.setOtpCode(otpCode);
        emailOtp.setExpiryTime(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        emailOtp.setUsed(false);
        emailOtpRepository.save(emailOtp);
    }


}
