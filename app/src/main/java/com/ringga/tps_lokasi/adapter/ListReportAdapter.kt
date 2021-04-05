package com.ringga.tps_lokasi.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ringga.tps_lokasi.R
import com.ringga.tps_lokasi.model.ListRespon
import com.ringga.tps_lokasi.ui_aksi.LoKasiActivity
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_upload.view.*

class ListReportAdapter (val context: Context): RecyclerView.Adapter<ListReportAdapter.MuvieViewHolder>() {

    private val muvies : MutableList<ListRespon> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MuvieViewHolder {
        return MuvieViewHolder(LayoutInflater.from(context).inflate(R.layout.list_upload,parent,false))
    }

    override fun onBindViewHolder(holder: ListReportAdapter.MuvieViewHolder, position: Int) {
        holder.bindmodel(muvies[position])
    }

    override fun getItemCount(): Int {
        return muvies.size
    }

    fun setMuvie(data: List<ListRespon>){
        muvies.clear()
        muvies.addAll(data)
        notifyDataSetChanged()

    }
    inner class MuvieViewHolder(item : View): RecyclerView.ViewHolder(item){
        val longitude: TextView =item.findViewById(R.id.tv_longitude)
        val latitude: TextView =item.findViewById(R.id.tv_latitude)
        val keterangan: TextView =item.findViewById(R.id.tv_keterangan)
        val iv_kondisi: TextView =item.findViewById(R.id.iv_kondisi)
        val card: CardView =item.findViewById(R.id.card)


        fun bindmodel(m :ListRespon){
            longitude.text = "longitude  :"+ m.long
            latitude.text = "latitude  :"+ m.lat
            keterangan.text = m.keterangan

          if(m.status == 1){
              iv_kondisi.text = "Pending"
              val colorValue = ContextCompat.getColor(context, R.color.kuning)
              iv_kondisi.setBackgroundColor(colorValue)
          }else if(m.status == 2){
              iv_kondisi.text = "Di Terima"
              val colorValue = ContextCompat.getColor(context, R.color.hijau)
              iv_kondisi.setBackgroundColor(colorValue)
          }else{
              iv_kondisi.text = "Di Tolak"
              val colorValue = ContextCompat.getColor(context, R.color.red)
              iv_kondisi.setBackgroundColor(colorValue)
          }

            card.setOnClickListener{
                val mAlert = AlertDialog.Builder(context)
                mAlert.setTitle("Peta")
                mAlert.setIcon(R.mipmap.ic_launcher)
                mAlert.setMessage("Klik untuk untuk menampilkan peta")
                mAlert.setCancelable(false)
                mAlert.setPositiveButton("Tugas"){_,_->
                    Toast.makeText(context ,m.long.toString() + m.lat.toString(), Toast.LENGTH_LONG).show()
                    var i = Intent(context, LoKasiActivity::class.java)
                    i.putExtra("longitude", m.long.toDouble())
                    i.putExtra("latitude", m.lat.toDouble())
                    i.putExtra("keterangan", m.keterangan)
                    context.startActivity(i)
                }
                mAlert.setNegativeButton("Batal"){_,_->
                    Toast.makeText(context ,"membatalkan", Toast.LENGTH_LONG).show()
                }
                val  mAlertDialog = mAlert.create()
                mAlertDialog.show()
            }
        }
    }
}