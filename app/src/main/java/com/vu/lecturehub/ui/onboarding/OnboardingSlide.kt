package com.vu.lecturehub.ui.onboarding

import androidx.annotation.DrawableRes

data class OnboardingSlide(
    val title: String,
    val description: String,
    @DrawableRes val mainIconRes: Int,
    @DrawableRes val badge1IconRes: Int,
    @DrawableRes val badge2IconRes: Int,
    @DrawableRes val badge3IconRes: Int
)
