package com.ringga.tps_lokasi.ui_aksi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.ringga.tps_lokasi.R
import kotlinx.android.synthetic.main.activity_lo_kasi.*

class LoKasiActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lo_kasi)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val extras = intent.extras
        val latitude = extras?.getDouble("latitude").toString()
        val longitude=  extras?.getDouble("longitude").toString()
        // Add a marker in Sydney and move the camera
        val sydney = "latitude :"+latitude+"\nlongitude :" +longitude
        tv_lokasi.text = sydney
        tv_keterangan.text = extras?.get("keterangan") as CharSequence?
    }

//menampilkan ke mab
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val extras = intent.extras
        val latitude = extras?.getDouble("latitude")
        val longitude= extras?.getDouble("longitude")
        val sydney = LatLng(latitude!!, longitude!!)
        val ket =extras.get("keterangan") as CharSequence?
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16.0f))
        mMap.addMarker(MarkerOptions().position(sydney).title(ket.toString()))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}