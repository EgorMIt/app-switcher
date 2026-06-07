package com.egormit.hdmiswitch

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.media.tv.TvContract
import android.media.tv.TvInputInfo
import android.media.tv.TvInputManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import kotlin.concurrent.thread

object HdmiInputSwitcher {
    private const val TAG = "HdmiInputSwitcher"
    private val mainHandler = Handler(Looper.getMainLooper())

    fun switchTo(
        context: Context,
        target: HdmiTarget,
        reason: String,
    ): Boolean {
        val appContext = context.applicationContext
        val inputInfo = findInputInfo(appContext, target)
        if (inputInfo == null) {
            Log.e(
                TAG,
                "TV input not found. target=$target reason=$reason " +
                    "available=${availableInputsLog(appContext)}",
            )
            return false
        }

        Log.i(
            TAG,
            "Switching to TV input: ${inputInfo.id}=${inputInfo.loadLabel(appContext)} " +
                "target=$target reason=$reason",
        )
        if (tryHdmiControlDeviceSelect(target, reason)) {
            return true
        }

        val uri = TvContract.buildChannelUriForPassthroughInput(inputInfo.id)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            component = ComponentName(
                "com.mitv.livetv",
                "com.mitv.livetv.tv.MainActivity",
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return startLiveTvActivity(appContext, intent, target, reason)
    }

    private fun tryHdmiControlDeviceSelect(
        target: HdmiTarget,
        reason: String,
    ): Boolean {
        val process = try {
            ProcessBuilder(
                "cmd",
                "hdmi_control",
                "deviceselect",
                target.deviceSelectLogicalAddress.toString(),
            )
                .redirectErrorStream(true)
                .start()
        } catch (e: Exception) {
            Log.w(TAG, "Cannot start hdmi_control command. target=$target reason=$reason", e)
            return false
        }

        val waiter = thread(
            name = "HdmiControlDeviceSelect",
            isDaemon = true,
        ) {
            process.waitFor()
        }

        try {
            waiter.join(HDMI_CONTROL_TIMEOUT_MS)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            process.destroy()
            return false
        }

        if (waiter.isAlive) {
            process.destroy()
            Log.w(TAG, "hdmi_control deviceselect timed out. target=$target reason=$reason")
            return false
        }

        val output = process.inputStream.bufferedReader().use { reader ->
            reader.readText().trim()
        }
        val succeeded = process.exitValue() == 0
        Log.i(
            TAG,
            "hdmi_control deviceselect finished. target=$target reason=$reason " +
                "exit=${process.exitValue()} output=$output",
        )
        return succeeded
    }

    private fun startLiveTvActivity(
        appContext: Context,
        intent: Intent,
        target: HdmiTarget,
        reason: String,
    ): Boolean {
        val overlay = showTemporaryOverlay(appContext, target, reason)
        val starter = {
            startLiveTvActivityOnce(appContext, intent, target, reason)
            Unit
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            starter()
        } else {
            mainHandler.post(starter)
        }
        mainHandler.postDelayed(
            starter,
            SECOND_ACTIVITY_START_DELAY_MS,
        )
        mainHandler.postDelayed(
            {
                overlay?.remove()
            },
            TEMPORARY_OVERLAY_LIFETIME_MS,
        )
        return true
    }

    private fun startLiveTvActivityOnce(
        appContext: Context,
        intent: Intent,
        target: HdmiTarget,
        reason: String,
    ): Boolean =
        try {
            appContext.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "Mi Live TV not found. target=$target reason=$reason", e)
            false
        } catch (e: SecurityException) {
            Log.e(TAG, "Cannot switch HDMI input. target=$target reason=$reason", e)
            false
        }

    private fun showTemporaryOverlay(
        appContext: Context,
        target: HdmiTarget,
        reason: String,
    ): TemporaryOverlay? {
        if (
            !Settings.canDrawOverlays(appContext)
        ) {
            Log.w(TAG, "SYSTEM_ALERT_WINDOW is not allowed. target=$target reason=$reason")
            return null
        }

        val windowManager = appContext.getSystemService(WindowManager::class.java)
        val view = View(appContext).apply {
            setBackgroundColor(Color.TRANSPARENT)
        }
        val layoutParams = WindowManager.LayoutParams(
            1,
            1,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply {
            alpha = 0.01f
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
        }

        return try {
            windowManager.addView(view, layoutParams)
            Log.i(TAG, "Temporary overlay added. target=$target reason=$reason")
            TemporaryOverlay(windowManager, view)
        } catch (e: Exception) {
            Log.e(TAG, "Cannot add temporary overlay. target=$target reason=$reason", e)
            null
        }
    }

    fun findInputInfo(
        context: Context,
        target: HdmiTarget,
    ): TvInputInfo? =
        tvInputManager(context).tvInputList.firstOrNull { inputInfo ->
            HdmiTarget.fromInputId(inputInfo.id) == target ||
                target.matchesLabel(inputInfo.loadLabel(context))
        }

    fun availableInputsLog(context: Context): String =
        tvInputManager(context).tvInputList.joinToString { inputInfo ->
            val label = inputInfo.loadLabel(context)
            "${inputInfo.id}=$label"
        }

    private fun tvInputManager(context: Context): TvInputManager =
        context.getSystemService(TvInputManager::class.java)

    private const val HDMI_CONTROL_TIMEOUT_MS = 700L
    private const val SECOND_ACTIVITY_START_DELAY_MS = 350L
    private const val TEMPORARY_OVERLAY_LIFETIME_MS = 2_500L
}

private data class TemporaryOverlay(
    private val windowManager: WindowManager,
    private val view: View,
) {
    fun remove() {
        try {
            windowManager.removeView(view)
        } catch (e: Exception) {
            Log.w("HdmiInputSwitcher", "Cannot remove temporary overlay", e)
        }
    }
}
