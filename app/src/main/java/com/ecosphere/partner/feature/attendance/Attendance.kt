package com.ecosphere.partner.feature.attendance

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecosphere.partner.core.common.UIState
import com.ecosphere.partner.core.common.startSlideActivity
import com.ecosphere.partner.core.common.toKm
import com.ecosphere.partner.core.datastore.SessionManager
import com.ecosphere.partner.core.util.CommonMethods
import com.ecosphere.partner.core.util.DateTimeUtils.toTime12Hour
import com.ecosphere.partner.core.util.ProgressDialogUtil
import com.ecosphere.partner.databinding.FragmentAttendanceBinding
import com.ecosphere.partner.feature.attendance.AttendanceMap
import com.ecosphere.partner.feature.tracking.model.TrackingStatus
import com.ecosphere.partner.feature.attendance.adapter.AttendanceSummaryAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class Attendance : Fragment() {
    private var _binding: FragmentAttendanceBinding? = null
    private val binding get() = _binding!!

    private val viewModel : AttendanceViewModel by viewModels()
    @Inject lateinit var sessionManager: SessionManager

    var selectedDate = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val statusBarHeight = CommonMethods.getStatusBarHeight(requireActivity())
        binding.viewStatusBar.layoutParams.height = statusBarHeight

        observeAttendanceState()
        setupDatePicker()
        setupRecycler()
        observeTripUIState()

        selectedDate = System.currentTimeMillis()

        binding.tvDate.text = SimpleDateFormat(
            "EEE, dd MMM yyyy",
            Locale.getDefault()
        ).format(Date(selectedDate))

        viewModel.getTrackingSessions(
            date = selectedDate
        )
        binding.ivRefresh.setOnClickListener {
            viewModel.getTrackingSessions(
                date = selectedDate
            )
        }

    }

    private fun setupDatePicker() {

        binding.ivCalender.setOnClickListener {

            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->

                    val selectedCalendar = Calendar.getInstance().apply {

                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, day)

                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    selectedDate = selectedCalendar.timeInMillis

                    binding.tvDate.text = SimpleDateFormat(
                        "EEE, dd MMM yyyy",
                        Locale.getDefault()
                    ).format(selectedCalendar.time)

                    viewModel.getTrackingSessions(
                        date = selectedDate
                    )
                },

                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)

            ).apply {
                datePicker.maxDate = calendar.timeInMillis
                show()
            }
        }
    }

    private val attendanceSummaryAdapter by lazy {
        AttendanceSummaryAdapter(
            onSessionClick = {
                requireActivity().startSlideActivity(Intent(requireContext(), AttendanceMap::class.java).apply {
                    putExtra(AttendanceMap.EXTRA_TRACKING_SESSION,it)
                })
            }
        )
    }

    private fun setupRecycler() {

        binding.rvSessions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = attendanceSummaryAdapter
            isNestedScrollingEnabled = false
            itemAnimator = null
        }
    }

    private fun observeTripUIState() {

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.uiState.collect { state ->
                    when (state) {
                        is UIState.Idle -> {}

                        is UIState.Loading -> {
                            ProgressDialogUtil.showAlertLoadingProgress(requireActivity(),lifecycleScope,"Loading...","Please wait while we are fetching your trip details")
                        }
                        is UIState.Success -> {
                            ProgressDialogUtil.dismiss()

                            binding.rvSessions.visibility = View.VISIBLE
                            binding.llNodata.visibility = View.GONE

                            val uiList = state.data
                                .reversed()
                                .mapIndexed { index, item ->
                                    item.copy(
                                        sessionNumber = state.data.size - index
                                    )
                                }
                            attendanceSummaryAdapter.submitList(uiList)

                            // First punch in (chronologically first session)
                            val firstSession = state.data.first()

                            // Check if any session is still running
                            val hasRunningSession =
                                state.data.any {
                                    it.status == TrackingStatus.STARTED.name
                                }

                            // Last completed session
                            val lastCompletedSession =
                                state.data
                                    .filter {
                                        it.status == TrackingStatus.STOPPED.name
                                    }
                                    .lastOrNull()

                            binding.tvFirstPunchTime.text = firstSession.startTime.toTime12Hour()


                            if (hasRunningSession) {
                                binding.tvLastPunchTime.text = "- -"
                                binding.liveIndicatorDutyStatus.start()
                                binding.liveIndicator.start()
                            } else {
                                binding.tvLastPunchTime.text =
                                    lastCompletedSession?.endTime?.toTime12Hour() ?: "- -"
                                binding.liveIndicatorDutyStatus.stop()
                                binding.liveIndicator.stop()
                            }

                            binding.tvTotalDistance.text = uiList.sumOf { it.totalDistance.toDouble() }.toFloat().toKm()
                            binding.tvTotalSessionCount.text = uiList.size.toString()
                        }
                        is UIState.Error -> {
                            ProgressDialogUtil.dismiss()

                            binding.liveIndicatorDutyStatus.stop()
                            binding.liveIndicator.stop()

                            binding.tvNodataTitle.text = "Error !"
                            binding.tvNodataDesc.text = state.message

                            binding.tvFirstPunchTime.text = "- -"
                            binding.tvLastPunchTime.text = "- -"
                            binding.tvTotalDistance.text = "- -"
                            binding.tvTotalSessionCount.text = "- -"
                        }
                        is UIState.Empty -> {
                            ProgressDialogUtil.dismiss()

                            binding.liveIndicatorDutyStatus.stop()
                            binding.liveIndicator.stop()

                            binding.tvFirstPunchTime.text = "- -"
                            binding.tvLastPunchTime.text = "- -"
                            binding.tvTotalDistance.text = "0"
                            binding.tvTotalSessionCount.text = "0"

                            binding.rvSessions.visibility = View.GONE
                            binding.llNodata.visibility = View.VISIBLE
                            binding.tvNodataTitle.text = "No Sessions Found"
                            binding.tvNodataDesc.text = "No tracking sessions are available for the selected date. ${binding.tvDate.text}"
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

                    if (isOnDuty){
                        binding.liveIndicatorDutyStatus.start()
                        binding.liveIndicator.start()

                        binding.tvDutyStatus.text = "On Duty"
                        binding.tvLive.text = "Live Tracking Active"
                    }else{
                        binding.liveIndicatorDutyStatus.stop()
                        binding.liveIndicator.stop()

                        binding.tvDutyStatus.text = "Off Duty"
                        binding.tvLive.text = "Live Tracking Stopped"
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}