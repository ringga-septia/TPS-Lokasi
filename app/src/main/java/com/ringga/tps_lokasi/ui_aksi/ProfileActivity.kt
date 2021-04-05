package com.ringga.tps_lokasi.ui_aksi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ringga.tps_lokasi.R
import com.ringga.tps_lokasi.db.SharedPrefManager
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val mypref = SharedPrefManager.getInstance(this)!!.user

        tv_nama.text ="Nama  :"+mypref.nama_lengkap
        tv_user_name.text="UserName  :"+mypref.username
        tv_email.text="Email :"+mypref.email
//        Picasso.get().load("http://192.168.8.101/app-tps/assets/images/pending.jpg").resize(253,253).centerCrop().into(image)

        btn_logout.setOnClickListener {
            SharedPrefManager.getInstance(this)!!.clear()
            startActivity(Intent(baseContext, LoginActivity::class.java))
            finish()
        }
        pass.setOnClickListener {
            startActivity(Intent(baseContext, PassActivity::class.java))
        }
    }
}