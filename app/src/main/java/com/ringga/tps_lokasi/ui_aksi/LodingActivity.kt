package com.ringga.tps_lokasi.ui_aksi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.ringga.tps_lokasi.R

class LodingActivity : AppCompatActivity() {

    internal val Time = 1000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loding)
//loding halaman
        Handler().postDelayed(
            {
                startActivity(Intent(baseContext, LoginActivity::class.java))
                //  anim.setVisibility(View.GONE)
                finish()
            },Time.toLong()
        )
    }
}