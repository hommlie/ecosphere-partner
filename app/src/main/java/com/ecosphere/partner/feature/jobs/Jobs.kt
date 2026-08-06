package com.ecosphere.partner.feature.jobs

import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ecosphere.partner.R
import com.ecosphere.partner.core.common.finishSlideActivity
import com.ecosphere.partner.databinding.ActivityJobsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Jobs : AppCompatActivity() {
    private lateinit var binding: ActivityJobsBinding

    private var title = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityJobsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
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
    }
}