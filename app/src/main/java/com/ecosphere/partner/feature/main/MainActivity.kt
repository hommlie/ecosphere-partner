package com.ecosphere.partner.feature.main

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ecosphere.partner.R
import com.ecosphere.partner.core.common.startSlideActivity
import com.ecosphere.partner.core.datastore.SessionManager
import com.ecosphere.partner.databinding.ActivityMainBinding
import com.ecosphere.partner.feature.attendance.Attendance
import com.ecosphere.partner.feature.dashboard.Dashboard
import com.ecosphere.partner.feature.login.ui.LoginAct
import com.ecosphere.partner.feature.patients.Patients
import com.ecosphere.partner.feature.permissions.manager.PermissionGuard
import com.ecosphere.partner.feature.setting.Setting
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    @Inject
    lateinit var permissionGuard: PermissionGuard
    @Inject lateinit var sessionManager: SessionManager
    private val viewModel: MainActivityViewModel by viewModels()

    private var temp = 0
    private var insetsController: WindowInsetsControllerCompat? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left,0, systemBars.right, systemBars.bottom)
            insets
        }

        insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController?.isAppearanceLightNavigationBars = true

        observeSessionExpired()

        if (intent.getStringExtra("bottom_nav_pos")!= null) {
            val pos = intent.getStringExtra("bottom_nav_pos").toString()
            setFragment(pos.toInt())
        } else {
            setFragment(1)
        }


    }

    override fun onResume() {
        super.onResume()

        permissionGuard.check(this)
//        lifecycleScope.launch {
//            Log.d("PERMISSION", "hasActiveSession = ${sessionRestoreManager.hasActiveSession()}")
//            Log.d("TEST", "OnDuty = ${sessionManager.isOnDuty()}")
//            Log.d("TEST", "Tracking = ${trackingController.isTracking()}")
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }


    private val menuSelectedColor by lazy(LazyThreadSafetyMode.NONE) {
        ContextCompat.getColor(this, R.color.color_primary)
    }
    private val menuUnSelectedColor by lazy(LazyThreadSafetyMode.NONE) {
        ContextCompat.getColor(this, R.color.gray_Text_color_dark)
    }

    fun onClick(v: View?) {
        when (v?.id) {
            R.id.ll_dashboard -> {
                if (temp != 1) {
                    setFragment(1)
                }
            }

            R.id.ll_patient -> {
                if (temp != 2) {
                    setFragment(2)
                }
            }

            R.id.ll_consultation -> {
                if (temp != 3) {
                    setFragment(3)
                }
            }
            R.id.ll_reports -> {
                if (temp != 4) {
                    setFragment(4)
                }
            }

        }
    }
    @SuppressLint("NewApi")
    fun setFragment(pos: Int) {
        temp = pos

        binding.ivDashboard.imageTintList = ColorStateList.valueOf(menuUnSelectedColor)
        binding.ivPatient.imageTintList = ColorStateList.valueOf(menuUnSelectedColor)
        binding.ivBilling.imageTintList = ColorStateList.valueOf(menuUnSelectedColor)
        binding.ivSetting.imageTintList = ColorStateList.valueOf(menuUnSelectedColor)


        when (pos) {
            1 -> {
                insetsController?.isAppearanceLightStatusBars = true
                binding.ivDashboard.imageTintList = ColorStateList.valueOf(menuSelectedColor)
                activateDashboard()
            }

            2 -> {
                insetsController?.isAppearanceLightStatusBars = true
                binding.ivPatient.imageTintList = ColorStateList.valueOf(menuSelectedColor)
                activatePatients()
            }
            3 -> {
                insetsController?.isAppearanceLightStatusBars = true
                binding.ivBilling.imageTintList = ColorStateList.valueOf(menuSelectedColor)
                activateBilling()
            }
            4 -> {
                insetsController?.isAppearanceLightStatusBars = true
                binding.ivSetting.imageTintList = ColorStateList.valueOf(menuSelectedColor)
                activateSetting()
            }
        }
    }



    private fun switchFragment(tag: String, fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()

        val current = supportFragmentManager.primaryNavigationFragment
        if (current != null) transaction.hide(current)

        var target = supportFragmentManager.findFragmentByTag(tag)
        if (target == null) {
            target = fragment
            transaction.add(R.id.frame_container, target, tag)
        } else {
            transaction.show(target)
        }

        transaction.setPrimaryNavigationFragment(target)
        transaction.commit()
    }

    fun activateDashboard(){
        switchFragment("Home", Dashboard())
        binding.tvDashboard.setTextColor(menuSelectedColor)
        binding.tvPatient.setTextColor(menuUnSelectedColor)
        binding.tvBilling.setTextColor(menuUnSelectedColor)
        binding.tvSetting.setTextColor(menuUnSelectedColor)
    }

    fun activatePatients(){
        switchFragment("Tasks", Patients())
        binding.tvDashboard.setTextColor(menuUnSelectedColor)
        binding.tvPatient.setTextColor(menuSelectedColor)
        binding.tvBilling.setTextColor(menuUnSelectedColor)
        binding.tvSetting.setTextColor(menuUnSelectedColor)
    }

    fun activateBilling(){
        switchFragment("Attendance", Attendance())
        binding.tvDashboard.setTextColor(menuUnSelectedColor)
        binding.tvPatient.setTextColor(menuUnSelectedColor)
        binding.tvBilling.setTextColor(menuSelectedColor)
        binding.tvSetting.setTextColor(menuUnSelectedColor)
    }

    fun activateSetting(){
        switchFragment("Setting", Setting())
        binding.tvDashboard.setTextColor(menuUnSelectedColor)
        binding.tvPatient.setTextColor(menuUnSelectedColor)
        binding.tvBilling.setTextColor(menuUnSelectedColor)
        binding.tvSetting.setTextColor(menuSelectedColor)
    }

    private fun observeSessionExpired() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                sessionManager.sessionExpired.collect {

                    Toast.makeText(
                        this@MainActivity,
                        "Session expired. Please login again.",
                        Toast.LENGTH_LONG
                    ).show()

                    startSlideActivity(
                        Intent(this@MainActivity, LoginAct::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                    )
                }
            }
        }
    }

}