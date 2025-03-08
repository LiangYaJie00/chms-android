package com.example.chms_android.ui.components
import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.example.chms_android.R

class VerificationCodeButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatButton(context, attrs, defStyleAttr) {

    private var defaultText: String = "发送验证码"
    private var timer: CountDownTimer? = null
    private val totalMillis: Long = 60000 // 60秒
    private val countDownInterval: Long = 1000 // 每秒更新一次

    init {
        text = defaultText

        // 显式设置文本居中
        textAlignment = TEXT_ALIGNMENT_CENTER
        gravity = android.view.Gravity.CENTER

        // 设置按钮的背景色为 ColorStateList
        val colorStateList = ContextCompat.getColorStateList(context, R.color.bg_btn_code_send)
        ViewCompat.setBackgroundTintList(this, colorStateList)

        setOnClickListener { startCountDown() }
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

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        timer?.cancel() // 确保在视图被移除时取消计时器
    }
}