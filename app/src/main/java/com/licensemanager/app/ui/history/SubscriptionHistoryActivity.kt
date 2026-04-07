package com.licensemanager.app.ui.history

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.licensemanager.app.LicenseManagerApp
import com.licensemanager.app.R
import com.licensemanager.app.databinding.ActivitySubscriptionHistoryBinding
import com.licensemanager.app.util.collectLatestLifecycleFlow

class SubscriptionHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubscriptionHistoryBinding
    private lateinit var adapter: SubscriptionHistoryAdapter

    private val viewModel: SubscriptionHistoryViewModel by viewModels {
        SubscriptionHistoryViewModel.Factory(
            (application as LicenseManagerApp).subscriptionRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySubscriptionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        setupRecyclerView()
        setupSort()
        observeState()
        viewModel.loadHistory(reset = true)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupRecyclerView() {
        adapter = SubscriptionHistoryAdapter()
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = adapter

        binding.rvHistory.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                val state = viewModel.uiState.value
                if (dy > 0 && lastVisibleItem >= totalItemCount - 3 && state.hasMore && !state.isLoading) {
                    viewModel.loadHistory()
                }
            }
        })
    }

    private fun setupSort() {
        binding.btnSort.setOnClickListener {
            viewModel.toggleSort()
        }
    }

    private fun observeState() {
        collectLatestLifecycleFlow(viewModel.uiState) { state ->
            binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

            adapter.submitList(state.items.toList())

            val showEmpty = state.items.isEmpty() && !state.isLoading
            binding.tvEmpty.visibility = if (showEmpty) View.VISIBLE else View.GONE
            binding.rvHistory.visibility = if (state.items.isEmpty()) View.GONE else View.VISIBLE

            binding.btnSort.text = if (state.sortOrder == "desc") {
                getString(R.string.sort_descending)
            } else {
                getString(R.string.sort_ascending)
            }

            state.error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }
}
