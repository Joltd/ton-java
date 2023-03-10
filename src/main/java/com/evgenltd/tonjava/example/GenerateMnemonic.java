package com.evgenltd.tonjava.example;

import com.evgenltd.tonjava.Utils;
import org.ton.crypto.SecureRandom;
import org.ton.mnemonic.Mnemonic;

import java.util.List;

public class GenerateMnemonic {

    public static void main(String[] args) {
        List<String> mnemonic = Utils.invoke(continuation -> Mnemonic.generate(
                "", // password, by default could be empty
                Mnemonic.DEFAULT_WORD_COUNT,
                Mnemonic.INSTANCE.mnemonicWords(),
                SecureRandom.INSTANCE,
                continuation
        ));
        System.out.println(String.join(" ", mnemonic));
    }

}
