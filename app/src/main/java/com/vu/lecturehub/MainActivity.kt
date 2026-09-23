package com.vu.lecturehub

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.vu.lecturehub.databinding.ActivityMainBinding
import com.vu.lecturehub.ui.MainViewModel
import com.vu.lecturehub.ui.courses.CoursesFragment
import com.vu.lecturehub.ui.home.HomeFragment
import com.vu.lecturehub.ui.saved.SavedFragment
import com.vu.lecturehub.util.ThemeManager

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG_HOME = "HOME"
        private const val TAG_COURSES = "COURSES"
        private const val TAG_SAVED = "SAVED"
        private const val KEY_ACTIVE_TAG = "KEY_ACTIVE_TAG"
    }

    private lateinit var binding: ActivityMainBinding
    val viewModel: MainViewModel by viewModels()

    private var activeTag: String = TAG_HOME

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            val home = HomeFragment()
            val courses = CoursesFragment()
            val saved = SavedFragment()
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, saved, TAG_SAVED).hide(saved)
                .add(R.id.fragment_container, courses, TAG_COURSES).hide(courses)
                .add(R.id.fragment_container, home, TAG_HOME)
                .commit()
            activeTag = TAG_HOME
        } else {
            activeTag = savedInstanceState.getString(KEY_ACTIVE_TAG, TAG_HOME) ?: TAG_HOME
            // Restore visibility state cleanly across activity recreation (theme change, rotation, etc.)
            val tx = supportFragmentManager.beginTransaction()
            listOf(TAG_HOME, TAG_COURSES, TAG_SAVED).forEach { tag ->
                val fragment = supportFragmentManager.findFragmentByTag(tag)
                if (fragment != null) {
                    if (tag == activeTag) {
                        tx.show(fragment)
                    } else {
                        tx.hide(fragment)
                    }
                }
            }
            tx.commit()
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    showTab(TAG_HOME)
                    true
                }
                R.id.nav_courses -> {
                    showTab(TAG_COURSES)
                    true
                }
                R.id.nav_saved -> {
                    showTab(TAG_SAVED)
                    true
                }
                else -> false
            }
        }

        // Ensure bottom nav matches active tag
        val expectedNavId = when (activeTag) {
            TAG_COURSES -> R.id.nav_courses
            TAG_SAVED -> R.id.nav_saved
            else -> R.id.nav_home
        }
        if (binding.bottomNavigation.selectedItemId != expectedNavId) {
            binding.bottomNavigation.selectedItemId = expectedNavId
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_ACTIVE_TAG, activeTag)
    }

    fun switchTab(menuItemId: Int) {
        binding.bottomNavigation.selectedItemId = menuItemId
    }

    private fun showTab(targetTag: String) {
        if (activeTag == targetTag) return

        val currentFragment = supportFragmentManager.findFragmentByTag(activeTag)
        var targetFragment = supportFragmentManager.findFragmentByTag(targetTag)

        val tx = supportFragmentManager.beginTransaction()

        if (currentFragment != null) {
            tx.hide(currentFragment)
        }

        // Hide any other visible fragments
        listOf(TAG_HOME, TAG_COURSES, TAG_SAVED).forEach { tag ->
            if (tag != targetTag && tag != activeTag) {
                supportFragmentManager.findFragmentByTag(tag)?.let { f ->
                    if (!f.isHidden) tx.hide(f)
                }
            }
        }

        if (targetFragment == null) {
            targetFragment = when (targetTag) {
                TAG_COURSES -> CoursesFragment()
                TAG_SAVED -> SavedFragment()
                else -> HomeFragment()
            }
            tx.add(R.id.fragment_container, targetFragment, targetTag)
        } else {
            tx.show(targetFragment)
        }

        tx.commit()
        activeTag = targetTag
    }
}
