package com.wjb.picturehelper


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.view.Gravity
import android.view.View
import com.wjb.picturehelper.picture.PictureHelper
import com.yibeixxkj.makemoney.utils.picture.PictureResultModel
import kotlinx.android.synthetic.main.layout_choose_picture.view.*
import org.jetbrains.anko.support.v4.toast

/**
 * 描述：
 * @param  needCompress 是否需要压缩
 * @param  compressName 压缩后的文件名称
 * @param  compressType 压缩规则
 */
class ChoosePictureDialog(
    private val needCompress: Boolean = true,
    private val compressName: String? = null,
    private val compressType: PictureHelper.CompressType = PictureHelper.CompressType.Default,
    val resultCaBack: (PictureResultModel?) -> Unit
) : BaseDialogFragment() {
    override fun initDialogUI(view: View) {

    }

    override fun getLayoutResOrView() = R.layout.layout_choose_picture
    override fun setGravity() {
        lp?.gravity = Gravity.BOTTOM
    }

    override fun getClickView(view: View): List<View?>? {
        return listOf(view.tv_take_photo, view.tv_openAlbum, view.tv_cancel)
    }

    override fun onSingleClick(view: View) {
        when (view.id) {
            R.id.tv_take_photo -> {
                getPermission(mutableListOf(Manifest.permission.CAMERA)) {granted,checked->
                    if (granted) {
                        PictureHelper.PictureBuild(this)
                            .setNeedCompress(needCompress)
                            .setCompressType(compressType)
                            .setCompressName(compressName)
                            .create()
                            .takePhoto()
                    } else {
                        moneyToast("拒绝权限将无法使用")
                        dialog?.dismiss()
                    }
                }
            }
            R.id.tv_openAlbum -> {
                getPermission(mutableListOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {granted,checked->
                    if (granted) {
                        PictureHelper.PictureBuild(this)
                            .setNeedCompress(needCompress)
                            .setCompressType(compressType)
                            .setCompressName(compressName)
                            .create()
                            .openAlbum()
                    } else {
                        moneyToast("拒绝权限将无法使用")
                        dialog?.dismiss()
                    }
                }
            }
            R.id.tv_cancel -> {
                dialog?.dismiss()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            when (requestCode) {
                PictureHelper.PITURE_ALBUM, PictureHelper.PITURE_PHOTO -> {
                    PictureHelper().getResult(data) {
                        resultCaBack.invoke(it)
                    }
                    dialog?.dismiss()
                }
            }
        }

    }

}