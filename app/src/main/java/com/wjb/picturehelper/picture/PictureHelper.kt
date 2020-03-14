package com.wjb.picturehelper.picture

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import android.provider.MediaStore
import android.annotation.SuppressLint
import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.fragment.app.Fragment
import com.wjb.picturehelper.App
import com.wjb.picturehelper.App.Companion.FILEPROVIDER_AUTHORITY
import com.yibeixxkj.makemoney.utils.picture.PictureResultModel
import org.jetbrains.anko.doAsync
import java.lang.ref.WeakReference
import org.jetbrains.anko.uiThread
import java.io.BufferedOutputStream
import java.io.FileOutputStream


/**
 * 描述：
 * setPhotoName设置拍照保存的名称
 * setNeedCompress 设置是否需要进行压缩， 默认不需要
 * setCompressName设置需要保存压缩后图片的名称
 * setCompressType 设置压缩方式，当setNeedCompress为true时生效
 * 默认为Default，LimitWH限制大小 ,IgnoreBy质量压缩,限制文件大小，可能出现模糊的情况
 * setIgnoreBySize 设置需要压缩到多大，当  setCompressType为IgnoreBy生效
 * setResWH 设置压缩图标的宽高，当  setCompressType为LimitWH生效
 */
class PictureHelper {
    companion object {
        /**以下类型与对象无关*/
        /**请求码-相机*/
        const val PITURE_PHOTO = 17
        /**请求码-相册*/
        const val PITURE_ALBUM = 1717
        /**当前的类型，作为处理返回结果的判断条件 */
        var currentType: Int? = null
        /**拍照图片Uri*/
        var photoUri: Uri? = null
        /**拍照图片路径*/
        var photoPath: String? = null
        /**拍照保存的文件名*/
        var mPhotoName: String? = null
        /**是否打开压缩*/
        var mNeedCompress: Boolean = false
        /**压缩后文件名,不设置就当需要保存在本地*/
        var mCompressName: String? = null
        /**设置压缩方式*/
        var mCompressType: CompressType = CompressType.Default
        /**设置质量压缩的要求*/
        var mIgnoreBySize: Int? = null
        /**设置需要的宽度*/
        var mReqWidth: Int? = null
        /**设置需要的高度*/
        var mReqHeight: Int? = null
    }

    private var mBuild: PictureBuild? = null

    constructor()
    constructor(build: PictureBuild) {
        mBuild = build
        mFragment = build.fragment
        mActivity = build.activity
        mPhotoName = build.photoName
        mNeedCompress = build.needCompress
        mCompressName = build.compressName
        mCompressType = build.compressType
        mIgnoreBySize = build.ignoreBySize
        mReqWidth = build.reqWidth
        mReqHeight = build.reqHeight
    }

