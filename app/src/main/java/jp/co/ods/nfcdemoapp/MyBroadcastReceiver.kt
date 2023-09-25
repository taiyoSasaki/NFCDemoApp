package jp.co.ods.nfcdemoapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.util.Log

class MyBroadcastReceiver(private val mHandler : Handler) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("onReceive", "Broadcastが発動しました。")
        if (intent?.action == "ods.NFC_FUNCTION_RESPONSE") {
            val data = intent.getStringExtra("response")

            val message = Message()
            message.what = MainActivity.MSG_BROADCAST_RECEIVE
            message.obj = data
            mHandler.sendMessage(message)
        }

    }


}