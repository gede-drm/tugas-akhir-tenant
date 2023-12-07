package com.geded.apartemenkutenant

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
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
import com.geded.apartemenkutenant.databinding.ActivityDetailTransactionServiceBinding
import com.geded.apartemenkutenant.databinding.ActivityProposePermissionBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import org.json.JSONObject

class DetailTransactionServiceActivity : AppCompatActivity() {
    private lateinit var binding:ActivityDetailTransactionServiceBinding
    var trxSvcItems:ArrayList<TransactionDetailItem> = arrayListOf()
    var trxSvcStatuses:ArrayList<TrxStatus> =  arrayListOf()
    var transferNotPaid = false
    var paymethod = ""
    var paymentproofurl = ""
    var unitphonenum = ""
    var service_type = ""
    var permit_need = 0
    var delivery = ""
    var status = ""
    var permission_status = ""
    var permission_approval_date = ""
    var permission_letter_url = ""
    var permission_qr_url = ""
    var transaction_id = 0
    var token = ""
    companion object{
        val TRANSACTION_ID = "TRANSACTION_ID"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTransactionServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        token = shared.getString(LoginActivity.TOKEN.toString(),"").toString()

        transaction_id = intent.getIntExtra(TRANSACTION_ID, 0)

        binding.btnPermissionDetailDT.isVisible = false

        binding.btnCallUnitDTS.setOnClickListener {
            if(unitphonenum != "") {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:$unitphonenum")
                startActivity(intent)
            }
        }

        binding.btnTFProofDTS.setOnClickListener {
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

        binding.btnSeeStatusDTS.setOnClickListener {
            if(trxSvcStatuses.size > 0) {
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
                recyclerView.adapter = TrxStatusAdapter(trxSvcStatuses, this)
                recyclerView.isVisible = true

                view.findViewById<Button>(R.id.btnCloseDialogTrxStatus).setOnClickListener {
                    dialog.dismiss()
                }
            }
        }
        binding.btnChangeStatusDTS.setOnClickListener {
            if (status != "" && delivery != "") {
                if(service_type == "laundry"){
                    if(status == "Belum dikonfirmasi"){
                        if(paymethod == "transfer") {
                            updateStatus("notransferproof", "Belum Pembayaran")
                        }
                        else{
                            if(delivery == "delivery"){
                                updateStatus("waiting", "Menunggu Pengambilan")
                            }
                            else{
                                updateStatus("waiting", "Menunggu Laundry")
                            }
                        }
                    }
                    else if(status == "Menunggu Pengambilan"){
                        updateStatus("pickup", "Sedang dilakukan Pengambilan")
                    }
                    else if(status == "Menunggu Laundry"){
                        updateStatus("wip", "Sedang dikerjakan")
                    }
                    else if(status == "Sedang dilakukan Pengambilan"){
                        updateStatus("wip", "Sedang dikerjakan")
                    }
                    else if(status == "Sedang dikerjakan"){
                        if(delivery == "delivery") {
                            updateStatus("finishwork", "Selesai dikerjakan")
                        }
                        else{
                            updateStatus("finishwork", "Siap diambil")
                        }
                    }
                    else if(status == "Selesai dikerjakan"){
                        updateStatus("finishwork", "Sedang Pengiriman")
                    }
                    else if(status == "Siap diambil" || status == "Sedang Pengiriman"){
                        updateStatus("done", "Selesai")
                    }
                }
                else{
                    if(permit_need == 1){
                        if(status == "Belum dikonfirmasi") {
                            updateStatus("confirmed", "Dikonfirmasi")
                        }
                        else if(status == "Dikonfirmasi") {
                            val intent = Intent(this, ProposePermissionActivity::class.java)
                            intent.putExtra(ProposePermissionActivity.TRANSACTION_ID, transaction_id)
                            intent.putExtra(ProposePermissionActivity.FINISH_DATE, binding.txtFinishDateDTS.text.toString())
                            startActivity(intent)
                        }
                        else if(status == "Menunggu Pengerjaan") {
                            updateStatus("process", "Sedang dikerjakan")
                        }
                        else if(status == "Sedang dikerjakan") {
                            updateStatus("done", "Selesai")
                        }
                    }
                    else{
                        if(status == "Belum dikonfirmasi") {
                            if (paymethod == "transfer") {
                                updateStatus("notranferproof", "Belum Pembayaran")
                            } else {
                                updateStatus("waiting", "Menunggu Pengerjaan")
                            }
                        }
                        else if(status == "Menunggu Pengerjaan"){
                            updateStatus("process", "Menuju Lokasi")
                        }
                        else if(status == "Menuju Lokasi"){
                            updateStatus("process", "Sedang dikerjakan")
                        }
                        else if(status == "Sedang dikerjakan"){
                            updateStatus("done", "Selesai")
                        }
                    }
                }
            }
        }
        binding.btnCancelDTS.setOnClickListener {
            if(status != "") {
                cancelTransaction()
            }
        }
        binding.btnPermissionDetailDT.setOnClickListener {
            if(permission_status != null && permission_approval_date != null && permission_letter_url != null){
                if(permission_status == "accept"){
                    val dialog = BottomSheetDialog(this)
                    val view = layoutInflater.inflate(R.layout.bottom_sheet_permsision_detail, null)
                    dialog.setCancelable(false)
                    dialog.setContentView(view)
                    dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    dialog.show()

                    val txtTitlePermDetail = view.findViewById<TextView>(R.id.txtTitlePermDetail)
                    txtTitlePermDetail.text = "Perizinan diterima pada $permission_approval_date"

                    val imgQRDT = view.findViewById<ImageView>(R.id.imgQRPermDetail)
                    Picasso.get().load(permission_qr_url).into(imgQRDT)
                    imgQRDT.isVisible = true

                    view.findViewById<ImageView>(R.id.txtInfoQRPermDetail).isVisible = true

                    view.findViewById<Button>(R.id.btnDownloadPerm).setOnClickListener {
                        val uri = Uri.parse(permission_letter_url)
                        val request: DownloadManager.Request = DownloadManager.Request(uri)
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        request.setTitle("ApartemenKu Tenant - Perizinan")
                        request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, "Perizinan-$transaction_id.pdf");
                        val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                        dm.enqueue(request)
                    }

                    view.findViewById<Button>(R.id.btnCloseDialogPerm).setOnClickListener {
                        dialog.dismiss()
                    }
                }
                else{
                    val dialog = BottomSheetDialog(this)
                    val view = layoutInflater.inflate(R.layout.bottom_sheet_permsision_detail, null)
                    dialog.setCancelable(false)
                    dialog.setContentView(view)
                    dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    dialog.show()

                    val txtTitlePermDetail = view.findViewById<TextView>(R.id.txtTitlePermDetail)
                    txtTitlePermDetail.text = "Perizinan ditolak pada $permission_approval_date"

                    view.findViewById<ImageView>(R.id.imgQRPermDetail).isVisible = false
                    view.findViewById<ImageView>(R.id.txtInfoQRPermDetail).isVisible = false

                    view.findViewById<Button>(R.id.btnDownloadPerm).setOnClickListener {
                        val uri = Uri.parse(permission_letter_url)
                        val request: DownloadManager.Request = DownloadManager.Request(uri)
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        request.setTitle("Downloading Surat Perizinan")
                        request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOCUMENTS, "Perizinan-$transaction_id.pdf");
                        val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                        dm.enqueue(request)
                    }

                    view.findViewById<Button>(R.id.btnCloseDialogPerm).setOnClickListener {
                        dialog.dismiss()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getSvcTrxData()
    }

    fun getSvcTrxData(){
        trxSvcItems.clear()
        trxSvcStatuses.clear()

        val q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "transaction/trxsvcdetail"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                Log.d("VOLLEY", it)
                val obj = JSONObject(it)
                if (obj.getString("status") == "success") {
                    val dataObj = obj.getJSONObject("data")

                    binding.txtUnitNameDTS.text = dataObj.getString("unit_apart")
                    unitphonenum = dataObj.getString("unit_phone")
                    delivery = dataObj.getString("delivery")
                    paymethod = dataObj.getString("payment")
                    binding.txtTrxDateDTS.text = dataObj.getString("transaction_date")
                    binding.txtFinishDateDTS.text = dataObj.getString("finish_date")
                    if(dataObj.getString("pickup_date") != ""){
                        binding.txtPickupDateDTS.text = dataObj.getString("pickup_date")
                    }
                    else{
                        binding.txtViewTrxPickupDateDTS.isVisible = false
                        binding.txtPickupDateDTS.isVisible = false
                    }

                    if(delivery == "delivery"){
                        binding.txtViewTrxDeliveryDTS.text = "Tanggal Pengerjaan"
                        binding.txtDeliveryDTS.text = "Pengerjaan di tempat"
                    }
                    else{
                        if(dataObj.getString("svc_type") == "laundry"){
                            binding.txtDeliveryDTS.text = "Taruh-Ambil Sendiri"
                        }
                        else {
                            binding.txtViewTrxDeliveryDTS.text = "Tanggal Pengerjaan"
                        }
                    }

                    if(paymethod == "transfer"){
                        binding.txtPayMethodDTS.text = "Transfer"
                        if(dataObj.getString("payment_proof_url") == ""){
                            transferNotPaid = true
                            binding.btnTFProofDTS.isVisible = false
                        }
                        else{
                            paymentproofurl = dataObj.getString("payment_proof_url")
                        }
                    }
                    else{
                        binding.txtPayMethodDTS.text = "Tunai"
                        binding.btnTFProofDTS.isVisible = false
                    }

                    status = dataObj.getString("status")
                    service_type = dataObj.getString("svc_type")
                    permit_need = dataObj.getInt("permission_need")

                    // Set Button
                    if(service_type == "laundry"){
                        if(status == "Menunggu Pengambilan"){
                            binding.btnCancelDTS.isVisible = false
                            binding.btnChangeStatusDTS.text = "Ambil Laundry"
                        } else if(status == "Menunggu Laundry"){
                            binding.btnCancelDTS.isVisible = false
                            binding.btnChangeStatusDTS.text = "Mulai Pengerjaan Laundry"
                        } else if(status == "Sedang dilakukan Pengambilan") {
                            binding.btnCancelDTS.isVisible = false
                            binding.btnChangeStatusDTS.text = "Mulai Pengerjaan Laundry"
                        } else if(status == "Sedang dikerjakan") {
                            binding.btnCancelDTS.isVisible = false
                            binding.btnChangeStatusDTS.text = "Pengerjaan Selesai"
                        } else if(status == "Siap diambil") {
                            binding.btnCancelDTS.isVisible = false
                            binding.btnChangeStatusDTS.text = "Selesaikan Transaksi"
                        }
                        else if(status == "Selesai dikerjakan") {
                            binding.btnCancelDTS.isVisible = false
                            binding.btnChangeStatusDTS.text = "Kirim Laundry"
                        }
                        else if(status == "Sedang Pengiriman") {
                            binding.btnCancelDTS.isVisible = false
                            binding.btnChangeStatusDTS.text = "Selesaikan Transaksi"
                        }
                    }
                    else{
                        if(permit_need == 1){
                            if(dataObj.getString("permission_status") == "accept" || dataObj.getString("permission_status")  == "reject"){
                                binding.btnPermissionDetailDT.isVisible = true
                                permission_status = dataObj.getString("permission_status")
                                permission_approval_date = dataObj.getString("permission_approval_date")
                                permission_letter_url = dataObj.getString("permission_letter")
                                if(permission_status == "accept") {
                                    permission_qr_url = dataObj.getString("permission_qr")
                                }
                            }
                            else{
                                binding.btnPermissionDetailDT.isVisible = false
                            }

                            if(status == "Dikonfirmasi") {
                                binding.btnCancelDTS.isVisible = false
                                binding.btnChangeStatusDTS.text = "Ajukan Perizinan"
                            }
                            else if(status == "Pengajuan Perizinan" || status == "Perizinan ditolak") {
                                binding.btnCancelDTS.isVisible = false
                                binding.btnChangeStatusDTS.isVisible = false
                            }
                            else if(status == "Menunggu Pengerjaan"){
                                binding.btnCancelDTS.isVisible = false
                                binding.btnChangeStatusDTS.text = "Mulai Pengerjaan"
                            }
                            else if(status == "Sedang dikerjakan"){
                                binding.btnCancelDTS.isVisible = false
                                binding.btnChangeStatusDTS.text = "Selesaikan Transaksi"
                            }
                        }
                        else{
                            if(status == "Menunggu Pengerjaan") {
                                binding.btnCancelDTS.isVisible = false
                                binding.btnChangeStatusDTS.text = "Menuju ke Lokasi"
                            }
                            else if(status == "Menuju Lokasi"){
                                binding.btnCancelDTS.isVisible = false
                                binding.btnChangeStatusDTS.text = "Mulai Pengerjaan"
                            }
                            else if(status == "Sedang dikerjakan"){
                                binding.btnCancelDTS.isVisible = false
                                binding.btnChangeStatusDTS.text = "Selesaikan Transaksi"
                            }
                        }
                    }
                    if(status == "Belum Pembayaran") {
                        if(permit_need == 0) {
                            binding.btnCancelDTS.isVisible = true
                            val cL = binding.constraintLayoutCardDTS
                            val cset = ConstraintSet()
                            cset.clone(cL)
                            cset.connect(
                                R.id.btnCancelDTS,
                                ConstraintSet.START,
                                R.id.constraintLayoutCardDTS,
                                ConstraintSet.START,
                                24
                            )
                            cset.connect(
                                R.id.btnCancelDTS,
                                ConstraintSet.END,
                                R.id.constraintLayoutCardDTS,
                                ConstraintSet.END,
                                24
                            )
                            cset.applyTo(cL)
                            val btnLParams = binding.btnCancelDTS.layoutParams
                            btnLParams.width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
                            binding.btnCancelDTS.layoutParams = (btnLParams)
                            binding.btnChangeStatusDTS.isVisible = false
                        }
                        else{
                            binding.btnCancelDTS.isVisible = false
                            binding.btnChangeStatusDTS.isVisible = false
                        }
                    }
                    else if(status == "Belum dikonfirmasi"){
                        binding.btnCancelDTS.isVisible = true
                        binding.btnChangeStatusDTS.text = "Konfirmasi"
                    }
                    else if(status == "Dibatalkan" || status=="Selesai"){
                        binding.btnCallUnitDTS.isVisible = false
                        binding.btnCancelDTS.isVisible = false
                        binding.btnChangeStatusDTS.isVisible = false
                    }

                    val total_payment = Helper.formatter(dataObj.getDouble("total_payment"))
                    binding.txtTotalPaymentDTS.text = "Rp$total_payment"

                    val itemArr = dataObj.getJSONArray("items")
                    for (i in 0 until itemArr.length()) {
                        val itemObj = itemArr.getJSONObject(i)
                        val item = TransactionDetailItem(itemObj.getInt("id"), itemObj.getString("name"), itemObj.getString("photo_url"), itemObj.getDouble("price"), itemObj.getInt("quantity"), itemObj.getString("pricePer"), itemObj.getDouble("subtotal"))
                        trxSvcItems.add(item)
                    }
                    updateListItems()

                    val statusArr = dataObj.getJSONArray("statuses")
                    for (i in 0 until statusArr.length()){
                        val statusObj = statusArr.getJSONObject(i)
                        val status = TrxStatus(statusObj.getString("date"), statusObj.getString("description"))
                        trxSvcStatuses.add(status)
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
        var recyclerView = binding.recViewItemsDTS
        recyclerView.layoutManager = lm
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = TrxDetailItemsAdapter(trxSvcItems, this)
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
                    getSvcTrxData()
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
        val url = Global.urlWS + "transaction/cancel"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                Log.d("VOLLEY", it)
                val obj = JSONObject(it)
                if (obj.getString("status") == "success") {
                    Toast.makeText(this, "Validasi Pembayaran Berhasil!", Toast.LENGTH_SHORT).show()
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
        val url = Global.urlWS + "transaction/validatetransfer"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                Log.d("VOLLEY", it)
                val obj = JSONObject(it)
                if (obj.getString("status") == "success") {
                    Toast.makeText(this, "Transaksi Berhasil dibatalkan!", Toast.LENGTH_SHORT).show()
                    getSvcTrxData()
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