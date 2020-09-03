package com.jcoffee.ethkit.coin.pojo;

public class TransactionErc20Vo extends com.jcoffee.ethkit.coin.pojo.TransactionVo {
    private String contractAddress;

    public String getContractAddress() {
        return this.contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }
}
