package com.ecosphere.partner.feature.splash

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.ecosphere.partner.feature.main.MainActivity
import com.ecosphere.partner.R
import com.ecosphere.partner.core.common.Constants
import com.ecosphere.partner.core.datastore.SessionManager
import com.ecosphere.partner.core.network.TokenStore
import com.ecosphere.partner.databinding.ActivitySplashBinding
import com.ecosphere.partner.feature.login.ui.LoginAct
import com.ecosphere.partner.feature.permissions.manager.PermissionManager
import com.ecosphere.partner.feature.permissions.ui.PermissionsAct
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashAct : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var permissionManager: PermissionManager
    @Inject lateinit var tokenStore: TokenStore

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Example: dark icons for Android 15+
            insetsController?.isAppearanceLightStatusBars = false
            insetsController?.isAppearanceLightNavigationBars = false
        } else {
            // Example: light icons for older versions
            insetsController?.isAppearanceLightStatusBars = false
            insetsController?.isAppearanceLightNavigationBars = false
        }

        lifecycleScope.launch {
            delay(1500)
            routeUser()
        }
    }
    private suspend fun routeUser() {

        val isLoggedIn = sessionManager.isLoggedIn()

        if (isLoggedIn) {
            initialize()
        }

        when {

            !isLoggedIn -> {
                openLogin()
            }

            !permissionManager.hasAllRequiredPermissions() || !permissionManager.isGpsEnabled() -> {
                openPermissions()
            }
            else -> {
                openMain()
            }
        }
    }
    suspend fun initialize() {

        tokenStore.update(
            sessionManager.getAccessToken()
        )
    }

    private fun openLogin() {
        startActivity(Intent(this, LoginAct::class.java))
        finishAffinity()
    }

    private fun openPermissions() {
        startActivity(Intent(this, PermissionsAct::class.java).apply {
           putExtra(Constants.EXTRA_FROM, Constants.FROM_SPLASH)
        })
        finishAffinity()
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

}