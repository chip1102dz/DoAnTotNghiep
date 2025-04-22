package com.example.doantotnghiep.event;
import com.example.doantotnghiep.model.PaymentMethod;

public class PaymentMethodSelectedEvent {

    private PaymentMethod paymentMethod;

    public PaymentMethodSelectedEvent(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
