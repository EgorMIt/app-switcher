package com.egormit.hdmiswitch

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log

object XiaomiCecWakeFix {
    private const val TAG = "XiaomiCecWakeFix"
    private const val TV_USER_SETUP_COMPLETE = "tv_user_setup_complete"

    fun apply(
        context: Context,
        reason: String,
    ): Boolean {
        val appContext = context.applicationContext
        if (
            appContext.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "WRITE_SECURE_SETTINGS is not granted; cannot apply fix. reason=$reason")
            return false
        }

        return try {
            Settings.Secure.putInt(
                appContext.contentResolver,
                TV_USER_SETUP_COMPLETE,
                0,
            )
            Log.i(TAG, "Applied Xiaomi CEC wake fix. reason=$reason")
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to apply Xiaomi CEC wake fix. reason=$reason", e)
            false
        }
    }
}
