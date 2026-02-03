package com.linkersconsulting.appaint

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable Edge-to-Edge
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        
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
            val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (user != null) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }
    }
}
