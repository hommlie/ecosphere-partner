package com.ecosphere.partner.feature.jobs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecosphere.partner.R
import com.ecosphere.partner.core.common.UIState
import com.ecosphere.partner.core.common.finishSlideActivity
import com.ecosphere.partner.core.common.startSlideActivity
import com.ecosphere.partner.core.datastore.SessionManager
import com.ecosphere.partner.core.util.CommonMethods
import com.ecosphere.partner.core.util.NavigationUtils
import com.ecosphere.partner.core.util.ProgressDialogUtil
import com.ecosphere.partner.databinding.ActivityJobsBinding
import com.ecosphere.partner.feature.jobs.adapter.JobsAdapter
import com.ecosphere.partner.feature.login.ui.LoginAct
import com.ecosphere.partner.feature.qrscan.QrScanner
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max

@AndroidEntryPoint
class Jobs : AppCompatActivity() {
    private lateinit var binding: ActivityJobsBinding
    private val viewModel : JobsViewModel by viewModels()
    @Inject
    lateinit var sessionManager: SessionManager

    private var title = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityJobsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right,
                maxOf( systemBars.bottom, ime.bottom))
            insets
        }

        title = intent.getStringExtra("title") ?: "Today's Pending Jobs"
        binding.tvTitle.text = title

        binding.ivBack.setOnClickListener {
            finish()
            finishSlideActivity()
        }
        onBackPressedDispatcher.addCallback(this){
            finish()
            finishSlideActivity()
        }
        binding.ivScanner.setOnClickListener {
            startSlideActivity(Intent(this, QrScanner::class.java))
        }

        observeSessionExpired()
        setupJobRecyclerView()
        observeJobsUISate()

        viewModel.getPickUps()

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.getPickUps()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.tvSeachText.addTextChangedListener {
            viewModel.filter(it.toString())
        }
    }

    private val jobsAdapter by lazy {
        JobsAdapter(
            onViewClick = {
//                startSlideActivity(Intent(this, JobDetails::class.java))
            },
            onCallClick = {
                CommonMethods.openDialPad(
                    context = this,
                    phoneNum = it.customerPhone
                )
            },
            onDirectionClick = {
                NavigationUtils.openNavigation(
                    context = this,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    mapLink = it.mapLink
                )
            }
        )
    }

    private fun setupJobRecyclerView() {

        binding.rvJobs.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = jobsAdapter
            isNestedScrollingEnabled = true
            itemAnimator = null
        }
    }
    private fun observeJobsUISate(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    viewModel.jobs.collect { state ->
                        when (state) {
                            is UIState.Idle -> {
                                // no-op
                            }

                            is UIState.Loading -> {
                                ProgressDialogUtil.showAlertLoadingProgress(
                                    this@Jobs,
                                    lifecycleScope,
                                    "Loading...",
                                    "Please wait while we are getting your pickups"
                                )
                            }

                            is UIState.Success -> {
                                ProgressDialogUtil.dismiss()
//                                binding.rvJobs.visibility = View.VISIBLE
//                                binding.llNodata.visibility = View.GONE
//                            jobsAdapter.submitList(state.data)
                            }

                            is UIState.Empty -> {
                                ProgressDialogUtil.dismiss()
                                binding.rvJobs.visibility = View.GONE
                                binding.llNodata.visibility = View.VISIBLE
                                binding.tvNodataTitle.text = "No Pickup Assigned"
                                binding.tvNodataDesc.text = "Please wait for your manager to assign pickup."
                            }

                            is UIState.Error -> {
                                ProgressDialogUtil.dismiss()
                                binding.rvJobs.visibility = View.GONE
                                binding.llNodata.visibility = View.VISIBLE
                                binding.tvNodataTitle.text = "Error !"
                                binding.tvNodataDesc.text = state.message
                            }
                        }
                    }
                }
                launch {
                    viewModel.filteredJobs.collectLatest { data ->
                        if (data.isEmpty()){
                            binding.rvJobs.visibility = View.GONE
                            binding.llNodata.visibility = View.VISIBLE
                            binding.tvNodataTitle.text = "No Pickup Assigned"
                            binding.tvNodataDesc.text = "Please wait for your manager to assign pickup."
                        }else{
                            binding.rvJobs.visibility = View.VISIBLE
                            binding.llNodata.visibility = View.GONE
                            jobsAdapter.submitList(data)
                        }
                    }
                }
            }
        }
    }

    private fun observeSessionExpired() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                sessionManager.sessionExpired.collect {

                    Toast.makeText(
                        this@Jobs,
                        "Session expired. Please login again.",
                        Toast.LENGTH_LONG
                    ).show()

                    startSlideActivity(
                        Intent(this@Jobs, LoginAct::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                    )
                }
            }
        }
    }

}