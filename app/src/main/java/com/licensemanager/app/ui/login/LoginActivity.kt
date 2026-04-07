package com.licensemanager.app.ui.login

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
import com.licensemanager.app.databinding.ActivityLoginBinding
import com.licensemanager.app.ui.common.UiState
import com.licensemanager.app.ui.main.MainActivity
import com.licensemanager.app.ui.register.RegisterActivity
import com.licensemanager.app.util.collectLatestLifecycleFlow

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val viewModel: LoginViewModel by viewModels {
        LoginViewModel.Factory((application as LicenseManagerApp).authRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
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

        if (viewModel.isLoggedIn) {
            navigateToMain()
            return
        }

        setupUI()
        observeState()
    }

    private fun setupUI() {
        binding.etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validateEmail()
        }
        binding.etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validatePassword()
        }

        binding.btnLogin.setOnClickListener {
            if (!validateEmail() || !validatePassword()) return@setOnClickListener
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.login(email, password)
        }

        binding.btnGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateEmail(): Boolean {
        val email = binding.etEmail.text.toString().trim()
        return when {
            email.isEmpty() -> {
                binding.tilEmail.error = "Email is required"; false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.tilEmail.error = "Enter a valid email address"; false
            }
            else -> {
                binding.tilEmail.error = null; true
            }
        }
    }

    private fun validatePassword(): Boolean {
        val password = binding.etPassword.text.toString().trim()
        return when {
            password.isEmpty() -> {
                binding.tilPassword.error = "Password is required"; false
            }
            password.length < 6 -> {
                binding.tilPassword.error = "Password must be at least 6 characters"; false
            }
            else -> {
                binding.tilPassword.error = null; true
            }
        }
    }

    private fun observeState() {
        collectLatestLifecycleFlow(viewModel.loginState) { state ->
            when (state) {
                is UiState.Idle -> setInputsEnabled(true)

                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    setInputsEnabled(false)
                }

                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    navigateToMain()
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
        binding.btnLogin.isEnabled = enabled
        binding.etEmail.isEnabled = enabled
        binding.etPassword.isEnabled = enabled
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
