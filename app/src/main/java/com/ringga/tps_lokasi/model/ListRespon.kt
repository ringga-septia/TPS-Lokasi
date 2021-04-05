package com.ringga.tps_lokasi.model

data class ListRespon(
    val tgl_report:String,
    val lat:Double,
    val long:Double,
    val keterangan:String,
    val status:Int,
)
