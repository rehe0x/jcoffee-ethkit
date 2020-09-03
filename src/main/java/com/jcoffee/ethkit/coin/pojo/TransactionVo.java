package com.jcoffee.ethkit.coin.pojo;

import java.math.BigInteger;

public class TransactionVo {
    private String fromAddress;
    private String toAddress;
    private Double value;
    private BigInteger gasPrice = BigInteger.valueOf(8000000000L);
    private BigInteger gasLimit = BigInteger.valueOf(21000L);
    private BigInteger toVal;
    private String transactionHash = "";
    private String dateTime;
    private String msg = "转账请求已发出";
    private boolean success = true;

    public String getMsg() {
        return this.msg;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getFromAddress() {
        return this.fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getToAddress() {
        return this.toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public BigInteger getGasPrice() {
        return this.gasPrice;
    }

    public void setGasPrice(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
    }

    public BigInteger getGasLimit() {
        return this.gasLimit;
    }

    public void setGasLimit(BigInteger gasLimit) {
        this.gasLimit = gasLimit;
    }

    public String getTransactionHash() {
        return this.transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public String getDateTime() {
        return this.dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public BigInteger getToVal() {
        return this.toVal;
    }

    public void setToVal(BigInteger toVal) {
        this.toVal = toVal;
    }

    public Double getValue() {
        return this.value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
