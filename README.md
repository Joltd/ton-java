# About

In this repo you can find basic info about how to use [ton-kotlin](https://github.com/andreypfau/ton-kotlin) library for
interaction with TON blockchain on Java 8

[Introduction](https://ton.org/docs/learn/introduction) to TON blockchain

Common [Getting started](https://ton.org/docs/develop/getting-started)

# Dependency

For new or already existed Maven projects add following dependencies in pom.xml

```xml
<dependency>
    <groupId>org.ton</groupId>
    <artifactId>ton-kotlin-jvm</artifactId>
    <version>0.2.15</version>
</dependency>
<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-maven-serialization</artifactId>
    <version>1.8.10</version>
</dependency>
<dependency>
    <groupId>org.jetbrains.kotlinx</groupId>
    <artifactId>kotlinx-serialization-json-jvm</artifactId>
    <version>1.5.0</version>
</dependency>
```

Keep in mind that `ton-kotlin` mainly developed for Kotlin platform, and it also
contains transitive dependencies for kotlin/kotlinx and ktor libs

`kotlin-maven-serialization` used for deserialization of network config

# Initialization of LiteClient

For most operations with TON blockchain you need to initialize LiteClient

```java
String configData = readFromUrl("https://ton.org/testnet-global.config.json");
Json json = JsonKt.Json(Json.Default, builder -> {
    builder.setIgnoreUnknownKeys(true);
    return null;
});

LiteClientConfigGlobal config = json.decodeFromString(
        LiteClientConfigGlobal.Companion.serializer(), 
        configData
);

LiteClient client = new LiteClient(GlobalScope.INSTANCE.getCoroutineContext(), config);
```

Configs for mainnet and testnet are available by the following links:
- mainnet: https://ton.org/global-config.json
- testnet: https://ton.org/testnet-global.config.json

`readFromUrl()` - this is custom method for reading data from URL, you can use your own implementation

# Coroutines

Interaction with blockchain based on Kotlin Coroutines feature, so when you instantiate LiteClient 
you need to provide `CoroutineContext` for it. In our example we use `GlobalScope` for simplicity.

Almost all methods of LiteClient does not return result immediately, instead they accept 
callback function, also calling `Continuation`, which will be called when result will be ready.
For short up it is more suitable to use method wrapper, like this

```java
<T> Continuation<T> callback(Consumer<T> consumer) {
    return new Continuation<T>() {
        @NotNull
        @Override
        public CoroutineContext getContext() {
            return GlobalScope.INSTANCE.getCoroutineContext();
        }

        @Override
        public void resumeWith(@NotNull Object o) {
            consumer.accept((T) o);
        }
    };
}
```

Here is we also use `GlobalScope` as `CoroutineContext`, and also accept `Consumer` which will
be called when result will be ready

# Generating new account

For new account we should generate mnemonic phrase - 24 words, which will be used for creating 
public and private keys. Keys are used to sign messages sent to the blockchain. For callback function
we are used `callback()` method, which we defined earlier

```java
Mnemonic.generate(
        "", // password, by default could be empty
        Mnemonic.DEFAULT_WORD_COUNT,
        Mnemonic.INSTANCE.mnemonicWords(),
        SecureRandom.INSTANCE,
        callback(phrase -> {
            // fully prepared mnemonic phrase can be used for further operations
        })
);
```

# Smart contracts

Each actor in TON blockchain is a Smart Contract.

Each smart contract represented by Account.

Each Account have address. Address consists of 2 parts: workchain id and account id.
Address could be represented in 2 formats: Raw and user-friendly. 

Workchain id - just a number, exists only two workchains, for most cases 
you need basic workchain (0) 

Address id is a hash of Smart Contract code and initial data.

More about smart contracts and account addresses [here](https://ton.org/docs/learn/overviews/addresses)

# Wallet contract

Wallet - simple smart contract for storing, transferring tokens and sending messages. 
For using wallet you need to prepare address.

At first, we take mnemonic phrase (and password if it was specified) and generate seed from it.
Then we generate private and public keys from seed. Mnemonic phrase can be generated (see section above)
or could be used from existing wallet (for example from [Tonkeeper](https://tonkeeper.com/)).

```java
String mnemonic = ""; // space separated mnemonic phrase
String password = ""; // password specified on mnemonic phrase generation

byte[] seed = Mnemonic.toSeed(Utils.splitMnemonic(mnemonic), password);
PrivateKeyEd25519 privateKey = PrivateKeyEd25519.of(seed);
PublicKeyEd25519 publicKey = PublicKeyEd25519.of(privateKey);
```

Then we recreate init state of the wallet based on public key. Code of wallet smart contract
hardcoded in WalletV4R2Contract class.

> Wallet id could be different for wallets created in other tools, like Tonkeeper
 
```java
int workchain = 0;
StateInit stateInit = WalletV4R2Contract.createStateInit(
        publicKey,
        WalletContract.DEFAULT_WALLET_ID + workchain
);
AddrStd address1 = SmartContract.address(workchain, stateInit);
```

Or more suitable way

```java
int workchain = 0;
WalletV4R2Contract contract = new WalletV4R2Contract(workchain, publicKey);
AddrStd address2 = (AddrStd) contract.getAddress();
```

Address can be converted to user-friendly format

```java
String address = address1.toString(true, true, false, true);
```

Above `WalletV4R2Contract` creates in uninitialized state, which doesn't fit for 
interacting with blockchain. Instead, we should request `AccountInfo` and then
use it for creating `WalletV4R2Contract`.

```java
LiteClient client = Utils.initClient();
AccountInfo accountInfo = Utils.invoke(continuation -> client.getAccount(address, 0, continuation));
WalletV4R2Contract readyToUseContract = new WalletV4R2Contract(accountInfo);
```
`LiteClient.getAccount()` is suspendable method, so for invoking we use
our custom blocking method wrapper

`WalletV4R2Contract` created with `AccountInfo` have proper state and ready to use
in further blockchain operations