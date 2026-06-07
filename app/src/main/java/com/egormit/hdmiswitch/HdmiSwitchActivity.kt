package com.egormit.hdmiswitch

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast

abstract class HdmiSwitchActivity : Activity() {

    protected abstract val target: HdmiTarget

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        XiaomiCecWakeFix.apply(this, "activity_start")

        val inputInfo = HdmiInputSwitcher.findInputInfo(this, target)
        if (inputInfo == null) {
            Log.e(
                TAG,
                "TV input not found. target=$target available=${HdmiInputSwitcher.availableInputsLog(this)}",
            )
            Toast.makeText(
                this,
                getString(R.string.error_no_hdmi_input),
                Toast.LENGTH_LONG,
            ).show()
            finish()
            return
        }

        if (!HdmiInputSwitcher.switchTo(this, target, "activity_start")) {
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
