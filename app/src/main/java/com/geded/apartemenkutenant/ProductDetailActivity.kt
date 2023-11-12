package com.geded.apartemenkutenant

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.geded.apartemenkutenant.databinding.ActivityProductDetailBinding

class ProductDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductDetailBinding
    companion object{
        val PRODUCT_ID = "PRODUCT_ID"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}