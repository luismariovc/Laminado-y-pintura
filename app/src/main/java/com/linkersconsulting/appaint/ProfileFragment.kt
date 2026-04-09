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
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdView
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.FirebaseFirestore
import com.linkersconsulting.appaint.databinding.FragmentProfileBinding
import java.io.ByteArrayOutputStream

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var adView: AdView? = null

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
        setupAd()
    }

    private fun setupAd() {
        try {
            adView = AdManager.injectBannerAd(binding.adContainer)
        } catch (e: Exception) {
            android.util.Log.e("ProfileFragment", "Error setupAd: ${e.message}", e)
        }
    }

    override fun onResume() {
        super.onResume()
        AdManager.resumeBanner(adView)
    }

    override fun onPause() {
        super.onPause()
        AdManager.pauseBanner(adView)
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

        binding.btnDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmation()
        }
    }

    private fun showDeleteAccountConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar cuenta")
            .setMessage(
                "Se borrarán tus datos en el servidor y tu cuenta de acceso. " +
                    "Esta acción no se puede deshacer."
            )
            .setPositiveButton("Eliminar") { _, _ -> deleteAccountFromBackend() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Cumple políticas de Play Store: borrado en Firebase (Auth + datos asociados en Firestore si existen)
     * y datos locales de perfil.
     */
    private fun deleteAccountFromBackend() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "No hay sesión activa", Toast.LENGTH_SHORT).show()
            return
        }
        setAccountActionsEnabled(false)
        deleteFirestoreUserProfile(user.uid) {
            if (!isAdded) return@deleteFirestoreUserProfile
            user.delete()
                .addOnCompleteListener { task ->
                    if (!isAdded) return@addOnCompleteListener
                    setAccountActionsEnabled(true)
                    if (task.isSuccessful) {
                        onAccountDeletedSuccessfully()
                    } else {
                        val err = task.exception
                        if (err is FirebaseAuthRecentLoginRequiredException) {
                            promptReauthenticateAndDelete(user)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "No se pudo eliminar la cuenta: ${err?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
        }
    }

    private fun deleteFirestoreUserProfile(uid: String, onFinished: () -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .delete()
            .addOnCompleteListener {
                if (!isAdded) return@addOnCompleteListener
                onFinished()
            }
    }

    private fun promptReauthenticateAndDelete(user: com.google.firebase.auth.FirebaseUser) {
        val email = user.email
        if (email.isNullOrBlank()) {
            Toast.makeText(
                requireContext(),
                "No se puede verificar la cuenta. Inicia sesión de nuevo e inténtalo otra vez.",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        val input = android.widget.EditText(requireContext()).apply {
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = "Contraseña"
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Confirma tu identidad")
            .setMessage("Por seguridad, introduce tu contraseña para eliminar la cuenta.")
            .setView(input)
            .setPositiveButton("Eliminar") { _, _ ->
                val password = input.text.toString()
                if (password.isEmpty()) {
                    Toast.makeText(requireContext(), "Introduce la contraseña", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                reauthenticateAndDelete(user, email, password)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun reauthenticateAndDelete(
        user: com.google.firebase.auth.FirebaseUser,
        email: String,
        password: String
    ) {
        setAccountActionsEnabled(false)
        val credential = EmailAuthProvider.getCredential(email, password)
        user.reauthenticate(credential)
            .addOnCompleteListener { authTask ->
                if (!isAdded) return@addOnCompleteListener
                if (!authTask.isSuccessful) {
                    setAccountActionsEnabled(true)
                    Toast.makeText(
                        requireContext(),
                        "Contraseña incorrecta o sesión no válida",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addOnCompleteListener
                }
                deleteFirestoreUserProfile(user.uid) {
                    if (!isAdded) return@deleteFirestoreUserProfile
                    user.delete()
                        .addOnCompleteListener { delTask ->
                            if (!isAdded) return@addOnCompleteListener
                            setAccountActionsEnabled(true)
                            if (delTask.isSuccessful) {
                                onAccountDeletedSuccessfully()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "No se pudo eliminar: ${delTask.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }
            }
    }

    private fun setAccountActionsEnabled(enabled: Boolean) {
        binding.btnSignOut.isEnabled = enabled
        binding.btnDeleteAccount.isEnabled = enabled
    }

    private fun onAccountDeletedSuccessfully() {
        requireContext().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(requireContext(), "Cuenta eliminada", Toast.LENGTH_SHORT).show()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
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
        AdManager.destroyBanner(adView)
        super.onDestroyView()
        _binding = null
    }
}
