package com.vu.lecturehub.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.vu.lecturehub.data.model.Course
import com.vu.lecturehub.data.model.Department
import com.vu.lecturehub.data.repository.CourseRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository = CourseRepository(application)

    val allCourses: LiveData<List<Course>> = repository.allCourses.asLiveData()
    val bookmarkedCourses: LiveData<List<Course>> = repository.bookmarkedCourses.asLiveData()
    val recentlyWatchedCourses: LiveData<List<Course>> = repository.recentlyWatchedCourses.asLiveData()

    private val _departments = MutableLiveData<List<Department>>(emptyList())
    val departments: LiveData<List<Department>> = _departments

    private val _filteredCourses = MutableLiveData<List<Course>>(emptyList())
    val filteredCourses: LiveData<List<Course>> = _filteredCourses

    private var currentSearchQuery = ""
    private var currentSelectedDepartment = "All"
    private var rawCoursesList: List<Course> = emptyList()

    init {
        viewModelScope.launch {
            repository.initializeDatabaseIfNeeded()
        }

        viewModelScope.launch {
            repository.allCourses.collectLatest { list ->
                rawCoursesList = list
                if (_departments.value.isNullOrEmpty() && list.isNotEmpty()) {
                    _departments.postValue(repository.extractDepartments(list))
                }
                applyFilters()
            }
        }
    }

    fun setSearchQuery(query: String) {
        currentSearchQuery = query.trim()
        applyFilters()
    }

    fun selectDepartment(dept: Department) {
        currentSelectedDepartment = dept.name
        applyFilters()
    }

    fun toggleBookmark(course: Course) {
        viewModelScope.launch {
            repository.toggleBookmark(course)
        }
    }

    private fun applyFilters() {
        var result = rawCoursesList

        // Department filter
        if (currentSelectedDepartment != "All") {
            result = result.filter { it.department.equals(currentSelectedDepartment, ignoreCase = true) }
        }

        // Search query filter (matches Course Code or Title)
        if (currentSearchQuery.isNotEmpty()) {
            val q = currentSearchQuery.lowercase()
            result = result.filter {
                (it.courseCode?.lowercase()?.contains(q) == true) ||
                it.title.lowercase().contains(q) ||
                it.department.lowercase().contains(q)
            }
        }

        _filteredCourses.postValue(result)
    }
}
