package com.example.chms_android.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.chms_android.R

class BadgeDrawable(context: Context, layoutId: Int) : Drawable() {
    private val view: View = LayoutInflater.from(context).inflate(layoutId, null)
    
    init {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    }

    override fun draw(canvas: Canvas) {
        canvas.save()
        view.draw(canvas)
        canvas.restore()
    }

    override fun setAlpha(alpha: Int) {
        // Not implemented
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        // Not implemented
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun getIntrinsicWidth(): Int {
        return view.measuredWidth
    }

    override fun getIntrinsicHeight(): Int {
        return view.measuredHeight
    }
    
    companion object {
        fun createDoctorBadge(context: Context): BadgeDrawable {
            return BadgeDrawable(context, R.layout.badge_doctor)
        }
    }
}