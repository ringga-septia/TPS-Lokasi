package com.ringga.tps_lokasi.api

import com.ringga.tps_lokasi.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface Api {
    @FormUrlEncoded
    @POST("getlogin")
    fun userLogin(
            @Field("email") email:String,
            @Field("password") password: String
    ): Call<LoginRespon>


    @FormUrlEncoded
    @POST("registrasi")
    fun registrasi(
            @Field("nama_lengkap") nama_lengkap:String,
            @Field("username") username:String,
            @Field("email") email:String,
            @Field("password") password: String
    ):Call<UserRegis>

    @Multipart
    @POST("PostTps")
    fun uploadImage(
            @Part("id_member") id_member:Int,
            @Part("latitude") latitude:Double,
            @Part("longitude") longitude:Double,
            @Part("kab_id") kab_id:Int,
            @Part("kec_id") kec_id:Int,
            @Part("kel_id") kel_id:Int,
            @Part("keterangan") keterangan:String,
            @Part file:MultipartBody.Part
    ):Call<UploadRespon>

    @FormUrlEncoded
    @POST("listReport")
    fun listReport(
            @Field("id_member") email:Int
    ):Call<List<ListRespon>>

    @FormUrlEncoded
    @POST("data_list")
    fun data_list(
            @Field("id_member") email:Int
    ):Call<List<ListRespon>>

    @FormUrlEncoded
    @POST("pass")
    fun userPass(
            @Field("oldpass") oldpass:String,
            @Field("newpass") newpass: String,
            @Field("id_member") email:Int
    ): Call<PassRespon>

    @FormUrlEncoded
    @POST("lupa_pass")
    fun userLupaPass(
            @Field("email") email:String,
            @Field("ket") ket: String,
    ): Call<PassRespon>
}