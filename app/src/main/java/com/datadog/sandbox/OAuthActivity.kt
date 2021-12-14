package com.datadog.sandbox

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class OAuthActivity : AppCompatActivity(R.layout.oauth_activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("[Oauth] Create: $taskId")
        val intent = OAuthCallbackActivity.getIntent(this)

        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        println("[Oauth] Resume: $taskId")
    }

    companion object {

        fun getIntent(context: Context): Intent {
            return Intent(context, OAuthActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        }
    }
}