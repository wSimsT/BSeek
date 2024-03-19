package edu.temple.bseek

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    lateinit var createSessionButton: Button
    lateinit var joinSessionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createSessionButton = findViewById(R.id.startSessionButton)
        joinSessionButton = findViewById(R.id.joinSessionButton)

        createSessionButton.setOnClickListener {
            // start a session using an intent
        }

        joinSessionButton.setOnClickListener {
            // prompt the user for a session key
        }

    }
}