package com.davidparkeredwards.washboard

import android.content.Context
import com.google.firebase.database.DataSnapshot
import java.util.HashMap
import kotlin.collections.ArrayList

/**
 * Created by davidedwards on 7/24/18.
 */
class ZipInfo() {

    var zipCode = ""
    var serviced = false
    var windows = ArrayList<Window>()
    var standardPrice = 0
    var singlePrice = 0
    var enterprisePrice = 0


    fun toDb() : Map<String, Any> {
        var map = HashMap<String, Any>()

        map.put("zip_code", zipCode)
        map.put("serviced", serviced)

        var windowList = ArrayList<Map<String, Any>>()
        for(window in windows) {
            windowList.add(window.toDb())
        }

        if(windows != null) map.put("windows", windowList) else map.put("windows", "")
        map.put("standard_price", standardPrice)
        map.put("single_price", singlePrice)
        map.put("enterprise_price", enterprisePrice)

        return map
    }

    fun fromDb(datasnapshot: DataSnapshot, context: Context) : ZipInfo {
        this.zipCode = (datasnapshot.child("zip_code").value) as String
        this.serviced = (datasnapshot.child("serviced").value) as Boolean
        var windowList = (datasnapshot.child("windows").value) as ArrayList<HashMap<String, Any>>
        for(window in windowList) {
            this.windows.add(Window(8,8,8,8,8,
                    8,context).fromDb(window as Map<String, Any>))
        }
        this.standardPrice = ((datasnapshot.child("standard_price").value) as Long).toInt()
        this.singlePrice = ((datasnapshot.child("single_price").value) as Long).toInt()
        this.enterprisePrice = ((datasnapshot.child("enterprise_price").value) as Long).toInt()


        return this
    }
}