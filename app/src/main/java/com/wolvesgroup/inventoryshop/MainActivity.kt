package com.wolvesgroup.inventoryshop

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    val ref = FirebaseDatabase.getInstance().getReference("Inventory")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ref.addValueEventListener(eventListener)
    }

    private val eventListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val mResourceList: ArrayList<ProductModel> = ArrayList()
            for (ds in dataSnapshot.getChildren()) {
                val model: ProductModel? = ds.getValue(ProductModel::class.java)
                if (model != null) {
                    mResourceList.add(model)
                }
            }
            val adapter = InventoryAdapter(this@MainActivity, mResourceList)
            findViewById<RecyclerView>(R.id.rv_products).adapter = adapter
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Toast.makeText(this@MainActivity, "" + databaseError.message, Toast.LENGTH_SHORT).show()
        }
    }
}