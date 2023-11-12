package com.geded.apartemenkutenant

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.geded.apartemenkutenant.databinding.ActivityServiceDetailBinding

class ServiceDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityServiceDetailBinding
    companion object{
        val SERVICE_ID = "SERVICE_ID"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServiceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}