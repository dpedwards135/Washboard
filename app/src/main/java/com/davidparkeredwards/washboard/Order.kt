package com.davidparkeredwards.washboard

import android.content.Context
import android.util.Log

/**
 * Created by davidedwards on 7/21/18.
 */
class Order() {

    //Order types: Single, Standard, Enterprise, Incomplete
    var id = ""
    var orderType = "INCOMPLETE"
    var window : Window? = null
    var address = ""
    var cityAndState = ""
    var zip = ""
    var soiled = false
    var soiledNote = ""
    var cold = false
    var coldNote = ""
    var softener = false
    var softenerNote = ""
    var notes = ""
    var paused = false
    var orderInstances = ArrayList<OrderInstance>()
    var cancelled = false

    constructor(id: String, orderType: String, address: String, notes: String, paused: Boolean,
                orderInstances: ArrayList<OrderInstance>, cancelled: Boolean) : this() {
        this.id = id
        this.orderType = orderType
        this.address = address
        this.notes = notes
        this.paused = paused
        this.orderInstances = orderInstances
        this.cancelled = cancelled
    }

    fun toDb() : Map<String, Any> {
        var map = HashMap<String, Any>()

        map.put("id", id)
        map.put("order_type", orderType)
        if(window != null) map.put("window", window!!.toDb()) else map.put("window", "")
        map.put("address", address)
        map.put("soiled", soiled)
        map.put("soiled_note", soiledNote)
        map.put("cold", cold)
        map.put("cold_note", coldNote)
        map.put("softener", softener)
        map.put("softener_note", softenerNote)
        map.put("notes", notes)
        map.put("paused", paused)
        map.put("cancelled", cancelled)

        return map
    }

    fun fromDb(dataSnapshot: HashMap<String, Any>, context: Context) : Order {
        val map = dataSnapshot
        this.id = map.getValue("id") as String
        this.orderType = map.getValue("order_type") as String
        this.window = Window(8,8,8,8,8,8, context)
                .fromDb(dataSnapshot.getValue("window") as Map<String, Any>)
        this.address = map.getValue("address") as String
        this.soiled = map.getValue("soiled") as Boolean
        this.soiledNote = map.getValue("soiled_note") as String
        this.cold = map.getValue("cold") as Boolean
        this.coldNote = map.getValue("cold_note") as String
        this.softener = map.getValue("softener") as Boolean
        this.notes = map.getValue("notes") as String
        this.paused = map.getValue("paused") as Boolean
        this.cancelled = map.getValue("cancelled") as Boolean

        return this
    }

    companion object {
        fun ordersFromDb(dataSnapshot: HashMap<String, Any>, context: Context): ArrayList<Order> {

            Log.i("Order", "Orders: " + dataSnapshot.toString())
            var orders = ArrayList<Order>()

            for (child in (dataSnapshot)) {
                var order = Order().fromDb(child.value as HashMap<String, Any>, context)
                orders.add(order)
            }

            return orders
        }
    }


}