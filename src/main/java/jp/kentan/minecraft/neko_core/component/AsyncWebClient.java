package jp.kentan.minecraft.neko_core.component;

import okhttp3.*;

import java.util.Collections;


public class AsyncWebClient {

    private final OkHttpClient CLIENT;

    public AsyncWebClient(){
        ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .cipherSuites(
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)
                .build();

        CLIENT = new OkHttpClient.Builder()
                .connectionSpecs(Collections.singletonList(spec))
                .build();
    }

    public void fetch(String url, Callback callback){
        final Request request = new Request.Builder()
                .url(url)
                .build();

        CLIENT.newCall(request).enqueue(callback);
    }
}
