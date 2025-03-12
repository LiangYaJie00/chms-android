package com.example.chms_android.ui.components

import android.util.AttributeSet
import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.min


class CircularImageView : AppCompatImageView {

    constructor(context: Context?) : super(context!!)

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle)

    override fun onDraw(canvas: Canvas) {
        val radius = (min(width.toDouble(), height.toDouble()) / 2.0f).toFloat()
        val path: Path = Path()
        path.addCircle(width / 2.0f, height / 2.0f, radius, Path.Direction.CCW)
        canvas.clipPath(path)
        super.onDraw(canvas)
    }
}