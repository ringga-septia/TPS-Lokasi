package com.ringga.tps_lokasi.ui_aksi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.ringga.tps_lokasi.R
import com.ringga.tps_lokasi.db.SharedPrefManager
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        iv_lokasi.setOnClickListener {
            startActivity(Intent(baseContext, MapsActivity::class.java))
        }
        iv_listreport.setOnClickListener {
            startActivity(Intent(baseContext, ListTpsActivity::class.java))
        }
        iv_profile.setOnClickListener{
            startActivity(Intent(baseContext, ProfileActivity::class.java))
        }
//        iv_list_tps.setOnClickListener{
//            startActivity(Intent(baseContext, LisReportActivity::class.java))
//        }
    }
    override fun onStart() {
        super.onStart()

        if(!SharedPrefManager.getInstance(this)!!.isLoggedIn){
            Toast.makeText(applicationContext, SharedPrefManager.getInstance(this)!!.isLoggedIn.toString(), Toast.LENGTH_LONG).show()
            val intent = Intent(applicationContext, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
        }
    }
}