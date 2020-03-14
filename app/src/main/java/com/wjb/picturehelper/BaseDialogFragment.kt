package com.wjb.picturehelper

import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import org.jetbrains.anko.padding

abstract class BaseDialogFragment : AppCompatDialogFragment(), View.OnClickListener {

    var dialog: AlertDialog? = null
    /**
     * 上一次按钮点击时间
     */
    private var lastClickTime = 0L

    /**
     * 获取布局资源或者View
     */
    abstract fun getLayoutResOrView(): Int

    /**
     * 点击事件监听
     */
    abstract fun onSingleClick(view: View)

    /**
     * 设置快速点击的间隔
     * 默认300ms只处理一次点击
     */
    open fun getDoubleClickSpace() = 300L

    /**
     * 动态权限请求码，随意
     */
    private val needPermissionCode = 1450
    /**
     * 动态权限的回调，是否有此权限
     */
    private var needPermissionCall: Function2<Boolean, Boolean, Unit>? = null

    var lp: WindowManager.LayoutParams? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(activity as Context, R.style.bottomDialog_style)
        val layoutInflater = activity!!.layoutInflater
        val view = layoutInflater.inflate(getLayoutResOrView(), null, false)

        builder.setView(view)
        dialog = builder.create()
        dialog?.setCanceledOnTouchOutside(true)
        val window = dialog?.window
        window?.setWindowAnimations(animStyle())
        window?.attributes = setWindLayoutParams()

        getClickView(view)?.forEach { it?.setOnClickListener(this) }
        initDialogUI(view)
        return dialog!!
    }


    abstract fun getClickView(view: View): List<View?>?

    abstract fun initDialogUI(view: View)


    override fun onClick(v: View?) {
        if (System.currentTimeMillis() - lastClickTime < getDoubleClickSpace()) {
            return
        }
        lastClickTime = System.currentTimeMillis()
        v?.let {
            onSingleClick(it)
        }
    }

    private fun animStyle(): Int {
        return R.style.actionSheetDialogAnimation
    }


    private fun setWindLayoutParams(): WindowManager.LayoutParams? {
        lp = dialog?.window?.attributes
        setGravity()
        lp?.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp?.height = WindowManager.LayoutParams.WRAP_CONTENT
        return lp
    }

    protected open fun setGravity() {
        lp?.gravity = Gravity.CENTER
    }

    fun show(activity: FragmentActivity?) {
        tryError {
            activity?.let {
                show(it.supportFragmentManager, javaClass.simpleName)
            }
        }
    }

    override fun dismiss() {
        try {
            super.dismiss()
        } catch (e: Exception) {
            dismissAllowingStateLoss()
        }
    }

    /**
     * 请求用户权限
     */
    fun getPermission(
        permission: MutableList<String>,
        needPermissionCall: Function2<Boolean, Boolean, Unit>
    ) {
        if (context == null) {
            return
        }
        var isNeedRequest = false
        val list = mutableListOf<String>()
        permission.forEach {
            if (ActivityCompat.checkSelfPermission(
                    context!!,
                    it
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                isNeedRequest = true
                list.add(it)
            }
        }
        if (isNeedRequest) {
            this.needPermissionCall = needPermissionCall
            if (list.isNotEmpty()) {
                requestPermissions(list.toTypedArray(), needPermissionCode)
            } else {
                needPermissionCall.invoke(true, false)
            }
        } else {
            needPermissionCall.invoke(true, false)
        }
    }

    /**
     * 处理请求的权限结果
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == needPermissionCode) {
            var isGetAllPermission = true
            var checked = false
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    isGetAllPermission = false
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            activity!!,
                            permissions[i]
                        )
                    ) {
                        checked = true
                    }
                }
            }
            needPermissionCall?.invoke(isGetAllPermission, checked)
        }
    }
}