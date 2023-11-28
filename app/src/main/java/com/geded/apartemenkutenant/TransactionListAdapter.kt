package com.geded.apartemenkutenant

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.geded.apartemenkutenant.databinding.LayoutTransactionListBinding
import com.squareup.picasso.Picasso

class TransactionListAdapter( val transactions:ArrayList<TransactionList>, val context: FragmentActivity?):
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
        }
    }
}