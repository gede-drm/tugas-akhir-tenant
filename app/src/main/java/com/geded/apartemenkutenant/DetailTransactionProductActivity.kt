package com.geded.apartemenkutenant

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenkutenant.databinding.ActivityDetailTransactionProductBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import org.json.JSONObject

class DetailTransactionProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailTransactionProductBinding
    var trxProItems:ArrayList<TransactionDetailItem> = arrayListOf()
    var trxProStatuses:ArrayList<TrxStatus> =  arrayListOf()
    var transferNotPaid = false
    var paymentproofurl = ""
    var unitphonenum = ""
    var delivery = ""
    var status = ""
    var transaction_id = 0
    var token = ""
    companion object{
        val TRANSACTION_ID = "TRANSACTION_ID"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTransactionProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        token = shared.getString(LoginActivity.TOKEN.toString(),"").toString()

        transaction_id = intent.getIntExtra(TRANSACTION_ID, 0)

        binding.btnCallUnitDTP.setOnClickListener {
            if(unitphonenum != "") {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:$unitphonenum")
                startActivity(intent)
            }
        }

        binding.btnTFProofDTP.setOnClickListener {
            if (transferNotPaid == false) {
                if (paymentproofurl != "") {
                    val dialog = BottomSheetDialog(this)
                    val view = layoutInflater.inflate(R.layout.bottom_sheet_transfer_proof, null)
                    dialog.setCancelable(false)
                    dialog.setContentView(view)

                    dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

                    dialog.show()
                    val imgTFProofDT = view.findViewById<ImageView>(R.id.imgViewTFProofDT)
                    Picasso.get().load(paymentproofurl).into(imgTFProofDT)

                    view.findViewById<Button>(R.id.btnCloseDialogTFProof).setOnClickListener {
                        dialog.dismiss()
                    }
                }
            }
        }
        binding.btnSeeStatusDTP.setOnClickListener {
            if(trxProStatuses.size > 0) {
                val dialog = BottomSheetDialog(this)
                val view =layoutInflater.inflate(R.layout.bottom_sheet_trx_status, null)
                dialog.setCancelable(false)
                dialog.setContentView(view)

                dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

                dialog.show()

                val lm: LinearLayoutManager = LinearLayoutManager(this)
                var recyclerView = view.findViewById<RecyclerView>(R.id.recViewTrxStatus)
                recyclerView.layoutManager = lm
                recyclerView.setHasFixedSize(true)
                recyclerView.adapter = TrxStatusAdapter(trxProStatuses, this)
                recyclerView.isVisible = true

                view.findViewById<Button>(R.id.btnCloseDialogTrxStatus).setOnClickListener {
                    dialog.dismiss()
                }
            }
        }

        binding.btnChangeStatusDTP.setOnClickListener {
            if(status != "" && delivery != ""){
                if(status == "Belum dikonfirmasi"){
                    if(paymentproofurl != "") {
                        validateTransfer()
                    }
                    else{
                        updateStatus("prepare", "Diproses")
                    }
                }
                else if(status == "Diproses"){
                    updateStatus("prepared", "Selesai diproses")
                }
                else if(status == "Selesai diproses"){
                    if(delivery == "delivery") {
                        updateStatus("delivery", "Sedang diantar")
                    }
                    else{
                        updateStatus("readytopickup", "Siap diambil")
                    }
                }
                else if(status == "Siap diambil" || status == "Sedang diantar"){
                    if(delivery == "delivery") {
                        updateStatus("delivered", "Sudah diantar")
                    }
                    else{
                        updateStatus("pickedup", "Sudah diambil")
                    }
                }
            }
            else{
                Toast.makeText(this, "Terdapat Error, Silakan Buka Ulang Halaman Ini!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCancelDTP.setOnClickListener {
            if(status != "") {
                val builder = AlertDialog.Builder(this)
                builder.setCancelable(false)
                builder.setTitle("Konfirmasi")
                builder.setMessage("Apakah Anda Yakin untuk Membatalkan Transaksi?")
                builder.setPositiveButton("BATALKAN") { dialog, which ->
                    cancelTransaction()
                }
                builder.setNegativeButton("TETAP JALANKAN TRANSAKSI"){ dialog, which ->

                }
                builder.create().show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getProTrxData()
    }

    fun getProTrxData(){
        trxProItems.clear()
        trxProStatuses.clear()

        val q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "transaction/trxprodetail"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                Log.d("VOLLEY", it)
                val obj = JSONObject(it)
                if (obj.getString("status") == "success") {
                    val dataObj = obj.getJSONObject("data")

                    binding.txtUnitNameDTP.text = dataObj.getString("unit_apart")
                    unitphonenum = dataObj.getString("unit_phone")
                    delivery = dataObj.getString("delivery")
                    binding.txtTrxDateDTP.text = dataObj.getString("transaction_date")
                    binding.txtFinishDateDTP.text = dataObj.getString("finish_date")
                    if(dataObj.getString("pickup_date") != ""){
                        binding.txtPickupDateDTP.text = dataObj.getString("pickup_date")
                    }
                    else{
                        binding.txtViewTrxPickupDateDTP.isVisible = false
                        binding.txtPickupDateDTP.isVisible = false
                    }
                    if(dataObj.getString("delivery") == "delivery"){
                        binding.txtViewTrxDeliveryDTP.text = "Tanggal Kirim"
                        binding.txtDeliveryDTP.text = "Kirim"
                    }
                    else{
                        binding.txtViewTrxDeliveryDTP.text = "Tanggal Ambil"
                        binding.txtDeliveryDTP.text = "Ambil"
                    }

                    if(dataObj.getString("payment") == "transfer"){
                        binding.txtPayMethodDTP.text = "Transfer"
                        if(dataObj.getString("payment_proof_url") == ""){
                            transferNotPaid = true
                            binding.btnTFProofDTP.isVisible = false
                        }
                        else{
                            paymentproofurl = dataObj.getString("payment_proof_url")
                        }
                    }
                    else{
                        binding.txtPayMethodDTP.text = "Tunai"
                        binding.btnTFProofDTP.isVisible = false
                    }

                    status = dataObj.getString("status")
                    if(status == "Belum Pembayaran") {
                        binding.btnCancelDTP.isVisible = true
                        val cL = binding.constraintLayoutCardDTP
                        val cset = ConstraintSet()
                        cset.clone(cL)
                        cset.connect(R.id.btnCancelDTP, ConstraintSet.START, R.id.constraintLayoutCardDTP, ConstraintSet.START, 24)
                        cset.connect(R.id.btnCancelDTP, ConstraintSet.END, R.id.constraintLayoutCardDTP, ConstraintSet.END, 24)
                        cset.applyTo(cL)
                        val btnLParams = binding.btnCancelDTP.layoutParams
                        btnLParams.width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
                        binding.btnCancelDTP.layoutParams = (btnLParams)
                        binding.btnChangeStatusDTP.isVisible = false
                    }
                    else if(status == "Belum dikonfirmasi"){
                        binding.btnCancelDTP.isVisible = true
                        if(dataObj.getString("payment") == "transfer") {
                            binding.btnChangeStatusDTP.text = "Validasi Pembayaran & Konfirmasi"
                        }
                        else{
                            binding.btnChangeStatusDTP.text = " Konfirmasi"
                        }
                    }
                    else if(status == "Diproses"){
                        binding.btnCancelDTP.isVisible = false
                        binding.btnChangeStatusDTP.text = "Selesaikan Proses"
                    }
                    else if(status == "Selesai diproses"){
                        binding.btnCancelDTP.isVisible = false
                        if(dataObj.getString("delivery") == "delivery") {
                            binding.btnChangeStatusDTP.text = "Antar Barang"
                        }
                        else{
                            binding.btnChangeStatusDTP.text = "Barang Siap diambil"
                        }
                    }
                    else if(status == "Siap diambil" || status == "Sedang diantar"){
                        binding.btnCancelDTP.isVisible = false
                        if(dataObj.getString("delivery") == "delivery") {
                            binding.btnChangeStatusDTP.text = "Barang Sudah diantar"
                        }
                        else{
                            binding.btnChangeStatusDTP.text = "Barang Sudah diambil"
                        }
                    }
                    else{
                        binding.btnCallUnitDTP.isVisible = false
                        binding.btnCancelDTP.isVisible = false
                        binding.btnChangeStatusDTP.isVisible = false
                    }

                    val total_payment = Helper.formatter(dataObj.getDouble("total_payment"))
                    binding.txtTotalPaymentDTP.text = "Rp$total_payment"

                    val itemArr = dataObj.getJSONArray("items")
                    for (i in 0 until itemArr.length()) {
                        val itemObj = itemArr.getJSONObject(i)
                        val item = TransactionDetailItem(itemObj.getInt("id"), itemObj.getString("name"), itemObj.getString("photo_url"), itemObj.getDouble("price"), itemObj.getInt("quantity"), itemObj.getString("pricePer"), itemObj.getDouble("subtotal"))
                        trxProItems.add(item)
                    }
                    updateListItems()

                    val statusArr = dataObj.getJSONArray("statuses")
                    for (i in 0 until statusArr.length()){
                        val statusObj = statusArr.getJSONObject(i)
                        val status = TrxStatus(statusObj.getString("date"), statusObj.getString("description"))
                        trxProStatuses.add(status)
                    }
                    transaction_id = dataObj.getInt("id")
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setCancelable(false)
                    builder.setTitle("Terjadi Masalah")
                    builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                    builder.setPositiveButton("OK") { dialog, which ->
                        finish()
                    }
                    builder.create().show()
                }
            }, Response.ErrorListener {
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
                params["transaction_id"] = transaction_id.toString()
                params["token"] = token
                return params
            }
        }
        stringRequest.setShouldCache(false)
        q.add(stringRequest)
    }

    fun updateListItems(){
        val lm: LinearLayoutManager = LinearLayoutManager(this)
        var recyclerView = binding.recViewItemsDTP
        recyclerView.layoutManager = lm
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = TrxDetailItemsAdapter(trxProItems, this)
        recyclerView.isVisible = true
    }

    fun updateStatus(statusName:String, status:String){
        val q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "transaction/changestatus"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                Log.d("VOLLEY", it)
                val obj = JSONObject(it)
                if (obj.getString("status") == "success") {
                    Toast.makeText(this, "Status Transaksi Berhasil diubah!", Toast.LENGTH_SHORT).show()
                    getProTrxData()
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setCancelable(false)
                    builder.setTitle("Terjadi Masalah")
                    builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                    builder.setPositiveButton("OK") { dialog, which ->
                    }
                    builder.create().show()
                }
            }, Response.ErrorListener {
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
                    params["transaction_id"] = transaction_id.toString()
                    params["statusname"] = statusName
                    params["status"] = status
                    params["token"] = token
                    return params
                }
            }
        stringRequest.setShouldCache(false)
        q.add(stringRequest)
    }

    fun validateTransfer(){
        val q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "transaction/validatetransfer"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                Log.d("VOLLEY", it)
                val obj = JSONObject(it)
                if (obj.getString("status") == "success") {
                    Toast.makeText(this, "Validasi Pembayaran Berhasil!", Toast.LENGTH_SHORT).show()
                    updateStatus("prepare", "Diproses")
                }
                else{
                    Toast.makeText(this, "Terdapat Gangguan, Coba Beberapa Saat Lagi!", Toast.LENGTH_SHORT).show()
                }}, Response.ErrorListener {
                Toast.makeText(this, "Terdapat Gangguan, Coba Beberapa Saat Lagi!", Toast.LENGTH_SHORT).show()
            }){
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["transaction_id"] = transaction_id.toString()
                params["token"] = token
                return params
            }
        }
        stringRequest.setShouldCache(false)
        q.add(stringRequest)
    }

    fun cancelTransaction() {
        val q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "transaction/cancel"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                Log.d("VOLLEY", it)
                val obj = JSONObject(it)
                if (obj.getString("status") == "success") {
                    Toast.makeText(this, "Transaksi Berhasil dibatalkan!", Toast.LENGTH_SHORT).show()
                    getProTrxData()
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setCancelable(false)
                    builder.setTitle("Terjadi Masalah")
                    builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                    builder.setPositiveButton("OK") { dialog, which ->
                    }
                    builder.create().show()
                }
            }, Response.ErrorListener {
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
                params["transaction_id"] = transaction_id.toString()
                params["token"] = token
                return params
            }
        }
        stringRequest.setShouldCache(false)
        q.add(stringRequest)
    }
}