package com.ringga.tps_lokasi.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClien {
   // private val AUTH = "Basic "+ Base64.encodeToString("ringga:123456".toByteArray(), Base64.NO_WRAP)
    private const val BASE_URL = "http://risma-project.xyz/api/"
//    http://192.168.8.101/app-tps/api/   http://demo.mas-koding.com/tps-liar/api/
//    public val okHttpClient = OkHttpClient.Builder()
//        .addInterceptor { chain ->
//            val original = chain.request()
//            val requestBuilder = original.newBuilder()
//                .addHeader("Authorization",
//                    AUTH
//                )
//                .method(original.method(), original.body())
//
//            val request = requestBuilder.build()
//            chain.proceed(request)
//        }.build()

    public val okHttpClient = OkHttpClient.Builder()
            .readTimeout(1,TimeUnit.MINUTES)
            .connectTimeout(1,TimeUnit.MINUTES)
            .build()

    val instance: Api by lazy{
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        retrofit.create(Api::class.java)
    }
}