package com.vu.lecturehub.ui.saved

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.vu.lecturehub.data.model.Course
import com.vu.lecturehub.databinding.FragmentSavedBinding
import com.vu.lecturehub.ui.MainViewModel
import com.vu.lecturehub.ui.adapters.CourseAdapter
import com.vu.lecturehub.ui.detail.CourseDetailActivity

class SavedFragment : Fragment() {

    private var _binding: FragmentSavedBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var courseAdapter: CourseAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        courseAdapter = CourseAdapter(
            onCourseClick = { course -> openCourseDetail(course) },
            onBookmarkClick = { course -> viewModel.toggleBookmark(course) }
        )

        binding.rvSavedCourses.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = courseAdapter
        }

        viewModel.bookmarkedCourses.observe(viewLifecycleOwner) { bookmarked ->
            courseAdapter.submitList(bookmarked)
            binding.emptySavedView.visibility = if (bookmarked.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun openCourseDetail(course: Course) {
        val intent = Intent(requireContext(), CourseDetailActivity::class.java).apply {
            putExtra("EXTRA_COURSE", course)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
