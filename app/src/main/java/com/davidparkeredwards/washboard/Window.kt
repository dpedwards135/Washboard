package com.davidparkeredwards.washboard

import android.content.Context

/**
 * Created by davidedwards on 7/19/18.
 */
class Window constructor(pickupDay: Int, pickupStart: Int, pickupStop: Int, dropoffDay: Int, dropoffStart: Int, dropoffStop: Int, context: Context) {

    var twelvehrclock = true
    var pickupDay = pickupDay
    var pickupStart = pickupStart
    var pickupStop = pickupStop

    var dropoffDay = dropoffDay
    var dropoffStart = dropoffStart
    var dropoffStop = dropoffStop
    var context = context


    var dayList = arrayOf(context.resources.getString(R.string.monday),
            context.resources.getString(R.string.tuesday),
            context.resources.getString(R.string.wednesday),
            context.resources.getString(R.string.thursday),
            context.resources.getString(R.string.friday),
            context.resources.getString(R.string.saturday),
            context.resources.getString(R.string.sunday))


    fun toText() : String {
        var pstart = "" + pickupStart
        var pstop = "" + pickupStop
        var dstart = "" + dropoffStart
        var dstop = "" + dropoffStop
        var string1 = ""
        var string2 = ""
        var string = ""

        if(twelvehrclock) {

            if (pickupStart > 12) {
                pstart = "" + (pickupStart - 12) + " PM"
            } else if(pickupStart == 12) {
                pstart = "" + pickupStart + " PM"
            } else {
                pstart = "" + pickupStart + " AM"
            }

            if (pickupStop > 12) {
                pstop = "" + (pickupStop - 12) + " PM"
            } else if(pickupStop == 12) {
                pstop = "" + pickupStop + " PM"
            } else {
                pstop = "" + pickupStop + " AM"
            }

            if (dropoffStart > 12) {
                dstart = "" + (dropoffStart - 12) + " PM"
            } else if(dropoffStart == 12) {
                dstart = "" + dropoffStart + " PM"
            } else {
                dstart = "" + (dropoffStart) + " AM"
            }

            if (dropoffStop > 12) {
                dstop = "" + (dropoffStop - 12) + " PM"
            } else if(dropoffStop == 12) {
                dstop = "" + dropoffStop + " PM"
            } else {
                dstop = "" + (dropoffStop) + " AM"
            }
        }

        string1 =  "" + context.resources.getString(R.string.pickup) + " " + dayList[pickupDay].toString() + ": " + pstart + " - " + pstop + "\n"
        string2 = "" + context.resources.getString(R.string.dropoff) + " " + dayList[dropoffDay].toString()  + ": " + dstart + " - " + dstop
        string = string1 + string2

        return string
    }

    fun id() : String {
        return "" + pickupDay + pickupStart + pickupStop + dropoffDay + dropoffStart + dropoffStop
    }

    fun toDb() : Map<String, Any> {
        var map = HashMap<String, Any>()

        map.put("pick_up_day", pickupDay)
        map.put("pick_up_start", pickupStart)
        map.put("pick_up_stop", pickupStop)
        map.put("drop_off_day", dropoffDay)
        map.put("drop_off_start", dropoffStart)
        map.put("drop_off_stop", dropoffStop)

        return map
    }

    fun fromDb(map: Map<String, Any>) : Window {
        this.pickupDay = (map.get("pick_up_day") as Long).toInt()
        this.pickupStart = (map.get("pick_up_start") as Long).toInt()
        this.pickupStop = (map.get("pick_up_stop") as Long).toInt()
        this.dropoffDay = (map.get("drop_off_day") as Long).toInt()
        this.dropoffStart = (map.get("drop_off_start") as Long).toInt()
        this.dropoffStop = (map.get("drop_off_stop") as Long).toInt()

        return this
    }

}