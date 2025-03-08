package com.example.chms_android.login.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.chms_android.databinding.DialogBottomRegisterBinding
import com.example.chms_android.login.fragment.RegStepOneFragment
import com.example.chms_android.login.fragment.RegStepTwoFragment
import com.example.chms_android.login.vm.RegisterDialogVM
import com.example.chms_android.utils.NonSwipeableTouchHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RegisterBottomDialog: BottomSheetDialogFragment() {
    private var _binding: DialogBottomRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RegisterDialogVM

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogBottomRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        // 获取底部的 View
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            // 获取其行为
            val behavior = BottomSheetBehavior.from(it)
            // 设置视图高度为屏幕高度的 80%
            val layoutParams = it.layoutParams
            layoutParams.height = (resources.displayMetrics.heightPixels * 0.85).toInt()
            it.layoutParams = layoutParams
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(RegisterDialogVM::class.java)

        val helper = NonSwipeableTouchHelper(binding.vp2Dbr)
        // 禁用step的拖拽滑动
        helper.attach()

        val fragments = listOf(RegStepOneFragment(), RegStepTwoFragment())
        val adapter = StepPagerAdapter(this, fragments)
        binding.vp2Dbr.adapter = adapter

        viewModel.currentPage.observe(viewLifecycleOwner, { page ->
            binding.vp2Dbr.currentItem = page
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class StepPagerAdapter(
        fa: Fragment,
        private val fragments: List<Fragment>
    ) : FragmentStateAdapter(fa) {
        override fun getItemCount() = fragments.size
        override fun createFragment(position: Int) = fragments[position]
    }

}