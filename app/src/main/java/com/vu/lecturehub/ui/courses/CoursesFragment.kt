package com.vu.lecturehub.ui.courses

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.vu.lecturehub.R
import com.vu.lecturehub.data.model.Course
import com.vu.lecturehub.data.model.Department
import com.vu.lecturehub.databinding.FragmentCoursesBinding
import com.vu.lecturehub.ui.MainViewModel
import com.vu.lecturehub.ui.adapters.CourseAdapter
import com.vu.lecturehub.ui.adapters.SearchSuggestionAdapter
import com.vu.lecturehub.ui.adapters.SubjectAdapter
import com.vu.lecturehub.ui.detail.CourseDetailActivity

class CoursesFragment : Fragment() {

    private var _binding: FragmentCoursesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var courseAdapter: CourseAdapter
    private lateinit var subjectAdapter: SubjectAdapter
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

        setupPopularSubjectsGrid()
        setupCoursesRecyclerView()
        setupSearchDropdown()
        setupTrendingChips()
        setupSearch()
        observeData()
    }

    private fun setupPopularSubjectsGrid() {
        subjectAdapter = SubjectAdapter(emptyList()) { selectedDept ->
            filterByDepartment(selectedDept)
        }
        binding.rvPopularSubjects.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = subjectAdapter
        }

        binding.btnResetSubjectFilter.setOnClickListener {
            resetDepartmentFilter()
        }
    }

    private fun filterByDepartment(dept: Department) {
        viewModel.selectDepartment(dept)
        binding.tvCatalogSectionTitle.text = dept.name
        binding.btnResetSubjectFilter.visibility = View.VISIBLE
        hideSearchDropdown()

        // Smooth scroll to catalog section
        binding.scrollDiscover.post {
            binding.scrollDiscover.smoothScrollTo(0, binding.tvCatalogSectionTitle.top - 20)
        }
    }

    private fun resetDepartmentFilter() {
        viewModel.selectDepartment(Department("All", 0))
        binding.tvCatalogSectionTitle.text = getString(R.string.explore_courses_and_programs)
        binding.btnResetSubjectFilter.visibility = View.GONE
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

    private fun setupTrendingChips() {
        binding.chipTrendCs201.setOnClickListener { applySearchQuery("CS201") }
        binding.chipTrendPython.setOnClickListener { applySearchQuery("Python") }
        binding.chipTrendData.setOnClickListener { applySearchQuery("Data") }
        binding.chipTrendCalculus.setOnClickListener { applySearchQuery("Calculus") }
        binding.chipTrendAccounting.setOnClickListener { applySearchQuery("Accounting") }
    }

    private fun setupSearch() {
        binding.etSearch.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showSearchDropdown()
            }
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                binding.btnClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
                viewModel.setSearchQuery(query)

                // If user is actively typing, hide dropdown to reveal results list
                if (query.isNotEmpty()) {
                    binding.cardSearchDropdown.visibility = View.GONE
                } else if (binding.etSearch.hasFocus()) {
                    binding.cardSearchDropdown.visibility = View.VISIBLE
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideSearchDropdown()
                hideKeyboard()
                true
            } else {
                false
            }
        }

        binding.btnSearchAction.setOnClickListener {
            val query = binding.etSearch.text.toString().trim()
            viewModel.setSearchQuery(query)
            hideSearchDropdown()
            hideKeyboard()
        }

        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.text.clear()
            viewModel.setSearchQuery("")
            binding.cardSearchDropdown.visibility = View.GONE
        }

        // Hide search dropdown on scroll
        binding.scrollDiscover.setOnScrollChangeListener { _, _, _, _, _ ->
            if (binding.cardSearchDropdown.visibility == View.VISIBLE) {
                hideSearchDropdown()
            }
        }
    }

    private fun applySearchQuery(query: String) {
        binding.etSearch.setText(query)
        binding.etSearch.setSelection(query.length)
        viewModel.setSearchQuery(query)
        hideSearchDropdown()
        hideKeyboard()

        binding.scrollDiscover.post {
            binding.scrollDiscover.smoothScrollTo(0, binding.tvCatalogSectionTitle.top - 20)
        }
    }

    private fun showSearchDropdown() {
        binding.cardSearchDropdown.visibility = View.VISIBLE
    }

    private fun hideSearchDropdown() {
        binding.cardSearchDropdown.visibility = View.GONE
        binding.etSearch.clearFocus()
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
    }

    private fun observeData() {
        // Observe filtered courses
        viewModel.filteredCourses.observe(viewLifecycleOwner) { courses ->
            courseAdapter.submitList(courses)
            binding.emptyView.visibility = if (courses.isEmpty()) View.VISIBLE else View.GONE
        }

        // Observe departments for the 2-column grid
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
