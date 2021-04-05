package com.ringga.tps_lokasi.ui_aksi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.ringga.tps_lokasi.R
import com.ringga.tps_lokasi.api.RetrofitClien
import com.ringga.tps_lokasi.db.SharedPrefManager
import com.ringga.tps_lokasi.model.UserRegis
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.et_email
import kotlinx.android.synthetic.main.activity_register.et_pass
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        tv_login.setOnClickListener {
            startActivity(Intent(baseContext, LoginActivity::class.java))
        }

        btn_register.setOnClickListener {
            pb_loading.visibility = View.VISIBLE
            btn_register.visibility= View.GONE
            //mengambil data dari edittext dan melakukan pengecekan data
            val email = et_email.text.toString().trim()
            val nama_lengkap = et_nama_lengkap.text.toString().trim()
            val username = et_username.text.toString().trim()
            val password = et_pass.text.toString().trim()
            //  Login.setVisibility(View.VISIBLE)

            if(nama_lengkap.isEmpty()){
                et_nama_lengkap.error = "nama lengkap kosong"
                //  Login.setVisibility(View.GONE)
                btn_register.visibility= View.VISIBLE
                et_nama_lengkap.requestFocus()
                return@setOnClickListener
            }
            if(username.isEmpty()){
                et_username.error = "User name kosong"
                //  Login.setVisibility(View.GONE)
                btn_register.visibility= View.VISIBLE
                et_username.requestFocus()
                return@setOnClickListener
            }
            if(email.isEmpty()){
                et_email.error = "Email kosong"
                //  Login.setVisibility(View.GONE)
                et_email.requestFocus()
                btn_register.visibility= View.VISIBLE
                return@setOnClickListener
            }
            if(password.isEmpty()){
                et_pass.error = "Password kosong"
                //  Login.setVisibility(View.GONE)
                et_pass.requestFocus()
                btn_register.visibility= View.VISIBLE
                return@setOnClickListener
            }

            RetrofitClien.instance.registrasi(nama_lengkap ,username,email,password)
                .enqueue(object : Callback<UserRegis> {
                    override fun onResponse(call: Call<UserRegis>, response: Response<UserRegis>) {
                        if (response.isSuccessful){
                            btn_register.visibility= View.VISIBLE
                            Toast.makeText(applicationContext, response.body()?.pesan.toString(), Toast.LENGTH_LONG).show()
                            val intent = Intent(applicationContext,  LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    }

                    override fun onFailure(call: Call<UserRegis>, t: Throwable) {
                        Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                        btn_register.visibility= View.VISIBLE
                    }


                })

        }
    }
    override fun onStart() {
        super.onStart()

        if(SharedPrefManager.getInstance(this)!!.isLoggedIn){
            val intent = Intent(applicationContext, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
        }
    }
}