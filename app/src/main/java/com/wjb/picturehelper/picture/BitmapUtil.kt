package com.wjb.picturehelper.picture

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import com.wjb.picturehelper.App
import java.io.IOException

/**
 * 路漫漫其修远兮，吾将上下而求索
 * @author 服装学院的IT男
 * 时间: on 2019/10/24
 * 包名 com.yibeixxkj.makemoney.utils.picture
 * 描述：
 */
fun bitmap2Drawable(bitmap: Bitmap?): Drawable? {
    var drawable: Drawable? = null
    bitmap?.let {
        drawable = BitmapDrawable(App.mApp.resources, bitmap)
    }
    return drawable
}
/**
 * 获取图片旋转角度
 */
fun getImageOrientation(path :String): Int {
    var angle = 0
    try {
        val  exifInterface =  ExifInterface(path)
        when(exifInterface.getAttributeInt (ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)){
            ExifInterface.ORIENTATION_ROTATE_90 ->{
                angle = 90
            }
            ExifInterface.ORIENTATION_ROTATE_180 ->{
                angle = 180
            }
            ExifInterface.ORIENTATION_ROTATE_270 ->{
                angle = 270
            }
        }
    } catch (e : IOException) {
        e.printStackTrace()
        ExifInterface.ORIENTATION_NORMAL
    }
    return angle
}
/**
 * 旋转图片
 */
fun Bitmap.rotateBitmap(angle :Int):Bitmap{
    val matrix = Matrix()
    matrix.postRotate(angle.toFloat())
    return Bitmap.createBitmap(this, 0, 0, this.width, this.height, matrix, true)
}

