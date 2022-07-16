package com.transaction.order;

public class NotEnoughMoneyException extends Exception {

    /*
        결제 잔고가 부족하면 나는 예외처리 이다.
     */
    public NotEnoughMoneyException(String message) {
        super(message);
    }
}
