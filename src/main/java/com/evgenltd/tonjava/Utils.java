package com.evgenltd.tonjava;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.GlobalScope;
import kotlinx.serialization.json.Json;
import kotlinx.serialization.json.JsonKt;
import org.jetbrains.annotations.NotNull;
import org.ton.api.liteclient.config.LiteClientConfigGlobal;
import org.ton.lite.client.LiteClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Utils {

    private static final String CONFIG_JSON = "https://ton.org/testnet-global.config.json";

    public static LiteClient initClient() {
        String configData = Utils.readFromUrl(CONFIG_JSON);
        Json json = JsonKt.Json(Json.Default, builder -> {
            builder.setIgnoreUnknownKeys(true);
            return null;
        });

        LiteClientConfigGlobal config = json.decodeFromString(LiteClientConfigGlobal.Companion.serializer(), configData);

        return new LiteClient(GlobalScope.INSTANCE.getCoroutineContext(), config);
    }

    public static <T> Continuation<T> continuation(Consumer<T> consumer) {
        return ;
    }

    public static <T> T invoke(Consumer<Continuation<T>> suspendFunction) {
        CompletableFuture<T> future = new CompletableFuture<>();
        suspendFunction.accept(new Continuation<T>() {
            @NotNull
            @Override
            public CoroutineContext getContext() {
                return GlobalScope.INSTANCE.getCoroutineContext();
            }

            @Override
            public void resumeWith(@NotNull Object o) {
                future.complete((T) o);
            }
        });
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Unable to perform suspend function", e);
        }
    }

    public static String readFromUrl(String url) {
        try (
                InputStream stream = new URL(url).openStream();
                InputStreamReader streamReader = new InputStreamReader(stream);
                BufferedReader bufferedReader = new BufferedReader(streamReader)
        ) {
            return bufferedReader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> splitMnemonic(String mnemonic) {
        return Arrays.asList(mnemonic.split(" "));
    }

}
