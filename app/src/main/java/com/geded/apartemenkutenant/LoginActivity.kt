package com.geded.apartemenkutenant

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenkutenant.databinding.ActivityLoginBinding
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    private lateinit var binding:ActivityLoginBinding
    companion object{
        val USERNAME = "USERNAME"
        val TENANTID = "TENANTID"
        val TENANTNAME = "TENANTNAME"
        val TENANTTYPE = "TENANTTYPE"
        val TOKEN = "TOKEN"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        var usernameSP = shared.getString(LoginActivity.USERNAME, "")

        if(usernameSP!="")
        {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            this.finish()
        }

        binding.btnLogin.setOnClickListener {
            val inputUsername = binding.txtUsername.text
            val inputPassword = binding.txtPassword.text
            if ((inputUsername.toString() != "") && (inputPassword.toString() != "")) {
                var q = Volley.newRequestQueue(this)
                val url = Global.urlWS + "login"

                val stringRequest = object : StringRequest(
                    Method.POST, url,
                    Response.Listener {
                        Log.d("Success", it)
                        var obj = JSONObject(it)
                        var resultDb = obj.getString("status")
                        if (resultDb == "success") {
                            var array = obj.getJSONObject("data")
                            var username = inputUsername
                            var tenant_id = array["tenant_id"]
                            var tenant_name = array["tenant_name"]
                            var tenant_type = array["tenant_type"]
                            var token = array["token"]

                            var editor: SharedPreferences.Editor = shared.edit()
                            editor.putString(USERNAME, username.toString())
                            editor.putString(TENANTNAME, tenant_name.toString())
                            editor.putInt(TENANTID, tenant_id.toString().toInt())
                            editor.putString(TENANTTYPE, tenant_type.toString())
                            editor.putString(TOKEN, token.toString())
                            editor.apply()

                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            this.finish()
                        } else {
                            Toast.makeText(this, "Username atau Password Salah!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    Response.ErrorListener {
                        Toast.makeText(this, "Connection Error", Toast.LENGTH_SHORT).show()
                    }) {

                    override fun getParams(): MutableMap<String, String> {
                        val params = HashMap<String, String>()
                        params["username"] = inputUsername.toString()
                        params["password"] = inputPassword.toString()
                        return params
                    }
                }
                stringRequest.setShouldCache(false)
                q.add(stringRequest)
            } else{
                Toast.makeText(this, "Username atau Password Tidak Boleh Kosong!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}