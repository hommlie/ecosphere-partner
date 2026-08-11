package com.ecosphere.partner.feature.login.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ecosphere.partner.R
import com.ecosphere.partner.core.common.Constants
import com.ecosphere.partner.core.common.UIState
import com.ecosphere.partner.core.common.showToast
import com.ecosphere.partner.core.datastore.SessionManager
import com.ecosphere.partner.core.datastore.UserSession
import com.ecosphere.partner.core.util.CommonMethods
import com.ecosphere.partner.core.util.KeyboardUtils
import com.ecosphere.partner.core.util.ProgressDialogUtil
import com.ecosphere.partner.databinding.ActivityLoginBinding
import com.ecosphere.partner.feature.login.model.User
import com.ecosphere.partner.feature.login.model.Vehicle
import com.ecosphere.partner.feature.main.MainActivity
import com.ecosphere.partner.feature.permissions.manager.PermissionManager
import com.ecosphere.partner.feature.permissions.ui.PermissionsAct
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.getValue
import kotlin.toString

@AndroidEntryPoint
class LoginAct : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val viewModel : LoginViewModel by viewModels()

    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var permissionManager: PermissionManager

    var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            WindowCompat.getInsetsController(window, window.decorView)?.apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        } else {
            WindowCompat.getInsetsController(window, window.decorView)?.apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        }

        KeyboardUtils.setupHideKeyboardOnTouch(this, binding.root)

        observeLoginUIState()

        binding.btnLogin.setOnClickListener {
            viewModel.loginUser()
        }

        updateLoginButtonState()


//        val fullText = binding.tvForget.text.toString()
//        val colorMap = mapOf(
//            "Reset" to ContextCompat.getColor(this, R.color.color_icon),
//            "Account" to ContextCompat.getColor(this, R.color.color_icon)
//        )
//        binding.tvForget.setColoredText(fullText, colorMap)
//
//        val wordsToBold = listOf("Login", "Signup")
//        binding.tvByclick.setBoldText(binding.tvByclick.text.toString(), wordsToBold,R.font.poppins_bold)

        val normalColor = ContextCompat.getColor(this, R.color.color_primary)

        binding.edtUserId.addTextChangedListener { editable ->
            val email = editable?.toString()?.trim().orEmpty()
            viewModel.onEmailChanged(email)

            val isValid = CommonMethods.isValidEmail(email)

            if (!isValid) {

                // error color
//                binding.edtUserId.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.color_9C1335))

            } else {
                // normal color
                binding.edtUserId.backgroundTintList = ColorStateList.valueOf(normalColor)
            }
            updateLoginButtonState()
        }


        binding.edtPassword.addTextChangedListener {
            viewModel.onPasswordChanged(it.toString().trim())

            binding.edtPassword.backgroundTintList = ColorStateList.valueOf(normalColor)

            updateLoginButtonState()
        }

//        binding.btnContactus.setOnClickListener {
//            if (!ClickGuard.isClickAllowed()) return@setOnClickListener
//            CommonMethods.openMailApps(this,"info@octor.health")
//        }


