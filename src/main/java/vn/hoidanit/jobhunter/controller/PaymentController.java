package vn.hoidanit.jobhunter.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import vn.hoidanit.jobhunter.Util.constant.PaymentStatusEnum;
import vn.hoidanit.jobhunter.domain.Payment;
import vn.hoidanit.jobhunter.domain.request.PaymentRequest;
import vn.hoidanit.jobhunter.service.PaymentService;
import vn.hoidanit.jobhunter.service.SubscriptionService;
import vn.hoidanit.jobhunter.service.UserService;

@RestController
@RequestMapping("/api/v1")
public class PaymentController {
    private final PaymentService paymentService;
    private final UserService userService;
    private final SubscriptionService subscriptionService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public PaymentController(PaymentService paymentService,
            UserService userService,
            SubscriptionService subscriptionService) {
        this.paymentService = paymentService;
        this.userService = userService;
        this.subscriptionService = subscriptionService;

    }

    @PostMapping("/payments/create-url")
    public ResponseEntity<?> createPaymentUrl(@RequestBody PaymentRequest request) {
        try {
            String userEmail = userService.getCurrentUserEmail();
            System.out.println("Current user email: " + userEmail); // Log email

            System.out.println("Request data: " + request); // Log request

            String paymentUrl = paymentService.createPaymentUrl(request, userEmail);
            System.out.println("Generated URL: " + paymentUrl); // Log URL

            return ResponseEntity.ok(Map.of(
                    "code", "00",
                    "message", "success",
                    "data", paymentUrl));
        } catch (Exception e) {
            e.printStackTrace(); // Log full stack trace
            return ResponseEntity.badRequest().body(Map.of(
                    "code", "01",
                    "message", e.getMessage()));
        }
    }

    @GetMapping("/payments/vnpay-return")
    public void handleVnPayReturn(@RequestParam Map<String, String> queryParams, HttpServletResponse response)
            throws IOException {
        String vnp_ResponseCode = queryParams.get("vnp_ResponseCode");
        String vnp_TxnRef = queryParams.get("vnp_TxnRef"); // Mã payment_ref
        String vnp_Amount = queryParams.get("vnp_Amount"); // Số tiền thanh toán

        // Kiểm tra response code từ VNPay
        if ("00".equals(vnp_ResponseCode)) {
            try {
                // Tìm payment dựa trên payment_ref
                Payment payment = paymentService.findByPaymentRef(vnp_TxnRef);
                if (payment == null) {
                    response.sendRedirect(frontendUrl + "/donate?status=failure&message=Payment not found");
                    return;
                }

                // Validate số tiền
                long vnpayAmount = Long.parseLong(vnp_Amount) / 100;
                if (vnpayAmount != payment.getTotalPrice()) {
                    response.sendRedirect(frontendUrl + "/donate?status=failure&message=Invalid amount");
                    return;
                }

                // Cập nhật trạng thái payment
                paymentService.updatePaymentStatus(payment, PaymentStatusEnum.PAYMENT_SUCCEED);

                // Tạo subscription
                subscriptionService.createSubscription(payment);

                // Redirect về trang thành công
                response.sendRedirect(frontendUrl + "/donate?status=success");
            } catch (Exception e) {
                response.sendRedirect(frontendUrl + "/donate?status=failure&message=System error");
            }
        } else {
            // Cập nhật trạng thái payment thất bại
            Payment payment = paymentService.findByPaymentRef(vnp_TxnRef);
            if (payment != null) {
                paymentService.updatePaymentStatus(payment, PaymentStatusEnum.PAYMENT_FAILED);
            }

            // Redirect về trang thất bại
            response.sendRedirect(frontendUrl + "/donate?status=failure&message=Payment failed");
        }
    }

}
