package edu.temple.bseek

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView

class JoinSeek : AppCompatActivity() {


    val stopSeek = findViewById<Button>(R.id.stopSeekButton)
    val beginSeek = findViewById<Button>(R.id.beginSeekButton)
    val restartSeek = findViewById<Button>(R.id.restartSeekButton)

    val gameStatusTextView = findViewById<TextView>(R.id.statusTextView)
    val timerText = findViewById<TextView>(R.id.timerTextView)

    val timer = object: CountDownTimer(300000, 1000) {

        override fun onTick(millisUntilFinished: Long) {
            gameStatusTextView.text = "Active"
            timerText.text = (millisUntilFinished / 1000).toString()
        }

        override fun onFinish() {
            gameStatusTextView.text = "Inactive"
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.seek_session_join)
    }
}