package com.vu.lecturehub.ui.player

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.vu.lecturehub.data.model.Course
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
        setupYouTubePlayer(currentCourse!!)
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

    private fun setupYouTubePlayer(course: Course) {
        lifecycle.addObserver(binding.youtubePlayerView)

        val iFramePlayerOptions = IFramePlayerOptions.Builder()
            .controls(1)
            .listType("playlist")
            .list(course.playlistId)
            .build()

        binding.youtubePlayerView.initialize(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayerInstance = youTubePlayer
            }
        }, iFramePlayerOptions)
    }

    private fun setupPlaylistQueue(course: Course) {
        val lectures = repository.generateLectures(course)
        val adapter = LectureAdapter(lectures) { selectedLecture ->
            currentLectureIndex = selectedLecture.lectureIndex
            updateLectureUI(course, currentLectureIndex)
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
