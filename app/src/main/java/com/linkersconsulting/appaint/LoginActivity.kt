package com.linkersconsulting.appaint

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdView
import com.google.firebase.auth.FirebaseAuth
import com.linkersconsulting.appaint.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private var adView: AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable Edge-to-Edge
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        AdManager.initialize(this)
        setupListeners()
        setupHeaderImage()
        setupAd()
    }

    private fun setupAd() {
        try {
            adView = AdManager.injectBannerAd(binding.adContainer)
        } catch (e: Exception) {
            android.util.Log.e("LoginActivity", "Error setupAd: ${e.message}", e)
        }
    }

    override fun onPause() {
        super.onPause()
        AdManager.pauseBanner(adView)
    }

    override fun onResume() {
        super.onResume()
        AdManager.resumeBanner(adView)
    }

    override fun onDestroy() {
        AdManager.destroyBanner(adView)
        super.onDestroy()
    }

    private fun setupHeaderImage() {
        // Post to view queue to ensure dimensions are available
        binding.imgHeader.post {
            try {
                val imgHeader = binding.imgHeader
                val drawable = imgHeader.drawable ?: return@post
                
                val viewWidth = imgHeader.width
                val viewHeight = imgHeader.height
                
                if (viewWidth == 0 || viewHeight == 0) return@post

                val imageWidth = drawable.intrinsicWidth
                val imageHeight = drawable.intrinsicHeight

                val scale: Float = if (imageWidth * viewHeight > viewWidth * imageHeight) {
                    viewHeight.toFloat() / imageHeight.toFloat()
                } else {
                    viewWidth.toFloat() / imageWidth.toFloat()
                }

                val matrix = android.graphics.Matrix()
                matrix.postScale(scale, scale)
                
                // Align Bottom:
                // The scaled image height will be >= view height.
                // We want y-translation such that the bottom of image matches bottom of view.
                val scaledHeight = imageHeight * scale
                val dy = viewHeight - scaledHeight
                
                // Center Horizontally
                val scaledWidth = imageWidth * scale
                val dx = (viewWidth - scaledWidth) / 2

                matrix.postTranslate(dx, dy)
                
                imgHeader.scaleType = android.widget.ImageView.ScaleType.MATRIX
                imgHeader.imageMatrix = matrix
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                resetPassword(email)
            } else {
                Toast.makeText(this, "Please enter your email to reset password", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }


    }

    private fun loginUser(email: String, posted: String) {
        auth.signInWithEmailAndPassword(email, posted)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    Toast.makeText(this, "Authentication success: ${user?.email}", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to send reset email", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
