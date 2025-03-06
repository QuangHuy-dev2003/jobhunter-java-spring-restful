package vn.hoidanit.jobhunter.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import vn.hoidanit.jobhunter.domain.Payment;
import vn.hoidanit.jobhunter.domain.request.PaymentRequest;
import vn.hoidanit.jobhunter.domain.response.payment.PlanSalesDTO;
import vn.hoidanit.jobhunter.domain.response.payment.ResPaymentDTO;
import vn.hoidanit.jobhunter.repository.PaymentRepository;
import vn.hoidanit.jobhunter.service.PaymentService;
import vn.hoidanit.jobhunter.service.SubscriptionService;
import vn.hoidanit.jobhunter.service.UserService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.constant.PaymentStatusEnum;

@RestController
@RequestMapping("/api/v1")
public class PaymentController {

  private final PaymentService paymentService;
  private final UserService userService;
  private final SubscriptionService subscriptionService;
  private final PaymentRepository paymentRepository;

  @Value("${app.frontend-url}")
  private String frontendUrl;

  public PaymentController(PaymentService paymentService,
      UserService userService,
      SubscriptionService subscriptionService,
      PaymentRepository paymentRepository) {
    this.paymentService = paymentService;
    this.userService = userService;
    this.subscriptionService = subscriptionService;
    this.paymentRepository = paymentRepository;
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
  public void handleVnPayReturn(@RequestParam Map<String, String> queryParams,
      HttpServletResponse response) throws IOException {
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

  @GetMapping("/payments/success")
  @ApiMessage("Get all payment success")
  public ResponseEntity<?> getPaymentSuccess() throws Exception {
    List<ResPaymentDTO> payments = paymentService.getPaymentsByStatus(
        PaymentStatusEnum.PAYMENT_SUCCEED);
    return ResponseEntity.ok(Map.of(
        "code", "00",
        "message", "success",
        "data", payments));
  }

  @GetMapping("/payments")
  public ResponseEntity<?> getPayments(
      @RequestParam(required = false) String paymentRef) {
    List<ResPaymentDTO> payments = paymentService.getAllPayments(paymentRef);
    System.out.println("Payments Search: " + payments);

    return ResponseEntity.ok(Map.of(
        "code", "00",
        "message", "success",
        "data", payments));
  }

  @PutMapping("/payments/update/{id}")
  @ApiMessage("Update status payment")
  public ResponseEntity<?> updatePaymentStatus(@PathVariable long id,
      @RequestBody Map<String, PaymentStatusEnum> requestBody) {
    PaymentStatusEnum status = requestBody.get("status");
    try {

      Payment payment = paymentService.updateAPaymentStatus(id, status);
      return ResponseEntity.ok(Map.of(
          "code", "00",
          "message", "success",
          "data", payment));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
          "code", "01",
          "message", e.getMessage()));
    }
  }

  @GetMapping("/payments/plansales")
  @ApiMessage("Get count payments success for post limit")
  public ResponseEntity<?> getPaymentPlanSales(
      @RequestParam(value = "year", required = false) @Min(2025) @Max(2100) Integer year) {

    int currentYear = year != null ? year : LocalDate.now().getYear();
    List<PlanSalesDTO> payments = paymentService.findMonthlyPlanSales(currentYear);

    return ResponseEntity.ok(Map.of(
        "code", "00",
        "message", "success",
        "data", payments));
  }

  // delete by paymentref
  @DeleteMapping("/payments/{paymentRef}")
  @ApiMessage("Delete payment by payment ref")
  public ResponseEntity<?> deletePaymentByPaymentRef(@PathVariable String paymentRef) {
    try {
      paymentRepository.deleteByPaymentRef(paymentRef);
      return ResponseEntity.ok(Map.of(
          "code", "00",
          "message", "success"));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
          "code", "01",
          "message", e.getMessage()));
    }
  }

  // count total_price with payment_status = 'PAYMENT_SUCCEED'
  @GetMapping("/payments/total-price")
  @ApiMessage("Get total price of payments success")
  public ResponseEntity<?> getTotalPricePaymentSuccess() {
    Double totalPrice = paymentService.getTotalPricePaymentSuccess();
    return ResponseEntity.ok(Map.of(
        "code", "00",
        "message", "success",
        "data", totalPrice));
  }

}
