package com.vu.lecturehub.ui.player

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
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
    private var currentLoadedVideoId: String? = null

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
        setupPlayerWebView()
        setupPlaylistQueue(currentCourse!!)
        setupExternalButton()
        loadRealLectures(currentCourse!!)

        // Start playing initial video immediately
        val initialVideoId = currentLecture?.videoId
            ?: currentCourse?.firstVideoId
            ?: "4L6IRKz54EQ"
        loadVideoInPlayer(initialVideoId)
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

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupPlayerWebView() {
        val webView = binding.playerWebView

        // Crucial for YouTube: Enable third-party cookies for session tokens & embed verification
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false // Enables instant autoplay
            loadWithOverviewMode = true
            useWideViewPort = true
            allowFileAccess = false
            databaseEnabled = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                if (request != null && !request.isForMainFrame) {
                    return false
                }
                val url = request?.url?.toString() ?: return false
                val appOrigin = "https://$packageName"
                if (url.startsWith(appOrigin) || url.contains("youtube.com") || url.contains("googlevideo.com") || url.contains("ytimg.com") || url.contains("youtube-nocookie.com")) {
                    return false
                }
                return try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                    true
                } catch (e: Exception) {
                    false
                }
            }
        }

        webView.webChromeClient = WebChromeClient()

        webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun onVideoEnded() {
                runOnUiThread {
                    playNextLecture()
                }
            }
        }, "AndroidBridge")
    }

    private fun loadVideoInPlayer(videoId: String) {
        currentLoadedVideoId = videoId
        val appOrigin = "https://$packageName"
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <meta name="referrer" content="strict-origin-when-cross-origin">
                <style>
                    * { margin:0; padding:0; box-sizing:border-box; }
                    html, body { width:100%; height:100%; background:#000; overflow:hidden; }
                    #player { width:100%; height:100%; position:absolute; top:0; left:0; border:0; }
                </style>
                <script defer src="https://www.youtube.com/iframe_api"></script>
            </head>
            <body>
                <div id="player"></div>
                <script>
                    var player;
                    function onYouTubeIframeAPIReady() {
                        try {
                            player = new YT.Player('player', {
                                height: '100%',
                                width: '100%',
                                videoId: '$videoId',
                                playerVars: {
                                    'autoplay': 1,
                                    'playsinline': 1,
                                    'controls': 1,
                                    'rel': 0,
                                    'enablejsapi': 1,
                                    'fs': 1,
                                    'origin': '$appOrigin'
                                },
                                events: {
                                    'onReady': function(e) {
                                        try { e.target.playVideo(); } catch(err) {}
                                    },
                                    'onStateChange': function(e) {
                                        if (e.data === 0 && window.AndroidBridge) {
                                            window.AndroidBridge.onVideoEnded();
                                        }
                                    }
                                }
                            });
                        } catch(e) {}
                    }

                    function switchVideo(newId) {
                        if (player && typeof player.loadVideoById === 'function') {
                            player.loadVideoById(newId, 0);
                        } else {
                            location.href = "https://www.youtube.com/embed/" + newId + "?autoplay=1&playsinline=1&controls=1&rel=0&enablejsapi=1&origin=$appOrigin";
                        }
                    }
                </script>
            </body>
            </html>
        """.trimIndent()

        binding.playerWebView.loadDataWithBaseURL(
            appOrigin,
            html,
            "text/html",
            "UTF-8",
            null
        )
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
                        updateLectureUI(course, matching)
                        switchLecture(matching)
                    }
                }
            }
        }
    }

    private fun switchLecture(lecture: Lecture) {
        currentLecture = lecture
        updateLectureUI(currentCourse!!, lecture)
        val vid = lecture.videoId ?: currentCourse?.firstVideoId
        if (!vid.isNullOrEmpty() && vid != currentLoadedVideoId) {
            currentLoadedVideoId = vid
            binding.playerWebView.evaluateJavascript("switchVideo('$vid');", null)
        }
    }

    private fun playNextLecture() {
        val nextIndex = (currentLecture?.lectureIndex ?: 1) + 1
        val nextLecture = currentLectures.find { it.lectureIndex == nextIndex }
        if (nextLecture != null) {
            switchLecture(nextLecture)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.playerContainer.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        } else {
            binding.playerContainer.layoutParams.height = (220 * resources.displayMetrics.density).toInt()
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        binding.playerWebView.onPause()
        binding.playerWebView.evaluateJavascript("if (player && typeof player.pauseVideo === 'function') { player.pauseVideo(); }", null)
    }

    override fun onResume() {
        super.onResume()
        binding.playerWebView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.playerWebView.destroy()
    }
}
