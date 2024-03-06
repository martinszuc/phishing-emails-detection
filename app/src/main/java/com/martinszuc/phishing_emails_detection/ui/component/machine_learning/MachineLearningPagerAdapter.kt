package com.martinszuc.phishing_emails_detection.ui.component.machine_learning

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.martinszuc.phishing_emails_detection.ui.component.data_picking.DataPickingFragment
import com.martinszuc.phishing_emails_detection.ui.component.data_processing.DataProcessingFragment
import com.martinszuc.phishing_emails_detection.ui.component.retraining.RetrainingFragment
import com.martinszuc.phishing_emails_detection.ui.component.training.TrainingFragment

/**
 * Authored by matoszuc@gmail.com
 */

class MachineLearningPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    // The fragments are added to a list to manage the order they are displayed
    private val fragmentList = listOf(
        DataPickingFragment(),
        DataProcessingFragment(),
        TrainingFragment(),
        RetrainingFragment()
    )

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }
}
