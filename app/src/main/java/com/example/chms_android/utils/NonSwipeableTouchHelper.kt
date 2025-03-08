package com.example.chms_android.utils

import android.view.MotionEvent
import android.view.View
import androidx.viewpager2.widget.ViewPager2

class NonSwipeableTouchHelper(
    private val viewPager: ViewPager2
) : View.OnTouchListener {

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        // 返回 true 表示拦截触摸事件，从而禁止滑动
        return true
    }

    // 附加方法，用于禁用滑动
    fun attach() {
        viewPager.getChildAt(0).setOnTouchListener(this)
    }

    // 分离方法，用于重新启用滑动
    fun detach() {
        viewPager.getChildAt(0).setOnTouchListener(null)
    }
}