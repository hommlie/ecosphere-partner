package com.ecosphere.partner.core.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

class LiveRingIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val dotRadius = dp(5.5f)

    private var rippleRadius = 0f

    private var rippleAlpha = 255

    private val maxRippleRadius
        get() = width / 2f

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val ripplePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = dp(1.5f)
    }

    private var animator: ValueAnimator? = null

    init {
        start()
    }

    fun start() {

        stop()

        animator = ValueAnimator.ofFloat(0f, 1f).apply {

            duration = 1200

            repeatCount = ValueAnimator.INFINITE

            interpolator = LinearInterpolator()

            addUpdateListener {

                val progress = animatedValue as Float

                rippleRadius = progress * maxRippleRadius

                rippleAlpha = ((1f - progress) * 255).toInt()

                invalidate()
            }

            start()
        }
    }

    fun stop() {

        animator?.cancel()

        animator = null

        rippleRadius = 0f

        rippleAlpha = 255

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {

        super.onDraw(canvas)

        val cx = width / 2f

        val cy = height / 2f

        ripplePaint.alpha = rippleAlpha

        if (rippleRadius > 0f) {

            canvas.drawCircle(
                cx,
                cy,
                rippleRadius,
                ripplePaint
            )
        }

        canvas.drawCircle(
            cx,
            cy,
            dotRadius,
            dotPaint
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        start()
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }

    private fun dp(value: Float): Float {
        return value * resources.displayMetrics.density
    }
}