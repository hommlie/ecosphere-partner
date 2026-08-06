package com.ecosphere.partner.feature.setting

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.ecosphere.partner.core.common.startSlideActivity
import com.ecosphere.partner.core.datastore.SessionManager
import com.ecosphere.partner.core.util.ClickGuard
import com.ecosphere.partner.core.util.CommonMethods
import com.ecosphere.partner.databinding.FragmentSettingBinding
import com.ecosphere.partner.feature.login.ui.LoginAct
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class Setting : Fragment() {

    private var _binding : FragmentSettingBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val statusBarHeight = CommonMethods.getStatusBarHeight(requireActivity())
        binding.viewStatusBar.layoutParams.height = statusBarHeight

        lifecycleScope.launch {
            val driverName = sessionManager.getDriverName()
            val vehicleNumber = sessionManager.getVehicleNumber()
            binding.tvUsername.text = if (driverName.isNullOrEmpty()) "User Name | $vehicleNumber" else "$driverName | $vehicleNumber"
        }

        binding.btnAboutus.setOnClickListener {
            if (!ClickGuard.isClickAllowed()) return@setOnClickListener
            val intent = Intent(requireContext(), PrivacyPolicy::class.java)
            intent.putExtra("Type", "About Us")
            requireContext().startSlideActivity(intent)
        }
        binding.btnTermscondition.setOnClickListener {
            if (!ClickGuard.isClickAllowed()) return@setOnClickListener
            val intent = Intent(requireContext(), PrivacyPolicy::class.java)
            intent.putExtra("Type", "Terms & Conditions")
            requireContext().startSlideActivity(intent)
        }
        binding.btnPrivacypolicy.setOnClickListener {
            if (!ClickGuard.isClickAllowed()) return@setOnClickListener
            val intent = Intent(requireContext(), PrivacyPolicy::class.java)
            intent.putExtra("Type", "Privacy Policy")
            requireContext().startSlideActivity(intent)
        }

        binding.ivLogout.setOnClickListener {

            CommonMethods.showConfirmationDialog(
                context = requireContext(),
                title = "Logout",
                message = "Do you want to logout?",
                positiveText = "Logout",
                negativeText = "Stay",
                onConfirm = {
                    logout()
                }
            )
        }
        binding.btnLogout.setOnClickListener {

            CommonMethods.showConfirmationDialog(
                context = requireContext(),
                title = "Logout",
                message = "Do you want to logout?",
                positiveText = "Logout",
                negativeText = "Stay",
                onConfirm = {
                    logout()
                }
            )
        }

    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    private fun logout() {
        lifecycleScope.launch {

            if (sessionManager.isOnDuty()) {

                CommonMethods.showConfirmationDialog(
                    context = requireActivity(),
                    title = "Alert !",
                    message = "You can't logout until you are off duty.",
                    showNegativeButton = false,
                    positiveText = "OK",
                    onConfirm = {

                    }
                )
                return@launch

            }
            sessionManager.clearSession()

            val intent = Intent(
                requireContext(),
                LoginAct::class.java
            ).apply {

                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            }

            startActivity(intent)
            requireActivity().finish()

        }
    }

}