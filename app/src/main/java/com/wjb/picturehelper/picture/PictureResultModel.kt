package com.yibeixxkj.makemoney.utils.picture

import android.graphics.Bitmap

/**
 * @param bitmap 结果
 * @param path 原文件路径
 * @param compress 是否是压缩过的
 * @param compressPath 压缩后的文件路径
 */
data class PictureResultModel(val bitmap :Bitmap?,val path :String?,val compress :Boolean= false,val compressPath :String?)