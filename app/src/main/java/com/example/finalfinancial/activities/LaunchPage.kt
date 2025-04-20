package com.example.finalfinancial.activities

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.finalfinancial.R
import com.example.finalfinancial.databinding.ActivityLaunchPageBinding

class LaunchPage : AppCompatActivity() {
    lateinit var binding:ActivityLaunchPageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLaunchPageBinding.inflate(layoutInflater)
        setContentView(binding.root)


        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        binding.strtbtn.setOnClickListener{
            startActivity(Intent(this, MainActivity::class.java))
        }

    }
}