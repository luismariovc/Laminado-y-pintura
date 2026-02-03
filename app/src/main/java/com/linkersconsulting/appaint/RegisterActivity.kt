package com.linkersconsulting.appaint

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.linkersconsulting.appaint.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable Edge-to-Edge
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupListeners()
        setupHeaderImage()
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
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(email, password)
        }

        binding.tvLogin.setOnClickListener {
            finish() // Go back to Login Activity
        }
    }

    private fun registerUser(email: String, posted: String) {
        auth.createUserWithEmailAndPassword(email, posted)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                    // Navigate to Main Activity or back to Login
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity() // Clear activity stack
                } else {
                    Toast.makeText(baseContext, "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
}
