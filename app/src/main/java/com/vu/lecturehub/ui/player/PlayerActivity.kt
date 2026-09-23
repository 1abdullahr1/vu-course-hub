package com.vu.lecturehub.ui.player

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
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
    private var currentLecture: Lecture? = null
    private var currentLectures: List<Lecture> = emptyList()
    private lateinit var queueAdapter: LectureAdapter
    private var youTubePlayerInstance: YouTubePlayer? = null
    private var isPlayerReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = CourseRepository(this)

        @Suppress("DEPRECATION")
        currentCourse = intent.getSerializableExtra("EXTRA_COURSE") as? Course
        @Suppress("DEPRECATION")
        currentLecture = intent.getSerializableExtra("EXTRA_LECTURE") as? Lecture

        if (currentCourse == null) {
            finish()
            return
        }

        if (currentLecture == null) {
            currentLecture = Lecture(
                playlistId = currentCourse!!.playlistId,
                lectureIndex = 1,
                title = "Lecture 01 - ${currentCourse!!.title}",
                videoId = currentCourse!!.firstVideoId,
                thumbnailUrl = currentCourse!!.thumbnailUrl
            )
        }

        updateLectureUI(currentCourse!!, currentLecture!!)
        setupYouTubePlayer()
        setupPlaylistQueue(currentCourse!!)
        setupExternalButton()
        loadRealLectures(currentCourse!!)
    }

    private fun updateLectureUI(course: Course, lecture: Lecture) {
        val formattedNum = String.format("%02d", lecture.lectureIndex)
        binding.tvPlayerCourseCode.text = "${course.courseCode ?: "VU"} - Lecture #$formattedNum"
        binding.tvPlayerLectureTitle.text = lecture.title
        binding.tvPlayerCourseTitle.text = "${course.department} • Virtual University of Pakistan"

        lifecycleScope.launch {
            repository.updateWatchProgress(course.playlistId, lecture.lectureIndex)
        }
    }

    private fun setupExternalButton() {
        binding.btnOpenYoutube.setOnClickListener {
            val vid = currentLecture?.videoId
                ?: currentCourse?.firstVideoId
            if (!vid.isNullOrEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$vid"))
                startActivity(intent)
            }
        }
    }

    private fun setupYouTubePlayer() {
        lifecycle.addObserver(binding.youtubePlayerView)

        val iFramePlayerOptions = IFramePlayerOptions.Builder()
            .controls(1)
            .rel(0)
            .build()

        binding.youtubePlayerView.initialize(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayerInstance = youTubePlayer
                isPlayerReady = true
                val targetVideoId = currentLecture?.videoId
                    ?: currentCourse?.firstVideoId

                if (!targetVideoId.isNullOrEmpty()) {
                    youTubePlayer.loadVideo(targetVideoId, 0f)
                }
            }

            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                if (state == PlayerConstants.PlayerState.ENDED) {
                    playNextLecture()
                }
            }
        }, iFramePlayerOptions)
    }

    private fun setupPlaylistQueue(course: Course) {
        currentLectures = repository.generateLectures(course)
        queueAdapter = LectureAdapter(currentLectures) { selectedLecture ->
            switchLecture(selectedLecture)
        }

        binding.rvPlayerLectures.apply {
            layoutManager = LinearLayoutManager(this@PlayerActivity)
            this.adapter = queueAdapter
        }
    }

    private fun loadRealLectures(course: Course) {
        lifecycleScope.launch {
            val real = repository.fetchPlaylistVideos(course)
            if (real.isNotEmpty()) {
                currentLectures = real
                queueAdapter.updateLectures(real)
                if (currentLecture?.videoId.isNullOrEmpty()) {
                    val matching = real.find { it.lectureIndex == currentLecture?.lectureIndex } ?: real.firstOrNull()
                    if (matching?.videoId != null) {
                        currentLecture = matching
                        if (isPlayerReady) {
                            youTubePlayerInstance?.loadVideo(matching.videoId, 0f)
                        }
                    }
                }
            }
        }
    }

    private fun switchLecture(lecture: Lecture) {
        currentLecture = lecture
        updateLectureUI(currentCourse!!, lecture)
        val vid = lecture.videoId ?: currentCourse?.firstVideoId
        if (!vid.isNullOrEmpty() && isPlayerReady) {
            youTubePlayerInstance?.loadVideo(vid, 0f)
        }
    }

    private fun playNextLecture() {
        val nextIndex = (currentLecture?.lectureIndex ?: 1) + 1
        val nextLecture = currentLectures.find { it.lectureIndex == nextIndex }
        if (nextLecture != null) {
            switchLecture(nextLecture)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.youtubePlayerView.release()
    }
}
