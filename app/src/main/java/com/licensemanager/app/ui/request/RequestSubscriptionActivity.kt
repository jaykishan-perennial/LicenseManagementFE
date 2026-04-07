package com.licensemanager.app.ui.request

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.licensemanager.app.LicenseManagerApp
import com.licensemanager.app.R
import com.licensemanager.app.databinding.ActivityRequestSubscriptionBinding
import com.licensemanager.app.ui.common.UiState
import com.licensemanager.app.util.collectLatestLifecycleFlow

class RequestSubscriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestSubscriptionBinding
    private lateinit var packAdapter: PackAdapter

    private val viewModel: RequestSubscriptionViewModel by viewModels {
        RequestSubscriptionViewModel.Factory(
            (application as LicenseManagerApp).subscriptionRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRequestSubscriptionBinding.inflate(layoutInflater)
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

        setupToolbar()
        setupRecyclerView()
        setupSearch()
        setupSubmitButton()
        observeState()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupRecyclerView() {
        packAdapter = PackAdapter { pack ->
            viewModel.selectPack(pack)
        }
        binding.rvPacks.apply {
            layoutManager = LinearLayoutManager(this@RequestSubscriptionActivity)
            adapter = packAdapter
        }
    }

    private fun setupSearch() {
        binding.etSearch.doAfterTextChanged { text ->
            viewModel.filterPacks(text?.toString() ?: "")
        }
    }

    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            viewModel.requestSubscription()
        }

        binding.btnClearSelection.setOnClickListener {
            viewModel.clearSelection()
        }
    }

    private fun observeState() {
        collectLatestLifecycleFlow(viewModel.packsState) { state ->
            when (state) {
                is UiState.Idle -> {}

                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvPacks.visibility = View.GONE
                    binding.emptyState.visibility = View.GONE
                }

                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val packs = state.data
                    if (packs.isEmpty()) {
                        binding.rvPacks.visibility = View.GONE
                        binding.emptyState.visibility = View.VISIBLE
                        val searchQuery = binding.etSearch.text?.toString()?.trim() ?: ""
                        binding.tvEmptyMessage.text = if (searchQuery.isNotEmpty()) {
                            getString(R.string.no_packs_match)
                        } else {
                            getString(R.string.no_packs_available)
                        }
                    } else {
                        binding.rvPacks.visibility = View.VISIBLE
                        binding.emptyState.visibility = View.GONE
                        packAdapter.submitList(packs)
                    }
                }

                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvPacks.visibility = View.GONE
                    binding.emptyState.visibility = View.VISIBLE
                    binding.tvEmptyMessage.text = state.message
                }
            }
        }

        collectLatestLifecycleFlow(viewModel.selectedPack) { pack ->
            if (pack != null) {
                binding.selectedPackCard.visibility = View.VISIBLE
                binding.tvSelectedPack.text = getString(R.string.selected_pack, pack.name)
                binding.btnSubmit.isEnabled = true
                packAdapter.setSelectedSku(pack.sku)
            } else {
                binding.selectedPackCard.visibility = View.GONE
                binding.btnSubmit.isEnabled = false
                packAdapter.setSelectedSku(null)
            }
        }

        collectLatestLifecycleFlow(viewModel.requestState) { state ->
            when (state) {
                is UiState.Idle -> {
                    binding.progressBar.visibility = View.GONE
                }

                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSubmit.isEnabled = false
                }

                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Snackbar.make(
                        binding.root,
                        state.data.message ?: "Subscription request submitted",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    viewModel.resetState()
                    finish()
                }

                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSubmit.isEnabled = viewModel.selectedPack.value != null
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    viewModel.resetState()
                }
            }
        }
    }
}
