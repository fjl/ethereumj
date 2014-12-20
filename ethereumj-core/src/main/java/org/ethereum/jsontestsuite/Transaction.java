package org.ethereum.jsontestsuite;

import org.ethereum.util.ByteUtil;
import org.json.simple.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Arrays;

import static org.ethereum.util.ByteUtil.toHexString;

/**
 * www.etherj.com
 *
 * @author: Roman Mandeleil
 * Created on: 28/06/2014 10:25
 */

public class Transaction {

    byte[] data;
    long   gasLimit;
    long   gasPrice;
    long   nonce;
    byte[] secretKey;
    byte[] to;
    long   value;

/* e.g.
    "transaction" : {
            "data" : "",
            "gasLimit" : "10000",
            "gasPrice" : "1",
            "nonce" : "0",
            "secretKey" : "45a915e4d060149eb4365960e6a7a45f334393093061116b197e3240065ff2d8",
            "to" : "095e7baea6a6c7c4c2dfeb977efac326af552d87",
            "value" : "100000"
}
*/

    public Transaction(JSONObject callCreateJSON) {

        String dataStr        = callCreateJSON.get("data").toString();
        String gasLimitStr    = callCreateJSON.get("gasLimit").toString();
        String gasPriceStr    = callCreateJSON.get("gasPrice").toString();
        String nonceStr       = callCreateJSON.get("nonce").toString();
        String secretKeyStr   = callCreateJSON.get("secretKey").toString();
        String toStr          = callCreateJSON.get("to").toString();
        String valueStr       = callCreateJSON.get("value").toString();


        this.data      = Utils.parseData(dataStr);
        this.gasLimit  = Utils.parseLong(gasLimitStr);
        this.gasPrice  = Utils.parseLong(gasPriceStr);
        this.nonce     = Utils.parseLong(nonceStr);
        this.secretKey = Utils.parseData(secretKeyStr);
        this.to        = Utils.parseData(toStr);
        this.value     = Utils.parseLong(valueStr);
    }

    public byte[] getData() {
        return data;
    }

    public long getGasLimit() {
        return gasLimit;
    }

    public long getGasPrice() {
        return gasPrice;
    }

    public long getNonce() {
        return nonce;
    }

    public byte[] getSecretKey() {
        return secretKey;
    }

    public byte[] getTo() {
        return to;
    }

    public long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "data=" + toHexString(data) +
                ", gasLimit=" + gasLimit +
                ", gasPrice=" + gasPrice +
                ", nonce=" + nonce +
                ", secretKey=" + toHexString(secretKey) +
                ", to=" + toHexString(to) +
                ", value=" + value +
                '}';
    }
}