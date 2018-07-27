package com.davidparkeredwards.washboard

import java.util.*

/**
 * Created by davidedwards on 7/21/18.
 */
class OrderInstance() {

    //Order types: Single, Standard, Enterprise
    var id = ""
    var date = Date()
    var notes = ""

    constructor(id: String, date: Date, notes: String) : this() {

        this.id = id
        this.date = date
        this.notes = notes
    }

}