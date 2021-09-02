package com.dontpunchmugs.winratetracker

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils

class PopUpWindow : AppCompatActivity() {

    private lateinit var popupTitle : TextView
    private lateinit var popupText : TextView
    private lateinit var popupYesButton : Button
    private lateinit var popupNoButton : Button
    private lateinit var popupCardView : CardView
    private lateinit var popupBackground : ConstraintLayout
    private var popupYesDelaySeconds = 0
    private var isStatusBarDark = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0,0)
        setContentView(R.layout.activity_pop_up_window)

        popupTitle = findViewById(R.id.popup_window_title)
        popupText = findViewById(R.id.popup_window_text)
        popupYesButton = findViewById(R.id.popup_window_yes_button)
        popupNoButton = findViewById(R.id.popup_window_no_button)
        popupCardView = findViewById(R.id.popup_window_view_with_border)
        popupBackground = findViewById(R.id.popup_window_background_container)

        // Extra data from intent
        val bundle = intent.extras
        if (bundle != null) {
            popupTitle.text = bundle.getString("popuptitle", popupTitle.text.toString())
            popupText.text = bundle.getString("popuptext", popupText.text.toString())
            popupYesButton.text = bundle.getString("popupyestext", popupYesButton.text.toString())
            popupNoButton.text = bundle.getString("popupnotext", popupNoButton.text.toString())
            popupYesDelaySeconds = bundle.getInt("popupyesdelayseconds", popupYesDelaySeconds)
            isStatusBarDark = bundle.getBoolean("darkstatusbar", isStatusBarDark)
        }

        // Set the Status bar appearance for different API levels
        if (Build.VERSION.SDK_INT in 19..20) {
            setWindowFlag(this, true)
        }
        if (Build.VERSION.SDK_INT >= 19) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        if (Build.VERSION.SDK_INT >= 21) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // If you want dark status bar, set darkStatusBar to true
                if (isStatusBarDark) {
                    this.window.decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
                this.window.statusBarColor = Color.TRANSPARENT
                setWindowFlag(this, false)
            }
        }

        // Fade animation for the background of Popup Window
        val alpha = 100 //between 0-255
        val alphaColor = ColorUtils.setAlphaComponent(Color.parseColor("#000000"), alpha)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), Color.TRANSPARENT, alphaColor)
        colorAnimation.duration = 500 // milliseconds
        colorAnimation.addUpdateListener { animator ->
            popupBackground.setBackgroundColor(animator.animatedValue as Int)
        }
        colorAnimation.start()

        // Fade animation for the Popup Window
        popupCardView.alpha = 0f
        popupCardView.animate().alpha(1f).setDuration(500).setInterpolator(
            DecelerateInterpolator()
        ).start()

        popupYesButton.setOnClickListener {
            setResult(Activity.RESULT_OK)
            onBackPressed()
        }

        // Close the Popup Window when you press the button
        popupNoButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            onBackPressed()
        }
    }

    private fun setWindowFlag(activity: Activity, on: Boolean) {
        val win = activity.window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        } else {
            winParams.flags = winParams.flags and WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS.inv()
        }
        win.attributes = winParams
    }

    override fun onBackPressed() {
        // Fade animation for the background of Popup Window when you press the back button
        val alpha = 100 // between 0-255
        val alphaColor = ColorUtils.setAlphaComponent(Color.parseColor("#000000"), alpha)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), alphaColor, Color.TRANSPARENT)
        colorAnimation.duration = 500 // milliseconds
        colorAnimation.addUpdateListener { animator ->
            popupBackground.setBackgroundColor(
                animator.animatedValue as Int
            )
        }

        // Fade animation for the Popup Window when you press the back button
        popupCardView.animate().alpha(0f).setDuration(500).setInterpolator(
            DecelerateInterpolator()
        ).start()

        // After animation finish, close the Activity
        colorAnimation.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                finish()
                overridePendingTransition(0, 0)
            }
        })
        colorAnimation.start()
    }
}
