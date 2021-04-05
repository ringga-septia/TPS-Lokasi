package com.ringga.tps_lokasi.ui_aksi

import android.content.Intent

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.ringga.tps_lokasi.R
import com.ringga.tps_lokasi.adapter.UploadRequestBody
import com.ringga.tps_lokasi.adapter.getFileName
import com.ringga.tps_lokasi.adapter.snackbar
import com.ringga.tps_lokasi.api.RetrofitClien
import com.ringga.tps_lokasi.db.SharedPrefManager
import com.ringga.tps_lokasi.model.UploadRespon
import kotlinx.android.synthetic.main.activity_upload.*
import okhttp3.MultipartBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.util.*
import kotlin.collections.ArrayList


class UploadActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    lateinit var currentPhotoPath: String
    private var selectetImage:Uri? = null
    private   val REQUEST_CODE_IMAGE_PICKER = 100
    private   val REQUEST_CODE_IMAGE_CAMERA =101
    private var nameFile : String? =null

    var kabupatenList = ArrayList<String>()
    val kabupatenListId =ArrayList<Int>()
    var kecamatanList = ArrayList<String>()
    var kecamatanlistId =ArrayList<Int>()
    var kelurahanList = ArrayList<String>()
    val kelurahanListId =ArrayList<Int>()
    var kebupatenAdapter: ArrayAdapter<String>? = null
    var KecamatanAdapter: ArrayAdapter<String>? = null
    var kelurahanAdapter: ArrayAdapter<String>? = null
    var requestQueue: RequestQueue? = null
    var dataSpiner :Int?=null


    // data yang di ambil
    var kab_log :Int?=null
    var kec_log :Int?=null
    var kel_log :Int?=null
    val dataKelurahanSpiner:Int?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)
        image.setOnClickListener {
            showPictureDialog()

        }
        name_image.text =selectetImage.toString()
        btn_upload.setOnClickListener {
            btn_upload.visibility =View.GONE

           uploadImage()
        }

        requestQueue = Volley.newRequestQueue(this)
        val url = "http://risma-project.xyz/api/GetKabupaten"
        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST,
                url, null, { response ->
            try {
                val jsonArray = response.getJSONArray("data")
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val kabupaten = jsonObject.optString("nama")
                    val idKabupaten = jsonObject.optInt("id_kab")
                    kabupatenList.add(kabupaten)
                    kabupatenListId.add(idKabupaten)
                    kebupatenAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, kabupatenList)
                    kebupatenAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spiner_kabupaten.setAdapter(kebupatenAdapter)
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

        spiner_kabupaten.setOnItemSelectedListener(this)

        spiner_kecamatan.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                val item = parent.getItemAtPosition(pos)
                val dataIdkec = kecamatanlistId[pos]
                kelurahanList.clear()
                kec_log =kecamatanlistId.get(pos)
                keluraha(dataIdkec)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })

        spiner_kelurahan.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                kel_log =kelurahanListId[pos]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })
    }



    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf("Select photo from gallery", "Capture photo from camera")
        pictureDialog.setItems(pictureDialogItems
        ) { dialog, which ->
            when (which) {
                0 -> openImageChooser()
                1 -> dispatchTakePictureIntent()
            }
        }
        pictureDialog.show()
    }


    private fun uploadImage() {
        if (selectetImage == null) {
            btn_upload.visibility =View.VISIBLE
            layout_root.snackbar("Select an Image First")
            return
        }
        val extras = intent.extras
        val latitude = extras?.getString("latitude")?.toDouble()
        val longitude= extras?.getString("longitude")?.toDouble()
        //mendapatkan data user
        val mypref = SharedPrefManager.getInstance(this)!!.user
        val id_member = mypref.id_member
        //inputan user
        val keterangan = et_keterangan.text.toString().trim()

        if(nameFile != null){
            val parcelFileDescriptor = contentResolver.openFileDescriptor(selectetImage!!, "r", null) ?: return
            val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
            val file = File(cacheDir, nameFile)
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            val body = UploadRequestBody(file, "image", this)
//            mendapatkan data lokasi

            if (longitude != null && latitude != null) {
                RetrofitClien.instance.uploadImage(
                        id_member, latitude, longitude, kab_log!!,kec_log!! ,kel_log!! ,keterangan,
                        MultipartBody.Part.createFormData(
                                "file",
                                file.name,
                                body
                        )
                ).enqueue(object : Callback<UploadRespon> {
                    override fun onResponse(call: Call<UploadRespon>, response: Response<UploadRespon>) {
                        response.body()?.let {
                            layout_root.snackbar(it.pesan)
                            loding.progress = 100
                            btn_upload.visibility = View.VISIBLE

                        }
                    }

                    override fun onFailure(call: Call<UploadRespon>, t: Throwable) {
                        layout_root.snackbar(t.message!!)
                        btn_upload.visibility = View.VISIBLE
                    }

                })
            }
        }else if(nameFile == null){
            val parcelFileDescriptor = contentResolver.openFileDescriptor(selectetImage!!, "r", null) ?: return
            val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
            val file = File(cacheDir, contentResolver.getFileName(selectetImage!!))
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)

            val body = UploadRequestBody(file, "image", this)
//            mendapatkan data lokasi

            if (longitude != null && latitude != null) {
                RetrofitClien.instance.uploadImage(
                        id_member, latitude, longitude,kab_log!!,kec_log!! ,kel_log!!, keterangan,
                        MultipartBody.Part.createFormData(
                                "file",
                                file.name,
                                body
                        )
                ).enqueue(object : Callback<UploadRespon> {
                    override fun onResponse(call: Call<UploadRespon>, response: Response<UploadRespon>) {
                        response.body()?.let {
                            layout_root.snackbar(it.pesan)
                            loding.progress = 100
                            btn_upload.visibility = View.VISIBLE
                            startActivity(Intent(baseContext, HomeActivity::class.java))
                        }
                    }

                    override fun onFailure(call: Call<UploadRespon>, t: Throwable) {
                        layout_root.snackbar(t.message!!)
                        btn_upload.visibility = View.VISIBLE
                    }

                })
            }

        }


    }

    private fun openImageChooser(){
        Intent(Intent.ACTION_PICK).also {
            it.type ="image/*"
            val mimeTypes = arrayOf("image/jpeg", "image/png")
            it.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            startActivityForResult(it, REQUEST_CODE_IMAGE_PICKER)
        }
    }



