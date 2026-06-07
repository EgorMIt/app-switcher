package com.egormit.hdmiswitch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class XiaomiCecWakeFixReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.i(TAG, "Received BOOT_COMPLETED")
                XiaomiCecWakeFix.apply(context, "boot_completed")
                XiaomiCecWakeFixJobService.scheduleInitialRetry(context)
            }
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                Log.i(TAG, "Received MY_PACKAGE_REPLACED")
                XiaomiCecWakeFix.apply(context, "package_replaced")
            }
        }
    }

    companion object {
        private const val TAG = "XiaomiCecWakeFixReceiver"
    }
}
