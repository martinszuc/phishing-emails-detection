package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_details.full

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.databinding.FragmentDetailsFullBinding
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_details.full.adapter.EmailFullAdapter

class FullDetailsFragment : Fragment() {

    private var _binding: FragmentDetailsFullBinding? = null
    private val binding get() = _binding!!

    // Assuming EmailFull is Parcelable or Serializable. Use the appropriate method to retrieve it.
    private val email by lazy {
        arguments?.getParcelable<EmailFull>("email")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetailsFullBinding.inflate(inflater, container, false)
        val view = binding.root

        // For MinimalDetailsFragment use swipeRightIndicator, for FullDetailsFragment use swipeLeftIndicator
        val arrowIndicator: ImageView = binding.swipeLeftIndicator
        val animation = AnimationUtils.loadAnimation(context, R.anim.arrow_fade_animation)
        arrowIndicator.startAnimation(animation)

        if (email != null) {
            val emailFullAdapter = EmailFullAdapter()
            binding.fullDetailsRecyclerView.adapter = emailFullAdapter
            binding.fullDetailsRecyclerView.layoutManager = LinearLayoutManager(context)
            emailFullAdapter.emails = listOf(email!!)
            emailFullAdapter.notifyDataSetChanged()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        // Assuming the arrow indicator in FullDetailsFragment is swipeLeftIndicator
        val animation = AnimationUtils.loadAnimation(context, R.anim.arrow_fade_animation)
        binding.swipeLeftIndicator.startAnimation(animation)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(email: EmailFull): FullDetailsFragment {
            val fragment = FullDetailsFragment()
            val bundle = Bundle()
            bundle.putParcelable("email", email) // Make sure EmailFull implements Parcelable
            fragment.arguments = bundle
            return fragment
        }
    }
}
