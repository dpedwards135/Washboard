package com.davidparkeredwards.washboard

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_setup.*


class SetupActivity : AppCompatActivity(), EditOrderActivity {
    var stage = -1

    var order = Order()
    var checkedWindowTag = ""

    //val list = ArrayList<Window>()
    var radioGroup : RadioGroup? = null

    val mAuth = FirebaseAuth.getInstance()

    var userAddress = ""
    var userCityAndState = ""
    var userZip = ""
    var orderZip = ""
    var zipInfo : ZipInfo? = null
    var orderToEdit : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)
        toolbar.title = "Setup " + (stage + 1) + "/5"
        setSupportActionBar(toolbar)

        //list.clear()

        /*
        list.add(Window(1, 8, 12, 3, 8, 12, this))
        list.add(Window(3, 8, 12, 5, 8, 12, this))
        list.add(Window(6, 8, 12, 1, 8, 12, this))
        */

        orderToEdit = intent.getStringExtra("order_id")

        if(orderToEdit != "") {
            setOrder()
        }

        getUserAddress()
        updateFragment(false)




        fab.setOnClickListener { view ->
            updateFragment(false)
        }

        fab2.setOnClickListener { view ->
            this.onBackPressed()
        }
    }

    override fun onBackPressed() {
        if(stage > 0) updateFragment(true) else super.onBackPressed()
    }

    fun updateFragment(back: Boolean) {
        Log.i("Setup", "FAB")

        //saveData
        when(stage) {
            0 -> {}
            1 ->  {
                val group = findViewById<RadioGroup>(R.id.option_radios)
                when (group.checkedRadioButtonId) {
                    R.id.single_option -> order.orderType = "SINGLE"
                    R.id.standard_option -> order.orderType = "STANDARD"
                    R.id.enterprise_option -> order.orderType = "ENTERPRISE"
                    else -> { // Note the block
                        order.orderType = "INCOMPLETE"
                    }
                }
            }
            2 -> {

                if(findViewById<RadioButton>(radioGroup!!.checkedRadioButtonId) != null) {
                    val checkRadio = findViewById<RadioButton>(radioGroup!!.checkedRadioButtonId)
                    checkedWindowTag = checkRadio.tag as String

                    for(window in zipInfo!!.windows) {
                        if(window.id() == checkedWindowTag) {
                            order.window = window
                        }
                    }
                }


            }
            3 -> {

                order.soiled = (findViewById<CheckBox>(R.id.soiled_check).isChecked)
                if(order.soiled) {
                    order.soiledNote = (findViewById<EditText>(R.id.soiled_text)).text.toString()
                } else {
                    order.soiledNote = ""
                }

                order.cold = (findViewById<CheckBox>(R.id.cold_check).isChecked)
                if(order.cold) {
                    order.coldNote = (findViewById<EditText>(R.id.cold_text)).text.toString()
                } else {
                    order.coldNote = ""
                }

                order.softener = (findViewById<CheckBox>(R.id.softener_check).isChecked)
                if(order.softener) {
                    order.softenerNote = (findViewById<EditText>(R.id.softener_text)).text.toString()
                } else {
                    order.softenerNote = ""
                }
                order.notes = (findViewById<EditText>(R.id.additional_instructions)).text.toString()

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                if(currentFocus != null) {
                    imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0)
                }
            }
            4 -> {

                if(!back) saveOrder()
            }
        }

        //Load new fragment
        val ft = supportFragmentManager.beginTransaction()
        var fragment = android.support.v4.app.Fragment()
        stage = if(back) (stage -1) else (stage + 1)
        when (stage) {
            0 -> {
                fragment = IntroFragment()
                fab2.visibility = View.GONE
            }
            1 -> {
                fragment = OptionsFragment()
                fab2.visibility = View.VISIBLE
            }
            2 -> {
                fragment = WindowsFragment()
                fab.isEnabled = false
                radioGroup = RadioGroup(this)

                if (zipInfo != null) {
                    var radioCount = 0
                    for (window in zipInfo!!.windows) {
                        Log.i("Windows", "Add Window")
                        val radio = RadioButton(this)
                        radio.text = window.toText()
                        radio.tag = window.id()

                        radio.setOnClickListener(object : View.OnClickListener {
                            override fun onClick(p0: View?) {
                                fab.isEnabled = true

                            }
                        })

                        radio.setPadding(0, 0, 0, 20)
                        radioCount = radioCount + 1
                        radioGroup!!.addView(radio)

                        if (window.id() == checkedWindowTag) {
                            radio.isChecked = true
                            fab.isEnabled = true
                        }

                    }





                    fragment.radioGroup = radioGroup!!
                }
            }
            3 -> {
                fragment = InstructionsFragment()
                fab.isEnabled = true

            }
            4 -> {

                fragment = PaymentFragment()
                fab.isEnabled = true //change to false
            }
        }

        ft.replace(R.id.container_layout, fragment)
        ft.commit()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        if(stage == 0) {
            toolbar.title = "Welcome!"
        } else {
            toolbar.title = "Setup " + (stage) + "/4"
        }
    }

    fun setupInstructions() {
        var soiledBox = findViewById<CheckBox>(R.id.soiled_check)
        var coldBox = findViewById<CheckBox>(R.id.cold_check)
        var softenerBox = findViewById<CheckBox>(R.id.softener_check)

        var soiledTextView = (findViewById<EditText>(R.id.soiled_text))
        var coldTextView = (findViewById<EditText>(R.id.cold_text))
        var softenerTextView = (findViewById<EditText>(R.id.softener_text))

        soiledBox.setOnCheckedChangeListener{ soiledBox, isChecked ->
            Log.i("SETUP", "Soiled Box is " + isChecked)
            if(isChecked) soiledTextView.visibility = View.VISIBLE else soiledTextView.visibility = View.GONE
        }

        coldBox.setOnCheckedChangeListener{ coldBox, isChecked ->

            if(isChecked) coldTextView.visibility = View.VISIBLE else coldTextView.visibility = View.GONE
        }

        softenerBox.setOnCheckedChangeListener{ softenerBox, isChecked ->

            if(isChecked) softenerTextView.visibility = View.VISIBLE else softenerTextView.visibility = View.GONE
        }


        if(order.soiled) {
            soiledBox.isChecked = true
            soiledTextView.setText(order.soiledNote)
        }
        if(order.cold) {
            coldBox.isChecked = true
            coldTextView.setText(order.coldNote)
        }
        if(order.softener) {
            softenerBox.isChecked = true
            softenerTextView.setText(order.softenerNote)
        }


        if(order.address != "") {
            (findViewById<AutoCompleteTextView>(R.id.pickup_address)).setText(order.address)
            if(order.cityAndState != "") {
                (findViewById<AutoCompleteTextView>(R.id.pickup_city)).setText(order.cityAndState)
            } else {
                (findViewById<AutoCompleteTextView>(R.id.pickup_city)).setText(userCityAndState)
            }
            if(order.zip != "") {
                (findViewById<AutoCompleteTextView>(R.id.pickup_zip)).setText(order.zip)
            } else {
                (findViewById<AutoCompleteTextView>(R.id.pickup_zip)).setText(userCityAndState)
            }
        } else if(userZip == orderZip){
            (findViewById<AutoCompleteTextView>(R.id.pickup_address)).setText(userAddress)
        }







    }

    fun setupOptions() {
        //val group = findViewById<RadioGroup>(R.id.option_radios)
        findViewById<EditText>(R.id.zip_edittext).setText(orderZip)
        findViewById<Button>(R.id.update_button).setOnClickListener(object: View.OnClickListener {
            override fun onClick(p0: View?) {
                orderZip = (findViewById<EditText>(R.id.zip_edittext)).text.toString()
                getZipData(true)
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                if(currentFocus != null) {
                    imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0)
                }
            }
        })

        if(zipInfo == null || zipInfo?.serviced != true) {
            findViewById<RadioGroup>(R.id.option_radios).visibility = View.GONE
            findViewById<TextView>(R.id.options_intro).setText(getString(R.string.service_unavailable_zip))
            return
        } else {
            findViewById<RadioGroup>(R.id.option_radios).visibility = View.VISIBLE
            findViewById<TextView>(R.id.options_intro).setText(getString(R.string.options_intro))

        }

        findViewById<RadioButton>(R.id.single_option)
                .setText("$" + zipInfo!!.singlePrice + " " + getString(R.string.option1))
        findViewById<RadioButton>(R.id.standard_option)
                .setText("$" + zipInfo!!.standardPrice + " " + getString(R.string.option2))
        findViewById<RadioButton>(R.id.enterprise_option)
                .setText("$" + zipInfo!!.enterprisePrice + " " + getString(R.string.option3))

        when (order.orderType) {
            "SINGLE" ->  findViewById<RadioButton>(R.id.single_option).isChecked = true
            "STANDARD" -> findViewById<RadioButton>(R.id.standard_option).isChecked = true
            "ENTERPRISE" -> findViewById<RadioButton>(R.id.enterprise_option).isChecked = true
            else -> { // Note the block
                findViewById<RadioButton>(R.id.standard_option).isChecked = true
            }
        }

        fab.isEnabled = true
    }

    fun saveOrder() {
        val progressBar = findViewById<ProgressBar>(R.id.save_order_progress_bar)
        progressBar.animate()
        progressBar.visibility = View.VISIBLE



        val database = FirebaseDatabase.getInstance()
        val dbref = database.getReference("user/" + mAuth.currentUser?.uid + "/orders")
        dbref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if(dataSnapshot != null
                        && dataSnapshot.exists() ) {
                    if(order.id != null
                            && order.id != ""
                            && dataSnapshot.hasChildren()
                            && dataSnapshot.hasChild(order.id)) {
                        val dbOrder = order.toDb()
                        database.getReference("user/" + mAuth.currentUser?.uid + "/orders/" + order.id).updateChildren(dbOrder)
                        Log.i("SETUP", "UpdateChildren")
                    } else {
                        order.id = dbref.push().key
                        val dbOrder = order.toDb()
                        database.getReference("user/" + mAuth.currentUser?.uid + "/orders/" + order.id).setValue(dbOrder)
                        Log.i("SETUP", "SetValue 1")
                    }

                    progressBar.visibility = View.GONE
                }
                else {
                    order.id = dbref.push().key
                    val dbOrder = order.toDb()
                    database.getReference("user/" + mAuth.currentUser?.uid + "/orders/" + order.id).setValue(dbOrder)
                    progressBar.visibility = View.GONE
                    Log.i("SETUP", "SetValue 2")
                }
            }

            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }

    fun getUserAddress(){

            val mAuth = FirebaseAuth.getInstance()
            val dbref = FirebaseDatabase.getInstance().getReference("user/" + mAuth.currentUser?.uid)
            dbref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    if (dataSnapshot != null
                            && dataSnapshot.exists()
                            && dataSnapshot.hasChildren()
                            && dataSnapshot.hasChild("street_address")) {
                        userAddress = dataSnapshot.child("street_address").value as String
                        userCityAndState = dataSnapshot.child("city_state").value as String
                        userZip = dataSnapshot.child("zip").value as String
                        orderZip = userZip
                        getZipData(false)

                        Log.i("SETUP", "Get Address")
                    }
                }

                    override fun onCancelled(p0: DatabaseError?) {

                    }

                })
    }

    fun getZipData(changingZip : Boolean) {

        val dbref = FirebaseDatabase.getInstance().getReference("zip/" + orderZip)
        dbref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (dataSnapshot != null
                        && dataSnapshot.exists()
                        && dataSnapshot.hasChildren()
                        && dataSnapshot.hasChild("serviced")) {

                    zipInfo = ZipInfo().fromDb(dataSnapshot, this@SetupActivity)

                    if(changingZip) {
                        setupOptions()
                    }

                    Log.i("SETUP", "Get Address")
                }  else {
                Toast.makeText(this@SetupActivity, "Service not available for this zip code", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(p0: DatabaseError?) {

            }

        })
    }

    fun updateFBZipCodes() {
        val zips = ArrayList<String>()
        zips.add("84111")
        zips.add("84101")
        zips.add("84105")
        zips.add("84112")
        zips.add("84103")
        zips.add("84102")
        //Set ZipData
        for(zip in zips) {
            var zipInfo = ZipInfo()
            zipInfo.zipCode = zip
            zipInfo.serviced = true
            zipInfo.singlePrice = 25
            zipInfo.enterprisePrice = 18
            zipInfo.standardPrice = 20
            zipInfo.windows = ArrayList<Window>() //Add windows here

            var dbZipInfo = zipInfo.toDb()

            FirebaseDatabase.getInstance().getReference("zip/" + zipInfo.zipCode).setValue(dbZipInfo)
        }
    }

    fun setOrder() {
        val string = "user/" + mAuth.currentUser!!.uid + "/orders/" + orderToEdit
        Log.i("MAIN", "String: " + string)
        val dbref = FirebaseDatabase.getInstance().getReference(string)
        dbref.addValueEventListener((object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                Log.i("MAIN", "Getting orders")
                if (dataSnapshot != null) {
                    Log.i("MAIN", "Getting orders 2")
                    //orders = Order.ordersFromDb(dataSnapshot.value as HashMap<String, Any>, this@MainActivity)
                    order = Order().fromDb(dataSnapshot.value as HashMap<String, Any>, this@SetupActivity)
                    orderZip = order.zip

                }  else {

                }
            }

            override fun onCancelled(p0: DatabaseError?) {

            }

        }))
    }

    override fun onRadioClick() {
        fab.isEnabled = true
    }
}
