package com.ringga.tps_lokasi.model

data class LoginRespon(
    val status :Boolean,
    val message : String,
    val data : DataUser
    )