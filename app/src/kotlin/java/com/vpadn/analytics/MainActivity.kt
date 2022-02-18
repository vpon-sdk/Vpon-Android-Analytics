package com.vpadn.analytics

import android.Manifest.permission
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.vpon.sdk.VpdataAnalytics
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {
    private val PERMISSION_REQUEST_CODE = 2001

    private var tracker: VpdataAnalytics.Tracker? = null


    //TODO set your licenseKey & customerId
    private val licenseKey = "mock_license_key"
    private val customerId = "mock_custom_id"

    private val DEFAULT_EXTRA_DATA = "{\"Key1\":\"value1\",\"Key2\":\"value2\"}"

    private var payload = DEFAULT_EXTRA_DATA

    companion object {
         val LT = "com.vpadn.analytics.MainActivity"
    }


    protected override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()

        //request optional permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(permission.ACCESS_FINE_LOCATION, permission.ACCESS_COARSE_LOCATION, permission.READ_PHONE_STATE), PERMISSION_REQUEST_CODE)
        }
        val vpdataAnalytics = VpdataAnalytics

        //just for debug mode, remember to set to false before app release!!!
        //need to be set before vpdataAnalytics.initialize
        vpdataAnalytics.setDebugMode(true)

        //set VpdataAnalytics.OptIn according to users agreement
        vpdataAnalytics.initialize(this, licenseKey, customerId, VpdataAnalytics.OptIn.DEFAULT)
        initViews()

        //construct a Tracker for sending event
        tracker = VpdataAnalytics.Tracker()
    }

    class SpinnerFragment : DialogFragment() {
        private var o: Any? = null
        fun setCustomListener(o: Any?) {
            this.o = o
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder: AlertDialog.Builder
            val activityWeakReference = WeakReference<Activity?>(activity)
            builder = AlertDialog.Builder(activityWeakReference.get())
            val spinner = Spinner(activityWeakReference.get())
            val eventList = ArrayAdapter.createFromResource(activityWeakReference.get(),
                    R.array.events,
                    android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = eventList
            val sb = StringBuilder()
            spinner.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                    if (position != 0) {
                        if (o != null) {
                            if (view is TextView) {
                                sb.append(view.text)
                            }
                            val listener = o as EventSelectedListener
                            listener.onEventSelected(sb.toString())
                            dialog.dismiss()
                        }
                    }
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {
                    Log.e(LT, "onNothingSelected")
                }
            }
            val alertDialog = builder.create()
            alertDialog.setMessage(getString(R.string.title_select_event))
            alertDialog.setView(spinner)
            return alertDialog
        }
    }

    private fun initViews() {
        val tieExtraData: TextInputEditText = findViewById<TextInputEditText>(R.id.ie_extra_data)
        tieExtraData.setText(DEFAULT_EXTRA_DATA)
        tieExtraData.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(editable: Editable) {
                val txt = editable.toString()
                try {
                    JSONObject(txt)
                    payload = txt
                } catch (ignore: JSONException) {
                    Toast.makeText(getBaseContext(), "Not Valid JSON string", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    fun fireButton(v: View) {
        val id = v.id
        when (id) {
            R.id.btn_send_event -> {
                val spinnerFragment = SpinnerFragment()
                if (eventSelectedListener != null) {
                    spinnerFragment.setCustomListener(eventSelectedListener)
                }
                spinnerFragment.show(getSupportFragmentManager(), "spinner")
            }
        }
    }

    private fun sendEvent(event: String?) {
        Log.e(LT, "sendEvent($event) invoked!!")
        try {
            val jsonObject = JSONObject(payload)
            //send event
            tracker?.sendEvent(event, jsonObject)
        } catch (ignore: JSONException) {
        }
    }

    private val eventSelectedListener: EventSelectedListener = object : EventSelectedListener {
        override fun onEventSelected(event: String?) {
            sendEvent(event)
            Toast.makeText(baseContext, "kotlin", Toast.LENGTH_SHORT).show()
        }
    }

    interface EventSelectedListener {
        fun onEventSelected(event: String?)
    }

}