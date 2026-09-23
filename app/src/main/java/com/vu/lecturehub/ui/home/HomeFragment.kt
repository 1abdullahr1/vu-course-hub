package com.vu.lecturehub.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.vu.lecturehub.MainActivity
import com.vu.lecturehub.R
import com.vu.lecturehub.data.model.Course
import com.vu.lecturehub.databinding.FragmentHomeBinding
import com.vu.lecturehub.ui.MainViewModel
import com.vu.lecturehub.ui.adapters.CourseAdapter
import com.vu.lecturehub.ui.adapters.DepartmentAdapter
import com.vu.lecturehub.ui.detail.CourseDetailActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var departmentAdapter: DepartmentAdapter
    private lateinit var featuredAdapter: CourseAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDepartmentRecyclerView()
        setupFeaturedRecyclerView()

        // Quick search click: switch to Courses tab
        binding.cardSearchShortcut.setOnClickListener {
            (activity as? MainActivity)?.switchTab(R.id.nav_courses)
        }

        // Observe departments
        viewModel.departments.observe(viewLifecycleOwner) { depts ->
            departmentAdapter.updateDepartments(depts)
        }

        // Observe all courses to show popular/featured
        viewModel.allCourses.observe(viewLifecycleOwner) { courses ->
            // Filter popular starting courses (like CS101, CS201, MTH101, MGT211, ENG101)
            val popularCodes = setOf("CS101", "CS201", "MTH101", "MGT211", "ENG101", "CS301", "CS501", "PHY101")
            val featured = courses.filter { c ->
                popularCodes.contains(c.courseCode?.uppercase())
            }.ifEmpty {
                courses.take(8)
            }
            featuredAdapter.submitList(featured)
        }
    }

    private fun setupDepartmentRecyclerView() {
        departmentAdapter = DepartmentAdapter(emptyList()) { selectedDept ->
            viewModel.selectDepartment(selectedDept)
            (activity as? MainActivity)?.switchTab(R.id.nav_courses)
        }
        binding.rvDepartments.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = departmentAdapter
        }
    }

    private fun setupFeaturedRecyclerView() {
        featuredAdapter = CourseAdapter(
            onCourseClick = { course -> openCourseDetail(course) },
            onBookmarkClick = { course -> viewModel.toggleBookmark(course) }
        )
        binding.rvFeaturedCourses.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = featuredAdapter
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
