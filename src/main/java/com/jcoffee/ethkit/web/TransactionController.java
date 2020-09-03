package com.jcoffee.ethkit.web;

import com.jcoffee.ethkit.coin.account.TransactionErc20Task;
import com.jcoffee.ethkit.coin.account.TransactionErc20ToManyTask;
import com.jcoffee.ethkit.coin.account.TransactionOne2Many;
import com.jcoffee.ethkit.coin.account.TransactionTask;
import com.jcoffee.ethkit.coin.account.TransactionTask2;
import com.jcoffee.ethkit.coin.util.EtherscanUtil;
import com.jcoffee.ethkit.coin.util.TransErc20Util;
import com.jcoffee.ethkit.coin.util.TransactionUtil;
import com.jcoffee.ethkit.coin.util.WalletUtil;
import com.jcoffee.ethkit.common.JsonResult;
import com.jcoffee.ethkit.util.DateUtils;
import com.jcoffee.ethkit.util.IpUtils;
import com.jcoffee.ethkit.util.license.PubKeyUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.http.HttpService;

@Api(
        value = "transactionController",
        tags = {"transaction"},
        produces = "application/json"
)
@RestController
@RequestMapping({"transaction"})
public class TransactionController {
    @Value("${action.toOne}")
    private String toOne;
    @Value("${action.toMany}")
    private String toMany;
    @Autowired
    private TransactionTask transactionTask;
    @Autowired
    private TransactionOne2Many transactionOne2Many;
    @Autowired
    private TransactionTask2 transactionTask2;
    @Autowired
    private TransactionErc20Task transactionErc20Task;
    @Autowired
    private TransactionUtil transactionUtil;
    @Autowired
    private WalletUtil walletUtil;
    @Autowired
    private TransErc20Util transErc20Util;
    @Autowired
    private EtherscanUtil etherscanUtil;
    @Autowired
    private TransactionErc20ToManyTask erc20ToManyTask;

    @ApiOperation("多个钱包eth转到多个钱包")
    @RequestMapping(
            value = {"/toMany"},
            method = {RequestMethod.POST}
    )
    public JsonResult transToMore(@ApiParam(value = "主钱包地址，多个用逗号分隔",required = true) @RequestParam(required = true) String from, @ApiParam("次钱包地址，多个用逗号分隔") @RequestParam(required = true) String to, @ApiParam("转入钱包地址，多个用逗号分隔") @RequestParam(required = true) String address, @ApiParam("转账类型") @RequestParam(required = true) Integer transType, @ApiParam(value = "钱包密码",required = true) @RequestParam(required = true) String pwd, @ApiParam(value = "转账金额",required = true,defaultValue = "0") @RequestParam(required = true) Double value, @RequestParam(required = true) @ApiParam(value = "自定义 Gas Price,单位：Gwei",required = true,defaultValue = "20") Double gasPrice, @ApiParam(value = "自定义 Gas",required = true,defaultValue = "200000") @RequestParam(required = true) Integer gasLimit, @ApiParam(value = "numType",required = true) @RequestParam(required = true) Integer numType, @ApiParam(value = "转账金额2",required = true,defaultValue = "0") @RequestParam(required = true) Double value2, @ApiParam(value = "coin",required = true) @RequestParam(required = true) String coin) throws Exception {
        JsonResult rs = null;
        if (transType == 1) {
            if (StringUtils.split(from, ",").length == 1) {
                rs = this.transactionOne2Many.startProducer(from, to, pwd, value, gasPrice, gasLimit);
            } else {
                rs = this.transactionTask.startProducer(from, to, pwd, value, gasPrice, gasLimit);
            }
        } else if (transType == 3) {
            address = StringUtils.replace(address, "，", ",");
            address = StringUtils.trim(address);
            String fromAddrs = "";
            if (StringUtils.isNotBlank(from) && StringUtils.isNotBlank(to)) {
                fromAddrs = from + "," + to;
            } else if (StringUtils.isNotBlank(from)) {
                fromAddrs = from;
            } else if (StringUtils.isNotBlank(to)) {
                fromAddrs = to;
            }

            if (StringUtils.contains(coin, "ETH")) {
                if (!StringUtils.contains(address, ",")) {
                    rs = this.transactionTask2.startConsumer(from, to, address, pwd, value2, gasPrice, gasLimit, numType);
                } else if (StringUtils.contains(address, ",")) {
                    String key2 = "lualua20";
                    if (!StringUtils.contains(key2, "20")) {
                        JsonResult jsonResult = new JsonResult();
                        String message = "1.x为基础版本不支持向多个外部地址转账，如有需要请购买2.x版本";
                        System.err.println(message);
                        jsonResult.setMessage(message);
                        jsonResult.setSuccess(false);
                        return jsonResult;
                    }

                    if (!StringUtils.contains(fromAddrs, ",")) {
                        rs = this.transactionOne2Many.startProducer(fromAddrs, address, pwd, value2, gasPrice, gasLimit);
                    } else {
                        rs = this.transactionTask.startProducer(fromAddrs, address, pwd, value2, gasPrice, gasLimit);
                    }
                }
            } else if (!StringUtils.contains(address, ",")) {
                rs = this.transactionErc20Task.startConsumer(coin, from, to, address, pwd, value2, gasPrice, gasLimit, numType);
            } else if (StringUtils.contains(address, ",")) {
                if (!StringUtils.contains(fromAddrs, ",")) {
                    rs = this.erc20ToManyTask.one2many(coin, fromAddrs, address, pwd, value2, gasPrice, gasLimit, numType);
                } else {
                    rs = this.erc20ToManyTask.many2many(coin, fromAddrs, address, pwd, value2, gasPrice, gasLimit, numType);
                }
            }
        }

        return rs;
    }

