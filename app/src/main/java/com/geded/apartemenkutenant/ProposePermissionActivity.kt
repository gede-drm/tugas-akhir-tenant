package com.geded.apartemenkutenant

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenkutenant.databinding.ActivityProposePermissionBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.Calendar
import java.util.Date

class ProposePermissionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProposePermissionBinding
    var workers:ArrayList<Worker> = arrayListOf()
    var transaction_id = 0
    var token = ""
    var finish_date = ""
    var date_chose = ""
    var time_chose = ""
    companion object{
        val TRANSACTION_ID = "TRANSACTION_ID"
        val FINISH_DATE = "FINISH_DATE"
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProposePermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        token = shared.getString(LoginActivity.TOKEN.toString(),"").toString()

        transaction_id = intent.getIntExtra(TRANSACTION_ID, 0)
        finish_date = intent.getStringExtra(FINISH_DATE).toString()

        binding.txtFinishDatePerm.text = finish_date

        binding.txtDatePerm.setOnClickListener {
            val finishDateFormatter = SimpleDateFormat("yyyy-MM-dd")
            val finish_dateDate:Date = finishDateFormatter.parse(finish_date.substring(0,10))
            val finish_dateMillis = finish_dateDate.time

            val validator = listOf(DateValidatorPointForward.from(finish_dateMillis))
            val calendarConstraintBuilder = CalendarConstraints.Builder().setValidator(
                CompositeDateValidator.allOf(validator))
            val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Pilih Tanggal Pengerjaan").setSelection(
                MaterialDatePicker.todayInUtcMilliseconds()).setCalendarConstraints(calendarConstraintBuilder.build()).build()
            datePicker.addOnPositiveButtonClickListener {
                binding.txtTimePerm.setText("")
                time_chose = ""

                val txtDateFormatter = SimpleDateFormat("dd-MM-yyyy")
                val modelDateFormatter = SimpleDateFormat("yyyy-MM-dd")
                val txtDate = txtDateFormatter.format(Date(it))
                val modelDate = modelDateFormatter.format(Date(it))

                binding.txtDatePerm.setText(txtDate)
                date_chose = modelDate.toString()
            }
            datePicker.show(this.supportFragmentManager, "DATE_PICKER_SERVICE")
        }

        binding.txtTimePerm.setOnClickListener {
            if(binding.txtDatePerm.text.toString() != "") {
                val timePicker = MaterialTimePicker.Builder().setTitleText("Pilih Jam Ambil/Kirim")
                    .setTimeFormat(TimeFormat.CLOCK_24H).build()
                timePicker.addOnPositiveButtonClickListener {
                    val time = timePicker.hour.toString().padStart(2, '0') + ":" + timePicker.minute.toString().padStart(2, '0')

                    val c = Calendar.getInstance();
                    val df = SimpleDateFormat("yyyy-MM-dd")
                    val tf = SimpleDateFormat("HH:mm");
                    val timeSelected = LocalTime.parse(time)
                    val timeNow = LocalTime.parse(tf.format(c.getTime()))
                    val finishTime = LocalTime.parse(finish_date.substring(11, 16))
                    if(df.format(c.getTime()) == finish_date.substring(0, 10)){
                        if(timeSelected.isAfter(timeNow)){
                            if(timeSelected.isAfter(finishTime)) {
                                binding.txtTimePerm.setText(time)
                                time_chose = time
                            }
                            else{
                                Toast.makeText(this, "Waktu harus lebih dari waktu pengerjaan!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        else{
                            Toast.makeText(this, "Waktu harus lebih dari saat ini!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else {
                        binding.txtTimePerm.setText(time)
                        time_chose = time
                    }
                }
                timePicker.show(this.supportFragmentManager, "TIME_PICKER_SERVICE")
            }
            else{
                Toast.makeText(this, "Silakan pilih tanggal selesai terlebih dahulu!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnPermAddWorker.setOnClickListener {
            val name = binding.txtPermWName.text.toString()
            val nik = binding.txtPermWNIK.text.toString()
            if(name != "" && nik != "") {
                val worker = Worker(name, nik)
                workers.add(worker)

                var list = ""
                workers.forEachIndexed { idx, it ->
                    if(idx != workers.size-1) {
                        list += "${idx + 1}. ${it.name} (${it.nik})\n"
                    }
                    else{
                        list += "${idx + 1}. ${it.name} (${it.nik})"
                    }
                }
                binding.txtPermWList.text = list
                binding.txtPermWName.setText("")
                binding.txtPermWNIK.setText("")
            }
            else{
                Toast.makeText(this, "Nama dan NIK Pekerja tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnClearWList.setOnClickListener {
            workers.clear();
            binding.txtPermWList.text = "Belum ada pekerja"
        }

        binding.btnProposePermission.setOnClickListener {
            val description = binding.txtPermDesc.text.toString()
            if(workers.size > 0 && description != "" && date_chose != "" && time_chose != "") {
                proposePermission(description)
            }
            else {
                Toast.makeText(this, "Pastikan Semua Data telah Terisi!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun proposePermission(description:String){
        val q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "transaction/proposepermission"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
                Log.d("VOLLEY", it)
                val obj = JSONObject(it)
                if (obj.getString("status") == "success") {
                    Toast.makeText(this, "Pengajuan Perizinan Berhasil!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                else if(obj.getString("status") == "exist") {
                    val builder = AlertDialog.Builder(this)
                    builder.setCancelable(false)
                    builder.setTitle("Pernah Diajukan")
                    builder.setMessage("Pengajuan Perizinan untuk Transaksi ini Telah Pernah dilakukan Sebelumnya!")
                    builder.setPositiveButton("OK") { dialog, which ->
                        finish()
                    }
                    builder.create().show()
                }
                else {
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
                params["description"] = description.toString()
                params["end_date"] = "$date_chose $time_chose:00"
                workers.forEachIndexed { idx, it ->
                    params["workers_name[$idx]"] = it.name
                    params["workers_nik[$idx]"] = it.nik
                }
                params["token"] = token
                return params
        }
    }
    stringRequest.setShouldCache(false)
    q.add(stringRequest)
    }
}