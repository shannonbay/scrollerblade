package com.github.shannonbay.wordstream

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

class StatsReviewFragment : Fragment() {

    companion object {
        fun newInstance() = StatsReviewFragment()
    }

    private lateinit var viewModel: StatsReviewViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val bookView = inflater.inflate(R.layout.fragment_stats_review, container, false)

        return bookView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(StatsReviewViewModel::class.java)

    }

}