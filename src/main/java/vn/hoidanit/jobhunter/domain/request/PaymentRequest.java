package vn.hoidanit.jobhunter.domain.request;

import lombok.Data;
import vn.hoidanit.jobhunter.Util.constant.PaymentMethodEnum;

@Data
public class PaymentRequest {
    private Long planId;
    private Double amount;
    private PaymentMethodEnum paymentMethod;
    private String orderInfo;
}
