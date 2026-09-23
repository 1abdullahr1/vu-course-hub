package com.vu.lecturehub.ui.detail

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.vu.lecturehub.R
import com.vu.lecturehub.data.model.Course
import com.vu.lecturehub.data.model.Lecture
import com.vu.lecturehub.data.repository.CourseRepository
import com.vu.lecturehub.databinding.ActivityCourseDetailBinding
import com.vu.lecturehub.ui.adapters.LectureAdapter
import com.vu.lecturehub.ui.player.PlayerActivity
import kotlinx.coroutines.launch

class CourseDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCourseDetailBinding
    private lateinit var repository: CourseRepository
    private var currentCourse: Course? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = CourseRepository(this)

        @Suppress("DEPRECATION")
        currentCourse = intent.getSerializableExtra("EXTRA_COURSE") as? Course

        if (currentCourse == null) {
            finish()
            return
        }

        setupToolbar()
        populateCourseDetails(currentCourse!!)
        setupLecturesList(currentCourse!!)
        setupActionButtons(currentCourse!!)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun populateCourseDetails(course: Course) {
        binding.detailTitle.text = course.title
        binding.detailCourseCode.text = course.courseCode ?: "VU"
        binding.detailDepartment.text = course.department

        val countText = if (course.videoCount > 0) "${course.videoCount} Lectures" else "Course Lectures"
        binding.detailLectureCount.text = countText

        updateBookmarkButton(course.isBookmarked)

        binding.detailThumbnail.load(course.thumbnailUrl) {
            crossfade(true)
            placeholder(R.drawable.playlist_placeholder)
            error(R.drawable.playlist_placeholder)
        }
    }

    private fun setupActionButtons(course: Course) {
        binding.btnPlayFirst.setOnClickListener {
            val lectureIndex = if (course.lastWatchedLectureIndex > 0) course.lastWatchedLectureIndex else 1
            playLecture(course, lectureIndex)
        }

        binding.btnBookmarkToggle.setOnClickListener {
            lifecycleScope.launch {
                repository.toggleBookmark(course)
                course.isBookmarked = !course.isBookmarked
                updateBookmarkButton(course.isBookmarked)
            }
        }
    }

    private fun updateBookmarkButton(isBookmarked: Boolean) {
        if (isBookmarked) {
            binding.btnBookmarkToggle.text = getString(R.string.enrolled_course)
            binding.btnBookmarkToggle.setIconResource(R.drawable.ic_bookmark)
        } else {
            binding.btnBookmarkToggle.text = getString(R.string.enroll_course)
            binding.btnBookmarkToggle.setIconResource(R.drawable.ic_bookmark_outlined)
        }
    }

    private fun setupLecturesList(course: Course) {
        val lectures = repository.generateLectures(course)
        val adapter = LectureAdapter(lectures) { lecture ->
            playLecture(course, lecture.lectureIndex)
        }

        binding.rvDetailLectures.apply {
            layoutManager = LinearLayoutManager(this@CourseDetailActivity)
            this.adapter = adapter
        }
    }

    private fun playLecture(course: Course, lectureIndex: Int) {
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra("EXTRA_COURSE", course)
            putExtra("EXTRA_LECTURE_INDEX", lectureIndex)
        }
        startActivity(intent)
    }
}
