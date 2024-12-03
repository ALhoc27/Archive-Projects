package ru.dubrovinalexeyleonidovich.mobileagent;

import android.support.annotation.NonNull;

import java.io.IOException;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import static okhttp3.internal.Util.UTF_8;

public class BasicAuthInterceptor implements Interceptor {

    private String credentials;

    BasicAuthInterceptor(String user, String password) {
         this.credentials = Credentials.basic(user, password, UTF_8);
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        Request authenticatedRequest = request.newBuilder()
                .header("Authorization", credentials).build();
        return chain.proceed(authenticatedRequest);
    }

}