    /**
     * Fragment
     */
    private var mFragment: WeakReference<Fragment>? = null
    /**
     * Activity
     */
    private var mActivity: WeakReference<Activity>? = null
    /**
     * 图片的默认名称
     */
    private val defaultName by lazy {
        "money${System.currentTimeMillis()}.jpg"
    }
    /**
     *  拍照
     */
    fun takePhoto() {
        currentType = PITURE_PHOTO
        //1.创建File对象，保存拍照后的图片，命名格式：money+时间戳
        val file = File(App.mApp.externalCacheDir, mPhotoName ?: defaultName)
        //2.如果文件存在，则删除
        try {
            if (file.exists()) {
                file.delete()
            }
            file.createNewFile()
            //createNewFile：没有文件，则创建，已有文件，则创建失败，没有目录，则报错
            //createTempFile：创建临时文件
        } catch (e: IOException) {
            e.printStackTrace()
        }
        //3.获取图片Uri
        photoUri = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Uri.fromFile(file)
        } else {
            FileProvider.getUriForFile(App.mApp, FILEPROVIDER_AUTHORITY, file)
        }
        photoPath = file.path
        //4.启动相机程序
        //android.media.action.IMAGE_CAPTURE
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        if (mActivity == null && mFragment == null) {
            return
        }
        if (mActivity != null) {
            mActivity!!.get()?.startActivityForResult(intent, PITURE_PHOTO)
        }
        if (mFragment != null) {
            mFragment!!.get()?.startActivityForResult(intent, PITURE_PHOTO)
        }

    }

    /**
     * 相册
     */
    fun openAlbum() {
        currentType = PITURE_ALBUM
        // 打开相册
        //Intent("android.intent.action.GET_CONTENT")
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        if (mActivity == null && mFragment == null) {
            return
        }
        if (mActivity != null) {
            mActivity!!.get()?.startActivityForResult(intent, PITURE_ALBUM)
        }
        if (mFragment != null) {
            mFragment!!.get()?.startActivityForResult(intent, PITURE_ALBUM)
        }
    }

    /**
     * 获取结果
     */
    fun getResult(data: Intent?, resultCallback: (PictureResultModel?) -> Unit) {
        val path = getFilePath(data)
        var compressPath : String?=null
        var bitmap = getBitmapResult(path)
        var resultModel: PictureResultModel? = null
        doAsync {
            if (mNeedCompress) {
                //如果打开了压缩，则需要判断压缩方式
                when (mCompressType) {
                    CompressType.Default -> {
                        bitmap = decodeStreamByPath(path)
                    }
                    CompressType.IgnoreBy -> {
                        mIgnoreBySize?.let {
                            bitmap =
                                compressBitmapQuality(bitmap, it)?.rotateBitmap(
                                    getImageOrientation(
                                        path!!
                                    )
                                )
                        }
                    }
                    CompressType.LimitWH -> {
                        mReqWidth?.let {
                            bitmap =
                                decodeStreamByPath(path, mReqWidth!!, mReqHeight!!)
                        }
                    }
                }
                //设置了名称即表示需要保存压缩都的文件
                val compressFile = File(App.mApp.externalCacheDir, mCompressName)
                if (compressFile.exists()) {
                    compressFile.delete()
                }
                val bos = BufferedOutputStream(FileOutputStream(compressFile))
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, bos)
                bos.flush()
                bos.close()
                compressPath = compressFile.path
            }

            resultModel = PictureResultModel(bitmap, path, mNeedCompress,compressPath)
            uiThread {
                resultCallback.invoke(resultModel)
            }
            releaseStatus()
        }
    }

    /**
     * 获取相册选择后的真是路径
     * 4.4以上解析方式
     */
    @TargetApi(19)
    private fun getFilePath(data: Intent?): String? {
        //图片路径
        var imgPath: String? = null
        var uri: Uri? = null
        if (currentType == null) {
            return null
        }
        if (currentType == PITURE_PHOTO) {
            imgPath = photoPath
        }
        if (currentType == PITURE_ALBUM) {
            uri = data?.data
            uri?.let {
                //解析封装过的Uri
                if (DocumentsContract.isDocumentUri(App.mApp, it)) {
                    //1.如果是Document类型，就取出document_id
                    val documentId = DocumentsContract.getDocumentId(uri)
                    if ("com.android.providers.media.documents" == it.authority) {
                        // 2.解析出数字格式的id
                        val id = documentId.split(":")[1]
                        val selection = MediaStore.Images.Media._ID + "=" + id
                        imgPath = getImagePath(uri, selection)
                    } else if ("com.android.providers.downloads.documents" == it.authority) {
                        val contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"),
                            documentId.toLong()
                        )
                        imgPath = getImagePath(contentUri, null)
                    }
                } else if ("content".equals(it.scheme, true)) {
                    //忽略大小写
                    // 2.如果是content类型的Uri，则使用普通方式处理
                    imgPath = getImagePath(it, null)
                } else if ("file".equals(it.scheme, true)) {
                    // 3.如果是file类型的Uri，直接获取图片路径即可
                    imgPath = uri.path
                }
            }
        }
        return imgPath
    }

    /**
     * 获取Bitmap类型结果
     */
    private fun getBitmapResult(path: String?): Bitmap? {
        return BitmapFactory.decodeFile(path)
    }

    /**
     * 通过Uri和selection来获取真实的图片路径
     */
    @SuppressLint("Recycle")
    private fun getImagePath(uri: Uri?, selection: String?): String? {
        var path: String? = null
        uri?.let {
            val cursor =
                App.mApp.contentResolver.query(uri, null, selection, null, null)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
                }
                cursor.close()
            }
        }
        return path
    }

    /**
     * 重置与对象无关的变量
     */
    private fun releaseStatus() {
        mActivity = null
        mFragment = null
        mBuild = null
        mPhotoName = null
        mNeedCompress = false
        mCompressName = null
        mCompressType = CompressType.Default
        mIgnoreBySize = null
        mReqWidth = null
        mReqHeight = null
    }

    class PictureBuild {

        constructor(fragment: Fragment) {
            this.fragment = WeakReference(fragment)
        }

        constructor(activity: Activity) {
            this.activity = WeakReference(activity)
        }

        /**Fragment*/
        var fragment: WeakReference<Fragment>? = null
        /**Activity*/
        var activity: WeakReference<Activity>? = null
        /**拍照保存的文件名*/
        var photoName: String? = null
        /**是否打开压缩*/
        var needCompress: Boolean = false
        /**压缩后文件名*/
        var compressName: String? = null
        /**设置压缩方式*/
        var compressType: CompressType = CompressType.Default
        /**设置质量压缩的要求*/
        var ignoreBySize: Int? = null
        /**设置需要的宽度*/
        var reqWidth: Int? = null
        /**设置需要的高度*/
        var reqHeight: Int? = null

        fun setPhotoName(name: String): PictureBuild {
            photoName = if (name.endsWith(".jpg") || name.endsWith(".png")) {
                name
            } else {
                "$name.jpg"
            }
            return this
        }

        fun setNeedCompress(need: Boolean): PictureBuild {
            needCompress = need
            return this
        }

        fun setCompressName(name: String?): PictureBuild {
            compressName = if (name == null){
                "moneyCompress${System.currentTimeMillis()}.jpg"
            }else{
                if (name.endsWith(".jpg") || name.endsWith(".png")) {
                    name
                } else {
                    "$name.jpg"
                }
            }
            return this
        }

        fun setCompressType(type: CompressType): PictureBuild {
            compressType = type
            return this
        }

        fun setIgnoreBySize(size: Int): PictureBuild {
            ignoreBySize = size
            return this
        }

        fun setResWH(resW: Int, resH: Int): PictureBuild {
            reqWidth = resW
            reqHeight = resH
            return this
        }

        fun create(): PictureHelper {
            return PictureHelper(this)
        }
    }

    /**
     * 压缩方式
     * Default默认 使用鲁班压缩      ---------------尺寸压缩
     * LimitWH限制大小 根据宽高计算采样率进行压缩----尺寸压缩
     * IgnoreBy压缩的阈值 单位为K     --------------质量压缩
     */
    enum class CompressType {
        Default,
        LimitWH,
        IgnoreBy,
    }
}