    @ApiOperation("查询转账是否已被打包")
    @RequestMapping(
            value = {"/checkTransactionHash"},
            method = {RequestMethod.GET}
    )
    public Object checkThrans(@ApiParam(value = "转账任务Hash",required = true,defaultValue = "0x75b3b59e803f266e508d473fc72026394537559c153289a93b8301ccd0e70b8a") @RequestParam(required = true) String transactionHash) throws Exception {
        String token = this.transactionUtil.getToken();
        boolean success = this.transactionUtil.checkTransaction(transactionHash, token);
        String msg = "转账还未被打包，过一会儿再查询吧";
        if (success) {
            msg = "转账已被打包,tx:" + transactionHash;
        }

        return msg;
    }

    @ApiOperation("批量创建钱包，数量不要设置过大，会等待很长时间，每次最大允许设置100")
    @RequestMapping(
            value = {"/createWallet"},
            method = {RequestMethod.POST}
    )
    public JsonResult createWallet(@ApiParam(value = "钱包类别",required = true,defaultValue = "1") @RequestParam(required = true,defaultValue = "1") Integer walletType, @ApiParam(value = "创建方式",required = true,defaultValue = "1") @RequestParam(required = true,defaultValue = "1") Integer createType, @ApiParam(value = "钱包密码",required = false) @RequestParam(required = true) String pwd, @ApiParam(value = "生成钱包数量",required = false,defaultValue = "10") @RequestParam(required = false,defaultValue = "10") Integer count, @RequestParam(required = false) @ApiParam(value = "keystore",required = false) String keystore, @ApiParam(value = "privatekey",required = false) @RequestParam(required = false) String privatekey) throws Exception {
        String rootDir = this.transactionUtil.getRootDir();
        String path = rootDir + "/wallet/" + DateUtils.fomatToTimeStr(new Date());
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }

