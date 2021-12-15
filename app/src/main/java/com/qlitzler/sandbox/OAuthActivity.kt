package com.qlitzler.sandbox

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class OAuthActivity : AppCompatActivity(R.layout.oauth_activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("[Oauth] Create: $taskId")
        findViewById<Button>(R.id.getCallback).setOnClickListener {
            val intent = OAuthCallbackActivity.getIntent(this)

            startActivity(intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        println("[Oauth] Resume: $taskId")
        if (intent.extras?.getBoolean(CALLBACK, false) == true) {
            setResult(RESULT_OK)
            finish()
        }
    }

    companion object {

        const val CALLBACK = "callback"

        fun getIntent(context: Context): Intent {
            return Intent(context, OAuthActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        }
    }
}