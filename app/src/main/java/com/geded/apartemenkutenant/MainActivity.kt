package com.geded.apartemenkutenant

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.geded.apartemenkutenant.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val fragments: ArrayList<Fragment> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        val tenant_name = shared.getString(LoginActivity.TENANTNAME, "")

        /* BottomNav */
        fragments.add(OrdersFragment())
        fragments.add(OrderHistoryFragment())
        fragments.add(StoreItemsFragment())
        fragments.add(SettingFragment())

        binding.viewPager.adapter = ViewPagerAdapter(this, fragments)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                binding.bottomNavView.selectedItemId = binding.bottomNavView.menu.getItem(position).itemId
            }
        })
        binding.bottomNavView.setOnItemSelectedListener {
            binding.viewPager.currentItem = when(it.itemId){
                R.id.itemOrders -> 0
                R.id.itemHisOrders -> 1
                R.id.itemItems -> 2
                R.id.itemSetting -> 3
                else -> 0
            }
            true
        }
    }
}