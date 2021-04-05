package com.ringga.tps_lokasi.ui_aksi

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.ringga.tps_lokasi.BuildConfig
import com.ringga.tps_lokasi.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.activity_maps.btn_kirim
import org.json.JSONException
import java.text.DateFormat
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private val TAG = MainActivity::class.java.simpleName
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    private val REQUEST_CHECK_SETTINGS = 0x1
    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000
    private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2
    private val KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates"
    private val KEY_LOCATION = "location"
    private val KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string"
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mSettingsClient: SettingsClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var mLocationSettingsRequest: LocationSettingsRequest? = null
    private var mLocationCallback: LocationCallback? = null
    private var mCurrentLocation: Location? = null
    private var mStartUpdatesButton: Button? = null
    private var mStopUpdatesButton: Button? = null
    private var mLatitudeLabel: String? = null
    private var mLongitudeLabel: String? = null
    private var mLastUpdateTimeLabel: String? = null
    private var mRequestingLocationUpdates: Boolean? = null
    private var mLastUpdateTime: String? = null

    var requestQueue: RequestQueue? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mStartUpdatesButton = findViewById<View>(R.id.start_updates_button) as Button
        mStopUpdatesButton = findViewById<View>(R.id.stop_updates_button) as Button
        mLatitudeLabel = resources.getString(R.string.latitude_label)
        mLongitudeLabel = resources.getString(R.string.longitude_label)
        mLastUpdateTimeLabel = resources.getString(R.string.last_update_time_label)

        mRequestingLocationUpdates = false
        mLastUpdateTime = ""

        updateValuesFromBundle(savedInstanceState)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mSettingsClient = LocationServices.getSettingsClient(this)

        createLocationCallback()
        createLocationRequest()
        buildLocationSettingsRequest()

        btn_kirim.setOnClickListener {
            val latitude =  mCurrentLocation!!.getLatitude().toString()
            val longitude = mCurrentLocation!!.getLongitude().toString()

            val i = Intent(this, UploadActivity::class.java)
            i.putExtra("latitude", latitude)
            i.putExtra("longitude", longitude)
            startActivity(i)

        }




    }


//update lokasi
    private fun updateValuesFromBundle(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                    KEY_REQUESTING_LOCATION_UPDATES)
            }
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            }
            if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_TIME_STRING)) {
                mLastUpdateTime = savedInstanceState.getString(KEY_LAST_UPDATED_TIME_STRING)
            }
            updateUI()
        }
    }
    //menjarankan gps dan get lokasi
    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS)
        mLocationRequest!!.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
        mLocationRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    }
    //meminta balikan lokasi
    private fun createLocationCallback() {
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                mCurrentLocation = locationResult.lastLocation
                mLastUpdateTime = DateFormat.getTimeInstance().format(Date())
                updateLocationUI()
            }
        }
    }
    //memperbarui data
    private fun buildLocationSettingsRequest() {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)
        mLocationSettingsRequest = builder.build()
    }
//hasil jika data didapatkan
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> when (resultCode) {
                RESULT_OK -> Log.i(TAG, "User agreed to make required location settings changes.")
                RESULT_CANCELED -> {
                    Log.i(TAG, "User chose not to make required location settings changes.")
                    mRequestingLocationUpdates = false
                    updateUI()
                }
            }
        }
    }
    //fungso tombol start
    fun startUpdatesButtonHandler(view: View?) {
        if (!mRequestingLocationUpdates!!) {
            mRequestingLocationUpdates = true
            setButtonsEnabledState()
            startLocationUpdates()
        }
    }
    //fungsi stop
    fun stopUpdatesButtonHandler(view: View?) {
        stopLocationUpdates()
    }
    //melakukan update lokasi
    private fun startLocationUpdates() {
        mSettingsClient!!.checkLocationSettings(mLocationSettingsRequest)
            .addOnSuccessListener(this) {
                Log.i(TAG, "All location settings are satisfied.")
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return@addOnSuccessListener
                }
                mFusedLocationClient!!.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper())
                updateUI()
            }
            .addOnFailureListener(this) { e ->
                val statusCode = (e as ApiException).statusCode
                when (statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                "location settings ")
                        try {
                            val rae = e as ResolvableApiException
                            rae.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                        } catch (sie: IntentSender.SendIntentException) {
                            Log.i(TAG, "PendingIntent unable to execute request.")
                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        val errorMessage = "Location settings are inadequate, and cannot be " +
                                "fixed here. Fix in Settings."
                        Log.e(TAG, errorMessage)
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                        mRequestingLocationUpdates = false
                    }
                }
                updateUI()
            }
    }
