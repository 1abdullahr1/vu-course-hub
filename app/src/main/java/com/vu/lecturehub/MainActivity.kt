package com.vu.lecturehub

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.vu.lecturehub.databinding.ActivityMainBinding
import com.vu.lecturehub.ui.MainViewModel
import com.vu.lecturehub.ui.courses.CoursesFragment
import com.vu.lecturehub.ui.home.HomeFragment
import com.vu.lecturehub.ui.saved.SavedFragment
import com.vu.lecturehub.util.ThemeManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val viewModel: MainViewModel by viewModels()

    private val homeFragment = HomeFragment()
    private val coursesFragment = CoursesFragment()
    private val savedFragment = SavedFragment()
    private var activeFragment: Fragment = homeFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, savedFragment, "SAVED").hide(savedFragment)
                .add(R.id.fragment_container, coursesFragment, "COURSES").hide(coursesFragment)
                .add(R.id.fragment_container, homeFragment, "HOME")
                .commit()
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    showFragment(homeFragment)
                    true
                }
                R.id.nav_courses -> {
                    showFragment(coursesFragment)
                    true
                }
                R.id.nav_saved -> {
                    showFragment(savedFragment)
                    true
                }
                else -> false
            }
        }
    }

    fun switchTab(menuItemId: Int) {
        binding.bottomNavigation.selectedItemId = menuItemId
    }

    private fun showFragment(target: Fragment) {
        if (activeFragment != target) {
            supportFragmentManager.beginTransaction()
                .hide(activeFragment)
                .show(target)
                .commit()
            activeFragment = target
        }
    }
}
