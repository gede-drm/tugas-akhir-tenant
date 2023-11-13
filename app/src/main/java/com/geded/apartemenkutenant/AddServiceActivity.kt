package com.geded.apartemenkutenant

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.geded.apartemenkutenant.databinding.ActivityAddServiceBinding

class AddServiceActivity : AppCompatActivity() {
    private lateinit var binding:ActivityAddServiceBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}