//        onBackPressedDispatcher.addCallback(this){
//            if (binding.signupCard.visibility == View.VISIBLE){
//                binding.signupCard.visibility = View.GONE
//                binding.loginCard.visibility = View.VISIBLE
//            }else{
//                finish()
//            }
//        }
//        binding.tvSignup.setOnClickListener {
//            binding.signupCard.visibility = View.VISIBLE
//            binding.loginCard.visibility = View.GONE
//        }
//        binding.btnLoginCard.setOnClickListener {
//            binding.signupCard.visibility = View.GONE
//            binding.loginCard.visibility = View.VISIBLE
//        }
//        binding.tvWebLink.setOnClickListener {
//            if (!ClickGuard.isClickAllowed()) return@setOnClickListener
//            val url = "https://octor.health/"
//            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//
//            if (intent.resolveActivity(packageManager) != null) {
//                startActivity(intent)
//            } else {
//                Toast.makeText(this, "No browser found", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        binding.btnLogin.setOnClickListener {
//            if (!ClickGuard.isClickAllowed()) return@setOnClickListener
//            val errorColor = ContextCompat.getColor(this, R.color.color_9C1335)
//
//            when {
//                viewModel.enteredEmail.value.isEmpty() -> {
//                    binding.icEmailDanger.visibility = View.VISIBLE
//                    binding.edtEmail.backgroundTintList =  ColorStateList.valueOf(errorColor)
//                    binding.tvError.text = "Email cannot be empty"
//                    binding.tvError.visibility = View.VISIBLE
//                }
//
//                !CommonMethods.isValidEmail(viewModel.enteredEmail.value) -> {
//                    binding.icEmailDanger.visibility = View.VISIBLE
//                    binding.edtEmail.backgroundTintList =  ColorStateList.valueOf(errorColor)
//                    binding.tvError.text = "Please enter a valid email address"
//                    binding.tvError.visibility = View.VISIBLE
//                }
//
//                viewModel.enteredPassword.value.isEmpty() -> {
////                    binding.icPasswordDanger.visibility = View.VISIBLE
//                    binding.edtPassword.backgroundTintList = ColorStateList.valueOf(errorColor)
//                    binding.tvError.text = "Password cannot be empty"
//                    binding.tvError.visibility = View.VISIBLE
//                }
//
//                else -> {
//                    //  All good
//                    binding.tvError.visibility = View.GONE
//                    println("firebase token: ${viewModel.firebasetoken.value}")
//                    viewModel.loginUser()
//                }
//            }
//        }
//
//        binding.tvCondition.setOnClickListener {
//            if (!ClickGuard.isClickAllowed()) return@setOnClickListener
//            val intent = Intent(this, WebActivity::class.java)
//            intent.putExtra("url","https://octor.health/terms-conditions?isMob=true")
//            startSlideActivity(intent)
//        }
//        binding.tvPrivacypolicy.setOnClickListener {
//            if (!ClickGuard.isClickAllowed()) return@setOnClickListener
//            val intent = Intent(this,WebActivity::class.java)
//            intent.putExtra("url","https://octor.health/privacy-policy?isMob=true")
//            startSlideActivity(intent)
//        }

        /*binding.tvForget.setOnClickListener {
            if (!ClickGuard.isClickAllowed()) return@setOnClickListener
            val intent = Intent(this@LoginAct,ForgetPassword::class.java)
            startSlideActivity(intent)
        }*/

    }

    private fun observeLoginUIState(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is UIState.Idle -> {
                            // no-op
                        }

                        is UIState.Loading -> {
                            ProgressDialogUtil.showLoadingProgress(this@LoginAct,lifecycleScope)
                        }

                        is UIState.Success -> {
                            ProgressDialogUtil.dismiss()

                            Log.d("LOGIN Check", "Success Called")
                            Log.d("LOGIN Check", "Data = ${state.data}")
                            Log.d("LOGIN Check", "Token = ${state.data.tokens}")
                            Log.d("LOGIN Check", "User = ${state.data.user}")

                            val tokens = state.data.tokens
                            val user = state.data.user
                            val vehicle = state.data.vehicle

                            if (tokens != null && user != null && vehicle !=null) {
                                saveSession(tokens, user, vehicle)
                                viewModel.resetUIState()
                                routeUser()
                            } else {
                                ProgressDialogUtil.dismiss()
                                showToast("Invalid login response.")
                                viewModel.resetUIState()
                            }

                        }
                        is UIState.Empty -> {

                        }

                        is UIState.Error -> {
                            Log.d("Login Check", state.message)
                            val errorColor = ContextCompat.getColor(this@LoginAct, R.color.color_9C1335)

                            if (state.message == "Error : Incorrect password."){
                                binding.edtPassword.backgroundTintList = ColorStateList.valueOf(errorColor)
                                //binding.icPasswordDanger.visibility = View.VISIBLE
                            }else{
                                binding.edtUserId.backgroundTintList = ColorStateList.valueOf(errorColor)
                            }
                            ProgressDialogUtil.dismiss()
                            viewModel.resetUIState()
                        }
                    }
                }
            }
        }
    }
    private fun updateLoginButtonState() {
        val email = viewModel.enteredEmail.value
        val password = viewModel.enteredPassword.value


        val isValid = email.isNotEmpty() &&
                password.isNotEmpty()

        if (isValid) {
            binding.btnLogin.isEnabled = true
            binding.btnLogin.alpha = 1f
        } else {
            binding.btnLogin.isEnabled = false
            binding.btnLogin.alpha = 0.6f
        }
    }

    private suspend fun saveSession(token: String, user: User, vehicle: Vehicle) {

        sessionManager.saveSession(

            UserSession(
                accessToken = token,
                driverId = user.id,
                driverName = user.name,
                driverProfile = user.profile,
                vehicleId = vehicle.vehicleId,
                vehicleNumber = vehicle.vehicleNumber,
                isLoggedIn = true
            )
        )
    }
    private fun routeUser() {

        when {

            !permissionManager.hasAllRequiredPermissions() || !permissionManager.isGpsEnabled() -> {
                openPermissions()
            }
            else -> {
                openMain()
            }
        }
    }

    private fun openPermissions() {
        startActivity(Intent(this, PermissionsAct::class.java).apply {
            putExtra(Constants.EXTRA_FROM, Constants.FROM_LOGIN)
        })
        finish()
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

}