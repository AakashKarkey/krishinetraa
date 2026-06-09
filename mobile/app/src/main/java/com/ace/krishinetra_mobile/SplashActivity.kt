package com.ace.krishinetra_mobile

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.ace.krishinetra_mobile.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        animateLogo()
    }

    private fun animateLogo() {
        val logo = binding.splashLogo
        val title = binding.splashTitle
        val subtitle = binding.splashSubtitle

        logo.alpha = 0f
        logo.scaleX = 0f
        logo.scaleY = 0f
        title.alpha = 0f
        title.translationY = 40f
        subtitle.alpha = 0f
        subtitle.translationY = 30f

        val logoScaleX = ObjectAnimator.ofFloat(logo, "scaleX", 0f, 1f).apply {
            duration = 600
            interpolator = AccelerateDecelerateInterpolator()
        }
        val logoScaleY = ObjectAnimator.ofFloat(logo, "scaleY", 0f, 1f).apply {
            duration = 600
            interpolator = AccelerateDecelerateInterpolator()
        }
        val logoFade = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f).apply {
            duration = 500
        }

        val titleFade = ObjectAnimator.ofFloat(title, "alpha", 0f, 1f).apply {
            duration = 500
        }
        val titleMove = ObjectAnimator.ofFloat(title, "translationY", 40f, 0f).apply {
            duration = 500
        }

        val subtitleFade = ObjectAnimator.ofFloat(subtitle, "alpha", 0f, 1f).apply {
            duration = 400
        }
        val subtitleMove = ObjectAnimator.ofFloat(subtitle, "translationY", 30f, 0f).apply {
            duration = 400
        }

        val logoSet = AnimatorSet().apply {
            playTogether(logoScaleX, logoScaleY, logoFade)
        }

        val textSet = AnimatorSet().apply {
            playTogether(titleFade, titleMove)
        }

        val subtitleSet = AnimatorSet().apply {
            playTogether(subtitleFade, subtitleMove)
        }

        val fullAnim = AnimatorSet().apply {
            playSequentially(logoSet, textSet, subtitleSet)
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator) {}
                override fun onAnimationEnd(p0: Animator) {
                    navigateToMain()
                }
                override fun onAnimationCancel(p0: Animator) {}
                override fun onAnimationRepeat(p0: Animator) {}
            })
        }

        fullAnim.startDelay = 200
        fullAnim.start()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}