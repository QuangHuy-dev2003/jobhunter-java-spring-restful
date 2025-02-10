package vn.hoidanit.jobhunter.service;

import jakarta.transaction.Transactional;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import vn.hoidanit.jobhunter.config.VNPayConfig;
import vn.hoidanit.jobhunter.domain.Payment;
import vn.hoidanit.jobhunter.domain.PostLimit;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.request.PaymentRequest;
import vn.hoidanit.jobhunter.domain.response.payment.PlanSalesDTO;
import vn.hoidanit.jobhunter.domain.response.payment.ResPaymentDTO;
import vn.hoidanit.jobhunter.repository.PaymentRepository;
import vn.hoidanit.jobhunter.repository.PostLimitRepository;
import vn.hoidanit.jobhunter.repository.UserRepository;
import vn.hoidanit.jobhunter.util.constant.PaymentMethodEnum;
import vn.hoidanit.jobhunter.util.constant.PaymentStatusEnum;

@Service
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final VNPayConfig vnPayConfig;
  private final UserRepository userRepository;
  private final UserService userService;
  private final PostLimitRepository postLimitRepository;

  public PaymentService(PaymentRepository paymentRepository,
      VNPayConfig vnPayConfig,
      UserRepository userRepository,
      PostLimitRepository postLimitRepository,
      UserService userService) {
    this.paymentRepository = paymentRepository;
    this.vnPayConfig = vnPayConfig;
    this.userRepository = userRepository;
    this.postLimitRepository = postLimitRepository;
    this.userService = userService;
  }

  public String createPaymentUrl(PaymentRequest request, String userEmail) {
    try {
      System.out.println("Starting createPaymentUrl process with email: " + userEmail);

      // Validate user
      User user = userRepository.findByEmail(userEmail);
      if (user == null) {
        throw new RuntimeException("User not found");
      }

      // Validate plan
      PostLimit postLimit = postLimitRepository.findById(request.getPlanId())
          .orElseThrow(() -> new RuntimeException("Plan not found"));

      // Create payment record
      Payment payment = new Payment();
      payment.setUser(user);
      payment.setPostLimit(postLimit);
      payment.setPaymentRef("PY-" + vnPayConfig.getRandomNumber(8));
      payment.setPaymentStatus(PaymentStatusEnum.PAYMENT_PENDING);
      payment.setPaymentMethod(PaymentMethodEnum.VNPAY);
      payment.setTotalPrice(request.getAmount());

      // Thêm thông tin về số tháng vào transferContent
      String orderInfo = request.getOrderInfo();
      payment.setTransferContent(orderInfo);

      // Lưu payment trước khi tạo URL
      payment = paymentRepository.save(payment);

      // Chuẩn bị tham số cho VNPay
      Map<String, String> vnp_Params = new HashMap<>();
      vnp_Params.put("vnp_Version", "2.1.0");
      vnp_Params.put("vnp_Command", "pay");
      vnp_Params.put("vnp_TmnCode", vnPayConfig.getTmnCode());

      // Chuyển đổi amount sang format của VNPay (nhân 100)
      long vnpAmount = (long) (request.getAmount() * 100);
      vnp_Params.put("vnp_Amount", String.valueOf(vnpAmount));

      vnp_Params.put("vnp_CurrCode", "VND");
      vnp_Params.put("vnp_TxnRef", payment.getPaymentRef());
//      vnp_Params.put("vnp_OrderInfo",
//          URLEncoder.encode(orderInfo, StandardCharsets.UTF_8.toString()));
      vnp_Params.put("vnp_OrderInfo", orderInfo);
      vnp_Params.put("vnp_OrderType", "250000"); // Mã danh mục hàng hóa
      vnp_Params.put("vnp_Locale", "vn");
      vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
      vnp_Params.put("vnp_IpAddr", "127.0.0.1");

      // Tạo thời gian giao dịch
      Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
      SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
      String createDate = formatter.format(cld.getTime());
      vnp_Params.put("vnp_CreateDate", createDate);

      // Tạo thời gian hết hạn
      cld.add(Calendar.MINUTE, 15);
      String expireDate = formatter.format(cld.getTime());
      vnp_Params.put("vnp_ExpireDate", expireDate);

      // Tạo chuỗi hash data và query
      List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
      Collections.sort(fieldNames);
      StringBuilder hashData = new StringBuilder();
      StringBuilder query = new StringBuilder();
      Iterator<String> itr = fieldNames.iterator();

      while (itr.hasNext()) {
        String fieldName = itr.next();
        String fieldValue = vnp_Params.get(fieldName);
        if ((fieldValue != null) && (!fieldValue.isEmpty())) {
          // Build hashData
          hashData.append(fieldName);
          hashData.append('=');
          hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

          // Build query
          query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
          query.append('=');
          query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

          if (itr.hasNext()) {
            query.append('&');
            hashData.append('&');
          }
        }
      }

      // Tạo secure hash
      String vnp_SecureHash = vnPayConfig.hmacSHA512(vnPayConfig.getHashSecret(),
          hashData.toString());
      query.append("&vnp_SecureHash=").append(vnp_SecureHash);

      // Tạo URL cuối cùng
      String paymentUrl = vnPayConfig.getPayUrl() + "?" + query;

      System.out.println("Generated payment URL: " + paymentUrl);
      return paymentUrl;

    } catch (Exception e) {
      System.out.println("Error in createPaymentUrl: " + e.getMessage());
      e.printStackTrace();
      throw new RuntimeException("Error creating payment URL: " + e.getMessage());
    }
  }

  @Transactional
  public void updatePaymentStatus(Payment payment, PaymentStatusEnum status) {
    payment.setPaymentStatus(status);
    payment.setUpdatedAt(Instant.now());
    paymentRepository.save(payment);
  }

  public Payment findByPaymentRef(String paymentRef) {
    return paymentRepository.findByPaymentRef(paymentRef)
        .orElseThrow(() -> new RuntimeException("Payment not found"));
  }

  public List<ResPaymentDTO> getPaymentsByStatus(PaymentStatusEnum status) {
    List<Payment> payments = paymentRepository.findByPaymentStatus(status);
    return payments.stream()
        .sorted(Comparator.comparing(Payment::getCreatedAt).reversed())
        .map(payment -> {
          ResPaymentDTO dto = new ResPaymentDTO();
          dto.setId(payment.getId());
          dto.setPaymentRef(payment.getPaymentRef());
          dto.setTotalPrice(payment.getTotalPrice());
          dto.setTransferContent(payment.getTransferContent());
          dto.setCreatedAt(LocalDateTime.ofInstant(payment.getCreatedAt(), ZoneId.systemDefault()));
          if (payment.getUpdatedAt() != null) {
            dto.setUpdatedAt(LocalDateTime.ofInstant(payment.getUpdatedAt(), ZoneId.systemDefault()));
          } else {
            dto.setUpdatedAt(null);
          }
          dto.setPaymentStatus(payment.getPaymentStatus().toString());
          dto.setPaymentMethod(payment.getPaymentMethod().toString());

          ResPaymentDTO.UserDto userDto = new ResPaymentDTO.UserDto(
              payment.getUser().getId(),
              payment.getUser().getEmail(),
              payment.getUser().getName()
          );
          dto.setUser(userDto);

          return dto;
        })
        .collect(Collectors.toList());
  }

  public List<ResPaymentDTO> getAllPayments() {
    List<Payment> payments = paymentRepository.findAll();
    return payments.stream()
        .sorted(Comparator.comparing(Payment::getCreatedAt).reversed())
        .map(payment -> {
          ResPaymentDTO dto = new ResPaymentDTO();
          dto.setId(payment.getId());
          dto.setPaymentRef(payment.getPaymentRef());
          dto.setTotalPrice(payment.getTotalPrice());
          dto.setTransferContent(payment.getTransferContent());
          dto.setCreatedAt(LocalDateTime.ofInstant(payment.getCreatedAt(), ZoneId.systemDefault()));
          if (payment.getUpdatedAt() != null) {
            dto.setUpdatedAt(LocalDateTime.ofInstant(payment.getUpdatedAt(), ZoneId.systemDefault()));
          } else {
            dto.setUpdatedAt(null);
          }
          dto.setPaymentStatus(payment.getPaymentStatus().toString());
          dto.setPaymentMethod(payment.getPaymentMethod().toString());

          ResPaymentDTO.UserDto userDto = new ResPaymentDTO.UserDto(
              payment.getUser().getId(),
              payment.getUser().getEmail(),
              payment.getUser().getName()
          );
          dto.setUser(userDto);

          return dto;
        })
        .collect(Collectors.toList());
  }

  public List<ResPaymentDTO> getAllPayments(String paymentRef) {
    List<Payment> payments = paymentRepository.findAll();

    Stream<Payment> paymentStream = payments.stream();

    if (paymentRef != null) {
      paymentStream = paymentStream.filter(p -> p.getPaymentRef().contains(paymentRef));
    }

    return paymentStream
        .sorted(Comparator.comparing(Payment::getCreatedAt).reversed())
        .map(this::mapToDTO)
        .collect(Collectors.toList());
  }

  private ResPaymentDTO mapToDTO(Payment payment) {
    ResPaymentDTO dto = new ResPaymentDTO();
    dto.setId(payment.getId());
    dto.setPaymentRef(payment.getPaymentRef());
    dto.setTotalPrice(payment.getTotalPrice());
    dto.setTransferContent(payment.getTransferContent());
    dto.setCreatedAt(LocalDateTime.ofInstant(payment.getCreatedAt(), ZoneId.systemDefault()));
    if (payment.getUpdatedAt() != null) {
      dto.setUpdatedAt(LocalDateTime.ofInstant(payment.getUpdatedAt(), ZoneId.systemDefault()));
    } else {
      dto.setUpdatedAt(null);
    }
    dto.setPaymentStatus(payment.getPaymentStatus().toString());
    dto.setPaymentMethod(payment.getPaymentMethod().toString());

    ResPaymentDTO.UserDto userDto = new ResPaymentDTO.UserDto(
        payment.getUser().getId(),
        payment.getUser().getEmail(),
        payment.getUser().getName()
    );
    dto.setUser(userDto);
    return dto;
  }


  public Payment updateAPaymentStatus(Long id, PaymentStatusEnum status) {
    Payment payment = paymentRepository.findById(id).orElseThrow(() ->
        new RuntimeException("Payment not found")
    );
    payment.setPaymentStatus(status);
    return paymentRepository.save(payment);
  }

  public List<PlanSalesDTO> findMonthlyPlanSales(int year) {
    List<Object[]> results = paymentRepository.findMonthlyPlanSalesData(year);
    return results.stream()
        .map(result -> new PlanSalesDTO(
            ((Number) result[0]).intValue(), // Chuyển sang intValue()
            (String) result[1],
            ((Number) result[2]).longValue()
        ))
        .collect(Collectors.toList());
  }


  public Double getTotalPricePaymentSuccess() {
    return paymentRepository.getTotalPriceByPaymentStatus(PaymentStatusEnum.PAYMENT_SUCCEED);
  }
}
