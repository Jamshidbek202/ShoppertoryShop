package com.wolvesgroup.inventoryshop

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class InventoryAdapter(context: Context, list: ArrayList<ProductModel>) :
    RecyclerView.Adapter<InventoryAdapter.MyViewHolder>() {
    var context: Context
    var list: ArrayList<ProductModel>

    init {
        this.context = context
        this.list = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v: View =
            LayoutInflater.from(parent.context).inflate(R.layout.product_item, parent, false)
        return MyViewHolder(v)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.txt_name.text = list[position].name
        holder.txt_price.text = "$"+list[position].price

        holder.itemView.setOnClickListener {
            val view1 = LayoutInflater.from(context).inflate(R.layout.buy_product_bs, null)

            val edt_quantity = view1.findViewById<EditText>(R.id.edt_product_quantity)
            val btn_buy = view1.findViewById<MaterialButton>(R.id.btn_buy)

            val bottomSheetDialog = BottomSheetDialog(context)
            bottomSheetDialog.setContentView(view1)
            bottomSheetDialog.show()

            btn_buy.setOnClickListener {

                if (list[position].quantity.toInt() >= edt_quantity.text.toString().toInt()){

                    list[position].sales_num = (list[position].sales_num.toInt()+edt_quantity.text.toString().toInt()).toString()
                    list[position].quantity = (list[position].quantity.toInt() - edt_quantity.text.toString().toInt()).toString()
                    val revenue = list[position].price.toInt()*edt_quantity.text.toString().toInt()

                    FirebaseDatabase.getInstance().getReference("Inventory").child(list[position].key).setValue(list[position])

                    FirebaseDatabase.getInstance().getReference("Statistics").child("total_revenue").addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val revenue_then = snapshot.getValue().toString()

                            var new_revenue = revenue_then.toInt()+revenue

                            FirebaseDatabase.getInstance().getReference("Statistics").child("total_revenue").setValue(new_revenue.toString())
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, ""+error, Toast.LENGTH_SHORT).show()
                            bottomSheetDialog.dismiss()
                        }

                    })

                    FirebaseDatabase.getInstance().getReference("Statistics").child("total_sales").addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val sales_then = snapshot.getValue().toString()

                            var new_sales = sales_then.toInt()+edt_quantity.text.toString().toInt()

                            FirebaseDatabase.getInstance().getReference("Statistics").child("total_sales").setValue(new_sales.toString())
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, ""+error, Toast.LENGTH_SHORT).show()
                            bottomSheetDialog.dismiss()
                        }

                    })

                    val currentDateTime = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofPattern("HH-MMMM-yyyy")
                    val time = currentDateTime.format(formatter)
                    val key = UUID.randomUUID().toString()

                    val historyModel = HistoryModel(key, list[position].name, list[position].key, edt_quantity.text.toString(), revenue.toString(), time)

                    FirebaseDatabase.getInstance().getReference("History").child(key).setValue(historyModel)

                    bottomSheetDialog.dismiss()

                } else {
                    Toast.makeText(context, "There is not enough product for your order!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txt_name: TextView
        var txt_price: TextView

        init {
            txt_name = itemView.findViewById(R.id.txt_resource_name)
            txt_price = itemView.findViewById(R.id.txt_resource_price)
        }
    }
}
