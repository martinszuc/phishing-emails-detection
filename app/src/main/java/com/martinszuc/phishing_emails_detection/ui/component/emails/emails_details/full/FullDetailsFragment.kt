package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_details.full

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.LinearLayoutManager
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.databinding.FragmentDetailsFullBinding
import com.martinszuc.phishing_emails_detection.ui.base.AbstractBaseFragment
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_details.full.adapter.EmailFullAdapter

/**
 * Authored by matoszuc@gmail.com
 */

class FullDetailsFragment : AbstractBaseFragment() {

    private var _binding: FragmentDetailsFullBinding? = null
    private val binding get() = _binding!!

    private val email: EmailFull? by lazy {
        arguments?.getParcelable<EmailFull>("email")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetailsFullBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
        startAnimation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpViews() {
        email?.let {
            val emailFullAdapter = EmailFullAdapter()
            binding.fullDetailsRecyclerView.adapter = emailFullAdapter
            binding.fullDetailsRecyclerView.layoutManager = LinearLayoutManager(context)
            emailFullAdapter.emails = listOf(it)
            emailFullAdapter.notifyDataSetChanged()
        }
    }

    private fun startAnimation() {
        binding.swipeLeftIndicator.startAnimation(AnimationUtils.loadAnimation(context, R.anim.arrow_fade_animation))
    }

    companion object {
        fun newInstance(email: EmailFull): FullDetailsFragment {
            val fragment = FullDetailsFragment()
            val bundle = Bundle()
            bundle.putParcelable("email", email)
            fragment.arguments = bundle
            return fragment
        }
    }
}
