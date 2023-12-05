package com.geded.apartemenkutenant

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.geded.apartemenkutenant.databinding.ActivityDetailTransactionServiceBinding

class DetailTransactionServiceActivity : AppCompatActivity() {
    private lateinit var binding:ActivityDetailTransactionServiceBinding
    var trxSvcItems:ArrayList<TransactionDetailItem> = arrayListOf()
    var trxSvcStatuses:ArrayList<TrxStatus> =  arrayListOf()
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

        transaction_id = intent.getIntExtra(DetailTransactionProductActivity.TRANSACTION_ID, 0)
    }

    fun getSvcTrxData(){
        trxSvcItems.clear()
        trxSvcStatuses.clear()
    }
}