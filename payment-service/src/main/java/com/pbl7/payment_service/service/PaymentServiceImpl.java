package com.pbl7.payment_service.service;


import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.pbl7.payment_service.configuration.VNPAYConfig;
import com.pbl7.payment_service.dto.OrderDTO;
import com.pbl7.payment_service.domain.PaymentOrderStatus;
import com.pbl7.payment_service.dto.request.OrderRequest;
import com.pbl7.payment_service.entity.PaymentOrder;
import com.pbl7.payment_service.repository.PaymentOrderRepository;
import com.pbl7.payment_service.repository.http.OrderClient;
import com.pbl7.payment_service.service.impl.PaymentService;
import com.pbl7.payment_service.utils.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentServiceImpl implements PaymentService {

    PaymentOrderRepository paymentOrderRepository;
    OrderClient orderClient;
    VNPAYConfig vnPayConfig;

    @Override
    public PaymentOrder createOrder(String userId, OrderRequest order) {

        log.info("Order to be sent: {}" , order);

        OrderDTO createdOrder = orderClient.createOrder(order);

        PaymentOrder paymentOrder = new PaymentOrder();
        paymentOrder.setAmount(createdOrder.getTotalPrice());
        paymentOrder.setUserId(userId);
        paymentOrder.setOrderId(createdOrder.getId());

        return paymentOrderRepository.save(paymentOrder);
    }

    @Override
    public PaymentOrder getPaymentOrderById(Long orderId) throws Exception {
        return paymentOrderRepository.findById(orderId).orElseThrow(() -> new Exception("payment order not found"));
    }

    @Override
    public PaymentOrder getPaymentOrderByPaymentId(String paymentId) throws Exception {
        PaymentOrder paymentOrder = paymentOrderRepository.findByPaymentLinkId(paymentId);
        if (paymentOrder == null) {
            throw new Exception("payment order not found with payment link id - " + paymentId);
        }
        return paymentOrder;
    }

    @Override
    public String getIdFromJwt(String jwt) throws Exception {
        if (jwt != null && jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        } else {
            throw new Exception("Invalid JWT token format");
        }

        JWSObject jwsObject = JWSObject.parse(jwt);

        Payload payload = jwsObject.getPayload();
        Map<String, Object> claimsMap = payload.toJSONObject();

        String userId = (String) claimsMap.get("sub");
        return userId;
    }

    @Override
    public Boolean proceedPayment(PaymentOrder paymentOrder, String paymentId, String paymentLinkId) {
        if (paymentOrder.getStatus().equals(PaymentOrderStatus.PENDING)) {
            paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
            paymentOrder.setPaymentLinkId(paymentLinkId);

            paymentOrderRepository.save(paymentOrder);

            return true;
        }

        return false;
    }


    public String createPaymentLink(HttpServletRequest req, Long orderId ,Long amount, String bankCode, String paymentLinkId)  {
        long totalAmount = amount * 100L;

        Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig();
        vnpParamsMap.put("vnp_Amount", String.valueOf(totalAmount));
        vnpParamsMap.put("vnp_ReturnUrl", vnPayConfig.getVnp_ReturnUrl());

        // Gắn orderId vào vnp_TxnRef để callback lấy được
        vnpParamsMap.put("vnp_TxnRef", String.valueOf(orderId));
        // (Có thể gắn thêm vào vnp_OrderInfo nếu muốn)

        if (bankCode != null && !bankCode.isEmpty()) {
            vnpParamsMap.put("vnp_BankCode", bankCode);
        }

        vnpParamsMap.put("vnp_IpAddr", VNPayUtil.getIpAddress(req));
        //build query url
        String queryUrl = VNPayUtil.getPaymentURL(vnpParamsMap, true);
        String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);

        String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;

        return vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;
    }
}
