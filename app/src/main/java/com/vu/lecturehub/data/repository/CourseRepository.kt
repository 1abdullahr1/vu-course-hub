package com.vu.lecturehub.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vu.lecturehub.data.db.AppDatabase
import com.vu.lecturehub.data.model.Course
import com.vu.lecturehub.data.model.Department
import com.vu.lecturehub.data.model.Lecture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

class CourseRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val courseDao = db.courseDao()

    val allCourses: Flow<List<Course>> = courseDao.getAllCourses()
    val bookmarkedCourses: Flow<List<Course>> = courseDao.getBookmarkedCourses()
    val recentlyWatchedCourses: Flow<List<Course>> = courseDao.getRecentlyWatchedCourses()

    suspend fun initializeDatabaseIfNeeded() = withContext(Dispatchers.IO) {
        val count = courseDao.getCount()
        if (count == 0) {
            val coursesFromAssets = loadCoursesFromAssets()
            if (coursesFromAssets.isNotEmpty()) {
                courseDao.insertAll(coursesFromAssets)
            }
        }
    }

    private fun loadCoursesFromAssets(): List<Course> {
        return try {
            context.assets.open("courses.json").use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val type = object : TypeToken<List<Course>>() {}.type
                    Gson().fromJson(reader, type) ?: emptyList()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun toggleBookmark(course: Course) = withContext(Dispatchers.IO) {
        val newStatus = !course.isBookmarked
        courseDao.setBookmarked(course.playlistId, newStatus)
    }

    suspend fun updateWatchProgress(playlistId: String, lectureIndex: Int) = withContext(Dispatchers.IO) {
        courseDao.updateWatchProgress(playlistId, lectureIndex, System.currentTimeMillis())
    }

    suspend fun getCourseById(playlistId: String): Course? = withContext(Dispatchers.IO) {
        courseDao.getCourseById(playlistId)
    }

    fun generateLectures(course: Course): List<Lecture> {
        val count = if (course.videoCount > 0) course.videoCount else 30
        return (1..count).map { idx ->
            val formattedIdx = String.format("%02d", idx)
            Lecture(
                playlistId = course.playlistId,
                lectureIndex = idx,
                title = "Lecture $formattedIdx - ${course.courseCode ?: course.title}",
                thumbnailUrl = course.thumbnailUrl,
                isCompleted = idx < course.lastWatchedLectureIndex
            )
        }
    }

    fun extractDepartments(courses: List<Course>): List<Department> {
        val counts = mutableMapOf<String, Int>()
        for (c in courses) {
            counts[c.department] = (counts[c.department] ?: 0) + 1
        }
        val list = counts.map { (dept, cnt) ->
            Department(name = dept, courseCount = cnt, isSelected = false)
        }.sortedByDescending { it.courseCount }

        return listOf(Department("All", courses.size, isSelected = true)) + list
    }
}
