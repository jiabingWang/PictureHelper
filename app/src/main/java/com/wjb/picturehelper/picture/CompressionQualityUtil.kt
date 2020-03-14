package com.wjb.picturehelper.picture

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * 描述：质量压缩
 * @param targetSize 目标大小
 */
fun compressBitmapQuality(bitmap: Bitmap?, targetSize: Int): Bitmap? {
    bitmap?.let {
        val outputStream = ByteArrayOutputStream()
        //质量压缩， quality为100表示不压缩，把压缩后的数据放到OutputStream
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        var options = 100
        while (outputStream.toByteArray().size / 1024 > targetSize) {
            //如果比需要的还大，就继续减少20%
            outputStream.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, outputStream)
            options -= 20
        }
        //把压缩后的数据放到ByteArrayInputStream中
        val byteArrayInputStream = ByteArrayInputStream(outputStream.toByteArray())
        //生成图片
        return BitmapFactory.decodeStream(byteArrayInputStream, null, null)
    }
    return null
}