//memperbarui tampilan
    private fun updateUI() {
        setButtonsEnabledState()
        updateLocationUI()
    }
    //set kondisi tombol
    private fun setButtonsEnabledState() {
        if (mRequestingLocationUpdates!!) {
            mStartUpdatesButton!!.isEnabled = false
            mStopUpdatesButton!!.isEnabled = true
        } else {
            mStartUpdatesButton!!.isEnabled = true
            mStopUpdatesButton!!.isEnabled = false
        }
    }

    private fun updateLocationUI() {
        if (mCurrentLocation != null) {

            val extras = intent.extras
            val latitude = mCurrentLocation!!.getLatitude()
            val longitude=  mCurrentLocation!!.getLongitude()
            val sydney = LatLng(latitude, longitude)


            mMap.clear()
            val markerOptions = MarkerOptions().position(sydney)
//mendapankan data marker
                requestQueue = Volley.newRequestQueue(this)
                val url = "http://demo.mas-koding.com/tps-liar/api/data_list"
                val jsonObjectRequest = JsonObjectRequest(
                        Request.Method.POST,
                        url, null, { response ->
                    try {
                        val jsonArray = response.getJSONArray("data")

                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val newData = LatLng(jsonObject.optDouble("lat"), jsonObject.optDouble("long"))
                            val markerAll = MarkerOptions().position(newData)
                            mMap.addMarker(markerAll)
                        }

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }) {
                    Toast.makeText(this, "data error", Toast.LENGTH_LONG).show()
                }
                requestQueue?.run {
                    add(jsonObjectRequest)
                }
            val pickupMarkerDrawable = resources.getDrawable(R.drawable.ic_log,null)
            mMap.addMarker(
                    MarkerOptions()
                            .position(sydney)
                            .title("Posisi Anda")
//                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_lokasi_saya))
                            .icon(BitmapDescriptorFactory.fromBitmap(pickupMarkerDrawable.toBitmap(pickupMarkerDrawable.intrinsicWidth, pickupMarkerDrawable.intrinsicHeight, null)))
            )


            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16.0f))
            btn_kirim.visibility = View.VISIBLE
            tv_lokasi.text = sydney.toString()
        }
    }



    //menyiapkan peta
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        updateLocationUI()
    }
//stop update lokasi
    private fun stopLocationUpdates() {
        if (!mRequestingLocationUpdates!!) {
            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.")
            return
        }
        mFusedLocationClient!!.removeLocationUpdates(mLocationCallback)
            .addOnCompleteListener(this) {
                mRequestingLocationUpdates = false
                setButtonsEnabledState()
            }
    }
//jika kembali ke halaman lagi
    override fun onResume() {
        super.onResume()
        if (mRequestingLocationUpdates!! && checkPermissions()) {
            startLocationUpdates()
        } else if (!checkPermissions()) {
            requestPermissions()
        }
        updateUI()
    }
//jika keluar
    override fun onPause() {
        super.onPause()

        stopLocationUpdates()
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates!!)
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation)
        savedInstanceState.putString(KEY_LAST_UPDATED_TIME_STRING, mLastUpdateTime)
        super.onSaveInstanceState(savedInstanceState)
    }

    private fun showSnackbar(mainTextStringId: Int, actionStringId: Int,
                             listener: View.OnClickListener) {
        Snackbar.make(
            findViewById(android.R.id.content),
            getString(mainTextStringId),
            Snackbar.LENGTH_INDEFINITE)
            .setAction(getString(actionStringId), listener).show()
    }
//cek permission android
    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION)
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
            Manifest.permission.ACCESS_FINE_LOCATION)

        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            showSnackbar(R.string.permission_rationale,
                android.R.string.ok) { // Request permission
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSIONS_REQUEST_CODE)
            }
        } else {
            Log.i(TAG, "Requesting permission")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSIONS_REQUEST_CODE)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>,
                                            grantResults: IntArray) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.size <= 0) {

                Log.i(TAG, "User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mRequestingLocationUpdates!!) {
                    Log.i(TAG, "Permission granted, updates requested, starting location updates")
                    startLocationUpdates()
                }
            } else {

                showSnackbar(R.string.permission_denied_explanation,
                    R.string.settings) { // Build intent that displays the App settings screen.
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package",
                        BuildConfig.APPLICATION_ID, null)
                    intent.data = uri
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            }
        }
    }
}


