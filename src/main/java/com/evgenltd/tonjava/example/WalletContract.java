package com.evgenltd.tonjava.example;

import com.evgenltd.tonjava.Utils;
import org.ton.api.pk.PrivateKeyEd25519;
import org.ton.api.pub.PublicKeyEd25519;
import org.ton.block.AccountInfo;
import org.ton.block.AddrStd;
import org.ton.block.StateInit;
import org.ton.contract.SmartContract;
import org.ton.contract.wallet.WalletV4R2Contract;
import org.ton.lite.client.LiteClient;
import org.ton.mnemonic.Mnemonic;

public class WalletContract {

    public static void main(String[] args) {

        String mnemonic = ""; // space separated mnemonic phrase
        String password = ""; // password specified on mnemonic phrase generation

        // public key used for recreating address
        // private key - for signing messages
        byte[] seed = Mnemonic.toSeed(Utils.splitMnemonic(mnemonic), password);
        PrivateKeyEd25519 privateKey = PrivateKeyEd25519.of(seed);
        PublicKeyEd25519 publicKey = PublicKeyEd25519.of(privateKey);

        // address recreation
        int workchain = 0;
        StateInit stateInit = WalletV4R2Contract.createStateInit(
                publicKey,
                org.ton.contract.wallet.WalletContract.DEFAULT_WALLET_ID + workchain
        );
        AddrStd address1 = SmartContract.address(workchain, stateInit);
        System.out.println(address1.toString(true, true, false, true));

        // more simple way
        WalletV4R2Contract contract = new WalletV4R2Contract(workchain, publicKey);
        AddrStd address2 = (AddrStd) contract.getAddress();
        System.out.println(address2.toString(true, true, false, true));

        // preparing contract for interacting with blockchain
        String address = ""; // user-friendly address of your wallet
        LiteClient client = Utils.initClient();
        AccountInfo accountInfo = Utils.invoke(continuation -> client.getAccount(address, 0, continuation));
        WalletV4R2Contract readyToUseContract = new WalletV4R2Contract(accountInfo);
//        readyToUseContract.transfer()
    }

}
