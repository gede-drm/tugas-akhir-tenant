package com.geded.apartemenkutenant

import android.app.Activity
import android.app.AlertDialog
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
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenkutenant.databinding.ActivityAddProductBinding
import com.google.android.material.navigation.NavigationView
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class AddProductActivity : AppCompatActivity() {
    private lateinit var binding:ActivityAddProductBinding
    val REQUEST_GALLERY = 2
    var uriBase64 = ""
    var tenant_id = 0
    var token = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        tenant_id = shared.getInt(LoginActivity.TENANTID,0)
        token = shared.getString(LoginActivity.TOKEN.toString(),"").toString()

        binding.btnPickPhotoAP.setOnClickListener {
            takeGallery()
        }

        binding.btnSaveProAP.setOnClickListener {
            val name = binding.txtProNameAP.text.toString()
            val description = binding.txtProDescAP.text.toString()
            val price = binding.txtProPriceAP.text.toString().toDouble()
            if(name != "" && description != "" && price > 0){
                if(uriBase64 != ""){
                    var q = Volley.newRequestQueue(this)
                    val url = Global.urlWS + "addproduct"

                    val stringRequest = object : StringRequest(
                        Method.POST, url,
                        Response.Listener {
                            val obj = JSONObject(it)
                            if(obj.getString("status")=="success") {
                                val builder = AlertDialog.Builder(this)
                                builder.setCancelable(false)
                                builder.setTitle("Produk Masuk Tersimpan")
                                builder.setMessage("Data Produk Telah Tersimpan.")
                                builder.setPositiveButton("OK") { dialog, which ->
                                    this.finish()
                                }
                                builder.create().show()
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
                            Log.d("ERROR VOLLEY", it.message.toString())
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
                            params["name"] = name
                            params["description"] = description
                            params["price"] = price.toString()
                            params["image"] = uriBase64
                            params["tenant_id"] = tenant_id.toString()
                            params["token"] = token.toString()
                            return params
                        }
                    }
                    val retryPolicy = DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    stringRequest.setRetryPolicy(retryPolicy);
                    q.add(stringRequest)
                }
                else{
                    Toast.makeText(this, "Terjadi Kesalahan dalam Pengambilan Foto, Silakan Pilih Foto Kembali", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(this, "Nama/Deskripsi/Harga Produk Tidak Boleh Kosong", Toast.LENGTH_SHORT).show()
            }
        }
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
                binding.imgViewProPhotoAP.setImageURI(extras)
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