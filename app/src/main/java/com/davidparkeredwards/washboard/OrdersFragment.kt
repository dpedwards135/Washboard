package com.davidparkeredwards.washboard


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [OrdersFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OrdersFragment : Fragment(), EditOrderDelegate {

    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null

    var orders = ArrayList<Order>()
    var displayOrderIndex = -1

    lateinit var editOrderController: EditOrderController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments?.getString(ARG_PARAM1)
            mParam2 = arguments?.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_orders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateOrders((activity as MainActivity).orders)

    }

    fun updateOrders(orders: ArrayList<Order>) {
        this.orders = orders



        if (view != null && context != null) {
            val stringArray = ArrayList<String>()
            stringArray.add(context!!.getString(R.string.orders))
            for (order in orders) {
                if (order.window != null) {
                    stringArray.add(order.window!!.toText())
                } else {
                    stringArray.add(order.id)
                }
            }
            val spinner = view!!.findViewById<Spinner>(R.id.order_spinner)
            val adapter = ArrayAdapter<String>(context, R.layout.support_simple_spinner_dropdown_item, stringArray)

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    if (stringArray.count() > 0)
                        displayOrderIndex = 0
                    updateOrderUI()
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position == 0) return
                    displayOrderIndex = position - 1
                    updateOrderUI()
                }

            }
            spinner.adapter = adapter
        }

        if (!orders.isEmpty() && displayOrderIndex == -1) {
            displayOrderIndex = 0
            updateOrderUI()
        }

    }

    fun updateOrderUI() {
        if (displayOrderIndex == -1) {
            view!!.findViewById<TextView>(R.id.order_title).setText(getString(R.string.no_orders_available))
            view!!.findViewById<LinearLayout>(R.id.order_info).visibility = View.VISIBLE
            return
        }


        val order = orders[displayOrderIndex]
        if (order.window == null) return

        if (view != null) {
            view!!.findViewById<TextView>(R.id.order_title).setText(getString(R.string.order_id) + order.id)
            view!!.findViewById<LinearLayout>(R.id.order_info).visibility = View.VISIBLE

            var text = ""
            if (order.paused) text = getString(R.string.paused) else text = getString(R.string.active)
            view!!.findViewById<TextView>(R.id.active_or_paused_text).setText(text)
            if (!order.paused) text = getString(R.string.paused) else text = getString(R.string.active)
            view!!.findViewById<Button>(R.id.pause_button).setText(text)

            var isToday = LocalDate.now().dayOfWeek.value == order.window!!.pickupDay + 1
            var pickupIsPast = LocalTime.now().hour > order.window!!.pickupStart

            var nextPickup = getString(R.string.next_pickup_is) +  " "
            if (isToday && !pickupIsPast) {
                nextPickup = nextPickup + getString(R.string.today)
            } else if (order.window!!.pickupDay < LocalDate.now().dayOfWeek.value) {
                nextPickup = nextPickup + order.window!!.dayList.get(order.window!!.pickupDay)
            }
            if(order.paused) nextPickup = getString(R.string.order_is_paused)
            view!!.findViewById<TextView>(R.id.next_pickup).setText(nextPickup)

            view!!.findViewById<TextView>(R.id.pickup_dropoff_text).setText(order.window!!.toText())

            var string = ""
            if (order.cold) string = string + getString(R.string.cold) + " " + getString(R.string.yes) + " - " + order.coldNote + "\n"
            if (order.soiled) string = string + getString(R.string.soiled) + getString(R.string.yes) + " - " + order.soiledNote + "\n"
            if (order.softener) string = string + getString(R.string.softener) + getString(R.string.yes) + " - " + order.softenerNote + "\n"
            string = string + order.notes

            view!!.findViewById<TextView>(R.id.view_notes).setText(string)
            view!!.findViewById<Button>(R.id.edit_button).setOnClickListener(object : View.OnClickListener {
                override fun onClick(p0: View?) {
                    editOrder(true)
                }
            })


        }

    }

    fun editOrder(editing: Boolean) {

        if (activity != null) {
            if (editing) {
                activity!!.findViewById<FrameLayout>(R.id.edit_order_container).visibility = View.VISIBLE
                val ft = activity!!.supportFragmentManager.beginTransaction()// fragmentManager.beginTransaction()
                ft.replace(R.id.edit_order_container, EditOrderFragment(), "EDIT_ORDER_FRAGMENT")
                ft.commit()


                activity!!.findViewById<LinearLayout>(R.id.order_info).visibility = View.GONE
                activity!!.findViewById<Spinner>(R.id.order_spinner).visibility = View.GONE
            } else {
                activity!!.findViewById<FrameLayout>(R.id.edit_order_container).visibility = View.GONE
                activity!!.findViewById<LinearLayout>(R.id.order_info).visibility = View.VISIBLE
                activity!!.findViewById<Spinner>(R.id.order_spinner).visibility = View.VISIBLE

            }

                editOrderController = EditOrderController(activity as MainActivity, this)
                (activity as MainActivity).editOrderController = editOrderController


                editOrderController.setup(orders.get(displayOrderIndex))

                //editOrderController.orderZip = orders.get(displayOrderIndex).zip

                Log.i("OrdersFragment", "Check 2" + editOrderController.orderZip + "     xx   " + displayOrderIndex + " xx " + orders.get(displayOrderIndex).zip)

                val ft = activity!!.supportFragmentManager.beginTransaction()
                var fragment = Fragment()
                var layout = 0
                for (i in 1..4) {
                    Log.i("OrdersFragment", "Loop")
                    fragment = editOrderController.getFragment(i)
                    when (i) {
                        1 -> layout = (R.id.options_fragment_container)
                        2 -> layout = R.id.windows_fragment_container
                        3 -> layout = R.id.instructions_fragment_container
                        4 -> layout = R.id.payment_fragment_container
                    }

                    ft.replace(layout, fragment)
                }
                Log.i("OrdersFragment", "Check 1")
                ft.commit()

                editOrderController.setup(orders.get(displayOrderIndex))


            }
        }


    fun updateEditOrderFields() {

    }

    override fun onRadioClick() {
        updateEditOrderFields()
    }

    fun saveFromSingleView() {
        editOrderController.saveFromSingleView()
        editOrder(false)
        orders.set(displayOrderIndex, editOrderController.order)
        (activity as MainActivity).orders = orders
        updateOrderUI()
    }


    override fun setupOptions() {
        Log.i("OrdersFragment", "SetupOptions()")
        editOrderController.setupOptions()
    }

    fun pauseOrder() {
        orders[displayOrderIndex].paused = !(orders[displayOrderIndex].paused)

        val order = orders[displayOrderIndex]

        val mAuth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance()
        val dbref = database.getReference("user/" + mAuth.currentUser?.uid + "/orders")
        dbref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                if (dataSnapshot != null
                        && dataSnapshot.exists()) {
                    if (order.id != null
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
                    if(activity is SetupActivity) {
                        //progressBar.visibility = View.GONE
                    }
                } else {
                    order.id = dbref.push().key
                    val dbOrder = order.toDb()
                    database.getReference("user/" + mAuth.currentUser?.uid + "/orders/" + order.id).setValue(dbOrder)
                    if(activity is SetupActivity) {
                        //progressBar.visibility = View.GONE
                    }
                    Log.i("SETUP", "SetValue 2")
                }
                if(activity != null) {

                    updateOrderUI()
                }
            }

            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }


    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment OrdersFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String, param2: String): OrdersFragment {
            val fragment = OrdersFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}

// Required empty public constructor
