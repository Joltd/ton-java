package com.evgenltd.tonjava.example;

import com.evgenltd.tonjava.Utils;
import org.ton.api.pk.PrivateKeyEd25519;
import org.ton.block.AccountInfo;
import org.ton.block.Coins;
import org.ton.contract.wallet.WalletTransfer;
import org.ton.contract.wallet.WalletTransferBuilder;
import org.ton.contract.wallet.WalletV4R2Contract;
import org.ton.lite.client.LiteClient;
import org.ton.mnemonic.Mnemonic;

public class TransferCoin {

    public static void main(String[] args) {
        String mnemonic = ""; // mnemonic phrase of your wallet
        String address = ""; // user-friendly address of your wallet
        String destination = ""; // user-friendly address of destination wallet

        // preapre seed and private key for sign transfer message
        byte[] seed = Mnemonic.toSeed(Utils.splitMnemonic(mnemonic), "");
        PrivateKeyEd25519 privateKey = PrivateKeyEd25519.of(seed);

        // prepare LiteClient
        LiteClient client = Utils.initClient();

        // get info about your wallet account and prepare contract object for transfer messages
        AccountInfo accountInfo = Utils.invoke(continuation -> client.getAccount(address, 0, continuation));
        WalletV4R2Contract contract = new WalletV4R2Contract(accountInfo);

        // get info about destination wallet account
        AccountInfo destinationInfo = Utils.invoke(continuation -> client.getAccount(destination, 0, continuation));

        // prepare transfer message
        WalletTransferBuilder builder = new WalletTransferBuilder();
        builder.setDestination(destinationInfo.getAddr());
        builder.setCoins(Coins.ofNano(100000000)); // 0.1 TON
        builder.setBounceable(false);

        // transfer message to blockchain
        // it will send external message to your wallet, which instruct it to send internal message to destination wallet
        // empty internal message means just transfer coins
        Utils.invoke(continuation -> contract.transfer(
                client.getLiteApi(),
                privateKey,
                new WalletTransfer[]{ builder.build() },
                continuation
        ));

    }

}
