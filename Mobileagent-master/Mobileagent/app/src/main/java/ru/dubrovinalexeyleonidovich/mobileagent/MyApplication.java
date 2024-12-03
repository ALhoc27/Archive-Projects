package ru.dubrovinalexeyleonidovich.mobileagent;

import android.app.Application;
import android.content.Context;

import com.raizlabs.android.dbflow.config.FlowManager;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MyApplication extends Application {
    public static Context applicationContext;

    private static RequestsApi requestsApi;

    private static String baseUrl = "";

    @Override
    public void onCreate() {
        super.onCreate();

        applicationContext = getApplicationContext();

        FlowManager.init(this);

//        initRetrofit();
    }

    private static RequestsApi getApi() {
        return requestsApi;
    }

    private static void initRetrofit(String login, String password) {

        OkHttpClient client = new OkHttpClient.Builder()
//                .addInterceptor(new BasicAuthInterceptor("MobileAgentService", "HfvdfkvdflkvdkOIJ)#F#Edfnvdfvvlx*^&*^%&*^(*6#@DR0j"))
                .addInterceptor(new BasicAuthInterceptor(login, password))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(baseUrl)                                       //Базовая часть адреса http://denjettmai.temp.swtest.ru/
                .addConverterFactory(new NullOnEmptyConverterFactory())
                .addConverterFactory(GsonConverterFactory.create())     //Конвертер, необходимый для преобразования JSON'а в объекты
                .build();
        requestsApi = retrofit.create(RequestsApi.class);               //Создаем объект, при помощи которого будем выполнять запросы
    }


    public static RequestsApi getRetrofit(String baseUrlNew, String login, String password) {

        if(baseUrl.equals(baseUrlNew)) {

            return requestsApi;

        } else {

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new BasicAuthInterceptor(login, password))
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .client(client)
                    .baseUrl(baseUrlNew)                                       //Базовая часть адреса http://denjettmai.temp.swtest.ru/
                    .addConverterFactory(new NullOnEmptyConverterFactory())
                    .addConverterFactory(GsonConverterFactory.create())     //Конвертер, необходимый для преобразования JSON'а в объекты
                    .build();
            requestsApi = retrofit.create(RequestsApi.class);               //Создаем объект, при помощи которого будем выполнять запросы
            baseUrl = baseUrlNew;

            return requestsApi;
        }
    }

}