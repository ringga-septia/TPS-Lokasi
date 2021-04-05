package com.ringga.tps_lokasi.ui_aksi

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.provider.SettingsSlicesContract.KEY_LOCATION
import com.google.android.material.snackbar.Snackbar
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.ringga.tps_lokasi.BuildConfig
import com.ringga.tps_lokasi.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_register.*
import java.text.DateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    private val REQUEST_CHECK_SETTINGS = 0x1
    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 20000
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
    private var mLastUpdateTimeTextView: TextView? = null
    private var mLatitudeTextView: TextView? = null
    private var mLongitudeTextView: TextView? = null
    private var mLatitudeLabel: String? = null
    private var mLongitudeLabel: String? = null
    private var mLastUpdateTimeLabel: String? = null
    private var mRequestingLocationUpdates: Boolean? = null
    private var mLastUpdateTime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        mStartUpdatesButton = findViewById<View>(R.id.start_updates_button) as Button
        mStopUpdatesButton = findViewById<View>(R.id.stop_updates_button) as Button
        mLatitudeTextView = findViewById<View>(R.id.latitude_text) as TextView
        mLongitudeTextView = findViewById<View>(R.id.longitude_text) as TextView
        mLastUpdateTimeTextView = findViewById<View>(R.id.last_update_time_text) as TextView

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

            var i = Intent(this, UploadActivity::class.java)
            i.putExtra("latitude", latitude)
            i.putExtra("longitude", longitude)
            startActivity(i)

        }

        im_log.setOnClickListener {
            val mAlert = AlertDialog.Builder(this)
            mAlert.setTitle("apakah anda ingin menampilkan lokasi ini..!!")
            mAlert.setIcon(R.mipmap.ic_launcher)
            mAlert.setMessage("ini adalah opsi untuk menampikan lokasi anda pada maps dan pada halaman ini memiliki fitur yang dapat mengarahkan anda langsung ke aplikasi google maps milik google")
            mAlert.setCancelable(false)
            mAlert.setPositiveButton("Tampilkan"){_,_->
                var i = Intent(this, MapsActivity::class.java)
//                i.putExtra("latitude",  mCurrentLocation!!.getLatitude().toString())
//                i.putExtra("longitude", mCurrentLocation!!.getLongitude().toString())
                startActivity(i)
            }
            mAlert.setNegativeButton("Batal"){_,_->
                finish()
            }

            val  mAlertDialog = mAlert.create()
            mAlertDialog.show()
        }
    }
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
    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS)
        mLocationRequest!!.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
        mLocationRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    }
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
    private fun buildLocationSettingsRequest() {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)
        mLocationSettingsRequest = builder.build()
    }

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
    fun startUpdatesButtonHandler(view: View?) {
        if (!mRequestingLocationUpdates!!) {
            mRequestingLocationUpdates = true
            setButtonsEnabledState()
            startLocationUpdates()
        }
    }
    fun stopUpdatesButtonHandler(view: View?) {
        stopLocationUpdates()
    }
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
                                rae.startResolutionForResult(this@MainActivity, REQUEST_CHECK_SETTINGS)
                            } catch (sie: IntentSender.SendIntentException) {
                                Log.i(TAG, "PendingIntent unable to execute request.")
                            }
                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            val errorMessage = "Location settings are inadequate, and cannot be " +
                                    "fixed here. Fix in Settings."
                            Log.e(TAG, errorMessage)
                            Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_LONG).show()
                            mRequestingLocationUpdates = false
                        }
                    }
                    updateUI()
                }
    }

    private fun updateUI() {
        setButtonsEnabledState()
        updateLocationUI()
    }
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
            mLatitudeTextView!!.text = String.format(Locale.ENGLISH, "%s: %f", mLatitudeLabel,
                    mCurrentLocation!!.getLatitude())

            mLongitudeTextView!!.text = String.format(Locale.ENGLISH, "%s: %f", mLongitudeLabel,
                    mCurrentLocation!!.getLongitude())
            mLastUpdateTimeTextView!!.text = String.format(Locale.ENGLISH, "%s: %s",
                    mLastUpdateTimeLabel, mLastUpdateTime)

            btn_kirim.visibility= View.VISIBLE
        }
    }

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

    override fun onResume() {
        super.onResume()
        if (mRequestingLocationUpdates!! && checkPermissions()) {
            startLocationUpdates()
        } else if (!checkPermissions()) {
            requestPermissions()
        }
        updateUI()
    }

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
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_PERMISSIONS_REQUEST_CODE)
            }
        } else {
            Log.i(TAG, "Requesting permission")
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
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