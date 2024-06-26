package com.geded.apartemenkutenant

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenkutenant.databinding.FragmentOrderHistoryBinding
import com.geded.apartemenkutenant.databinding.FragmentOrdersBinding
import org.json.JSONObject

class OrderHistoryFragment : Fragment() {
    private lateinit var binding: FragmentOrderHistoryBinding
    var transactions:ArrayList<TransactionList> = arrayListOf()
    var tenant_id = 0
    var token = ""
    var tenant_type = ""
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
        binding = FragmentOrderHistoryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.refreshLayoutHisOrders.setOnRefreshListener {
            if(binding.progressBarOrderHis.visibility == View.INVISIBLE) {
                binding.recViewHisOrders.isVisible = false
                getData()
            }
            else{
                binding.refreshLayoutHisOrders.isRefreshing = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.recViewHisOrders.visibility = View.INVISIBLE
        binding.progressBarOrderHis.visibility = View.VISIBLE
        getData()
    }

    fun getData(){
        transactions.clear()
        val q = Volley.newRequestQueue(activity)
        var url = ""
        if(tenant_type == "product") {
            url = Global.urlWS + "transaction/prohistory"
        }
        else{
            url = Global.urlWS + "transaction/svchistory"
        }

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                val obj = JSONObject(it)
                if(obj.getString("status")=="success") {
                    val data = obj.getJSONArray("data")
                    for (i in 0 until data.length()) {
                        var trxObj = data.getJSONObject(i)
                        var itemObj = trxObj.getJSONObject("item")
                        val trx = TransactionList(trxObj.getInt("id"), trxObj.getString("unit_no"), trxObj.getString("transaction_date"), trxObj.getString("total_payment").toDouble(), trxObj.getString("status"), itemObj.getString("image"), itemObj.getString("name"), itemObj.getInt("quantity"), trxObj.getInt("itemcount"))
                        transactions.add(trx)
                    }
                    updateList()
                }
                else if(obj.getString("status")=="empty"){
                    binding.txtEmptyHOL.visibility = View.VISIBLE
                    binding.refreshLayoutHisOrders.isRefreshing = false
                    binding.recViewHisOrders.visibility = View.INVISIBLE
                    binding.progressBarOrderHis.visibility = View.INVISIBLE
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
        var recyclerView = binding.recViewHisOrders
        recyclerView.layoutManager = lm
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = TransactionListAdapter(transactions,tenant_type, this.activity)
        recyclerView.isVisible = true
        binding.txtEmptyHOL.visibility = View.GONE
        binding.progressBarOrderHis.visibility = View.INVISIBLE
        binding.refreshLayoutHisOrders.isRefreshing = false
    }

}