package jp.co.ods.nfcdemoapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_restaurant.*

class RestaurantActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant)

        bill_button.setOnClickListener {
            //fasstapのアプリを起動させる
            val packageName = "my.com.softspace.gmo.fasstap.dev"
            val className = "my.com.softspace.mpos.SSMPOSMainActivity"
            intent.setClassName(packageName, className)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        fullscreen()
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
}