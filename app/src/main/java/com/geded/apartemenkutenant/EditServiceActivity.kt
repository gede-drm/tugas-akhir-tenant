package com.geded.apartemenkutenant

import android.R
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenkutenant.databinding.ActivityEditServiceBinding
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class EditServiceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditServiceBinding
    private lateinit var adapterPricePer:ArrayAdapter<String>
    val REQUEST_GALLERY = 2
    var service_id = 0
    var token = ""
    var uriBase64 = ""
    var initPhotoUrl = ""
    companion object{
        val SERVICE_ID = "SERVICE_ID"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        service_id = intent.getIntExtra(SERVICE_ID, 0)
        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        token = shared.getString(LoginActivity.TOKEN.toString(),"").toString()

        adapterPricePer = ArrayAdapter(this, R.layout.simple_list_item_1, arrayListOf("Paket", "Jam"))
        adapterPricePer.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPriceperES.adapter = adapterPricePer

        getServiceDetail()

        binding.btnResetPhotoES.setOnClickListener {
            Picasso.get().load(initPhotoUrl).into(binding.imgViewSvcPhotoES)
            uriBase64 = ""
        }

        binding.btnPickPhotoES.setOnClickListener {
            takeGallery()
        }

        binding.btnSaveSvcES.setOnClickListener {
            val name = binding.txtSvcNameES.text.toString()
            val description = binding.txtSvcDescES.text.toString()
            var pricePer = binding.spinnerPriceperES.selectedItem.toString()
            if(pricePer == "Jam"){
                pricePer = "hour"
            }
            else{
                pricePer = "package"
            }
            var permit_need = false
            if(binding.switchPermitNeedES.isChecked){
                permit_need = true
            }
            val priceStr = binding.txtSvcPriceES.text.toString()
            if(name != "" && description != "" && priceStr != ""){
                val price = priceStr.toDouble()
                if (price > 0) {
                    updateService(name, description, price, pricePer, permit_need)
                } else {
                    Toast.makeText(this, "Harga Layanan Harus Lebih Besar dari Rp0!", Toast.LENGTH_SHORT).show()
                }
            } else{
                Toast.makeText(this, "Nama/Deskripsi/Harga Layanan Tidak Boleh Kosong", Toast.LENGTH_SHORT).show()
            }
        }
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
                    initPhotoUrl = svcObj.getString("photo_url")
                    var permitNeed = false
                    var pricePer = svcObj.getString("pricePer")
                    if(pricePer == "package"){
                        pricePer = "Paket"
                    }
                    else{
                        pricePer = "Jam"
                    }
                    permitNeed = svcObj.getInt("permit_need") != 0
                    Picasso.get().load(initPhotoUrl).into(binding.imgViewSvcPhotoES)
                    binding.txtSvcNameES.setText(svcObj.getString("name"))
                    binding.txtSvcPriceES.setText(svcObj.getDouble("price").toInt().toString())
                    binding.switchPermitNeedES.isChecked = permitNeed
                    binding.txtSvcDescES.setText(svcObj.getString("description"))
                    var spinnerPos = 0;
                    spinnerPos = adapterPricePer.getPosition(pricePer)
                    binding.spinnerPriceperES.setSelection(spinnerPos)
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

    private fun updateService(name:String, description:String, price:Double, pricePer:String, permit_need:Boolean){
        var q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "updateservice"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener {
                val obj = JSONObject(it)
                Log.d("VOLLEY SUCCESS", it)
                if (obj.getString("status") == "success") {
                    Toast.makeText(this, "Perubahan Data Layanan Berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val builder = android.app.AlertDialog.Builder(this)
                    builder.setCancelable(false)
                    builder.setTitle("Terjadi Masalah")
                    builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                    builder.setPositiveButton("OK") { dialog, which ->
                        this.finish()
                    }
                    builder.create().show()
                }
            },
            Response.ErrorListener {
                Log.d("ERROR VOLLEY", it.message.toString())
                val builder = android.app.AlertDialog.Builder(this)
                builder.setCancelable(false)
                builder.setTitle("Terjadi Masalah")
                builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                builder.setPositiveButton("OK") { dialog, which ->
                    this.finish()
                }
                builder.create().show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["name"] = name
                params["description"] = description
                params["price"] = price.toString()
                params["pricePer"] = pricePer
                params["permit_need"] = permit_need.toString()
                params["image"] = uriBase64
                params["service_id"] = service_id.toString()
                params["token"] = token.toString()
                return params
            }
        }
        val retryPolicy =
            DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);
        q.add(stringRequest)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_GALLERY -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takeGallery()
                } else {
                    Toast.makeText(this, "Anda harus memperbolehkan akses aplikasi ke penyimpanan", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if(requestCode == REQUEST_GALLERY){
                val extras = data?.data
                val imageBitmap: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, extras)
                binding.imgViewSvcPhotoES.setImageURI(extras)
                uriBase64 = getImageUriFromBitmap(imageBitmap)
            }
        }
    }

    fun getImageUriFromBitmap(bitmap: Bitmap): String{
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes)

        val byteImagePhoto = bytes.toByteArray()
        val encodedImage = "data:image/jpeg;base64," + Base64.encodeToString(byteImagePhoto, Base64.DEFAULT)
        return encodedImage
    }

    fun takeGallery(){
        val i = Intent()
        i.type = "image/*"
        i.action = Intent.ACTION_PICK
        startActivityForResult(i, REQUEST_GALLERY)
    }
}