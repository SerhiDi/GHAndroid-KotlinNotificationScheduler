package com.example.trazi.notificationscheduler

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Switch
import android.app.job.JobScheduler
import android.content.Context
import android.widget.TextView
import android.widget.Toast
import android.app.job.JobInfo
import android.content.ComponentName
import android.view.View
import android.widget.RadioGroup

class MainActivity : AppCompatActivity() {
    private val JOB_ID = 0
    private var mScheduler: JobScheduler? = null

    private var mDeviceIdleSwitch: Switch? = null
    private var mDeviceChargingSwitch: Switch? = null

    private var mSeekBar: SeekBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mDeviceIdleSwitch = findViewById(R.id.idleSwitch)
        mDeviceChargingSwitch = findViewById(R.id.chargingSwitch)
        mSeekBar = findViewById(R.id.seekBar)

        val seekBarProgress = findViewById<TextView>(R.id.seekBarProgress)
        mScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        mSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (i > 0) {
                    seekBarProgress.text = getString(R.string.seconds, i)
                } else {
                    seekBarProgress.setText(R.string.not_set)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    fun scheduleJob(view: View) {
        val networkOptions = findViewById<RadioGroup>(R.id.networkOptions)

        val selectedNetworkID = networkOptions.checkedRadioButtonId

        var selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE

        val seekBarInteger = mSeekBar?.progress
        val seekBarSet = seekBarInteger!! > 0

        when (selectedNetworkID) {
            R.id.noNetwork -> selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE
            R.id.anyNetwork -> selectedNetworkOption = JobInfo.NETWORK_TYPE_ANY
            R.id.wifiNetwork -> selectedNetworkOption = JobInfo.NETWORK_TYPE_UNMETERED
        }

        val serviceName = ComponentName(
            packageName,
            NotificationJobService::class.java.name
        )
        val builder = JobInfo.Builder(JOB_ID, serviceName)
            .setRequiredNetworkType(selectedNetworkOption)
            .setRequiresDeviceIdle(mDeviceIdleSwitch!!.isChecked)
            .setRequiresCharging(mDeviceChargingSwitch!!.isChecked)

        if (seekBarSet) {
            builder.setOverrideDeadline((seekBarInteger * 1000).toLong())
        }
        val constraintSet = (selectedNetworkOption != JobInfo.NETWORK_TYPE_NONE
                || mDeviceChargingSwitch!!.isChecked
                || mDeviceIdleSwitch!!.isChecked
                || seekBarSet)

        if (constraintSet) {
            val myJobInfo = builder.build()
            mScheduler?.schedule(myJobInfo)
            Toast.makeText(this, R.string.job_scheduled, Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(
                this, R.string.no_constraint_toast,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun cancelJobs(view: View) {
        if (mScheduler != null) {
            mScheduler?.cancelAll()
            mScheduler = null
            Toast.makeText(this, R.string.jobs_canceled, Toast.LENGTH_SHORT)
                .show()
        }
    }

}
