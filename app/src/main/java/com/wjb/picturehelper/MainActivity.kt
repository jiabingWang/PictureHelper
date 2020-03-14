package com.wjb.picturehelper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_choose.setOnClickListener {
            ChoosePictureDialog {
                Glide.with(this).load(it?.bitmap).into(iv_demo)
            }.showNow(
                supportFragmentManager,
                MainActivity::class.java.simpleName
            )
        }

    }
}
