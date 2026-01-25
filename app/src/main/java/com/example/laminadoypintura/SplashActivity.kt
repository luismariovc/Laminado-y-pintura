package com.example.laminadoypintura

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val videoView = findViewById<VideoView>(R.id.vvSplashVideo)
        
        // Path to the video in res/raw
        val videoPath = "android.resource://$packageName/${R.raw.splash_video}"
        
        try {
            val uri = Uri.parse(videoPath)
            videoView.setVideoURI(uri)

            // Transition when video finishes
            videoView.setOnCompletionListener {
                goToMainActivity()
            }

            // Fallback if video error (e.g. file missing/corrupt)
            videoView.setOnErrorListener { _, _, _ ->
                goToMainActivity()
                true // Handled
            }

            videoView.start()
        } catch (e: Exception) {
            // Absolute fallback
            goToMainActivity()
        }
    }

    private fun goToMainActivity() {
        if (!isFinishing) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
