package com.wjb.picturehelper

import android.app.Application

/**
 * 路漫漫其修远兮，吾将上下而求索
 * @author 服装学院的IT男
 * 时间: on 2020-03-14
 * 包名 com.wjb.picturehelper
 * 描述：
 */
class App :Application() {
    companion object {
        lateinit var mApp :Application
        /**
         * fileProvider-authority
         */
        const val FILEPROVIDER_AUTHORITY = "com.wjb.picturehelper.fileProvider"
    }

    override fun onCreate() {
        super.onCreate()
        mApp =this
    }
}