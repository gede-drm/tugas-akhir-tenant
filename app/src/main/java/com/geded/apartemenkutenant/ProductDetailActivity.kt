package com.geded.apartemenkutenant

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenkutenant.databinding.ActivityProductDetailBinding
import com.squareup.picasso.Picasso
import org.json.JSONObject

class ProductDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductDetailBinding
    var token = ""
    var product_id = 0
    var stock = 0
    companion object{
        val PRODUCT_ID = "PRODUCT_ID"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        product_id = intent.getIntExtra(PRODUCT_ID, 0)
        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        token = shared.getString(LoginActivity.TOKEN.toString(),"").toString()

        getProductDetail()

        binding.btnAddProStockDP.setOnClickListener {
            if(binding.txtProStockQty.text.toString() != "") {
                val stockAdded = binding.txtProStockQty.text.toString().toInt()
                if (stockAdded >= 1) {
                    addStock(stockAdded)
                } else {
                    Toast.makeText(
                        this,
                        "Jumlah Stok yang Ingin ditambahkan Tidak Boleh Kurang dari 1!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else{
                Toast.makeText(
                    this,
                    "Jumlah Stok yang Ingin ditambahkan Tidak Boleh Kosong!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.btnEditPro.setOnClickListener {
            val intent = Intent(this, EditProductActivity::class.java)
            intent.putExtra(EditProductActivity.PRODUCT_ID, product_id)
            startActivity(intent)
        }
        binding.btnDelPro.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setCancelable(false)
            builder.setTitle("Hapus Produk")
            builder.setMessage("Apakah Anda Yakin untuk Menghapus Produk Ini?")
            builder.setPositiveButton("HAPUS"){dialog, which->
                deleteProduct()
            }
            builder.setNegativeButton("BATAL"){ dialog, which->
            }
            builder.create().show()
        }
    }

    override fun onResume() {
        super.onResume()
        getProductDetail()
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
                    val url = proObj.getString("photo_url")
                    val price = Helper.formatter(proObj.getDouble("price"))
                    stock = proObj.getInt("stock")
                    Picasso.get().load(url).into(binding.imgViewProPhotoDP)
                    binding.txtProNamePD.text = proObj.getString("name")
                    binding.txtProPricePD.text = "Rp$price"
                    binding.txtProStockPD.text = "Stok: " + stock
                    binding.txtProRatingPD.text = proObj.getDouble("rating").toString()
                    binding.txtProSoldPD.text = "Terjual: " + proObj.getInt("sold")
                    binding.txtProDescPD.text = proObj.getString("description")
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

    private fun addStock(stockAdded:Int){
        var q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "addproductstock"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener {
                val obj = JSONObject(it)
                if (obj.getString("status") == "success") {
                    Toast.makeText(this, "Stok Produk Berhasil ditambah!", Toast.LENGTH_SHORT).show()
                    stock += stockAdded
                    binding.txtProStockPD.text = "Stok: " + stock
                    binding.txtProStockQty.setText("")
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
                params["product_id"] = product_id.toString()
                params["stock_added"] = stockAdded.toString()
                params["token"] = token
                return params
            }
        }
        stringRequest.setShouldCache(false)
        q.add(stringRequest)
    }

    private fun deleteProduct(){
        var q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "deleteproduct"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener {
                val obj = JSONObject(it)
                if (obj.getString("status") == "success") {
                    Toast.makeText(this, "Produk Berhasil dihapus", Toast.LENGTH_SHORT).show()
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
                params["product_id"] = product_id.toString()
                params["token"] = token
                return params
            }
        }
        stringRequest.setShouldCache(false)
        q.add(stringRequest)
    }
}