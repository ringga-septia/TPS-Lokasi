package com.ringga.tps_lokasi.ui_aksi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.ringga.tps_lokasi.R
import com.ringga.tps_lokasi.api.RetrofitClien
import com.ringga.tps_lokasi.db.SharedPrefManager
import com.ringga.tps_lokasi.model.LoginRespon
import com.ringga.tps_lokasi.model.PassRespon
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.btn_login
import kotlinx.android.synthetic.main.activity_login.et_email
import kotlinx.android.synthetic.main.activity_pass.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PassActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pass)
        val mypref = SharedPrefManager.getInstance(this)!!.user
        val id_member = mypref.id_member
        btn_login.setOnClickListener {
            //mengambil data dari edittext dan melakukan pengecekan data
            val oldpass = et_old_pass.text.toString().trim()
            val newpass = et_new_pass.text.toString().trim()
            val verpass = et_very_pass.text.toString().trim()
            //  Login.setVisibility(View.VISIBLE)

            if(oldpass.isEmpty()){
                et_old_pass.error = "Email kosong"
                //  Login.setVisibility(View.GONE)
                et_old_pass.requestFocus()
                return@setOnClickListener
            }
            if(newpass.isEmpty()){

                et_new_pass.error = "Password kosong"
                //  Login.setVisibility(View.GONE)
                et_new_pass.requestFocus()
                return@setOnClickListener
            }
            if(verpass.isEmpty()){
                et_very_pass.error = "Password kosong"
                //  Login.setVisibility(View.GONE)
                et_very_pass.requestFocus()
                return@setOnClickListener
            }
            if (oldpass ==newpass){
                et_new_pass.error = "Password Lama Dan Baru Sama"
                //  Login.setVisibility(View.GONE)
                et_new_pass.requestFocus()
                return@setOnClickListener
            }
            if (verpass !=newpass){
                et_very_pass.error = "Password Berbeda"
                //  Login.setVisibility(View.GONE)
                et_very_pass.requestFocus()
                return@setOnClickListener
            }
            //melakukan absen dengan mengirimkan data yang telah di isi
            RetrofitClien.instance.userPass(oldpass, newpass, id_member)
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
                            SharedPrefManager.getInstance(applicationContext)!!.clear()
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