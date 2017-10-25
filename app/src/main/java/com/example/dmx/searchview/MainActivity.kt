package com.example.dmx.searchview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val searchView = findViewById<SearchView>(R.id.searchView)
        val btnReset = findViewById<Button>(R.id.btnReset)
        btnReset.setOnClickListener { view ->
            searchView.reset()
        }
    }
}