        JsonResult rs = this.walletUtil.createUTC(createType, walletType, pwd, path, count, keystore, privatekey);
        return rs;
    }

    @ApiOperation("moveWallet")
    @RequestMapping(
            value = {"/moveWallet"},
            method = {RequestMethod.POST}
    )
    public JsonResult moveWallet(@ApiParam(value = "钱包类别",required = true,defaultValue = "1") @RequestParam(required = true,defaultValue = "1") Integer walletType, @ApiParam(value = "已勾选地址集合",required = true) @RequestParam(required = true) String address) throws Exception {
        address = StringUtils.removeStart(address, ",");
        address = StringUtils.removeEnd(address, ",");
        String[] arr = StringUtils.split(address, ",");
        List list = Arrays.asList(arr);
        JsonResult rs = this.walletUtil.moveWallet(walletType, list);
        return rs;
    }

    @ApiOperation("生成机器公钥")
    @RequestMapping(
            value = {"/getPubKey"},
            method = {RequestMethod.GET}
    )
    public JsonResult getPubKey() throws Exception {
        JsonResult rs = PubKeyUtil.getPubKey();
        return rs;
    }

    @ApiOperation("getAddressList")
    @RequestMapping(
            value = {"/getAddressList"},
            method = {RequestMethod.POST}
    )
    public JsonResult getAddressList(@ApiParam(value = "文件夹",required = true) @RequestParam(required = true) String dir, @ApiParam(value = "币种",required = true,defaultValue = "ETH") @RequestParam(required = true,defaultValue = "ETH") String coin, @ApiParam(value = "是否查询余额，1 查询，0 不查询",required = false,defaultValue = "0") @RequestParam(required = false,defaultValue = "0") Integer isSearchBalance) throws Exception {
        if (!StringUtils.startsWith(dir, "0x")) {
            String rootDir = this.transactionUtil.getRootDir();
            dir = rootDir + "/eth/" + dir;
        }
        JsonResult rs = this.transactionUtil.getEthAdrrList(dir, coin, isSearchBalance);
        return rs;
    }

    @ApiOperation("gasPrice")
    @RequestMapping(
            value = {"/gasPrice"},
            method = {RequestMethod.GET}
    )
    public JsonResult gasPrice() throws Exception {
        JsonResult rs = new JsonResult();
        String token = this.transactionUtil.getToken();
        Web3j web3j = Web3j.build(new HttpService("https://mainnet.infura.io/v3/" + token));
        EthGasPrice ethGasPrice = web3j.ethGasPrice().send();
        BigInteger gasPrice = ethGasPrice.getGasPrice();
        BigDecimal balanceN = new BigDecimal(gasPrice);
        BigDecimal value = balanceN.divide(new BigDecimal("1000000000"), 2, 1);
        rs.setData(value);
        return rs;
    }

    @ApiOperation("addContract")
    @RequestMapping(
            value = {"/addContract"},
            method = {RequestMethod.POST}
    )
    public JsonResult addContract(@ApiParam(value = "address",required = true) @RequestParam(required = true) String address) throws Exception {
        JsonResult rs = this.etherscanUtil.addErc20ContractInfo(address);
        return rs;
    }

    @ApiOperation("getContractList")
    @RequestMapping(
            value = {"/getContractList"},
            method = {RequestMethod.GET}
    )
    public List getContractList() throws Exception {
        List list = this.etherscanUtil.getContractList();
        return list;
    }

    @ApiOperation("getErc20Balance")
    @RequestMapping(
            value = {"/getErc20Balance"},
            method = {RequestMethod.POST}
    )
    public JsonResult getErc20Balance(@ApiParam(value = "合约地址",required = true) @RequestParam(required = true) String contractAddress, @ApiParam(value = "已勾选地址集合",required = true) @RequestParam(required = true) String address) throws Exception {
        address = StringUtils.removeStart(address, ",");
        address = StringUtils.removeEnd(address, ",");
        String[] arr = StringUtils.split(address, ",");
        List list = Arrays.asList(arr);
        JsonResult rs = this.transErc20Util.getSumBalance(contractAddress, list);
        return rs;
    }

    @ApiOperation("clearWhiteSet")
    @RequestMapping(
            value = {"/clearWhiteSet"},
            method = {RequestMethod.GET}
    )
    public JsonResult clearWhiteSet() throws Exception {
        JsonResult rs = new JsonResult();
        IpUtils.clearWhiteSet();
        rs.setMessage("ok");
        return rs;
    }
}
