package com.example.spendysenseapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spendysenseapp.R
import com.example.spendysenseapp.RoomDB.Transaction
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter(private var transactionList: MutableList<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>(){

        //holds reference to each item UI elements
        class TransactionViewHolder(view : View) : RecyclerView.ViewHolder(view) {
            val transactionName: TextView = view.findViewById(R.id.transactionNameTv)
            val transactionDate: TextView = view.findViewById(R.id.transactionDateTv)
            val transactionAmount: TextView = view.findViewById(R.id.transactionAmountTv)
        }

    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    //create and return a new viewholder when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_list_item, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactionList[position]
        holder.transactionName.text = transaction.name
        holder.transactionDate.text = formatDate(transaction.DateCreated)
        holder.transactionAmount.text = "%.2f".format(transaction.amount) // Better number formatting
    }

    private fun formatDate(date: Date): String {
        return dateFormatter.format(date)
    }

    override fun getItemCount(): Int = transactionList.size

    fun updateData(newTransactions: List<Transaction>){
        transactionList.clear()
        transactionList.addAll(newTransactions)
        notifyDataSetChanged()
    }
}