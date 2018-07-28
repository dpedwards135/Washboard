package com.davidparkeredwards.washboard

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_setup.*


class SetupActivity : AppCompatActivity(), EditOrderDelegate {
    var stage = -1
    val editOrderController = EditOrderController(this, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)
        toolbar.title = "Setup " + (stage + 1) + "/5"
        setSupportActionBar(toolbar)

        editOrderController.setup(Order())

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
        Log.i("Setup", "Stage = " + stage)

        if(stage == 5) {
            startMainActivity()
        }

        //saveData
        when(stage) {
            0 -> {}
            1 ->  editOrderController.saveOrderType()
            2 -> editOrderController.saveWindowInfo()
            3 -> editOrderController.saveInstructionInfo()

            4 -> {

                if(!back) editOrderController.saveOrder()
            }
        }

        //Load new fragment
        val ft = supportFragmentManager.beginTransaction()
        //var fragment = android.support.v4.app.Fragment()
        stage = if(back) (stage -1) else (stage + 1)
        var fragment = editOrderController.getFragment(stage)

        when (stage) {
            0 -> {
                fab2.visibility = View.GONE
                fab.isEnabled = true
            }
            1 -> {
                fab2.visibility = View.VISIBLE
                if(editOrderController.order.orderType != "INCOMPLETE")  {
                    fab.isEnabled = true
                } else {
                    fab.isEnabled = false
                }
            }
            2 -> {
                if(editOrderController.order.window == null) {
                    fab.isEnabled = false
                } else {
                    fab.isEnabled = true
                }
            }
            3 -> {
                fab.isEnabled = true

            }
            4 -> {
                fab.isEnabled = true //change to false
                fab2.visibility = View.VISIBLE
            }
            5 -> {
                fab.isEnabled = true
                fab2.visibility = View.GONE

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

    override fun onRadioClick() {
        Log.i("SETUP", "onRadioClick")
        fab.isEnabled = true
    }

    override fun setupOptions() {
        editOrderController.setupOptions()
    }

    fun startMainActivity() {
        val mainActivity = Intent(this, MainActivity::class.java)
        mainActivity.putExtra("login_start", false)
        //mainActivity.extras.putBoolean("login_start", true)
        startActivity(mainActivity)
    }
}
