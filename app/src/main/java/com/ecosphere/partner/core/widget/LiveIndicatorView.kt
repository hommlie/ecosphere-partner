package com.ecosphere.partner.core.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import androidx.core.content.withStyledAttributes
import com.ecosphere.partner.R

class LiveIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(
    context,
    attrs,
    defStyleAttr
) {

    private var dotRadius = dp(3.5f)

    private var duration = 1200L

    private var rippleRadius = 0f

    private var rippleAlpha = 255

    private val maxRippleRadius
        get() = width / 2f

    private val dotPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {

            style = Paint.Style.FILL

            color = Color.WHITE
        }

    private val ripplePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {

            style = Paint.Style.FILL

            color = Color.WHITE
        }

    private var animator: ValueAnimator? = null

    init {

        context.withStyledAttributes(
            attrs,
            R.styleable.LiveIndicatorView
        ) {

            dotPaint.color =
                getColor(
                    R.styleable.LiveIndicatorView_dotColor,
                    Color.WHITE
                )

            ripplePaint.color =
                getColor(
                    R.styleable.LiveIndicatorView_rippleColor,
                    Color.WHITE
                )

            dotRadius =
                getDimension(
                    R.styleable.LiveIndicatorView_dotRadius,
                    dp(3.5f)
                )

            duration =
                getInt(
                    R.styleable.LiveIndicatorView_duration,
                    1200
                ).toLong()
        }

//        start()
    }

    fun start() {

        if (width == 0) {
            post {
                start()
            }
            return
        }

        if (animator?.isRunning == true)
            return

        animator =
            ValueAnimator.ofFloat(
                0f,
                1f
            ).apply {

                duration =
                    this@LiveIndicatorView.duration

                repeatCount =
                    ValueAnimator.INFINITE

                interpolator =
                    LinearInterpolator()

                addUpdateListener {

                    val progress =
                        animatedValue as Float

                    rippleRadius =
                        progress * maxRippleRadius

                    rippleAlpha =
                        ((1f - progress) * 255).toInt()

                    invalidate()
                }

                start()
            }
    }
//    override fun onVisibilityChanged(
//        changedView: View,
//        visibility: Int
//    ) {
//        super.onVisibilityChanged(changedView, visibility)
//
//        if (visibility == VISIBLE) {
//            start()
//        } else {
//            stop()
//        }
//    }

    fun stop() {

        animator?.removeAllUpdateListeners()
        animator?.cancel()
        animator = null
        rippleRadius = 0f
        rippleAlpha = 255
        invalidate()
    }

    fun setDotColor(
        @ColorInt color: Int
    ) {

        dotPaint.color = color

        invalidate()
    }

    fun setRippleColor(
        @ColorInt color: Int
    ) {

        ripplePaint.color = color

        invalidate()
    }

    fun setDotRadius(
        radiusDp: Float
    ) {

        dotRadius = dp(radiusDp)

        invalidate()
    }

    fun setDuration(
        duration: Long
    ) {

        this.duration = duration

        if (animator != null) {

            stop()

            start()
        }
    }

    override fun onDraw(
        canvas: Canvas
    ) {

        super.onDraw(canvas)

        val cx = width / 2f

        val cy = height / 2f

        ripplePaint.alpha =
            rippleAlpha

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

    private fun dp(
        value: Float
    ): Float {

        return value *
                resources.displayMetrics.density
    }
}