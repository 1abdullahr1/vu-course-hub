package com.vu.lecturehub.ui.courses

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vu.lecturehub.R
import com.vu.lecturehub.data.model.Course
import com.vu.lecturehub.data.model.Department
import com.vu.lecturehub.databinding.FragmentCoursesBinding
import com.vu.lecturehub.ui.MainViewModel
import com.vu.lecturehub.ui.adapters.CourseAdapter
import com.vu.lecturehub.ui.adapters.DiscoverHeaderAdapter
import com.vu.lecturehub.ui.adapters.SearchSuggestionAdapter
import com.vu.lecturehub.ui.adapters.SubjectAdapter
import com.vu.lecturehub.ui.detail.CourseDetailActivity

class CoursesFragment : Fragment() {

    private var _binding: FragmentCoursesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var courseAdapter: CourseAdapter
    private lateinit var subjectAdapter: SubjectAdapter
    private lateinit var headerAdapter: DiscoverHeaderAdapter
    private lateinit var searchSuggestionAdapter: SearchSuggestionAdapter

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

        setupAdapters()
        setupSearchDropdown()
        observeData()
    }

    private fun setupAdapters() {
        // 1. Subject adapter for 2-column grid in header
        subjectAdapter = SubjectAdapter(emptyList()) { selectedDept ->
            filterByDepartment(selectedDept)
        }

        // 2. Header adapter containing Hero banner, Search box, and Popular Subjects
        headerAdapter = DiscoverHeaderAdapter(
            subjectAdapter = subjectAdapter,
            onSearchQueryChanged = { query ->
                viewModel.setSearchQuery(query)
                if (query.isNotEmpty()) {
                    hideSearchDropdown()
                }
            },
            onSearchSubmitted = { query ->
                viewModel.setSearchQuery(query)
                hideSearchDropdown()
                hideKeyboard()
            },
            onSearchFocused = {
                showSearchDropdown()
            },
            onTrendingChipClicked = { tag ->
                applySearchQuery(tag)
            },
            onResetFilterClicked = {
                resetDepartmentFilter()
            }
        )

        // 3. Course list adapter for catalog items
        courseAdapter = CourseAdapter(
            onCourseClick = { course -> openCourseDetail(course) },
            onBookmarkClick = { course -> viewModel.toggleBookmark(course) }
        )

        // 4. Combine header and courses using ConcatAdapter (isolateViewTypes = true by default prevents ClassCastException)
        val concatAdapter = ConcatAdapter(headerAdapter, courseAdapter)

        binding.rvCourses.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = concatAdapter
            setHasFixedSize(true)
            // Smoothly hide search dropdown when user scrolls
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState != RecyclerView.SCROLL_STATE_IDLE && binding.cardSearchDropdown.visibility == View.VISIBLE) {
                        hideSearchDropdown()
                        hideKeyboard()
                    }
                }
            })
        }
    }

    private fun filterByDepartment(dept: Department) {
        viewModel.selectDepartment(dept)
        headerAdapter.updateCatalogHeader(dept.name, true)
        hideSearchDropdown()

        // Smooth scroll to catalog items (just below header)
        binding.rvCourses.post {
            val total = binding.rvCourses.adapter?.itemCount ?: 0
            if (total > 1) {
                (binding.rvCourses.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(1, 0)
            }
        }
    }

    private fun resetDepartmentFilter() {
        viewModel.selectDepartment(Department("All", 0))
        headerAdapter.updateCatalogHeader(getString(R.string.explore_courses_and_programs), false)
    }

    private fun setupSearchDropdown() {
        searchSuggestionAdapter = SearchSuggestionAdapter(emptyList()) { course ->
            hideSearchDropdown()
            openCourseDetail(course)
        }
        binding.rvSearchPopular.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchSuggestionAdapter
        }

        // Trending now rows in dropdown
        binding.itemTrendCs.setOnClickListener { applySearchQuery("CS") }
        binding.itemTrendData.setOnClickListener { applySearchQuery("Data") }
        binding.itemTrendMath.setOnClickListener { applySearchQuery("MTH") }
        binding.itemTrendBusiness.setOnClickListener { applySearchQuery("MGT") }
    }

    private fun applySearchQuery(query: String) {
        headerAdapter.setSearchQueryText(query)
        viewModel.setSearchQuery(query)
        hideSearchDropdown()
        hideKeyboard()

        // Smooth scroll to catalog items
        binding.rvCourses.post {
            val total = binding.rvCourses.adapter?.itemCount ?: 0
            if (total > 1) {
                (binding.rvCourses.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(1, 0)
            }
        }
    }

    private fun showSearchDropdown() {
        if (binding.cardSearchDropdown.visibility != View.VISIBLE) {
            binding.cardSearchDropdown.alpha = 0f
            binding.cardSearchDropdown.visibility = View.VISIBLE
            binding.cardSearchDropdown.animate()
                .alpha(1f)
                .setDuration(200)
                .start()
        }
    }

    private fun hideSearchDropdown() {
        if (binding.cardSearchDropdown.visibility == View.VISIBLE) {
            binding.cardSearchDropdown.animate()
                .alpha(0f)
                .setDuration(150)
                .withEndAction {
                    binding.cardSearchDropdown.visibility = View.GONE
                }
                .start()
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun observeData() {
        // Observe filtered courses
        viewModel.filteredCourses.observe(viewLifecycleOwner) { courses ->
            courseAdapter.submitList(courses)
            binding.emptyView.visibility = if (courses.isEmpty()) View.VISIBLE else View.GONE
        }

        // Observe departments for the 2-column grid in header
        viewModel.departments.observe(viewLifecycleOwner) { depts ->
            subjectAdapter.updateDepartments(depts)
        }

        // Populate "Most popular programs" in search dropdown from top courses
        viewModel.allCourses.observe(viewLifecycleOwner) { courses ->
            val topPopular = courses.filter { c ->
                val code = c.courseCode?.uppercase() ?: ""
                code in setOf("CS101", "CS201", "MTH101", "MGT211")
            }.ifEmpty { courses.take(4) }
            searchSuggestionAdapter.updateSuggestions(topPopular)
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
