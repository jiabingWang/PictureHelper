package com.wjb.picturehelper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import okhttp3.OkHttpClient
import org.jetbrains.anko.dip
import java.math.BigDecimal

/**
 * 描述： kotlin常用的方式类
 */
/**
 * 设置textView从资源文件
 */
infix fun TextView.textFrom(@StringRes strRes: Int) {
    text = try {
        this.context.getText(strRes)
    } catch (e: Throwable) {
        strRes.toString()
    }
}

/**
 * 设置textView从资源文件
 */
infix fun TextView.textFrom(str: CharSequence?) {
    text = str
}

/**
 * 从一个url加载圆形图片到view
 */
infix fun ImageView.setCircleImageFromNet(url: String?) {
    if (url == null) {
        setImageResource(R.drawable.place_holder)
    } else {
        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.place_holder)
            .error(R.drawable.place_holder)
            .apply(
                RequestOptions
                    .bitmapTransform(CircleCrop())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
            )
            .into(this)

    }
}

/**
 * 从一个url加载圆角图片到view，自定义圆角半径
 */
fun ImageView.setRoundImageFromNet(url: String?, circleDp: Int) {
    if (url == null) {
        setImageResource(R.drawable.place_holder)
    } else {
        var roundedCorners = RoundedCorners(circleDp)
        var options = RequestOptions.bitmapTransform(roundedCorners)
        Glide.with(this).load(url).placeholder(R.drawable.place_holder)
            .error(R.drawable.place_holder)
            .apply(options)
            .into(this)

    }
}

/**
 * 从一个url加载圆形图片到view，自定义圆角半径
 */
fun ImageView.setCircleImageFromNet(url: String?, radius: Int) {
    if (url == null) {
        setImageResource(R.drawable.place_holder)
    } else {
        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.place_holder)
            .error(R.drawable.place_holder)
            .apply(
                RequestOptions
                    .bitmapTransform(RoundedCorners(this.context.dip(radius)))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
            )
            .into(this)

    }
}

/**
 * 从资源文件图片到view
 */
infix fun ImageView.setImageFromR(resId: Int?) {
    if (resId == null) {
        setImageResource(R.drawable.place_holder)
    } else {
        Glide.with(this)
            .load(resId)
            .placeholder(R.drawable.place_holder)
            .error(R.drawable.place_holder)
            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
            .into(this)

    }
}

/**
 * 从资源文件加载图片到View（圆角）
 */
infix fun ImageView.setCircleImageFromR(resId: Int?) {
    if (resId == null) {
        setImageResource(R.drawable.place_holder)
    } else {
        Glide.with(this)
            .load(resId)
            .placeholder(R.drawable.place_holder)
            .error(R.drawable.place_holder)
            .apply(
                RequestOptions
                    .bitmapTransform(CircleCrop())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
            )
            .into(this)

    }
}

/**
 * 从一个url加载图片到view
 */
infix fun ImageView.setImageFromNet(url: String?) {
    if (url == null) {
        setImageResource(R.drawable.place_holder)
    } else {
        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.place_holder)
            .error(R.drawable.place_holder)
            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
            .into(this)
    }
}

/**
 * 从一个url加载图片到view
 */

infix fun ImageView.setImageFromNetCenterCrop(url: String?) {
    if (url == null) {
        setImageResource(R.drawable.place_holder)
    } else {
        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.place_holder)
            .error(R.drawable.place_holder)
            .centerCrop()
            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
            .into(this)
    }
}

/**
 * view隐藏
 */
fun View.gone() {
    if (visibility != View.GONE) {
        visibility = View.GONE
    }
}

/**
 * view可见
 */
fun View.visible() {
    if (visibility != View.VISIBLE) {
        visibility = View.VISIBLE
    }
}

/**
 * view隐藏
 */
fun View.invisible() {
    if (visibility != View.INVISIBLE) {
        visibility = View.INVISIBLE
    }
}

/**
 * view是否可见
 */
