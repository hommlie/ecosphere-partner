package com.ecosphere.partner.feature.patients

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecosphere.partner.core.common.startSlideActivity
import com.ecosphere.partner.core.util.KeyboardUtils
import com.ecosphere.partner.databinding.FragmentPatientsBinding
import com.ecosphere.partner.feature.patients.adapter.PatientLoadStateAdapter
import com.ecosphere.partner.feature.patients.adapter.PatientsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.getValue

@AndroidEntryPoint
class Patients : Fragment() {

    private var _binding : FragmentPatientsBinding?=null
    private val binding get() = _binding!!

    private val viewModel: PatientViewModel by viewModels()
    private lateinit var adapter: PatientsAdapter
    private var hasLoadedOnce = false
    private var shouldScroll = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPatientsBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        KeyboardUtils.setupHideKeyboardOnTouch(
            requireActivity(),
            view
        )

        binding.btnAddPatient.setOnClickListener {
            shouldScroll = true
            requireActivity().startSlideActivity(Intent(requireActivity(), AddPatient::class.java))
        }

        setupRecycler()
        setupSwipeRefresh()
        observePatients()
        handleLoadState()
    }
    private fun setupRecycler() {
        adapter = PatientsAdapter()
        binding.rvPatients.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPatients.adapter =
            adapter.withLoadStateFooter(
                footer =
                    PatientLoadStateAdapter {
                        adapter.retry()
                    }
            )
    }

    private fun observePatients() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getPatientsPager().collectLatest {
                    adapter.submitData(it)
                }
            }
        }
    }

    private fun handleLoadState() {

        adapter.addLoadStateListener { loadState ->

            val isRefreshing = loadState.source.refresh is LoadState.Loading
            val isNotLoading = loadState.source.refresh is LoadState.NotLoading
            val isError = loadState.source.refresh is LoadState.Error
            val isEmpty = isNotLoading && adapter.itemCount == 0
            val errorState = loadState.source.refresh as? LoadState.Error

            // Stop swipe refresh animation
            binding.swipeRefresh.isRefreshing = false

            // 1. Shimmer (ONLY first load)
            if (!hasLoadedOnce && isRefreshing && adapter.itemCount == 0) {
                showShimmer(true)
            } else {
                showShimmer(false)
            }

            // 2. Empty state (ONLY when truly empty)
            binding.tvNodata.visibility = if (isEmpty) View.VISIBLE else View.GONE

            // scroll to top when refresh completes
            if (isNotLoading && hasLoadedOnce && shouldScroll) {
                binding.rvPatients.post {
                    binding.rvPatients.scrollToPosition(0)
                }
                shouldScroll = false
            }

            if (isEmpty) {
                binding.tvNodataTitle.text = "No Patients Record"
                binding.tvNodataDesc.text = "There is no patients to show you right now"
            }

            // 3. Error handling (CRITICAL FIX)
            if (isError) {

                errorState?.let {

                    val message =
                        when (it.error) {
                            is UnknownHostException -> "No internet connection"
                            is SocketTimeoutException -> "Server timeout"
                            else -> it.error.localizedMessage ?: "Something went wrong"
                        }

                    // ONLY show full error screen if no data ever loaded
                    if (!hasLoadedOnce) {
                        binding.tvNodata.visibility = View.VISIBLE
                        binding.tvNodataTitle.text = "Something went wrong"
                        binding.tvNodataDesc.text = message

                    } else {
                        // If data already exists → keep showing it
                        binding.tvNodata.visibility = View.GONE
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            // 4. RecyclerView visibility (IMPORTANT FIX)
            binding.rvPatients.visibility = if (isEmpty && !hasLoadedOnce) View.GONE else View.VISIBLE

            // 5. Mark first successful load
            if (isNotLoading && adapter.itemCount > 0) {
                hasLoadedOnce = true
            }
        }
    }


    private fun showShimmer(show: Boolean) {
        if (show) {
            binding.shimmerLayout.visibility = View.VISIBLE
            binding.rvPatients.visibility = View.GONE
        } else {
            binding.shimmerLayout.visibility = View.GONE
            binding.rvPatients.visibility = View.VISIBLE
        }
    }
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            adapter.refresh()
        }
    }


    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

}