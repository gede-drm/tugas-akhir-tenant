package com.geded.apartemenkutenant

import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.geded.apartemenkutenant.databinding.LayoutTransactionListBinding

class TransactionHistoryListAdapter( val trxHistories:ArrayList<TransactionList>, val context: FragmentActivity?):
    RecyclerView.Adapter<TransactionHistoryListAdapter.TransactionHistoryListViewHolder>() {
    class TransactionHistoryListViewHolder(val binding: LayoutTransactionListBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionHistoryListViewHolder {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        return trxHistories.size
    }

    override fun onBindViewHolder(holder: TransactionHistoryListViewHolder, position: Int) {
        with(holder.binding) {
        }
    }
}