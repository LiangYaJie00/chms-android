package com.example.chms_android.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.chms_android.R
import com.example.chms_android.databinding.CompRawContainerBinding

class RawContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {
    private val binding: CompRawContainerBinding

    init {
        binding = CompRawContainerBinding.inflate(LayoutInflater.from(context), this)

        if (attrs != null) {
            val attributes = context.obtainStyledAttributes(attrs, R.styleable.RawContainer)

            // Get the resourceId for the start icon
            val startIconResId = attributes.getResourceId(R.styleable.RawContainer_startIcon, 0)
            if (startIconResId != 0) {
                // Set the icon and ensure the ImageView is visible
                binding.root.findViewById<ImageView>(R.id.iv_start_icon).apply {
                    setImageResource(startIconResId)
                    visibility = View.VISIBLE
                }
            } else {
                // Hide the ImageView if no icon is provided
                binding.root.findViewById<ImageView>(R.id.iv_start_icon).visibility = View.GONE
            }

            // Set the title of the TextView
            binding.root.findViewById<TextView>(R.id.tv_container_title)
                .text = attributes.getString(R.styleable.RawContainer_title)

            // Set the icon of the end ImageView
            binding.root.findViewById<ImageView>(R.id.iv_end_icon)
                .setImageResource(attributes.getResourceId(R.styleable.RawContainer_endIcon, R.mipmap.right))

            // Set background based on mode
            val mode = attributes.getInt(R.styleable.RawContainer_rawMode, 0)
            val backgroundRes = when (mode) {
                1 -> R.drawable.bg_top_round
                2 -> R.drawable.bg_bottom_round
                3 -> R.drawable.bg_all_round
                else -> R.drawable.bg_all_square
            }

            binding.root.setBackgroundResource(backgroundRes)

            attributes.recycle()
        }
    }

}