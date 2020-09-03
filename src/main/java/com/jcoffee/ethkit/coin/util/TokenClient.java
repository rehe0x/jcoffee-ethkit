package com.jcoffee.ethkit.coin.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;
import org.web3j.utils.Convert.Unit;

public class TokenClient {
    private static Web3j web3j;
    private static Admin admin;
    private static String fromAddress = "0x585b4e6c436fbac38146a7104ddadc575c33adda";
    private static String contractAddress = "0xc81774fd1b51bc7e26b2bad562dcdb0a2cbebaa2";
    private static String emptyAddress = "0x0000000000000000000000000000000000000000";

    public TokenClient() {
    }

    public static void main(String[] args) {
        web3j = Web3j.build(new HttpService("https://mainnet.infura.io/v3/f5k7X36NcmyCs0UksgVd"));
        admin = Admin.build(new HttpService("https://mainnet.infura.io/v3/f5k7X36NcmyCs0UksgVd"));
        System.out.println(getTokenBalance(web3j, fromAddress, contractAddress));
    }

    public static BigInteger getTokenBalance(Web3j web3j, String fromAddress, String contractAddress) {
        String methodName = "balanceOf";
        List<Type> inputParameters = new ArrayList();
        List<TypeReference<?>> outputParameters = new ArrayList();
        Address address = new Address(fromAddress);
        inputParameters.add(address);
        TypeReference<Uint256> typeReference = new TypeReference<Uint256>() {
        };
        outputParameters.add(typeReference);
        Function function = new Function(methodName, inputParameters, outputParameters);
        String data = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction(fromAddress, contractAddress, data);
        BigInteger balanceValue = BigInteger.ZERO;

        try {
            EthCall ethCall = (EthCall)web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
            List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
            if (results.size() == 0) {
                return balanceValue;
            }

            balanceValue = (BigInteger)((Type)results.get(0)).getValue();
        } catch (IOException var14) {
            System.err.println("查询地址【" + fromAddress + "】代币余额失败，错误提示：" + var14.getMessage());
        }

        return balanceValue;
    }

    public static BigInteger getEthBalance(Web3j web3j, String fromAddress) {
        BigInteger balanceValue = BigInteger.ZERO;

        try {
            EthGetBalance ethGetBalance = web3j.ethGetBalance(fromAddress, DefaultBlockParameter.valueOf("latest")).send();
            BigInteger balance = ethGetBalance.getBalance();
            if (balance != null) {
                balanceValue = balance;
            }
        } catch (IOException var5) {
            System.err.println("查询地址【" + fromAddress + "】eth余额失败，错误提示：" + var5.getMessage());
        }

        return balanceValue;
    }

    public static String getTokenName(Web3j web3j, String contractAddress) {
        String methodName = "name";
        String name = null;
        String fromAddr = emptyAddress;
        List<Type> inputParameters = new ArrayList();
        List<TypeReference<?>> outputParameters = new ArrayList();
        TypeReference<Utf8String> typeReference = new TypeReference<Utf8String>() {
        };
        outputParameters.add(typeReference);
        Function function = new Function(methodName, inputParameters, outputParameters);
        String data = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction(fromAddr, contractAddress, data);

        try {
            EthCall ethCall = (EthCall)web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).sendAsync().get();
            List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
            name = ((Type)results.get(0)).getValue().toString();
        } catch (ExecutionException | InterruptedException var13) {
            var13.printStackTrace();
        }

