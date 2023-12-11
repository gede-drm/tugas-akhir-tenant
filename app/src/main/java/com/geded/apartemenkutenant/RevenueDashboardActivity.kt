package com.geded.apartemenkutenant

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenkutenant.databinding.ActivityRevenueDashboardBinding
import org.json.JSONObject

class RevenueDashboardActivity : AppCompatActivity() {
    private lateinit var binding:ActivityRevenueDashboardBinding
    var tenant_id = 0
    var token = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRevenueDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        tenant_id = shared.getInt(LoginActivity.TENANTID,0)
        token = shared.getString(LoginActivity.TOKEN.toString(),"").toString()

        binding.cardTotalRev.isVisible = false
        binding.cardMonthSold.isVisible = false
        binding.cardMonthRev.isVisible = false
        binding.cardTodaySold.isVisible = false
        binding.cardTodayRev.isVisible = false
    }

    override fun onResume() {
        super.onResume()
        binding.cardTotalRev.isVisible = false
        binding.cardMonthSold.isVisible = false
        binding.cardMonthRev.isVisible = false
        binding.cardTodaySold.isVisible = false
        binding.cardTodayRev.isVisible = false

        getData()
    }

    fun getData(){
        val q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "revenuesummary"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                val obj = JSONObject(it)
                if(obj.getString("status")=="success") {
                    val totalRev = Helper.formatter(obj.getDouble("total_revenue"))
                    val monthRev = Helper.formatter(obj.getDouble("month_revenue"))
                    val todayRev = Helper.formatter(obj.getDouble("today_revenue"))
                    val monthSold = obj.getInt("month_sold")
                    val todaySold = obj.getInt("today_sold")

                    binding.txtTotalRevenue.text = "Rp$totalRev"
                    binding.txtMonthRevenue.text = "Rp$monthRev"
                    binding.txtTodayRevenue.text = "Rp$todayRev"

                    binding.txtMonthSold.text = monthSold.toString()
                    binding.txtTodaySold.text = todaySold.toString()

                    binding.cardTotalRev.isVisible = true
                    binding.cardMonthSold.isVisible = true
                    binding.cardMonthRev.isVisible = true
                    binding.cardTodaySold.isVisible = true
                    binding.cardTodayRev.isVisible = true
                } else{
                    val builder = AlertDialog.Builder(this)
                    builder.setCancelable(false)
                    builder.setTitle("Terjadi Masalah")
                    builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                    builder.setPositiveButton("OK"){dialog, which->
                        finish()
                    }
                    builder.create().show()
                }
            },
            Response.ErrorListener {
                val builder = AlertDialog.Builder(this)
                builder.setCancelable(false)
                builder.setTitle("Terjadi Masalah")
                builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                builder.setPositiveButton("OK"){dialog, which->
                    finish()
                }
                builder.create().show()
            }){
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["tenant_id"] = tenant_id.toString()
                params["token"] = token.toString()
                return params
            }
        }
        stringRequest.setShouldCache(false)
        q.add(stringRequest)
    }
}