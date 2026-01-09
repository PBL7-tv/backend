package com.pbl7.payment_service.controller;


import com.pbl7.payment_service.domain.PaymentMethod;
import com.pbl7.payment_service.dto.ApiResponse;
import com.pbl7.payment_service.dto.OrderDTO;
import com.pbl7.payment_service.dto.request.OrderRequest;
import com.pbl7.payment_service.dto.response.PaymentResponse;
import com.pbl7.payment_service.entity.PaymentOrder;
import com.pbl7.payment_service.repository.http.OrderClient;
import com.pbl7.payment_service.service.impl.PaymentService;
import com.pbl7.payment_service.service.impl.TransactionService;
import com.pbl7.payment_service.repository.PaymentOrderRepository;
import com.pbl7.payment_service.domain.PaymentOrderStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/online")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {

    PaymentService paymentService;

    OrderClient orderClient;
    TransactionService transactionService;
    final PaymentOrderRepository paymentOrderRepository;

    @PostMapping
    public ResponseEntity<PaymentResponse> createOrder(@RequestBody OrderRequest order,
                                                       @RequestParam String paymentMethod,
                                                       HttpServletRequest request,
                                                       @RequestHeader("Authorization") String jwt
    ) throws Exception {
        String userId = paymentService.getIdFromJwt(jwt);

        PaymentOrder paymentOrder = paymentService.createOrder(userId, order);

        PaymentResponse paymentResponse = new PaymentResponse();

        PaymentMethod method = PaymentMethod.valueOf(paymentMethod.toUpperCase());
        paymentOrder.setPaymentMethod(method);
        paymentOrder = paymentOrderRepository.save(paymentOrder);

        if (paymentMethod.equalsIgnoreCase("VNPay")) {
            String paymentLinkId = UUID.randomUUID().toString();
            paymentOrder.setPaymentLinkId(paymentLinkId);
            paymentOrder = paymentOrderRepository.save(paymentOrder);
            String bankCode = "NCB";
            String paymentUrl = paymentService.createPaymentLink(request, paymentOrder.getId(), paymentOrder.getAmount(),
                    bankCode, paymentLinkId);
            paymentResponse.setCode(1000);
            paymentResponse.setMessage("Payment link generated");
            paymentResponse.setPaymentUrl(paymentUrl);
        }

        return new ResponseEntity<>(paymentResponse, HttpStatus.CREATED);
    }

    @GetMapping("/vn-pay-callback")
    public void payCallbackHandler(HttpServletResponse response,
                                   @RequestParam String vnp_ResponseCode,
                                   @RequestParam(required = false) String vnp_TxnRef,
                                   @RequestParam(required = false) String vnp_OrderInfo) throws IOException {
        String paymentOrderId = vnp_TxnRef;
        System.out.println("VNPay callback received: vnp_ResponseCode=" + vnp_ResponseCode + ", paymentOrderId=" + paymentOrderId);
        if ("00".equals(vnp_ResponseCode) && paymentOrderId != null) {
            PaymentOrder paymentOrder = paymentOrderRepository.findById(Long.valueOf(paymentOrderId)).orElse(null);
            System.out.println("PaymentOrder found: " + paymentOrder);
            if (paymentOrder != null) {
                paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
                paymentOrderRepository.save(paymentOrder);
                if (paymentOrder.getOrderId() != null) {
                    var order = orderClient.getOrderById(paymentOrder.getOrderId());
                    transactionService.createTransaction(order);
                }
                Long orderId = paymentOrder.getOrderId();
                response.sendRedirect("http://localhost:3000/payment-success/" + orderId);
            } else {
                response.sendRedirect("http://localhost:3000/payment-failure");
            }
        } else {
            response.sendRedirect("http://localhost:3000/payment-failure");
        }
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse> paymentSuccess(@PathVariable String paymentId,
                                                      @RequestHeader("Authorization") String jwt,
                                                      @RequestParam String paymentLinkId) throws Exception {

        PaymentOrder paymentOrder = paymentService.getPaymentOrderByPaymentId(paymentLinkId);

        boolean paymentSuccess = paymentService.proceedPayment(paymentOrder, paymentId, paymentLinkId);

        if (paymentSuccess) {

            OrderDTO order = orderClient.getOrderById(paymentOrder.getOrderId());

            transactionService.createTransaction(order);

        }


        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Payment Success");

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

}
