package com.geded.apartemenkutenant

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenkutenant.databinding.ActivityServiceDetailBinding
import com.squareup.picasso.Picasso
import org.json.JSONObject

class ServiceDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityServiceDetailBinding
    val SERVICE_ID = "SERVICE_ID"
    var token = ""
    var service_id = 0
    var availability = false
    companion object{
        val SERVICE_ID = "SERVICE_ID"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServiceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        service_id = intent.getIntExtra(ServiceDetailActivity.SERVICE_ID, 0)
        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        token = shared.getString(LoginActivity.TOKEN.toString(),"").toString()

        getServiceDetail()

        binding.switchSvcAvailability.setOnClickListener {
            changeAvailability()
        }
        binding.btnEditSvc.setOnClickListener {
            val intent = Intent(this, EditServiceActivity::class.java)
            intent.putExtra(EditServiceActivity.SERVICE_ID, service_id)
            startActivity(intent)
        }
        binding.btnDelSvc.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setCancelable(false)
            builder.setTitle("Hapus Layanan")
            builder.setMessage("Apakah Anda Yakin untuk Menghapus Layanan Ini?")
            builder.setPositiveButton("HAPUS"){dialog, which->
                deleteService()
            }
            builder.setNegativeButton("BATAL"){ dialog, which->
            }
            builder.create().show()
        }
    }

    override fun onResume() {
        super.onResume()
        getServiceDetail()
    }

    private fun getServiceDetail(){
        var q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "getservicedetail"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener {
                Log.d("VOLLEY", it)
                val obj = JSONObject(it)
                if (obj.getString("status") == "success") {
                    val svcObj = obj.getJSONObject("data")
                    val url = svcObj.getString("photo_url")
                    val price = Helper.formatter(svcObj.getDouble("price"))
                    var permitNeed = ""
                    if(svcObj.getInt("permit_need") == 0){
                        permitNeed = "Perlu"
                    }
                    else{
                        permitNeed = "Tidak Perlu"
                    }
                    availability = svcObj.getInt("availability") != 0
                    Picasso.get().load(url).into(binding.imgViewSvcPhotoSD)
                    binding.txtSvcNameSD.text = svcObj.getString("name")
                    binding.txtSvcPriceSD.text = "Rp$price"
                    binding.txtSvcPermitNeedSD.text = "Perizinan: $permitNeed"
                    binding.txtSvcRatingPD.text = svcObj.getDouble("rating").toString()
                    binding.txtSvcSoldSD.text = "Terjual: " + svcObj.getInt("sold")
                    binding.txtSvcDescSD.text = svcObj.getString("description")
                    binding.switchSvcAvailability.isChecked = availability
                }
            },
            Response.ErrorListener {
                val builder = AlertDialog.Builder(this)
                builder.setCancelable(false)
                builder.setTitle("Terjadi Masalah")
                builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                builder.setPositiveButton("OK"){dialog, which->
                    this.finish()
                }
                builder.create().show()
            }
        )
        {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["service_id"] = service_id.toString()
                params["token"] = token
                return params
            }
        }
        stringRequest.setShouldCache(false)
        q.add(stringRequest)
    }

    private fun changeAvailability(){
        var q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "changeserviceavailability"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener {
                val obj = JSONObject(it)
                if (obj.getString("status") == "success") {
                    Toast.makeText(this, "Ketersediaan Layanan Berhasil diubah!", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(this, "Terjadi Masalah Jaringan! Silakan Coba Lagi Nanti", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener {
                Toast.makeText(this, "Terjadi Masalah Jaringan! Silakan Coba Lagi Nanti", Toast.LENGTH_SHORT).show()
            }
        )
        {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["service_id"] = service_id.toString()
                params["token"] = token
                return params
            }
        }
        stringRequest.setShouldCache(false)
        q.add(stringRequest)
    }

    private fun deleteService(){
        var q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "deleteservice"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener {
                val obj = JSONObject(it)
                if (obj.getString("status") == "success") {
                    Toast.makeText(this, "Layanan Berhasil dihapus", Toast.LENGTH_SHORT).show()
                    this.finish()
                }
                else{
                    Toast.makeText(this, "Terjadi Masalah Jaringan! Silakan Coba Lagi Nanti", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener {
                Toast.makeText(this, "Terjadi Masalah Jaringan! Silakan Coba Lagi Nanti", Toast.LENGTH_SHORT).show()
            }
        )
        {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["service_id"] = service_id.toString()
                params["token"] = token
                return params
            }
        }
        stringRequest.setShouldCache(false)
        q.add(stringRequest)
    }
}