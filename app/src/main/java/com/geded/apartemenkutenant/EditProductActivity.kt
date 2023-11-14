package com.geded.apartemenkutenant

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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenkutenant.databinding.ActivityEditProductBinding
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class EditProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProductBinding
    val REQUEST_GALLERY = 2
    var token = ""
    var product_id = 0
    var base64Image = ""
    var initPhotoUrl = ""
    companion object{
        val PRODUCT_ID = "PRODUCT_ID"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        product_id = intent.getIntExtra(ProductDetailActivity.PRODUCT_ID, 0)
        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        token = shared.getString(LoginActivity.TOKEN.toString(),"").toString()

        getProductDetail()

        binding.btnResetPhotoEP.setOnClickListener {
            Picasso.get().load(initPhotoUrl).into(binding.imgViewProPhotoEP)
            base64Image = ""
        }

        binding.btnPickPhotoEP.setOnClickListener {
            takeGallery()
        }

        binding.btnSaveProEP.setOnClickListener {
            val name = binding.txtProNameEP.text.toString()
            val description = binding.txtProDescEP.text.toString()
            val priceStr = binding.txtProPriceEP.text.toString()
            if(name != "" && description != "" && priceStr != "") {
                val price = priceStr.toDouble()
                if (price > 0) {
                    updateProduct(name, description, price.toString(), base64Image)
                }
                else{
                    Toast.makeText(this, "Harga Produk Harus Lebih Besar dari Rp0!", Toast.LENGTH_SHORT).show()
                }
            }
            else {
                Toast.makeText(this, "Nama/Deskripsi/Harga Produk Tidak Boleh Kosong", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getProductDetail(){
        var q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "getproductdetail"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener {
                val obj = JSONObject(it)
                if (obj.getString("status") == "success") {
                    val proObj = obj.getJSONObject("data")
                    initPhotoUrl = proObj.getString("photo_url")
                    Picasso.get().load(initPhotoUrl).into(binding.imgViewProPhotoEP)
                    binding.txtProNameEP.setText(proObj.getString("name"))
                    binding.txtProPriceEP.setText(proObj.getDouble("price").toInt().toString())
                    binding.txtProDescEP.setText(proObj.getString("description"))
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
                params["product_id"] = product_id.toString()
                params["token"] = token
                return params
            }
        }
        q.add(stringRequest)
    }

    fun updateProduct(name:String, description:String, price:String, image:String){
        var q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "updateproduct"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener {
                val obj = JSONObject(it)
                if (obj.getString("status") == "success") {
                    Toast.makeText(this, "Perubahan Data Produk Berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                else{
                    val builder = AlertDialog.Builder(this)
                    builder.setCancelable(false)
                    builder.setTitle("Terjadi Masalah")
                    builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                    builder.setPositiveButton("OK"){dialog, which->
                        this.finish()
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
                    this.finish()
                }
                builder.create().show()
            }
        )
        {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["product_id"] = product_id.toString()
                params["name"] = name
                params["description"] = description
                params["price"] = price
                params["image"] = image
                params["token"] = token
                return params
            }
        }
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
                binding.imgViewProPhotoEP.setImageURI(extras)
                base64Image = getImageUriFromBitmap(imageBitmap)
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