override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQUEST_CODE_IMAGE_CAMERA && resultCode == RESULT_OK) {
        val f =  File(currentPhotoPath);
        image.setImageURI(Uri.fromFile(f))
        Log.d("tag","alamat file" + f.name)

        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        this.sendBroadcast(mediaScanIntent)

        selectetImage = contentUri
        nameFile =f.name
        name_image.text = f.totalSpace.toString()

    } else if(requestCode == REQUEST_CODE_IMAGE_PICKER && resultCode == RESULT_OK){
        if (data != null)
            {
                Log.d("tag","alamat file" + data.data)
                selectetImage = data.data
                image.setImageURI(selectetImage)
                name_image.text =selectetImage.toString()
            }
    }
}

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = android.icu.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
//        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }


    private fun dispatchTakePictureIntent() {
        Toast.makeText(this,"open Camera ",Toast.LENGTH_LONG).show()
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {

                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                            this,
                            "com.ringga.android.fileprovider",
                            it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_CODE_IMAGE_CAMERA)
                }
            }
        }
    }

    override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (adapterView?.id == R.id.spiner_kabupaten) {
            kecamatanList.clear()

            val kecamatanId =  position
            dataSpiner = kabupatenListId[kecamatanId]
            kab_log= kabupatenListId[kecamatanId]

            val url = "http://risma-project.xyz/api/GetKecamatan?id_kab=$dataSpiner"
            requestQueue = Volley.newRequestQueue(this)
            val jsonObjectRequest = JsonObjectRequest(Request.Method.POST,
                    url, null, { response ->
                try {
                    val jsonArray2 = response.getJSONArray("data")

                    for (i in 0 until jsonArray2.length()) {
                        val jsonObject = jsonArray2.getJSONObject(i)
                        val kecamatan = jsonObject.optString("nama")
                        val kecId =jsonObject.optInt("id_kec")
                        kecamatanlistId.add(kecId)
                        kecamatanList.add(kecamatan)
                        KecamatanAdapter = ArrayAdapter<String>(this,
                                android.R.layout.simple_spinner_item, kecamatanList)
                        KecamatanAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spiner_kecamatan.setAdapter(KecamatanAdapter)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }) { }
            requestQueue?.run {
                add<JSONObject>(jsonObjectRequest)
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Toast.makeText(this, "data error", Toast.LENGTH_LONG).show()
    }

    private fun keluraha(dataIdkec: Int) {

        val url = "http://risma-project.xyz/api/GetKelurahan?id_kec=$dataIdkec"
        requestQueue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST,
                url, null, { response ->
            try {
                val jsondata = response.getJSONArray("data")
//                text.text= jsondata.toString()
                for (i in 0 until jsondata.length()) {
                    val jsonDataKeluraha = jsondata.getJSONObject(i)
                    val kecamatan = jsonDataKeluraha.optString("nama")
                    val kecamatanId = jsonDataKeluraha.optInt("id_kel")
                    kelurahanList.add(kecamatan)
                    kelurahanListId.add(kecamatanId)
                    kelurahanAdapter = ArrayAdapter<String>(this,
                            android.R.layout.simple_spinner_item, kelurahanList)
                    KecamatanAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spiner_kelurahan.setAdapter(kelurahanAdapter)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }) { }
        requestQueue?.run {
            add<JSONObject>(jsonObjectRequest)
        }
    }
}