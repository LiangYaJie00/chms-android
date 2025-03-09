package com.example.chms_android.ui.components
import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.example.chms_android.R

// 定义接口
interface OnVerificationButtonClickListener {
    fun shouldStartCountDown(view: VerificationCodeButton): Boolean
}

class VerificationCodeButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatButton(context, attrs, defStyleAttr) {

    private var defaultText: String = "发送验证码"
    private var timer: CountDownTimer? = null
    private val totalMillis: Long = 60000 // 60秒
    private val countDownInterval: Long = 1000 // 每秒更新一次

    // 外部设置的点击监听接口
    private var externalClickListener: OnVerificationButtonClickListener? = null

    init {
        text = defaultText

        // 使用内部的点击事件逻辑
        super.setOnClickListener { view ->
            // 检查外部监听器是否允许开始倒计时
            val startCountDown = externalClickListener?.shouldStartCountDown(this) ?: true
            if (startCountDown) {
                startCountDown()
            }
        }


        // 显式设置文本居中
        textAlignment = TEXT_ALIGNMENT_CENTER
        gravity = android.view.Gravity.CENTER

        // 设置按钮的背景色为 ColorStateList
        val colorStateList = ContextCompat.getColorStateList(context, R.color.bg_btn_code_send)
        ViewCompat.setBackgroundTintList(this, colorStateList)

    }

    private fun startCountDown() {
        // 禁用按钮
        isEnabled = false

        // 开始倒计时
        timer = object : CountDownTimer(totalMillis, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                text = "重新发送 ($secondsRemaining s)"
            }

            override fun onFinish() {
                isEnabled = true
                text = defaultText
            }
        }.start()
    }

    // 提供一个方法供外部调用以设置监听器
    fun setOnVerificationButtonClickListener(listener: OnVerificationButtonClickListener) {
        externalClickListener = listener
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        timer?.cancel() // 确保在视图被移除时取消计时器
    }
}