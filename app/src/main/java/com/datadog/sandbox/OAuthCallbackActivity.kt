package com.datadog.sandbox

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class OAuthCallbackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("[Callback]: $taskId")
        val intent = OAuthActivity.getIntent(this)

        startActivity(intent)
        finish()
    }

    companion object {

        fun getIntent(context: Context): Intent {
            return Intent(context, OAuthCallbackActivity::class.java)
        }
    }
}