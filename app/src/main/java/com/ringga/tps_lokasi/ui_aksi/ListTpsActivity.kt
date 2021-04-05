package com.ringga.tps_lokasi.ui_aksi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.ringga.tps_lokasi.R
import com.ringga.tps_lokasi.adapter.MuvieAdapter
import com.ringga.tps_lokasi.api.RetrofitClien
import com.ringga.tps_lokasi.db.SharedPrefManager
import com.ringga.tps_lokasi.model.ListRespon
import kotlinx.android.synthetic.main.activity_list_tps.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListTpsActivity : AppCompatActivity() {

    lateinit var muvieAdapter: MuvieAdapter
    val lm = LinearLayoutManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_tps)

        val mypref = SharedPrefManager.getInstance(this)!!.user
        val id_member = mypref.id_member
//        get data dari api
        RetrofitClien.instance.listReport(id_member).enqueue(object : Callback<List<ListRespon>> {
            override fun onResponse(
                call: Call<List<ListRespon>>,
                response: Response<List<ListRespon>>
            ) {
                Toast.makeText(applicationContext, "menampilkan data", Toast.LENGTH_LONG).show()
                val data = response.body()

                if (data != null) {
                    showClass(data)
                }
                //set tombol
                pb_loding.visibility = View.GONE
            }

            override fun onFailure(call: Call<List<ListRespon>>, t: Throwable) {
                Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
            }


        })
    }
//respon ke list
    private fun showClass(Respons:List<ListRespon>){
        rv_upload.layoutManager = lm
        muvieAdapter = MuvieAdapter(this)
        rv_upload.adapter = muvieAdapter
        muvieAdapter.setMuvie(Respons)


    }
}