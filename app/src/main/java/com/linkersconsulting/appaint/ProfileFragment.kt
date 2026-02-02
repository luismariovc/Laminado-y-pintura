package com.linkersconsulting.appaint

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.linkersconsulting.appaint.databinding.FragmentProfileBinding
import java.io.ByteArrayOutputStream

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // Image Picker
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@registerForActivityResult

        try {
            val context = requireContext()
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Toast.makeText(context, "Error: No se pudo acceder a la imagen", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            inputStream.use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream)
                if (bitmap == null) {
                    Toast.makeText(context, "Error: No se pudo decodificar la imagen", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }

                // Compress and Encode
                saveProfileImage(bitmap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al procesar imagen: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUser()
        loadProfileImage()
        setupListeners()
    }

    private fun setupUser() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            binding.tvUserEmail.text = user.email
            // If displayName is available, use it, otherwise default
            binding.tvUserName.text = if (!user.displayName.isNullOrEmpty()) user.displayName else "Administrador"
        }
    }

    private fun setupListeners() {
        // Edit Profile Pic
        binding.profilePicContainer.setOnClickListener {
            pickImage.launch("image/*")
        }

        // Sign Out
        binding.btnSignOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            
            // Navigate to Login and clear stack
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun saveProfileImage(bitmap: Bitmap) {
        // Redimensionar si es muy grande (max 600px)
        val scaledBitmap = if (bitmap.width > 600) {
            val ratio = 600.0 / bitmap.width
            Bitmap.createScaledBitmap(bitmap, 600, (bitmap.height * ratio).toInt(), true)
        } else bitmap

        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)

        // Save to SharedPreferences
        val prefs = requireContext().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        prefs.edit().putString("profile_image_base64", base64String).apply()

        // Update UI
        updateProfileImageUI(scaledBitmap)
        Toast.makeText(context, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show()
    }

    private fun loadProfileImage() {
        val prefs = requireContext().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
        val base64String = prefs.getString("profile_image_base64", null)

        if (base64String != null) {
            try {
                val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                if (bitmap != null) {
                    updateProfileImageUI(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateProfileImageUI(bitmap: Bitmap) {
        binding.ivProfile.setImageBitmap(bitmap)
        binding.ivProfile.imageTintList = null
        binding.ivProfile.setPadding(0, 0, 0, 0)
        binding.ivProfile.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