        return name;
    }

    public static String getTokenSymbol(Web3j web3j, String contractAddress) {
        String methodName = "symbol";
        String symbol = null;
        String fromAddr = emptyAddress;
        List<Type> inputParameters = new ArrayList();
        List<TypeReference<?>> outputParameters = new ArrayList();
        TypeReference<Utf8String> typeReference = new TypeReference<Utf8String>() {
        };
        outputParameters.add(typeReference);
        Function function = new Function(methodName, inputParameters, outputParameters);
        String data = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction(fromAddr, contractAddress, data);

        try {
            EthCall ethCall = (EthCall)web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).sendAsync().get();
            List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
            symbol = ((Type)results.get(0)).getValue().toString();
        } catch (ExecutionException | InterruptedException var13) {
            var13.printStackTrace();
        }

        return symbol;
    }

    public static int getTokenDecimals(Web3j web3j, String contractAddress) {
        String methodName = "decimals";
        String fromAddr = emptyAddress;
        int decimal = -1;
        List<Type> inputParameters = new ArrayList();
        List<TypeReference<?>> outputParameters = new ArrayList();
        TypeReference<Uint8> typeReference = new TypeReference<Uint8>() {
        };
        outputParameters.add(typeReference);
        Function function = new Function(methodName, inputParameters, outputParameters);
        String data = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction(fromAddr, contractAddress, data);

        try {
            EthCall ethCall = (EthCall)web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).sendAsync().get();
            List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
            decimal = Integer.parseInt(((Type)results.get(0)).getValue().toString());
        } catch (ExecutionException | InterruptedException var13) {
            var13.printStackTrace();
        }

        return decimal;
    }

    public static BigInteger getTokenTotalSupply(Web3j web3j, String contractAddress) {
        String methodName = "totalSupply";
        String fromAddr = emptyAddress;
        BigInteger totalSupply = BigInteger.ZERO;
        List<Type> inputParameters = new ArrayList();
        List<TypeReference<?>> outputParameters = new ArrayList();
        TypeReference<Uint256> typeReference = new TypeReference<Uint256>() {
        };
        outputParameters.add(typeReference);
        Function function = new Function(methodName, inputParameters, outputParameters);
        String data = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction(fromAddr, contractAddress, data);

        try {
            EthCall ethCall = (EthCall)web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).sendAsync().get();
            List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
            totalSupply = (BigInteger)((Type)results.get(0)).getValue();
        } catch (ExecutionException | InterruptedException var13) {
            var13.printStackTrace();
        }

        return totalSupply;
    }

    public static String sendTokenTransaction(String fromAddress, String password, String toAddress, String contractAddress, BigInteger amount) {
        String txHash = null;

        try {
            PersonalUnlockAccount personalUnlockAccount = (PersonalUnlockAccount)admin.personalUnlockAccount(fromAddress, password, BigInteger.valueOf(10L)).send();
            if (personalUnlockAccount.accountUnlocked()) {
                String methodName = "transfer";
                List<Type> inputParameters = new ArrayList();
                List<TypeReference<?>> outputParameters = new ArrayList();
                Address tAddress = new Address(toAddress);
                Uint256 value = new Uint256(amount);
                inputParameters.add(tAddress);
                inputParameters.add(value);
                TypeReference<Bool> typeReference = new TypeReference<Bool>() {
                };
                outputParameters.add(typeReference);
                Function function = new Function(methodName, inputParameters, outputParameters);
                String data = FunctionEncoder.encode(function);
                EthGetTransactionCount ethGetTransactionCount = (EthGetTransactionCount)web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.PENDING).sendAsync().get();
                BigInteger nonce = ethGetTransactionCount.getTransactionCount();
                BigInteger gasPrice = Convert.toWei(BigDecimal.valueOf(5L), Unit.GWEI).toBigInteger();
                Transaction transaction = Transaction.createFunctionCallTransaction(fromAddress, nonce, gasPrice, BigInteger.valueOf(60000L), contractAddress, data);
                EthSendTransaction ethSendTransaction = (EthSendTransaction)web3j.ethSendTransaction(transaction).sendAsync().get();
                txHash = ethSendTransaction.getTransactionHash();
            }
        } catch (Exception var20) {
            var20.printStackTrace();
        }

        return txHash;
    }

    private static String calculateContractAddress(String address, long nonce) {
        byte[] addressAsBytes = Numeric.hexStringToByteArray(address);
        byte[] calculatedAddressAsBytes = Hash.sha3(RlpEncoder.encode(new RlpList(new RlpType[]{RlpString.create(addressAsBytes), RlpString.create(nonce)})));
        calculatedAddressAsBytes = Arrays.copyOfRange(calculatedAddressAsBytes, 12, calculatedAddressAsBytes.length);
        String calculatedAddressAsHex = Numeric.toHexString(calculatedAddressAsBytes);
        return calculatedAddressAsHex;
    }
}
