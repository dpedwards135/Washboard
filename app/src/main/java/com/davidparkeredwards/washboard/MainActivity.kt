package com.davidparkeredwards.washboard

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*



class MainActivity : AppCompatActivity() {

    val user = User()
    val mAuth = FirebaseAuth.getInstance()
    var orders = ArrayList<Order>()
    var mode = -1
    var ordersFragment = OrdersFragment()
    lateinit var editOrderController : EditOrderController


    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.order_history -> {

                val ft = supportFragmentManager.beginTransaction()// fragmentManager.beginTransaction()
                ft.replace(R.id.main_container_layout, PickupFragment(), "PICKUP_FRAGMENT")
                ft.commit()
                mode = 0

                return@OnNavigationItemSelectedListener true
            }
            R.id.orders_view -> {

                val ft = supportFragmentManager.beginTransaction()// fragmentManager.beginTransaction()
                ordersFragment = OrdersFragment()
                ft.replace(R.id.main_container_layout, ordersFragment, "ORDER_FRAGMENT")
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

        mOnNavigationItemSelectedListener.onNavigationItemSelected(navigation.getMenu().getItem(0))
    }

    fun getOrders() {
        val string = "user/" + mAuth.currentUser!!.uid
        Log.i("MAIN", "String: " + string)
        val dbref = FirebaseDatabase.getInstance().getReference(string)
        dbref.addValueEventListener((object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                Log.i("MAIN", "Getting orders")
                if (dataSnapshot != null
                        && dataSnapshot.exists()
                        && dataSnapshot.hasChildren()) {


                    val orderSnapshot = dataSnapshot.child("/orders/").value as HashMap<String, Any>
                    Log.i("MAIN", "Getting orders 2")
                    orders = Order.ordersFromDb(orderSnapshot, this@MainActivity)


                    user.name = dataSnapshot.child("name").value as String
                    user.email = dataSnapshot.child("email").value as String
                    user.address = dataSnapshot.child("street_address").value as String
                    user.cityState = dataSnapshot.child("city_state").value as String
                    user.zip = dataSnapshot.child("zip").value as String
                    user.phone = dataSnapshot.child("phone").value as String

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

        if(mode == 2) {


            findViewById<TextView>(R.id.email_display).setText(user.email)
            findViewById<TextView>(R.id.name_display).setText(user.name)
            findViewById<TextView>(R.id.address_display).setText(user.address)
            findViewById<TextView>(R.id.city_display).setText(user.cityState)
            findViewById<TextView>(R.id.zip_display).setText(user.zip)
            findViewById<TextView>(R.id.phone_display).setText(user.phone)
            findViewById<Button>(R.id.edit_account).setOnClickListener(object: View.OnClickListener{
                override fun onClick(p0: View?) {
                    editAccount() //To change body of created functions use File | Settings | File Templates.
                }
            })
            findViewById<Button>(R.id.reset_password_button).setOnClickListener(object: View.OnClickListener{
                override fun onClick(p0: View?) {
                    resetPassword()
                }
            })

            findViewById<Button>(R.id.logout_button).setOnClickListener(object: View.OnClickListener{
                override fun onClick(p0: View?) {
                    logout()
                }
            })
        }
    }

    fun saveFromSingleView(view: View) {
        (supportFragmentManager.findFragmentByTag("ORDER_FRAGMENT") as OrdersFragment).saveFromSingleView()
    }

    fun pauseOrder(view: View) {
        (supportFragmentManager.findFragmentByTag("ORDER_FRAGMENT") as OrdersFragment).pauseOrder()

    }

    fun editAccount() {

        var alert = AlertDialog.Builder(this)

        val layoutInflater = layoutInflater
        val editAccountView = layoutInflater.inflate(R.layout.create_account_dialog,null)
        alert.setTitle(getString(R.string.edit_account))
        alert.setView(editAccountView)
        alert.setCancelable(true)

        val a : AlertDialog = alert.create()
        a.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        a.show()


        editAccountView.findViewById<TextView>(R.id.email_in).setText(user.email)
        editAccountView.findViewById<AutoCompleteTextView>(R.id.email_in).isEnabled = false
        editAccountView.findViewById<TextView>(R.id.name_in).setText(user.name)
        editAccountView.findViewById<TextView>(R.id.address_in).setText(user.address)
        editAccountView.findViewById<TextView>(R.id.city_in).setText(user.cityState)
        editAccountView.findViewById<TextView>(R.id.zip_in).setText(user.zip)
        editAccountView.findViewById<TextView>(R.id.phone_in).setText(user.phone)
        editAccountView.findViewById<TextView>(R.id.password_in).visibility = View.GONE
        editAccountView.findViewById<Button>(R.id.go_create_account).setText(getString(R.string.save_changes))

        editAccountView.findViewById<Button>(R.id.go_create_account).setOnClickListener {
            val username = editAccountView.findViewById<AutoCompleteTextView>(R.id.email_in)
            val name = editAccountView.findViewById<AutoCompleteTextView>(R.id.name_in)
            val streetAddress = editAccountView.findViewById<AutoCompleteTextView>(R.id.address_in)
            val cityAndState = editAccountView.findViewById<AutoCompleteTextView>(R.id.city_in)
            val zip = editAccountView.findViewById<AutoCompleteTextView>(R.id.zip_in)
            val phone = editAccountView.findViewById<AutoCompleteTextView>(R.id.phone_in)

            val userRef = FirebaseDatabase.getInstance().getReference("user/" + mAuth.currentUser?.uid)
            userRef.updateChildren(hashMapOf<String, String>(
                    "name" to name?.text.toString(),
                    "email" to username?.text.toString(),
                    "street_address" to streetAddress.text.toString(),
                    "city_state" to cityAndState.text.toString(),
                    "zip" to zip.text.toString(),
                    "phone" to phone.text.toString(),
                    "finished_setup" to "false"
            ) as Map<String, Any>)
            a.dismiss()
        }

    }

    fun resetPassword() {
        var auth = FirebaseAuth.getInstance()
        var emailAddress = user.email

        auth.sendPasswordResetEmail(emailAddress).addOnCompleteListener(object: OnCompleteListener<Void> {
            override fun onComplete(p0: Task<Void>) {
                if(p0.isSuccessful) {
                    Toast.makeText(this@MainActivity,getString(R.string.reset_email_sent),Toast.LENGTH_SHORT).show()
                }
            }
        })

    }

    fun logout() {
        var auth = FirebaseAuth.getInstance()
        auth.signOut()

        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
