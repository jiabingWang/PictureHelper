package com.wjb.picturehelper.picture

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.*
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * 描述：采样率压缩
 * （尺寸压缩）
 */
/**
 * decodeString是一种有序的文件流，两次调用decodeString影响文件流的位置属性，
 * 导致第二次调用得到的是null，解决方式是通过文件流得到对应的文件描述符，
 * 然后再通过BitmapFactory.decodeFileDescriptorl来加载一张缩放后的图
 * ----任玉刚 Part12
 */
private fun decodeSampledBitmapFromFileDescriptor(fd: FileDescriptor, reqWidth: Int =-1, reqHeight: Int=-1 ): Bitmap {

    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFileDescriptor(fd, null, options)
    options.inSampleSize = computeSize(options, reqWidth,reqHeight)
    options.inJustDecodeBounds = false
    return BitmapFactory.decodeFileDescriptor(fd, null, options)
}
/**
 * 计算采样率
 * @param reqWidth 需要宽度
 * @param reqHeight 需要高度
 */
private fun computeSize(options: BitmapFactory.Options,reqWidth:Int=-1,reqHeight :Int=-1): Int {
    var computeSize = 1
    var srcWidth = options.outWidth
    var srcHeight = options.outHeight
    if (reqWidth ==-1&&reqHeight ==-1){
        //相当于用户不指定大小，则使用Luban采样率算法，高仿微信
        srcWidth = if (srcWidth % 2 == 1) srcWidth + 1 else srcWidth
        srcHeight = if (srcHeight % 2 == 1) srcHeight + 1 else srcHeight
        val longSide = max(srcWidth, srcHeight)
        val shortSide = min(srcWidth, srcHeight)
        val scale = shortSide.toFloat() / longSide
        computeSize = if (scale <= 1 && scale > 0.5625) {
            if (longSide < 1664) {
                1
            } else if (longSide < 4990) {
                2
            } else if (longSide in 4991..10239) {
                4
            } else {
                if (longSide / 1280 == 0) 1 else longSide / 1280
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            if (longSide / 1280 == 0) 1 else longSide / 1280
        } else {
            ceil(longSide / (1280.0 / scale)).toInt()
        }
    }else{
        //据要求大小，计算采样率
        if (reqWidth == 0 || reqHeight == 0) {
            return  1
        }
        val height = options.outHeight
        val width = options.outWidth


        if (height > reqHeight || width > reqWidth) {
            val  halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / computeSize) >= reqHeight
                && (halfWidth / computeSize) >= reqWidth) {
                computeSize *= 2
            }
        }
        Log.d("jiaBing","computeSize---$computeSize")
    }
    return computeSize
}
/**
 * 压缩Uri
 */
fun decodeStreamByUri(context : Activity, uri : Uri?, reqWidth:Int=-1, reqHeight :Int=-1) :Bitmap?{
    return if (uri ==null){
        Log.d("jiaBing","decodeStreamByUri---URI为空")
        null
    }else{
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri), null, options)
        val size =  computeSize(options,reqWidth,reqHeight)
        val bitmapOptions = BitmapFactory.Options()
        bitmapOptions.inSampleSize = size
        bitmapOptions.inJustDecodeBounds = false
        BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri), null, bitmapOptions)
    }
}
/**
 * 压缩File
 */
fun decodeStreamByFile(file : File, reqWidth:Int=-1, reqHeight :Int=-1):Bitmap? {
    val fd = FileInputStream(file).fd
    return decodeSampledBitmapFromFileDescriptor(fd,reqWidth,reqHeight)
}
/**
 * 压缩通过文件路径
 */
fun decodeStreamByPath( path :String?,reqWidth:Int=-1,reqHeight :Int=-1) :Bitmap?{
    path?.let {
        val  fd  =  FileInputStream(path).fd
        return decodeSampledBitmapFromFileDescriptor(fd,reqWidth,reqHeight).rotateBitmap(
            getImageOrientation(path)
        )
    }
    return  null
}
/**
 * 压缩Bitmap
 */
fun decodeStreamByBitmap(srcImg :Bitmap?,reqWidth:Int=-1,reqHeight :Int=-1) :Bitmap?{
    val  baos =  ByteArrayOutputStream()
    srcImg?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    val byteArrayInput =  ByteArrayInputStream(baos.toByteArray())
    val buffInput=  BufferedInputStream(byteArrayInput)

    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeStream(buffInput,null,options)

    val size =  computeSize(options,reqWidth,reqHeight)

    val bitmapOptions = BitmapFactory.Options()
    bitmapOptions.inSampleSize = size
    bitmapOptions.inJustDecodeBounds = false
    return BitmapFactory.decodeStream(buffInput,null,options)
}


