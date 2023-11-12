package com.geded.apartemenkutenant

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenkutenant.databinding.FragmentSettingBinding
import org.json.JSONObject

class SettingFragment : Fragment() {
    private lateinit var binding: FragmentSettingBinding
    var username = ""
    var tenant_id = 0
    var tenant_name = ""
    var tenant_type = ""
    var token = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var shared: SharedPreferences = requireActivity().getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        username = shared.getString(LoginActivity.USERNAME.toString(),"").toString()
        tenant_name = shared.getString(LoginActivity.TENANTNAME.toString(),"").toString()
        tenant_id = shared.getInt(LoginActivity.TENANTID,0)
        tenant_type = shared.getString(LoginActivity.TENANTTYPE.toString(),"").toString()
        if(tenant_type == "product"){
            tenant_type = "Barang"
        }
        else{
            tenant_type = "Jasa"
        }
        token = shared.getString(LoginActivity.TOKEN.toString(),"").toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.txtTenantNameSt.text = tenant_name
        binding.txtTypeSt.text = tenant_type

        binding.btnSwitchStatus.setOnClickListener {
            changeStatus()
        }
        binding.btnLogout.setOnClickListener {
            var shared: SharedPreferences = requireActivity().getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
            var editor: SharedPreferences.Editor = shared.edit()

            val q = Volley.newRequestQueue(activity)
            val url = Global.urlGeneralWS + "cleartoken"

            var stringRequest = object : StringRequest(
                Method.POST, url, Response.Listener {
                    val obj = JSONObject(it)
                    if(obj.getString("status")=="success") {
                        editor.putString(LoginActivity.USERNAME, "")
                        editor.putString(LoginActivity.TENANTNAME, "")
                        editor.putInt(LoginActivity.TENANTID, 0)
                        editor.putString(LoginActivity.TENANTTYPE, "")
                        editor.putString(LoginActivity.TOKEN, "")
                        editor.apply()

                        activity?.let{ fragmentActivity ->
                            val intent = Intent(fragmentActivity, LoginActivity::class.java)
                            fragmentActivity.startActivity(intent)
                            fragmentActivity.finish()
                        }
                    }
                    else{
                        val builder = AlertDialog.Builder(activity)
                        builder.setCancelable(false)
                        builder.setTitle("Terjadi Masalah")
                        builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                        builder.setPositiveButton("OK"){dialog, which->
                        }
                        builder.create().show()
                    }
                },
                Response.ErrorListener {
                    val builder = AlertDialog.Builder(activity)
                    builder.setCancelable(false)
                    builder.setTitle("Terjadi Masalah")
                    builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                    builder.setPositiveButton("OK"){dialog, which->
                    }
                    builder.create().show()
                }){
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["username"] = username.toString()
                    params["token"] = token.toString()
                    return params
                }
            }
            stringRequest.setShouldCache(false)
            q.add(stringRequest)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.progressBarSt.isVisible = true
        binding.txtTenantNameSt.isVisible = false
        binding.txtAddressSt.isVisible = false
        binding.txtPhoneSt.isVisible = false
        binding.cardView.isVisible = false
        binding.cardView2.isVisible = false

        binding.btnSwitchStatus.isVisible = false
        binding.txtStatusSt.isVisible = false

        getStatus()
        getProfile()
    }

    fun getProfile(){
        val q = Volley.newRequestQueue(activity)
        val url = Global.urlWS + "getprofile"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                val obj = JSONObject(it)
                if(obj.getString("status")=="success") {
                    var array = obj.getJSONObject("data")
                    binding.txtAddressSt.text = array["address"].toString()
                    binding.txtPhoneSt.text = array["phone_number"].toString()
                    binding.txtOpenHourSt.text = array["service_hour_start"].toString()
                    binding.txtCloseHourSt.text = array["service_hour_end"].toString()
                    binding.txtBankSt.text = array["bank_name"].toString()
                    binding.txtBankHolderSt.text = array["account_holder"].toString()
                    binding.txtBankAccountSt.text = array["bank_account"].toString()
                    var delivery = ""
                    if(array["delivery"].toString().toInt() == 0){
                        delivery = "Tidak"
                    }
                    else{
                        delivery = "Ya"
                    }
                    binding.txtDeliverySt.text = delivery

                    binding.progressBarSt.isVisible = false
                    binding.txtTenantNameSt.isVisible = true
                    binding.txtAddressSt.isVisible = true
                    binding.txtPhoneSt.isVisible = true
                    binding.cardView.isVisible = true
                    binding.cardView2.isVisible = true
                }
            },
            Response.ErrorListener {
                val builder = AlertDialog.Builder(activity)
                builder.setCancelable(false)
                builder.setTitle("Terjadi Masalah")
                builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                builder.setPositiveButton("OK"){dialog, which->
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

    fun getStatus(){
        val q = Volley.newRequestQueue(activity)
        val url = Global.urlWS + "getstatus"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                val obj = JSONObject(it)
                if(obj.getString("status")=="success") {
                    var status = obj.getString("tenant_status")
                    if(status == "open"){
                        binding.txtStatusSt.text = "Buka"
                        binding.btnSwitchStatus.text = "Tutup Toko"
                    }
                    else{
                        binding.txtStatusSt.text = "Tutup"
                        binding.btnSwitchStatus.text = "Buka Toko"
                    }
                    binding.progressBarSt.isVisible = false
                    binding.btnSwitchStatus.isVisible = true
                    binding.txtStatusSt.isVisible = true
                }
            },
            Response.ErrorListener {
                val builder = AlertDialog.Builder(activity)
                builder.setCancelable(false)
                builder.setTitle("Terjadi Masalah")
                builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                builder.setPositiveButton("OK"){dialog, which->
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

    fun changeStatus(){
        val q = Volley.newRequestQueue(activity)
        val url = Global.urlWS + "changestatus"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                val obj = JSONObject(it)
                if(obj.getString("status")=="success") {
                    Toast.makeText(activity, "Toko Berhasil dibuka!", Toast.LENGTH_SHORT).show()
                    getStatus()
                }
                else{
                    val builder = AlertDialog.Builder(activity)
                    builder.setCancelable(false)
                    builder.setTitle("Terjadi Masalah")
                    builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                    builder.setPositiveButton("OK"){dialog, which->
                    }
                    builder.create().show()
                }
            },
            Response.ErrorListener {
                val builder = AlertDialog.Builder(activity)
                builder.setCancelable(false)
                builder.setTitle("Terjadi Masalah")
                builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                builder.setPositiveButton("OK"){dialog, which->
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