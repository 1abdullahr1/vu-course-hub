package com.vu.lecturehub.ui.onboarding

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.vu.lecturehub.MainActivity
import com.vu.lecturehub.R
import com.vu.lecturehub.databinding.ActivityOnboardingBinding
import com.vu.lecturehub.util.OnboardingManager

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var slides: List<OnboardingSlide>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupStatusBar()
        setupSlides()
        setupListeners()
    }

    private fun setupStatusBar() {
        window.statusBarColor = Color.WHITE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    private fun setupSlides() {
        slides = listOf(
            OnboardingSlide(
                title = getString(R.string.onboarding_title_1),
                description = getString(R.string.onboarding_desc_1),
                mainIconRes = R.drawable.ic_onboarding_shield,
                badge1IconRes = R.drawable.ic_onboarding_check,
                badge2IconRes = R.drawable.ic_onboarding_lock,
                badge3IconRes = R.drawable.ic_bell
            ),
            OnboardingSlide(
                title = getString(R.string.onboarding_title_2),
                description = getString(R.string.onboarding_desc_2),
                mainIconRes = R.drawable.ic_onboarding_grad,
                badge1IconRes = R.drawable.ic_play,
                badge2IconRes = R.drawable.ic_book,
                badge3IconRes = R.drawable.ic_library
            ),
            OnboardingSlide(
                title = getString(R.string.onboarding_title_3),
                description = getString(R.string.onboarding_desc_3),
                mainIconRes = R.drawable.ic_onboarding_history,
                badge1IconRes = R.drawable.ic_bookmark,
                badge2IconRes = R.drawable.ic_trending_up,
                badge3IconRes = R.drawable.ic_play
            )
        )

        binding.viewPager.adapter = OnboardingAdapter(slides)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateIndicators(position)
            }
        })

        updateIndicators(0)
    }

    private fun setupListeners() {
        binding.btnSkip.setOnClickListener {
            completeOnboarding()
        }

        binding.btnAction.setOnClickListener {
            val currentPos = binding.viewPager.currentItem
            if (currentPos < slides.size - 1) {
                binding.viewPager.currentItem = currentPos + 1
            } else {
                completeOnboarding()
            }
        }
    }

    private fun updateIndicators(position: Int) {
        val indicators = listOf(binding.indicator0, binding.indicator1, binding.indicator2)
        val density = resources.displayMetrics.density

        for (i in indicators.indices) {
            val view = indicators[i]
            val params = view.layoutParams
            if (i == position) {
                params.width = (24 * density).toInt()
                view.setBackgroundResource(R.drawable.indicator_dot_active)
            } else {
                params.width = (8 * density).toInt()
                view.setBackgroundResource(R.drawable.indicator_dot_inactive)
            }
            view.layoutParams = params
        }

        binding.btnAction.text = if (position == slides.size - 1) {
            getString(R.string.onboarding_get_started)
        } else {
            getString(R.string.onboarding_next)
        }
    }

    private fun completeOnboarding() {
        OnboardingManager.setOnboardingCompleted(this)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }

    override fun onBackPressed() {
        val currentPos = binding.viewPager.currentItem
        if (currentPos > 0) {
            binding.viewPager.currentItem = currentPos - 1
        } else {
            super.onBackPressed()
        }
    }
}
