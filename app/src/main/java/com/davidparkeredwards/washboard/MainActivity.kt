package com.davidparkeredwards.washboard

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val mAuth = FirebaseAuth.getInstance()
    var orders = ArrayList<Order>()
    var mode = -1

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.pickup_view -> {

                val ft = supportFragmentManager.beginTransaction()// fragmentManager.beginTransaction()
                ft.replace(R.id.main_container_layout, PickupFragment(), "PICKUP_FRAGMENT")
                ft.commit()
                mode = 0

                return@OnNavigationItemSelectedListener true
            }
            R.id.orders_view -> {

                val ft = supportFragmentManager.beginTransaction()// fragmentManager.beginTransaction()
                ft.replace(R.id.main_container_layout, OrdersFragment(), "ORDER_FRAGMENT")
                ft.commit()

                mode = 1

                return@OnNavigationItemSelectedListener true
            }
            R.id.account_view -> {

                val ft = supportFragmentManager.beginTransaction()// fragmentManager.beginTransaction()
                ft.replace(R.id.main_container_layout, AccountFragment(), "ACCOUNT_FRAGMENT")
                ft.commit()
                mode = 2

                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        getOrders()
    }

    fun getOrders() {
        val string = "user/" + mAuth.currentUser!!.uid + "/orders/"
        Log.i("MAIN", "String: " + string)
        val dbref = FirebaseDatabase.getInstance().getReference(string)
        dbref.addValueEventListener((object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                Log.i("MAIN", "Getting orders")
                if (dataSnapshot != null) {
                    Log.i("MAIN", "Getting orders 2")
                    orders = Order.ordersFromDb(dataSnapshot.value as HashMap<String, Any>, this@MainActivity)

                    updateUI()
                }  else {

                }
            }

            override fun onCancelled(p0: DatabaseError?) {

            }

        }))
    }

    fun updateUI() {

        if(mode == 1) {
            (supportFragmentManager.findFragmentByTag("ORDER_FRAGMENT") as OrdersFragment).updateOrders(orders)
        }
    }

}
