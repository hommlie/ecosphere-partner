package com.ecosphere.partner.feature.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.ecosphere.partner.R
import com.ecosphere.partner.core.common.UIState
import com.ecosphere.partner.core.datastore.SessionManager
import com.ecosphere.partner.core.datastore.UserSession
import com.ecosphere.partner.core.util.CommonMethods
import com.ecosphere.partner.core.util.ProgressDialogUtil
import com.ecosphere.partner.databinding.FragmentDashboardBinding
import android.graphics.drawable.Drawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.ecosphere.partner.core.common.startSlideActivity
import com.ecosphere.partner.feature.jobs.Jobs
import com.ecosphere.partner.feature.tracking.controller.TrackingController
import com.ecosphere.partner.feature.tracking.recovery.SessionRestoreManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class Dashboard : Fragment() {

    private var _binding : FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()

    @Inject
    lateinit var trackingController: TrackingController
    @Inject
    lateinit var sessionManager: SessionManager
    @Inject
    lateinit var sessionRestoreManager: SessionRestoreManager
    private var userData : UserSession?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashboardBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val statusBarHeight = CommonMethods.getStatusBarHeight(requireActivity())
        binding.viewStatusBar.layoutParams.height = statusBarHeight

        observeUi()
        observeAttendanceState()
        observeTracking()

        binding.tvGreeting.text = CommonMethods.getGreetingMessage()
        lifecycleScope.launch {
            userData = sessionManager.getSession()
            setUpNameAndProfile(userData)
        }

        binding.dutyToggle.setOnDutyChangedListener { isOn ->
            viewModel.onDutyToggleChanged(isOn)
        }

        binding.mcvTotalPickup.setOnClickListener {
//            if (sharePreference.getString(PrefKeys.Punch_Status) == "1") {
            val intent = Intent(requireActivity(), Jobs::class.java)
            intent.putExtra("title","Today's Total Pickup")
            requireContext().startSlideActivity(intent)
//            }else{
//                CommonMethods.alertErrorOrValidationDialog(requireActivity(),"Please punch-in to explore jobs")
//            }
        }
        binding.mcvPendingPickup.setOnClickListener {
//            if (sharePreference.getString(PrefKeys.Punch_Status) == "1") {
            val intent = Intent(requireActivity(),Jobs::class.java)
            intent.putExtra("title","Today's Pending Pickup")
            requireContext().startSlideActivity(intent)
//            }else{
//                CommonMethods.alertErrorOrValidationDialog(requireActivity(),"Please punch-in to explore jobs")
//            }
        }
        binding.mcvCompletedPickup.setOnClickListener {
            val intent = Intent(requireActivity(),Jobs::class.java)
            intent.putExtra("title","Today's Completed Pickup")
            requireContext().startSlideActivity(intent)
        }

    }

    private fun setUpNameAndProfile(userData: UserSession?) {
        val userName = userData?.driverName?.trim()
        val userProfile = userData?.driverProfile

        binding.tvUserName.text =
            if (userName.isNullOrBlank()) "Guest User" else userName

        if (userProfile.isNullOrBlank()) {

            binding.ivProfile.visibility = View.GONE
            binding.tvShortName.visibility = View.VISIBLE

            binding.tvShortName.text =
                if (userName.isNullOrBlank()) {
                    "GU"
                } else {
                    getInitials(userName)
                }

        } else {

            binding.ivProfile.visibility = View.VISIBLE
            binding.tvShortName.visibility = View.GONE

            Glide.with(binding.ivProfile)
                .load(userProfile)
                .placeholder(R.drawable.ic_placeholder_profile)
                .error(R.drawable.ic_placeholder_profile)
                .listener(object : RequestListener<Drawable> {

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {

                        binding.ivProfile.visibility = View.GONE
                        binding.tvShortName.visibility = View.VISIBLE
                        binding.tvShortName.text =
                            if (userName.isNullOrBlank()) "GU" else getInitials(userName)

                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean = false
                })
                .into(binding.ivProfile)
        }
    }

    private fun observeUi() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {

                    viewModel.punchInState.collect { state ->

                        when (state) {

                            UIState.Loading -> {
                                ProgressDialogUtil.showAlertLoadingProgress(requireActivity(),lifecycleScope,"Loading...","Please wait while we are checking your attendance.")
                            }

                            is UIState.Success -> {

                                try {
                                    trackingController.start()
                                    sessionManager.setOnDuty(true)
                                    Toast.makeText(
                                        requireContext(),
                                        "Punch In Successful",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        requireContext(),
                                        e.message?: "Unable to start tracking.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }finally {
                                    ProgressDialogUtil.dismiss()
                                    viewModel.clearPunchInState()
                                }

                            }

                            is UIState.Error -> {
                                ProgressDialogUtil.dismiss()
                                Toast.makeText(
                                    requireContext(),
                                    state.message,
                                    Toast.LENGTH_SHORT
                                ).show()

                                viewModel.clearPunchInState()

                            }
                            is UIState.Empty -> {}
                            is UIState.Idle -> {}
                            null -> Unit
                        }

                    }

                }

                launch {

                    viewModel.punchOutState.collect { state ->

                        when (state) {

                            is UIState.Idle -> {}

                            UIState.Loading -> {
                                ProgressDialogUtil.showAlertLoadingProgress(requireActivity(),lifecycleScope,"Loading...","Please wait while we are checking your attendance.")
                            }

                            is UIState.Success -> {

                                try {

                                    sessionManager.setOnDuty(false)
                                    trackingController.stop()

                                    Toast.makeText(
                                        requireContext(),
                                        "Punch Out Successful",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }catch (e : Exception) {

                                    Toast.makeText(
                                        requireContext(),
                                        e.message ?: "Unable to stop tracking.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }finally {
                                    ProgressDialogUtil.dismiss()
                                    viewModel.clearPunchOutState()
                                }


                            }

                            is UIState.Error -> {
                                ProgressDialogUtil.dismiss()
                                Toast.makeText(
                                    requireContext(),
                                    state.message,
                                    Toast.LENGTH_SHORT
                                ).show()

                                viewModel.clearPunchOutState()

                            }
                            is UIState.Empty -> {}

                            null -> Unit
                        }

                    }
                }
            }
        }
    }

    private fun observeAttendanceState() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                sessionManager.observeOnDuty().collect { isOnDuty ->

                    binding.dutyToggle.setChecked(isOnDuty)
                    if (isOnDuty){
                        binding.tvDutyStatus.text = "You're On Duty"
                        binding.tvShiftStatus.text = "On Shift"
                    }else{
                        binding.tvDutyStatus.text = "You're Off Duty"
                        binding.tvShiftStatus.text = "Off Shift"
                    }
                }
            }
        }
    }

    private fun observeTracking() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.trackingUiState.collect { state ->

                    binding.tvTotalDistance.text = state.distanceText
                    binding.tvCurrentSpeed.text = state.speedText
                }
            }
        }
    }

    private fun getInitials(name: String): String {
        return name.trim()
            .split("\\s+".toRegex())
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}