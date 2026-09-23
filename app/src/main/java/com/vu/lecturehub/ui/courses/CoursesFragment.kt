package com.vu.lecturehub.ui.courses

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.vu.lecturehub.data.model.Course
import com.vu.lecturehub.databinding.FragmentCoursesBinding
import com.vu.lecturehub.ui.MainViewModel
import com.vu.lecturehub.ui.adapters.CourseAdapter
import com.vu.lecturehub.ui.adapters.DepartmentAdapter
import com.vu.lecturehub.ui.detail.CourseDetailActivity

class CoursesFragment : Fragment() {

    private var _binding: FragmentCoursesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var courseAdapter: CourseAdapter
    private lateinit var departmentAdapter: DepartmentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoursesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDepartmentFilter()
        setupCoursesRecyclerView()
        setupSearch()

        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
        }

        // Observe filtered courses
        viewModel.filteredCourses.observe(viewLifecycleOwner) { courses ->
            courseAdapter.submitList(courses)
            binding.emptyView.visibility = if (courses.isEmpty()) View.VISIBLE else View.GONE
        }

        // Observe departments
        viewModel.departments.observe(viewLifecycleOwner) { depts ->
            departmentAdapter.updateDepartments(depts)
        }
    }

    private fun setupDepartmentFilter() {
        departmentAdapter = DepartmentAdapter(emptyList()) { selectedDept ->
            viewModel.selectDepartment(selectedDept)
        }
        binding.rvFilterDepartments.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = departmentAdapter
        }
    }

    private fun setupCoursesRecyclerView() {
        courseAdapter = CourseAdapter(
            onCourseClick = { course -> openCourseDetail(course) },
            onBookmarkClick = { course -> viewModel.toggleBookmark(course) }
        )
        binding.rvCourses.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = courseAdapter
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                binding.btnClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
                viewModel.setSearchQuery(query)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.text.clear()
            viewModel.setSearchQuery("")
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
