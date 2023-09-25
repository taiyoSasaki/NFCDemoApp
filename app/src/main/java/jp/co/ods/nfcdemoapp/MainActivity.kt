package jp.co.ods.nfcdemoapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val MSG_TPOFF_UI = 1
        const val MSG_USBOFF_UI = 2
        const val MSG_BROADCAST_RECEIVE = 3
    }

    private lateinit var nfcAdapter: NfcAdapter
    private var mTag : Tag? = null

    private var isTPOff = false
    private var isUSBOff = false

    private lateinit var receiver :MyBroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // BroadcastReceiverを登録
        receiver = MyBroadcastReceiver(mHandler)
        val filter = IntentFilter("ods.NFC_FUNCTION_RESPONSE")
        registerReceiver(receiver, filter)

        tp_off_label.isVisible = false
        usb_off_label.isVisible = false

        //NFCの初期化
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

    }


    override fun onResume() {
        super.onResume()
        fullscreen()

        if (!nfcAdapter.isEnabled) { //NFCがオフの場合は設定画面へ遷移
            //ダイアログの表示
            val builder = AlertDialog.Builder(this)
                    .setTitle("NFCの設定")
                    .setMessage("NFCの設定をONにしてください")
                    .setPositiveButton("OK") { _, _ ->
                        //設定画面に遷移
                        val intent = Intent()
                        intent.action = Settings.ACTION_NFC_SETTINGS
                        startActivity(intent)
                    }
                    .setNeutralButton("キャンセル") { _, _ ->
                        finish()
                    }
            builder.create()
            builder.show()
        } else{
            nfcAdapter.enableReaderMode(this,
                    { tag ->
                        Log.d("onTagDiscovered", "タグを検出しました　→　$tag")
                        if (tag != null) {
                            mTag = tag
                            doMethod()
                        }
                    },  NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or NfcAdapter.FLAG_READER_NFC_F or NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS, null)
        }

    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableReaderMode(this)
    }

    private fun doMethod() {
        val id = bytesToHexString(mTag!!.id)
        Log.d("doMethod", "読み取ったID = $id")

        val intent = Intent("ods.NFC_ID_CHECK")
        intent.putExtra("request_id", id)
        sendBroadcast(intent)
    }

    //Handlerの設定
    private val mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        //メッセージ受信
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_TPOFF_UI -> tp_off_label.isVisible = isTPOff
                MSG_USBOFF_UI -> usb_off_label.isVisible = isUSBOff

                MSG_BROADCAST_RECEIVE -> {
                    Log.d("BROADCAST_RECEIVE", "function = ${msg.obj}")
                    when(msg.obj as String) {
                        "A" -> {
                            setTPEnable(isTPOff)
                            isTPOff = !isTPOff
                            sendEmptyMessage(MSG_TPOFF_UI)
                        }
                        "B" -> {
                            setUSBEnable(isUSBOff)
                            isUSBOff = !isUSBOff
                            sendEmptyMessage(MSG_USBOFF_UI)
                        }
                        "C" -> {
                            setReboot()
                        }
                    }


                }

            }
        }
    }

    private fun setUSBEnable(boolean: Boolean) {
        if (boolean) {
            val intent = Intent("android.intent.action.USB_TYPEA")
            intent.putExtra("usb_mode", "1")
            sendBroadcast(intent)
        } else {
            val intent = Intent("android.intent.action.USB_TYPEA")
            intent.putExtra("usb_mode", "0")
            sendBroadcast(intent)
        }
    }

    private fun setTPEnable(boolean: Boolean) {
        if (boolean) {
            val intent = Intent("android.intent.action.TP")
            intent.putExtra("tp_mode", "1")
            sendBroadcast(intent)
        } else {
            val intent = Intent("android.intent.action.TP")
            intent.putExtra("tp_mode", "0")
            sendBroadcast(intent)
        }
    }

    private fun setReboot() {
        val intent = Intent("com.android.action.REBOOT")
        sendBroadcast(intent)
    }

    //bytesを16進数型文字列に変換用関数
    private fun bytesToHexString(bytes: ByteArray): String {
        val sb = StringBuilder()
        val formatter = Formatter(sb)
        for (b in bytes) {
            formatter.format("%02x", b)
        }
        //大文字にして戻す（見た目の調整だけ）
        return sb.toString().toUpperCase(Locale.ROOT)
    }

    private fun fullscreen() {
        //全画面モード
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        actionBar?.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        // BroadcastReceiverを解除
        unregisterReceiver(receiver)

        if (isTPOff) {
            setTPEnable(isTPOff)
            isTPOff = !isTPOff
        }
    }
}