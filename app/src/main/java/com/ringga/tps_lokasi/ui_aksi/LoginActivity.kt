package com.ringga.tps_lokasi.ui_aksi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.ringga.tps_lokasi.R
import com.ringga.tps_lokasi.api.RetrofitClien
import com.ringga.tps_lokasi.db.SharedPrefManager
import com.ringga.tps_lokasi.model.LoginRespon
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
//fungsi tombol pindah halaman
        tv_register.setOnClickListener {
            startActivity(Intent(baseContext, RegisterActivity::class.java))
        }
        tv_lupa.setOnClickListener {
            startActivity(Intent(baseContext, LupaPassActivity::class.java))
        }

        btn_login.setOnClickListener {
            //mengambil data dari edittext dan melakukan pengecekan data
            val email = et_email.text.toString().trim()
            val password = et_pass.text.toString().trim()
            //  Login.setVisibility(View.VISIBLE)

            if(email.isEmpty()){
                et_email.error = "Email kosong"
                //  Login.setVisibility(View.GONE)
                et_email.requestFocus()
                return@setOnClickListener
            }
            if(password.isEmpty()){

                et_pass.error = "Password kosong"
                //  Login.setVisibility(View.GONE)
                et_pass.requestFocus()
                return@setOnClickListener
            }
            //melakukan absen dengan mengirimkan data yang telah di isi
            RetrofitClien.instance.userLogin(email, password)
                .enqueue(object : Callback<LoginRespon> {
                    override fun onFailure(call: Call<LoginRespon>, t: Throwable) {
                        //  Login.setVisibility(View.GONE)
                        Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                    }
                    //melakukan validasi respon berhasil di ambil
                    override fun onResponse(
                        call: Call<LoginRespon>,
                        response: Response<LoginRespon>
                    ) {
                        //respon login berhasil
                        if (response.isSuccessful){
                            Toast.makeText(applicationContext, response.body()?.data.toString(), Toast.LENGTH_LONG).show()
                            response.body()?.data?.let { it1 ->
                                SharedPrefManager.getInstance(applicationContext)!!.saveUser(response.body()?.data!!)

                                val intent = Intent(applicationContext,  HomeActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
//                                startActivity(Intent(baseContext, HomeActivity::class.java))
//                                //   Toast.makeText(applicationContext, response.body()?.user?.toString() , Toast.LENGTH_LONG).show()
                            }
                        } else {
                            //respon login gagal
                            if(response.code() == 400) {
                                //   Login.setVisibility(View.GONE)
                                Toast.makeText(
                                    applicationContext,
                                    "user tidak ditemukan",
                                    Toast.LENGTH_LONG
                                ).show()
                            }else{
                                // Login.setVisibility(View.GONE)
                                Toast.makeText(
                                    applicationContext,
                                    "user belum memiliki akses",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
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