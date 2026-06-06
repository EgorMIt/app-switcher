package com.egormit.hdmiswitch

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.media.tv.TvContract
import android.media.tv.TvInputInfo
import android.media.tv.TvInputManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast

abstract class HdmiSwitchActivity : Activity() {

    protected abstract val inputLabelAliases: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inputInfo = findInputInfo()
        if (inputInfo == null) {
            Log.e(TAG, "TV input not found. aliases=$inputLabelAliases available=${availableInputsLog()}")
            Toast.makeText(
                this,
                getString(R.string.error_no_hdmi_input),
                Toast.LENGTH_LONG,
            ).show()
            finish()
            return
        }

        Log.i(TAG, "Switching to TV input: ${inputInfo.id}=${inputInfo.loadLabel(this)}")
        val uri = TvContract.buildChannelUriForPassthroughInput(inputInfo.id)
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

    private fun findInputInfo(): TvInputInfo? {
        val aliases = inputLabelAliases.map { it.normalized() }
        return tvInputManager.tvInputList.firstOrNull { inputInfo ->
            inputInfo.loadLabel(this).toString().normalized() in aliases
        }
    }

    private fun availableInputsLog(): String =
        tvInputManager.tvInputList.joinToString { inputInfo ->
            val label = inputInfo.loadLabel(this)
            "${inputInfo.id}=$label"
        }

    private val tvInputManager: TvInputManager
        get() = getSystemService(TvInputManager::class.java)

    companion object {
        private const val TAG = "HdmiSwitch"
    }
}

private fun String.normalized(): String = trim().lowercase()
