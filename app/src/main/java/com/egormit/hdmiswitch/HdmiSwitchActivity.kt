package com.egormit.hdmiswitch

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast

abstract class HdmiSwitchActivity : Activity() {

    protected abstract val passthroughPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri = Uri.parse("content://android.media.tv/passthrough/$passthroughPath")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            component = ComponentName(
                "com.mitv.livetv",
                "com.mitv.livetv.tv.MainActivity",
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "Mi Live TV not found", e)
            Toast.makeText(
                this,
                getString(R.string.error_no_livetv),
                Toast.LENGTH_LONG,
            ).show()
        }
        finish()
    }

    companion object {
        private const val TAG = "HdmiSwitch"
    }
}
