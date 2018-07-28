package com.davidparkeredwards.washboard

import com.stripe.android.model.Token

/**
 * Created by davidedwards on 7/28/18.
 */
class CardData() {

    var id = ""
    var lastFour = ""
    var cardType = ""
    var token : Token? = null
    var valid = 100
    var expMonth = 0
    var expYear = 0
}