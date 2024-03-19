package com.denicks21.recorder

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {
    enum class Status { Nothing, Recording, Playing }

    private lateinit var startTV: TextView
    private lateinit var stopTV: TextView
    private lateinit var playTV: TextView
    private lateinit var stopPlayTV: TextView
    private lateinit var statusTV: TextView
    private var mRecorder: MediaRecorder? = null
    private var mPlayer: MediaPlayer? = null
    private var status: Status = Status.Nothing
    private var mFileName: File? = null
    private var buttonAnimation: Animation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusTV = findViewById(R.id.idTVstatus)
        startTV = findViewById(R.id.btnRecord)
        stopTV = findViewById(R.id.btnStop)
        playTV = findViewById(R.id.btnPlay)
        stopPlayTV = findViewById(R.id.btnStopPlay)

        startTV.setOnClickListener {
            startRecording()
        }

        stopTV.setOnClickListener {
            pauseRecording()
        }

        playTV.setOnClickListener {
            playAudio()
        }

        stopPlayTV.setOnClickListener {
            pausePlaying()
        }

        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation)
    }

    private fun startRecording() {
        startTV.startAnimation(buttonAnimation)
        if (checkPermissions()) {
            if (status == Status.Recording) {
                Toast.makeText(applicationContext, "Already recording", Toast.LENGTH_SHORT).show()
                return
            }

            mFileName = File(getExternalFilesDir("")?.absolutePath,"Record.3gp")

            var n = 0
            while (mFileName!!.exists()) {
                n++
                mFileName = File(getExternalFilesDir("")?.absolutePath,"Record$n.3gp")
            }

            mRecorder = MediaRecorder()
            mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mRecorder!!.setOutputFile(mFileName)

            try {
                mRecorder!!.prepare()
            } catch (e: IOException) {
                Log.e("TAG", "prepare() failed")
            }

            mRecorder!!.start()
            status = Status.Recording
            statusTV.text = "Recording in progress"
        } else {
            requestPermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_AUDIO_PERMISSION_CODE -> if (grantResults.isNotEmpty()) {
                val permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (permissionToRecord && permissionToStore) {
                    Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkPermissions(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(applicationContext, permission.WRITE_EXTERNAL_STORAGE)
        val result1 = ContextCompat.checkSelfPermission(applicationContext, permission.RECORD_AUDIO)
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this,
            arrayOf(permission.RECORD_AUDIO, permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_AUDIO_PERMISSION_CODE)
    }

    fun playAudio() {
        playTV.startAnimation(buttonAnimation)
        mPlayer = MediaPlayer()

        if (mFileName == null){
            Toast.makeText(applicationContext, "Nothing has been recorded", Toast.LENGTH_SHORT).show()
        }

        if (status == Status.Playing) {
            Toast.makeText(applicationContext, "Already playing", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            mPlayer!!.setDataSource(mFileName.toString())
            mPlayer!!.setOnCompletionListener {
                statusTV.text = "Recording ended"
                status = Status.Nothing
                mPlayer = null
            }
            mPlayer!!.prepare()

            mPlayer!!.start()
            statusTV.text = "Listening recording"
        } catch (e: IOException) {
            Log.e("TAG", "prepare() failed")
        }
    }

    fun pauseRecording() {
        stopTV.startAnimation(buttonAnimation)
        if (status != Status.Recording) {
            Toast.makeText(applicationContext, "Registration not started", Toast.LENGTH_SHORT).show()
            return
        }

        mRecorder!!.stop()
        status = Status.Nothing

        val savedUri = Uri.fromFile(mFileName)
        val msg = "File saved: " + savedUri!!.lastPathSegment
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()

        // Release the class mRecorder
        mRecorder!!.release()
        statusTV.text = "Recording interrupted"
    }

    @SuppressLint("SetTextI18n")
    private fun pausePlaying() {
        stopPlayTV.startAnimation(buttonAnimation)
        if (mPlayer != null && mPlayer!!.isPlaying) {
            mPlayer!!.stop()
            mPlayer!!.release()
            statusTV.text = "Recording stopped"
        }
        else {
            Toast.makeText(applicationContext, "Nothing is playing", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val REQUEST_AUDIO_PERMISSION_CODE = 1
    }
}