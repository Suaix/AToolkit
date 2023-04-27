package com.atoolkit.aqrcode.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.atoolkit.aqrcode.databinding.AqrFragmentScanBinding


/**
 * Author:summer
 * Time: 2023/4/27 15:14
 * Description: AScanFragment是
 */

class AScanFragment : Fragment() {

    private lateinit var mBinding: AqrFragmentScanBinding
    private var mViewModel: AScanViewModel? = null

    companion object {
        fun newInstance(bundle: Bundle?): AScanFragment {
            val fBundle = bundle ?: Bundle()
            val fragment = AScanFragment()
            fragment.arguments = fBundle
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = AqrFragmentScanBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
    }

    override fun onStart() {
        super.onStart()
        mViewModel = ViewModelProvider(requireActivity())[AScanViewModel::class.java]
        mViewModel?.init(mBinding.pvCameraPreview)
    }

    private fun initListener() {

    }

}