fun View.isVisible(): Boolean {
    return visibility == View.VISIBLE
}

/**
 * view是否可见
 */
fun View.isInVisible(): Boolean {
    return visibility == View.INVISIBLE
}

/**
 * view是否隐藏
 */
fun View.isGone(): Boolean {
    return visibility == View.GONE
}

fun ViewGroup.inflate(@LayoutRes id: Int, isAttach: Boolean = false): View {
    return LayoutInflater.from(this.context).inflate(id, this, isAttach)
}

/**
 * bigDecima 金额格式化
 */
fun String.bigDecimalFormat(): String {
    val decimal = BigDecimal(this)
    return decimal.stripTrailingZeros().toPlainString()
}

fun Int.bigDecimalFormat(): String {
    val decimal = BigDecimal(this)
    return decimal.stripTrailingZeros().toPlainString()
}

fun BigDecimal.bigDecimalFormat(): String {
    return this.stripTrailingZeros().toPlainString()
}

/**
 * 状态栏文字颜色，只有白色和黑色
 */
fun Activity.isStatusBarBlackTextColor(isDark: Boolean) {
    if (isDark) {
        //状态栏文字黑色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    } else {
        //系统默认，状态栏文字白色
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE//白色
    }
}

/**
 * 回到桌面，不退出app
 */
fun Activity.goHome() {
    val home = Intent(Intent.ACTION_MAIN)
    home.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    home.addCategory(Intent.CATEGORY_HOME)
    startActivity(home)
}

/**
 * 跳转到浏览器
 */
fun Context.goToWeb(url: String) {
    try {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * 设置窗口的透明度
 */
fun Activity.setWindowAlpha(bgAlpha: Float) {
    val lp = window.attributes
    if (lp.alpha != bgAlpha) {
        lp.alpha = bgAlpha //0.0-1.0
        window.attributes = lp
    }
}

/**
 * 隐藏键盘
 */
fun Activity?.hiddenKeyboard() {
    this ?: return
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    val focusView = window.decorView.findFocus()
    if (imm != null && focusView != null) {
        imm.hideSoftInputFromWindow(focusView.windowToken, 0)
    }
}

/**
 * 隐藏键盘
 */
fun Activity?.showKeyboard(focusView: View?) {
    this ?: return
    focusView ?: return
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    focusView.requestFocus()
    imm?.showSoftInput(focusView, 0)
}


/**
 * 获取图片
 */
fun Context.getResDrawable(@DrawableRes drawableRes: Int?): Drawable? {
    drawableRes ?: return null
    return ContextCompat.getDrawable(this, drawableRes)
}

/**
 * 通用的错误捕获，默认不处理错误，只是防止崩溃
 */
fun tryError(
    handlerError: ((Throwable) -> Unit)? = null,
    finally: (() -> Unit)? = null,
    code: () -> Unit
) {
    try {
        code.invoke()
    } catch (e: Throwable) {
        e.printStackTrace()
        handlerError?.invoke(e)
    } finally {
        finally?.invoke()
    }
}

fun Context.moneyToast(s: String?) {
    s?.let {
        val view = Toast.makeText(this, "", Toast.LENGTH_SHORT).view
        val mToast = Toast(this)
        mToast.view = view
        mToast.setText(it)
        mToast.duration = Toast.LENGTH_SHORT
        mToast.show()
    }
}

fun Fragment.moneyToast(s: String?) {
    s?.let {
        val view = Toast.makeText(this.context, "", Toast.LENGTH_SHORT).view
        val mToast = Toast(this.activity)
        mToast.view = view
        mToast.setText(it)
        mToast.duration = Toast.LENGTH_SHORT
        mToast.show()
    }
}

fun Context.moneyShortToast(s: String?) {
    s?.let {
        val view = Toast.makeText(this, "", Toast.LENGTH_SHORT).view
        val mToast = Toast(this)
        mToast.view = view
        mToast.setText(it)
        mToast.duration = Toast.LENGTH_SHORT
        mToast.show()
    }
}



