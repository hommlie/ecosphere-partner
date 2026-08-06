package com.ecosphere.partner.core.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.ecosphere.partner.R
import com.ecosphere.partner.databinding.ViewDutyToggleBinding

class DutyToggleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = ViewDutyToggleBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    private var checked = false

    private var onText = "ON DUTY"
    private var offText = "OFF DUTY"

    private var interactionEnabled = true

    private var listener: ((Boolean) -> Unit)? = null

    // Drag values
    private var downX = 0f
    private var thumbStartX = 0f
    private var isDragging = false

    // Calculated positions
    private var minThumbX = 0f
    private var maxThumbX = 0f

    // spacing
    private val thumbMargin by lazy { dp(5) }
    private val labelGap by lazy { dp(10) }

    // animation
    private val springForce = SpringForce().apply {
        dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
        stiffness = SpringForce.STIFFNESS_LOW
    }

    private lateinit var thumbSpring: SpringAnimation

    init {

        attrs?.let {

            val ta = context.obtainStyledAttributes(
                it,
                R.styleable.DutyToggleView
            )

            checked = ta.getBoolean(
                R.styleable.DutyToggleView_checked,
                false
            )

            onText = ta.getString(
                R.styleable.DutyToggleView_onText
            ) ?: "ON DUTY"

            offText = ta.getString(
                R.styleable.DutyToggleView_offText
            ) ?: "OFF DUTY"

            ta.recycle()
        }

        initAnimation()

        doOnLayout {

            calculateBounds()

            moveImmediately(
                if (checked) maxThumbX else minThumbX
            )

            updateUi()

            setupTouch()
        }
    }

    // -----------------------------------------------------
    // Public API
    // -----------------------------------------------------

    fun isChecked() = checked

    fun setChecked(
        checked: Boolean,
        notify: Boolean = false,
        animate: Boolean = true
    ) {
        this.checked = checked

        if (!isLaidOut || maxThumbX == 0f) {

            doOnLayout {

                calculateBounds()

                if (animate) {
                    animateToState()
                } else {
                    moveImmediately(
                        if (checked) maxThumbX else minThumbX
                    )
                    updateUi()
                }
            }

        } else {

            if (animate) {
                animateToState()
            } else {
                moveImmediately(
                    if (checked) maxThumbX else minThumbX
                )
                updateUi()
            }
        }

        if (notify)
            listener?.invoke(checked)
    }

    fun setInteractionEnabled(enable: Boolean) {
        interactionEnabled = enable
    }

    fun setOnDutyChangedListener(
        listener: (Boolean) -> Unit
    ) {
        this.listener = listener
    }

    // -----------------------------------------------------
    // Initial calculations
    // -----------------------------------------------------

    private fun calculateBounds() {

        minThumbX = thumbMargin.toFloat()

        maxThumbX =
            (width - thumbMargin - binding.thumb.width).toFloat()
    }

    private fun initAnimation() {

        thumbSpring = SpringAnimation(
            binding.thumb,
            DynamicAnimation.X
        ).apply {

            spring = springForce

            addUpdateListener { _, value, _ ->
                updateLabelPosition(value)
            }
        }
    }

    // -----------------------------------------------------
    // UI
    // -----------------------------------------------------

    private fun updateUi() {

        binding.track.setBackgroundResource(
            if (checked)
                R.drawable.bg_toggle_on
            else
                R.drawable.bg_toggle_off
        )
        binding.thumb.background.setTint(
            ContextCompat.getColor(
                context,
                if (checked)
                    R.color.color_primary
                else
                    R.color.white
            )
        )

        binding.tvLabel.text =
            if (checked)
                onText
            else
                offText
    }

    private fun moveImmediately(x: Float) {

        binding.thumb.x = x

        updateLabelPosition(x)
    }

    private fun dp(value: Int): Int =
        (resources.displayMetrics.density * value).toInt()

    private fun setupTouch() {

        binding.thumb.setOnTouchListener { _, event ->

            if (!interactionEnabled)
                return@setOnTouchListener true

            when (event.actionMasked) {

                MotionEvent.ACTION_DOWN -> {

                    thumbSpring.cancel()

                    downX = event.rawX

                    thumbStartX = binding.thumb.x

                    isDragging = false

                    true
                }

                MotionEvent.ACTION_MOVE -> {

                    val dx = event.rawX - downX

                    if (kotlin.math.abs(dx) > 4f)
                        isDragging = true

                    val newX = (thumbStartX + dx)
                        .coerceIn(minThumbX, maxThumbX)

                    moveImmediately(newX)

                    true
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {

                    releaseThumb()

                    true
                }

                else -> false
            }
        }
    }

    private fun releaseThumb() {

        val center =
            binding.thumb.x + (binding.thumb.width / 2f)

        val checkedNow =
            center > width / 2f

        if (checked != checkedNow) {

            checked = checkedNow

            listener?.invoke(checked)
        }

        animateToState()
    }

    private fun updateLabelPosition(
        thumbX: Float
    ) {

        val progress =
            ((thumbX - minThumbX) /
                    (maxThumbX - minThumbX))
                .coerceIn(0f, 1f)

        val labelWidth =
            binding.tvLabel.width.toFloat()

        val thumbWidth =
            binding.thumb.width.toFloat()

        val startX =
            thumbWidth +
                    thumbMargin +
                    labelGap

        val endX =
            width -
                    thumbWidth -
                    labelWidth -
                    thumbMargin -
                    labelGap

        binding.tvLabel.x =
            startX +
                    ((endX - startX) * progress)
    }

    private fun animateToState() {

        updateUi()

        val target =
            if (checked)
                maxThumbX
            else
                minThumbX

        thumbSpring.animateToFinalPosition(target)

//        updateLabelPosition(target)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onDetachedFromWindow() {
        thumbSpring.cancel()
        super.onDetachedFromWindow()
    }

    fun setOnText(text: String) {

        onText = text

        if (checked)
            binding.tvLabel.text = text
    }

    fun setOffText(text: String) {

        offText = text

        if (!checked)
            binding.tvLabel.text = text
    }

    fun toggle() {
        setChecked(!checked, true)
    }
}

