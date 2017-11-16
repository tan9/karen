package com.example.tang.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import android.content.Context
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val numbersSharedPreferences = getSharedPreferences("handled_incoming_calls", Context.MODE_PRIVATE)
        val map = numbersSharedPreferences.all

        val array = map.keys.sorted().reversed().map { key -> map[key] }
        Log.v("karen", map.toString() + map.size)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, array)

        val lv = findViewById<ListView>(R.id.list_view)
        lv.adapter = adapter

        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.SEND_SMS),
                PERMISSION_READ_STATE)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_READ_STATE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted!
                    // you may now do the action that requires this permission

                } else {
                    // permission denied
                    Log.wtf("karen", "Permission denied.")
                }
                return
            }
        }
    }

    companion object {
        val PERMISSION_READ_STATE = 0x0078
    }
}
