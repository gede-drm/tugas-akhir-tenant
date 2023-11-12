package com.geded.apartemenkutenant

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenkutenant.databinding.FragmentStoreItemsBinding
import org.json.JSONObject

class StoreItemsFragment : Fragment() {
    private lateinit var binding: FragmentStoreItemsBinding
    var tenant_id = 0
    var token = ""
    var tenant_type = ""
    var products:ArrayList<Product> = arrayListOf()
    var services:ArrayList<Service> = arrayListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var shared: SharedPreferences = requireActivity().getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        tenant_id = shared.getInt(LoginActivity.TENANTID,0)
        tenant_type = shared.getString(LoginActivity.TENANTTYPE.toString(),"").toString()
        token = shared.getString(LoginActivity.TOKEN.toString(),"").toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentStoreItemsBinding.inflate(layoutInflater)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabAddItem.setOnClickListener {
            activity?.let {
                var intent:Intent = Intent()
                if(tenant_type == "product") {
                    intent = Intent(it, AddProductActivity::class.java)
                }
                else{
                    intent = Intent(it, AddServiceActivity::class.java)
                }
                it.startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateAPI()
    }

    fun updateAPI(){
        products.clear()
        services.clear()

        val q = Volley.newRequestQueue(activity)
        var url = ""
        if(tenant_type == "product") {
            url = Global.urlWS + "getproducts"
        }
        else{
            url = Global.urlWS + "getservices"
        }

        Log.d("VOLLEYURL", url)

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                val obj = JSONObject(it)
                if(obj.getString("status")=="success") {
                    val data = obj.getJSONArray("data")
                    if(tenant_type == "product") {
                        for (i in 0 until data.length()) {
                            var proObj = data.getJSONObject(i)
                            val pro = Product(proObj.getInt("id"), proObj.getString("name"), "", proObj.getString("photo_url"), proObj.getDouble("price"), proObj.getInt("stock"), proObj.getDouble("rating"))
                            products.add(pro)
                        }
                    }
                    else{
                        for (i in 0 until data.length()) {
                            var svcObj = data.getJSONObject(i)
                            val svc = Service(svcObj.getInt("id"), svcObj.getString("name"), "", svcObj.getInt("permit_need"), svcObj.getString("photo_url"), svcObj.getString("pricePer"), svcObj.getDouble("price"), svcObj.getInt("availability"), svcObj.getDouble("rating"))
                            services.add(svc)
                        }
                    }
                    updateList()
                }
                else if(obj.getString("status")=="empty"){
                    binding.txtEmptyIL.visibility = View.VISIBLE
//                    binding.refreshLayoutPermission.isRefreshing = false
                    binding.recViewItems.visibility = View.INVISIBLE
                }
            },
            Response.ErrorListener {
                val builder = AlertDialog.Builder(activity)
                builder.setCancelable(false)
                builder.setTitle("Terjadi Masalah")
                builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                builder.setPositiveButton("OK"){dialog, which->
                    activity?.finish()
                    System.exit(0)
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

    fun updateList() {
        val lm: LinearLayoutManager = LinearLayoutManager(activity)
        var recyclerView = binding.recViewItems
        recyclerView.layoutManager = lm
        recyclerView.setHasFixedSize(true)
        if(tenant_type == "product") {
            recyclerView.adapter = ProductListAdapter(products, this.activity)
        }else{
            recyclerView.adapter = ServiceListAdapter(services, this.activity)
        }
        recyclerView.isVisible = true
        binding.txtEmptyIL.visibility = View.GONE
//        binding.refreshLayoutPermission.isRefreshing = false
    }
}