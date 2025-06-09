package com.example.spendysenseapp.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.spendysenseapp.R
import com.example.spendysenseapp.RoomDB.Transaction
import java.text.SimpleDateFormat
import java.util.Locale


//this is the main adapter class for the transactions recycler view
//present in both view all transaction and the home view
//it contains the name, the date and the amount for that transaction
//it also contains the ability to click the recycler view item itself
//in order to navigate to the transactions details
class TransactionAdapter(
    private var transactionList: MutableList<Transaction>,
    private var onTransactionClick: (String) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>(){

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
        holder.transactionDate.text = formatDate(transaction.dateCreated)
        holder.transactionAmount.text = "R%.2f".format(transaction.amount)
        //this is the code that navigates to the transactions details
        holder.itemView.setOnClickListener {
            onTransactionClick(transaction.id)
        }
    }

    private fun formatDate(date: Long): String {
        return dateFormatter.format(date)
    }

    override fun getItemCount(): Int = transactionList.size

    fun updateData(newTransactions: List<Transaction>){
        transactionList.clear()
        transactionList.addAll(newTransactions)
        notifyDataSetChanged()
    }
}