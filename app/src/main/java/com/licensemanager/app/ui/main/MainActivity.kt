package com.licensemanager.app.ui.main

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import com.licensemanager.app.LicenseManagerApp
import com.licensemanager.app.R
import com.licensemanager.app.data.remote.dto.SubscriptionData
import com.licensemanager.app.databinding.ActivityMainBinding
import com.licensemanager.app.ui.common.UiState
import com.licensemanager.app.ui.history.SubscriptionHistoryActivity
import com.licensemanager.app.ui.login.LoginActivity
import com.licensemanager.app.ui.request.RequestSubscriptionActivity
import com.licensemanager.app.util.collectLatestLifecycleFlow

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val app by lazy { application as LicenseManagerApp }

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory(app.subscriptionRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        setupToolbar()
        setupUI()
        observeState()
        viewModel.loadSubscription()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadSubscription()
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_refresh -> {
                    viewModel.loadSubscription()
                    true
                }
                R.id.action_logout -> {
                    showLogoutDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupUI() {
        val userName = app.preferenceManager.getUserName() ?: "User"
        binding.tvWelcome.text = getString(R.string.welcome_user, userName)

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadSubscription()
        }

        binding.btnRequestSubscription.setOnClickListener {
            startActivity(Intent(this, RequestSubscriptionActivity::class.java))
        }

        binding.btnDeactivate.setOnClickListener {
            showDeactivateDialog()
        }

        binding.btnViewHistory.setOnClickListener {
            startActivity(Intent(this, SubscriptionHistoryActivity::class.java))
        }
    }

    private fun observeState() {
        collectLatestLifecycleFlow(viewModel.subscriptionState) { state ->
            binding.swipeRefresh.isRefreshing = false
            when (state) {
                is UiState.Idle -> Unit

                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    if (state.data != null) showSubscription(state.data)
                    else showNoSubscription()
                }

                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    showNoSubscription()
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        collectLatestLifecycleFlow(viewModel.deactivateEvent) { result ->
            result.onSuccess { response ->
                Snackbar.make(
                    binding.root,
                    response.message ?: "Subscription deactivated",
                    Snackbar.LENGTH_LONG
                ).show()
            }
            result.onFailure { error ->
                Snackbar.make(
                    binding.root,
                    error.message ?: "Failed to deactivate",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showSubscription(subscription: SubscriptionData) {
        binding.cardSubscription.visibility = View.VISIBLE
        binding.cardNoSubscription.visibility = View.GONE
        binding.btnDeactivate.visibility = if (subscription.status == "active") View.VISIBLE else View.GONE

        binding.tvPackName.text = subscription.packName ?: "N/A"
        binding.tvPrice.text = subscription.price?.let { "$%.2f".format(it) } ?: "N/A"
        binding.tvAssigned.text = subscription.assignedAt?.take(10) ?: "N/A"
        binding.tvExpires.text = subscription.expiresAt?.take(10) ?: "N/A"

        applyStatusBadge(binding.tvStatus, subscription.status ?: "unknown")
        applyValidityBadge(binding.tvValidity, subscription.isValid ?: false)
    }

    private fun showNoSubscription() {
        binding.cardSubscription.visibility = View.GONE
        binding.cardNoSubscription.visibility = View.VISIBLE
        binding.btnDeactivate.visibility = View.GONE
    }

    private fun applyStatusBadge(view: TextView, status: String) {
        val (textColor, bgColor) = when (status.lowercase()) {
            "active" -> Pair(getColor(R.color.status_active), getColor(R.color.status_active_bg))
            "requested", "approved" -> Pair(getColor(R.color.status_requested), getColor(R.color.status_requested_bg))
            else -> Pair(getColor(R.color.status_inactive), getColor(R.color.status_inactive_bg))
        }
        view.text = status.replaceFirstChar { it.uppercase() }
        view.setTextColor(textColor)
        view.background = GradientDrawable().apply {
            setColor(bgColor)
            cornerRadius = 16f
        }
    }

    private fun applyValidityBadge(view: TextView, isValid: Boolean) {
        val (text, textColor, bgColor) = if (isValid) {
            Triple(getString(R.string.valid), getColor(R.color.status_active), getColor(R.color.status_active_bg))
        } else {
            Triple(getString(R.string.invalid), getColor(R.color.status_inactive), getColor(R.color.status_inactive_bg))
        }
        view.text = text
        view.setTextColor(textColor)
        view.background = GradientDrawable().apply {
            setColor(bgColor)
            cornerRadius = 16f
        }
    }

    private fun showDeactivateDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.deactivate_title)
            .setMessage(R.string.deactivate_message)
            .setPositiveButton(R.string.confirm) { _, _ -> viewModel.deactivateSubscription() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.logout_title)
            .setMessage(R.string.logout_message)
            .setPositiveButton(R.string.confirm) { _, _ -> logout() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun logout() {
        app.authRepository.logout()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
