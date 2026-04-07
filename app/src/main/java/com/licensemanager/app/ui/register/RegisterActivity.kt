package com.licensemanager.app.ui.register

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import com.licensemanager.app.LicenseManagerApp
import com.licensemanager.app.databinding.ActivityRegisterBinding
import com.licensemanager.app.ui.common.UiState
import com.licensemanager.app.ui.main.MainActivity
import com.licensemanager.app.util.collectLatestLifecycleFlow

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private val viewModel: RegisterViewModel by viewModels {
        RegisterViewModel.Factory((application as LicenseManagerApp).authRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                maxOf(systemBars.bottom, ime.bottom)
            )
            insets
        }

        setupUI()
        observeState()
    }

    private fun setupUI() {
        binding.etName.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) validateName() }
        binding.etEmail.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) validateEmail() }
        binding.etPassword.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) validatePassword() }
        binding.etPhone.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) validatePhone() }

        binding.btnRegister.setOnClickListener {
            if (!validateName() || !validateEmail() || !validatePassword() || !validatePhone()) return@setOnClickListener
            viewModel.register(
                binding.etName.text.toString().trim(),
                binding.etEmail.text.toString().trim(),
                binding.etPassword.text.toString().trim(),
                binding.etPhone.text.toString().trim()
            )
        }

        binding.btnGoToLogin.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun observeState() {
        collectLatestLifecycleFlow(viewModel.registerState) { state ->
            when (state) {
                is UiState.Idle -> setInputsEnabled(true)
                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    setInputsEnabled(false)
                }
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    setInputsEnabled(true)
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setInputsEnabled(enabled: Boolean) {
        binding.btnRegister.isEnabled = enabled
        binding.etName.isEnabled = enabled
        binding.etEmail.isEnabled = enabled
        binding.etPassword.isEnabled = enabled
        binding.etPhone.isEnabled = enabled
    }

    private fun validateName(): Boolean {
        val name = binding.etName.text.toString().trim()
        return when {
            name.isEmpty() -> { binding.tilName.error = "Name is required"; false }
            name.length < 2 -> { binding.tilName.error = "Name must be at least 2 characters"; false }
            else -> { binding.tilName.error = null; true }
        }
    }

    private fun validateEmail(): Boolean {
        val email = binding.etEmail.text.toString().trim()
        return when {
            email.isEmpty() -> { binding.tilEmail.error = "Email is required"; false }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.tilEmail.error = "Enter a valid email address"; false
            }
            else -> { binding.tilEmail.error = null; true }
        }
    }

    private fun validatePassword(): Boolean {
        val password = binding.etPassword.text.toString().trim()
        return when {
            password.isEmpty() -> { binding.tilPassword.error = "Password is required"; false }
            password.length < 6 -> { binding.tilPassword.error = "Password must be at least 6 characters"; false }
            else -> { binding.tilPassword.error = null; true }
        }
    }

    private fun validatePhone(): Boolean {
        val phone = binding.etPhone.text.toString().trim()
        return when {
            phone.isEmpty() -> { binding.tilPhone.error = "Phone is required"; false }
            phone.length < 7 -> { binding.tilPhone.error = "Phone must be at least 7 characters"; false }
            else -> { binding.tilPhone.error = null; true }
        }
    }
}
