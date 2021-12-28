package com.example.doit.about

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.doit.R
import com.example.doit.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        val binding = DataBindingUtil.inflate<FragmentAboutBinding>(
            inflater, R.layout.fragment_about, container, false
        )

        binding.privatePolicyText.apply {
            isClickable = true
            movementMethod = LinkMovementMethod.getInstance()
            text = HtmlCompat.fromHtml(
                "<a href=''>Private Policy</a>", FROM_HTML_MODE_LEGACY
            )
        }

        return binding.root
    }
}