package com.vu.lecturehub.ui.player

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.vu.lecturehub.data.model.Course
import com.vu.lecturehub.data.model.Lecture
import com.vu.lecturehub.data.repository.CourseRepository
import com.vu.lecturehub.databinding.ActivityPlayerBinding
import com.vu.lecturehub.ui.adapters.LectureAdapter
import kotlinx.coroutines.launch

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var repository: CourseRepository
    private var currentCourse: Course? = null
    private var currentLectureIndex: Int = 1
    private var youTubePlayerInstance: YouTubePlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = CourseRepository(this)

        @Suppress("DEPRECATION")
        currentCourse = intent.getSerializableExtra("EXTRA_COURSE") as? Course
        currentLectureIndex = intent.getIntExtra("EXTRA_LECTURE_INDEX", 1)

        if (currentCourse == null) {
            finish()
            return
        }

        updateLectureUI(currentCourse!!, currentLectureIndex)
        setupYouTubePlayer(currentCourse!!, currentLectureIndex)
        setupPlaylistQueue(currentCourse!!)
    }

    private fun updateLectureUI(course: Course, lectureIndex: Int) {
        val formattedNum = String.format("%02d", lectureIndex)
        binding.tvPlayerCourseCode.text = "${course.courseCode ?: "VU"} - Lecture #$formattedNum"
        binding.tvPlayerLectureTitle.text = "Lecture $formattedNum: ${course.title}"
        binding.tvPlayerCourseTitle.text = "${course.department} • Virtual University of Pakistan"

        // Update watch progress in Room DB
        lifecycleScope.launch {
            repository.updateWatchProgress(course.playlistId, lectureIndex)
        }
    }

    private fun setupYouTubePlayer(course: Course, initialLectureIndex: Int) {
        lifecycle.addObserver(binding.youtubePlayerView)

        binding.youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayerInstance = youTubePlayer
                // Load the playlist starting from the selected lecture index (0-indexed)
                val startIndex = (initialLectureIndex - 1).coerceAtLeast(0)
                youTubePlayer.loadPlaylist(course.playlistId, startIndex, 0f)
            }
        })
    }

    private fun setupPlaylistQueue(course: Course) {
        val lectures = repository.generateLectures(course)
        val adapter = LectureAdapter(lectures) { selectedLecture ->
            currentLectureIndex = selectedLecture.lectureIndex
            updateLectureUI(course, currentLectureIndex)
            val startIndex = (currentLectureIndex - 1).coerceAtLeast(0)
            youTubePlayerInstance?.loadPlaylist(course.playlistId, startIndex, 0f)
        }

        binding.rvPlayerLectures.apply {
            layoutManager = LinearLayoutManager(this@PlayerActivity)
            this.adapter = adapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.youtubePlayerView.release()
    }
}
