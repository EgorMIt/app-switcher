package com.egormit.hdmiswitch

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.os.PersistableBundle
import android.util.Log

class XiaomiCecWakeFixJobService : JobService() {

    override fun onStartJob(params: JobParameters): Boolean {
        val attempt = params.extras.getInt(EXTRA_ATTEMPT, 0)
        XiaomiCecWakeFix.apply(this, "boot_retry_$attempt")
        scheduleNextRetry(this, attempt + 1)
        return false
    }

    override fun onStopJob(params: JobParameters): Boolean = true

    companion object {
        private const val TAG = "XiaomiCecWakeFixJob"
        private const val JOB_ID_BASE = 42_105
        private const val EXTRA_ATTEMPT = "attempt"
        private val RETRY_DELAYS_MS = longArrayOf(
            30_000L,
            120_000L,
            300_000L,
        )

        fun scheduleInitialRetry(context: Context) {
            cancelRetries(context)
            scheduleRetry(context, 0)
        }

        private fun cancelRetries(context: Context) {
            val scheduler = context.applicationContext.getSystemService(JobScheduler::class.java)
            for (attempt in RETRY_DELAYS_MS.indices) {
                scheduler.cancel(JOB_ID_BASE + attempt)
            }
        }

        private fun scheduleNextRetry(
            context: Context,
            attempt: Int,
        ) {
            scheduleRetry(context, attempt)
        }

        private fun scheduleRetry(
            context: Context,
            attempt: Int,
        ) {
            val delay = RETRY_DELAYS_MS.getOrNull(attempt) ?: return
            val appContext = context.applicationContext
            val scheduler = appContext.getSystemService(JobScheduler::class.java)
            val jobInfo = JobInfo.Builder(
                JOB_ID_BASE + attempt,
                ComponentName(appContext, XiaomiCecWakeFixJobService::class.java),
            )
                .setMinimumLatency(delay)
                .setOverrideDeadline(delay + 30_000L)
                .setExtras(
                    PersistableBundle().apply {
                        putInt(EXTRA_ATTEMPT, attempt)
                    },
                )
                .build()
            val result = scheduler.schedule(jobInfo)
            Log.i(TAG, "Scheduled Xiaomi CEC wake fix retry. attempt=$attempt result=$result")
        }
    }
}
