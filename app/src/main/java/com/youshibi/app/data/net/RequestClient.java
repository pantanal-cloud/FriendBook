package com.youshibi.app.data.net;


import android.util.Log;

import com.google.gson.Gson;
import com.youshibi.app.BuildConfig;
import com.youshibi.app.data.net.converter.GsonConverterFactory;
import com.youshibi.app.data.net.converter.NullOnEmptyConverterFactory;
import com.youshibi.app.data.net.interceptor.HeaderInterceptor;
import com.youshibi.app.data.net.interceptor.LoggingInterceptor;
import com.youshibi.app.util.OkHttp3Util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;

/**
 * 作者: 赵成柱 on 2016/7/19 0019.
 */
public class RequestClient {

    public static String token = "";
    private static volatile ServerAPI sServerAPI;//单例模式

    public static ServerAPI getServerAPI() {
        if (sServerAPI == null) {
            synchronized (RequestClient.class) {
                if (sServerAPI == null) {
                    OkHttpClient.Builder clientBuilder = getClientBuilder();
                    HashMap<String, String> headerMap = new HashMap<>();
                    headerMap.put("appver", String.valueOf(BuildConfig.VERSION_CODE));
                    Map<String, String> paramMap = new HashMap<>();
                    paramMap.put("wechatOpenId", "_guest_");

                    OkHttp3Util.doPost(ServerAPI.BASE_URL + "/v1/login", paramMap, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String result = response.body().string();
                            Gson gson = new Gson();
                            Map map = gson.fromJson(result, Map.class);
                            Map dataMap = (Map) map.get("data");
                            token = dataMap.get("token").toString();
                        }
                    });

                    // 等待返回结果
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        Log.e("RequestClient", "sleep error", e);
                    }
                    headerMap.put("X-Token", token);
                    clientBuilder.addInterceptor(new HeaderInterceptor(headerMap));
                    //配置日志拦截器
                    if (BuildConfig.DEBUG) {
                        clientBuilder
                                .addInterceptor(new LoggingInterceptor());
                    }

                    sServerAPI = getRetrofitBuilder(ServerAPI.BASE_URL, clientBuilder.build()).build().create(ServerAPI.class);
                }
            }
        }
        return sServerAPI;

    }

    /**
     * @param url    域名
     * @param client okhttp请求客户端
     * @return retrofit的构建器
     */
    private static Retrofit.Builder getRetrofitBuilder(String url, OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(new NullOnEmptyConverterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(client);
    }


    private static OkHttpClient.Builder getClientBuilder() {
        return new OkHttpClient.Builder()
                .connectTimeout(HttpConfig.CONNECT_TIME_OUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(HttpConfig.READ_TIME_OUT_SECONDS, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);//重试
        // .writeTimeout(HttpConfig.WRITE_TIME_OUT_SECONDS, TimeUnit.SECONDS);
    }
}
