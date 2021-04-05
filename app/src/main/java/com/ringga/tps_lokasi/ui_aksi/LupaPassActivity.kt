package com.ringga.tps_lokasi.ui_aksi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.ringga.tps_lokasi.R
import com.ringga.tps_lokasi.api.RetrofitClien
import com.ringga.tps_lokasi.db.SharedPrefManager
import com.ringga.tps_lokasi.model.PassRespon
import kotlinx.android.synthetic.main.activity_lupa_pass.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LupaPassActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lupa_pass)

        btn_kirim.setOnClickListener {
            //mengambil data dari edittext dan melakukan pengecekan data
            val oldpass = et_email.text.toString().trim()
            val newpass = et_pesan.text.toString().trim()
            //  Login.setVisibility(View.VISIBLE)

            if(oldpass.isEmpty()){
                et_email.error = "Email kosong"
                //  Login.setVisibility(View.GONE)
                et_email.requestFocus()
                return@setOnClickListener
            }
            if(newpass.isEmpty()){

                et_pesan.error = "Password kosong"
                //  Login.setVisibility(View.GONE)
                et_pesan.requestFocus()
                return@setOnClickListener
            }

            //melakukan absen dengan mengirimkan data yang telah di isi
            RetrofitClien.instance.userLupaPass(oldpass, newpass)
                .enqueue(object : Callback<PassRespon> {
                    override fun onFailure(call: Call<PassRespon>, t: Throwable) {
                        //  Login.setVisibility(View.GONE)
                        Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                    }
                    //melakukan validasi respon berhasil di ambil
                    override fun onResponse(
                        call: Call<PassRespon>,
                        response: Response<PassRespon>
                    ) {
                        //respon berhasil
                        val data = response.body()
                        if (data?.message == true){
                            Toast.makeText(applicationContext, data.pesan, Toast.LENGTH_LONG).show()
                            startActivity(Intent(baseContext, LoginActivity::class.java))
                            finish()
                        }else{
                            Toast.makeText(applicationContext, data?.pesan, Toast.LENGTH_LONG).show()
                        }
                    }
                })
        }
    }
}