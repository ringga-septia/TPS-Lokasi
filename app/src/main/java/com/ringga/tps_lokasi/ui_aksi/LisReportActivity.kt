package com.ringga.tps_lokasi.ui_aksi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.ringga.tps_lokasi.R
import com.ringga.tps_lokasi.adapter.ListReportAdapter
import com.ringga.tps_lokasi.api.RetrofitClien
import com.ringga.tps_lokasi.db.SharedPrefManager
import com.ringga.tps_lokasi.model.ListRespon
import kotlinx.android.synthetic.main.activity_lis_report.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LisReportActivity : AppCompatActivity() {

    lateinit var muvieAdapter: ListReportAdapter
    val lm = LinearLayoutManager(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lis_report)

        val mypref = SharedPrefManager.getInstance(this)!!.user
        val id_member = mypref.id_member
//get data dari api
        RetrofitClien.instance.data_list(id_member).enqueue(object : Callback<List<ListRespon>> {
            override fun onResponse(
                    call: Call<List<ListRespon>>,
                    response: Response<List<ListRespon>>
            ) {
                Toast.makeText(applicationContext, "menampilkan data", Toast.LENGTH_LONG).show()
                val data = response.body()

                if (data != null) {
                    showClass(data)
                }
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
        muvieAdapter = ListReportAdapter(this)
        rv_upload.adapter = muvieAdapter
        muvieAdapter.setMuvie(Respons)
    }
}