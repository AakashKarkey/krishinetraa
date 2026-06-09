package com.ace.krishinetra_mobile.utils

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import com.ace.krishinetra_mobile.R

enum class ToastType {
    SUCCESS, ERROR, INFO
}

object Toaster {
    private var currentToast: View? = null

    fun show(
        container: View,
        message: String,
        type: ToastType = ToastType.INFO,
        duration: Long = 2500
    ) {
        currentToast?.let { dismissImmediate(it) }

        val context = container.context
        val toast = TextView(context).apply {
            setText(message)
            setTextSize(14f)
            setTextColor(context.getColor(android.R.color.white))
            setPadding(48, 20, 48, 20)
            setTypeface(android.graphics.Typeface.DEFAULT_BOLD)
            letterSpacing = 0.02f

            val bg = GradientDrawable().apply {
                cornerRadius = 16f
                val colorRes = when (type) {
                    ToastType.SUCCESS -> R.color.toast_success_bg
                    ToastType.ERROR -> R.color.toast_error_bg
                    ToastType.INFO -> R.color.toast_info_bg
                }
                setColor(context.getColor(colorRes))
            }
            background = bg
            elevation = 8f * context.resources.displayMetrics.density
        }

        val root = container.rootView as? FrameLayout ?: return

        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            bottomMargin = (100 * context.resources.displayMetrics.density).toInt()
        }

        root.addView(toast, params)

        toast.alpha = 0f
        toast.translationY = 80f

        val fadeIn = ObjectAnimator.ofFloat(toast, "alpha", 0f, 1f)
        fadeIn.duration = 300
        fadeIn.interpolator = AccelerateDecelerateInterpolator()

        val slideIn = ObjectAnimator.ofFloat(toast, "translationY", 80f, 0f)
        slideIn.duration = 300
        slideIn.interpolator = AccelerateDecelerateInterpolator()

        AnimatorSet().apply {
            playTogether(fadeIn, slideIn)
            start()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            dismiss(toast, root)
        }, duration)

        currentToast = toast
    }

    private fun dismiss(toast: View, root: FrameLayout) {
        val fadeOut = ObjectAnimator.ofFloat(toast, "alpha", 1f, 0f)
        fadeOut.duration = 250
        fadeOut.interpolator = AccelerateDecelerateInterpolator()

        val slideOut = ObjectAnimator.ofFloat(toast, "translationY", 0f, 40f)
        slideOut.duration = 250
        slideOut.interpolator = AccelerateDecelerateInterpolator()

        val exitSet = AnimatorSet()
        exitSet.playTogether(fadeOut, slideOut)
        exitSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {}
            override fun onAnimationEnd(p0: Animator) {
                root.removeView(toast)
                if (currentToast == toast) {
                    currentToast = null
                }
            }
            override fun onAnimationCancel(p0: Animator) {}
            override fun onAnimationRepeat(p0: Animator) {}
        })
        exitSet.start()
    }

    private fun dismissImmediate(toast: View) {
        (toast.parent as? FrameLayout)?.removeView(toast)
        if (currentToast == toast) currentToast = null
    }
}