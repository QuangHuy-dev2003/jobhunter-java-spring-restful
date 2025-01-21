package vn.hoidanit.jobhunter.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import vn.hoidanit.jobhunter.Util.constant.PaymentMethodEnum;
import vn.hoidanit.jobhunter.Util.constant.PaymentStatusEnum;
import vn.hoidanit.jobhunter.config.VNPayConfig;
import vn.hoidanit.jobhunter.domain.Payment;
import vn.hoidanit.jobhunter.domain.PostLimit;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.request.PaymentRequest;
import vn.hoidanit.jobhunter.repository.PaymentRepository;
import vn.hoidanit.jobhunter.repository.PostLimitRepository;
import vn.hoidanit.jobhunter.repository.UserRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final VNPayConfig vnPayConfig;
    private final UserRepository userRepository;
    private final PostLimitRepository postLimitRepository;

    public PaymentService(PaymentRepository paymentRepository,
            VNPayConfig vnPayConfig,
            UserRepository userRepository,
            PostLimitRepository postLimitRepository) {
        this.paymentRepository = paymentRepository;
        this.vnPayConfig = vnPayConfig;
        this.userRepository = userRepository;
        this.postLimitRepository = postLimitRepository;
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
            vnp_Params.put("vnp_OrderInfo",
                    URLEncoder.encode(orderInfo, StandardCharsets.UTF_8.toString()));
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
}
