package com.geded.apartemenkutenant

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.geded.apartemenkutenant.databinding.LayoutTransactionListBinding
import com.squareup.picasso.Picasso

class TransactionListAdapter( val transactions:ArrayList<TransactionList>, val tenant_type:String, val context: FragmentActivity?):
    RecyclerView.Adapter<TransactionListAdapter.TransactionListViewHolder>() {
    class TransactionListViewHolder(val binding: LayoutTransactionListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionListViewHolder {
        val binding = LayoutTransactionListBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return TransactionListViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    override fun onBindViewHolder(holder: TransactionListViewHolder, position: Int) {
        val url = transactions[position].imgProduct
        val price = Helper.formatter(transactions[position].totalTransaction)
        with(holder.binding) {
            Picasso.get().load(url).into(imgProductTL)
            txtUnitTL.text = transactions[position].unit_no
            txtDateTL.text = "("+transactions[position].date.substring(0, 10)+")"
            txtTotalPriceTL.text = "Rp$price"
            txtStatusTL.text = transactions[position].status
            txtProNameTL.text = transactions[position].productName
            txtProQtyTL.text = transactions[position].productQty.toString() + " Barang"
            if(transactions[position].remainingProductQty == 0){
                txtRemainingQtyTL.text = ""
            }
            else{
                txtRemainingQtyTL.text = "+" + transactions[position].remainingProductQty.toString() + " Item lainnya"
            }
            if(transactions[position].status == "Dibatalkan"){
                txtStatusTL.setTextColor(ContextCompat.getColor(txtStatusTL.context, R.color.error_bg))
            }
            else{
                txtStatusTL.setTextColor(ContextCompat.getColor(txtStatusTL.context, R.color.blue_500))
            }
        }
        holder.binding.btnDetailTL.setOnClickListener {
            if (tenant_type == "product"){
                val intent = Intent(this.context, DetailTransactionProductActivity::class.java)
                intent.putExtra(
                    DetailTransactionProductActivity.TRANSACTION_ID,
                    transactions[position].id
                )
                context?.startActivity(intent)
            }
            else{
                val intent = Intent(this.context, DetailTransactionServiceActivity::class.java)
                intent.putExtra(
                    DetailTransactionServiceActivity.TRANSACTION_ID,
                    transactions[position].id
                )
                context?.startActivity(intent)
            }
        }
    }
}