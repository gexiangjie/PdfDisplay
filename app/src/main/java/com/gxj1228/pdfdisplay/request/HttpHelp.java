package com.gxj1228.pdfdisplay.request;

import com.gxj1228.pdfdisplay.request.api.FileApi;
import com.gxj1228.pdfdisplay.request.widget.ProgressResponseBody;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * @author gxj
 * @date 2018/7/31
 */
public class HttpHelp {

    private volatile static HttpHelp instance;
    private HttpHelp() {
    }

    public static HttpHelp getInstance() {
        if (instance == null) {
            synchronized (HttpHelp.class) {
                if (instance == null) {
                    instance = new HttpHelp();
                }
            }
        }
        return instance;
    }


    /**
     * 文件下载
     *
     * @param progressListener
     * @return
     */
    public FileApi getFileApi(final ProgressResponseBody.ProgressListener progressListener) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .retryOnConnectionFailure(true)
                //设置超时时间
                .connectTimeout(10, TimeUnit.SECONDS)
                //设置读取超时时间
                .readTimeout(10, TimeUnit.SECONDS)
                //设置写入超时时间
                .writeTimeout(10, TimeUnit.SECONDS)
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        okhttp3.Response orginalResponse = chain.proceed(chain.request());
                        return orginalResponse.newBuilder()
                                .body(new ProgressResponseBody(orginalResponse.body(), progressListener))
                                .build();
                    }
                })
                .build();

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("http://static-public.jesselivermore.com")
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());

        FileApi fileApi = builder.build().create(FileApi.class);
        return fileApi;
    }
}
