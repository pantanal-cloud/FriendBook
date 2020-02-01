package com.youshibi.app.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 *
 */
public class OkHttp3Util {

    /**
     *      * 懒汉 安全 加同步
     *      * 私有的静态成员变量 只声明不创建
     *      * 私有的构造方法
     *      * 提供返回实例的静态方法
     *     
     */
    private static OkHttpClient okHttpClient = null;


    private OkHttp3Util() {
    }

    public static OkHttpClient getInstance() {
        if (okHttpClient == null) {
            //加同步安全
            synchronized (OkHttp3Util.class) {
                if (okHttpClient == null) {
                    //okhttp可以缓存数据....指定缓存路径
                    File sdcache = new File(Environment.getExternalStorageDirectory(), "cache");
                    //指定缓存大小
                    int cacheSize = 10 * 1024 * 1024;

                    okHttpClient = new OkHttpClient.Builder()//构建器
                            .connectTimeout(15, TimeUnit.SECONDS)//连接超时
                            .writeTimeout(20, TimeUnit.SECONDS)//写入超时
                            .readTimeout(20, TimeUnit.SECONDS)//读取超时

                            //.addInterceptor(new CommonParamsInterceptor())//添加的是应用拦截器...公共参数
                            //.addNetworkInterceptor(new CacheInterceptor())//添加的网络拦截器

                            .cache(new Cache(sdcache.getAbsoluteFile(), cacheSize))//设置缓存
                            .build();
                }
            }

        }

        return okHttpClient;
    }

    /**
     *      * get请求
     *      * 参数1 url
     *      * 参数2 回调Callback
     *     
     */

    public static void doGet(String oldUrl, Callback callback) {

        //要添加的公共参数...map
        Map<String, String> map = new HashMap<>();
        //  map.put("source", "android");

        StringBuilder stringBuilder = new StringBuilder();//创建一个stringBuilder

        stringBuilder.append(oldUrl);

        if (oldUrl.contains("?")) {
            //?在最后面....2类型
            if (oldUrl.indexOf("?") == oldUrl.length() - 1) {

            } else {
                //3类型...拼接上&
                stringBuilder.append("&");
            }
        } else {
            //不包含? 属于1类型,,,先拼接上?号
            stringBuilder.append("?");
        }

        //添加公共参数....
        for (Map.Entry<String, String> entry : map.entrySet()) {
            //拼接
            stringBuilder.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue())
                    .append("&");
        }

        //删掉最后一个&符号
        if (stringBuilder.indexOf("&") != -1) {
            stringBuilder.deleteCharAt(stringBuilder.lastIndexOf("&"));
        }

        String newUrl = stringBuilder.toString();//新的路径


        //创建OkHttpClient请求对象
        OkHttpClient okHttpClient = getInstance();
        //创建Request
        Request request = new Request.Builder().url(newUrl).build();
        //得到Call对象
        Call call = okHttpClient.newCall(request);
        //执行异步请求
        call.enqueue(callback);


    }

    /**
     *      * post请求
     *      * 参数1 url
     *      * 参数2 Map<String, String> params post请求的时候给服务器传的数据
     *      *      add..("","")
     *      *      add()
     *     
     */

    public static void doPost(String url, Map<String, String> params, Callback callback) {
        //要添加的公共参数...map
        Map<String, String> map = new HashMap<>();
        // map.put("source", "android");


        //创建OkHttpClient请求对象
        OkHttpClient okHttpClient = getInstance();
        //3.x版本post请求换成FormBody 封装键值对参数

        FormBody.Builder builder = new FormBody.Builder();
        //遍历集合
        for (String key : params.keySet()) {
            builder.add(key, params.get(key));

        }

        //添加公共参数
        for (Map.Entry<String, String> entry : map.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }

        //创建Request
        Request request = new Request.Builder().url(url).post(builder.build()).build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);

    }


    public static String doPost(String url, Map<String, String> params) {
        //要添加的公共参数...map
        Map<String, String> map = new HashMap<>();
        // map.put("source", "android");


        //创建OkHttpClient请求对象
        OkHttpClient okHttpClient = getInstance();
        //3.x版本post请求换成FormBody 封装键值对参数

        FormBody.Builder builder = new FormBody.Builder();
        //遍历集合
        for (String key : params.keySet()) {
            builder.add(key, params.get(key));

        }

        //添加公共参数
        for (Map.Entry<String, String> entry : map.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }

        //创建Request
        Request request = new Request.Builder().url(url).post(builder.build()).build();

        try {
            Response response = okHttpClient.newCall(request).execute();
            String strBody = response.body().string();
            return strBody;
        } catch (Exception e) {
            Log.e("OkHttp3Util", "exe post url error,url:" + url, e);
            return "";
        }
    }
}
