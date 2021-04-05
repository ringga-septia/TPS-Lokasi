package com.ringga.tps_lokasi.db

import android.content.Context
import android.content.SharedPreferences
import com.ringga.tps_lokasi.model.DataUser

class SharedPrefManager private constructor(mCtx: Context) {
    private val mCtx: Context
    fun saveUser(user: DataUser) {
        val sharedPreferences: SharedPreferences = mCtx.getSharedPreferences(
            SHARED_PREF_NAME,
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.putInt("id_member", user.id_member)
        editor.putString("nama_lengkap", user.nama_lengkap)
        editor.putString("username", user.username)
        editor.putString("email", user.email)
        editor.apply()
    }
    val isLoggedIn: Boolean
        get() {
            val sharedPreferences: SharedPreferences = mCtx.getSharedPreferences(
                SHARED_PREF_NAME,
                Context.MODE_PRIVATE
            )
            return sharedPreferences.getInt("id_member", -1) != -1
        }

    val user: DataUser
        get() {
            val sharedPreferences: SharedPreferences = mCtx.getSharedPreferences(
                SHARED_PREF_NAME,
                Context.MODE_PRIVATE
            )
            return DataUser(
                sharedPreferences.getInt("id_member", -1),
                sharedPreferences.getString("nama_lengkap", null)!!,
                sharedPreferences.getString("username", null)!!,
                sharedPreferences.getString("email", null)!!
            )
        }

    fun clear() {
        val sharedPreferences: SharedPreferences = mCtx.getSharedPreferences(
            SHARED_PREF_NAME,
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    companion object {
        private const val SHARED_PREF_NAME = "my_shared_preff"
        private var mInstance: SharedPrefManager? = null
        @Synchronized
        fun getInstance(mCtx: Context): SharedPrefManager? {
            if (mInstance == null) {
                mInstance = SharedPrefManager(mCtx)
            }
            return mInstance as SharedPrefManager
        }
    }

    init {
        this.mCtx = mCtx
    }
}