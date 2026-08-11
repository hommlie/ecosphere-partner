package com.ecosphere.partner.feature.qrscan.util

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import com.ecosphere.partner.R
import android.animation.ValueAnimator

class ScannerOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val frameSize = dp(280f)

    private val frameRadius = dp(18f)

    private val cornerRadius = dp(18f)

    private val cornerLength = dp(42f)

    private val scanHeight = dp(4f)

    private val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B3000000")
    }

    private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val cornerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {

        style = Paint.Style.STROKE
        strokeWidth = dp(5f)
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = ContextCompat.getColor(
            context,
            R.color.green
        )
        setShadowLayer(
            dp(10f),
            0f,
            0f,
            Color.parseColor("#6600E676")
        )
    }

    private val scanPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val scanRect = RectF()

    fun getScannerRect(): RectF = RectF(scanRect)

    private var scanY = 0f

    private var glowFraction = 0f

    private var scanAnimator: ValueAnimator? = null

    private var glowAnimator: ValueAnimator? = null

    init {

        setLayerType(
            LAYER_TYPE_SOFTWARE,
            null
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val layer = canvas.saveLayer(null, null)

        // Dark overlay
        canvas.drawRect(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            overlayPaint
        )

        val left = (width - frameSize) / 2f
        val top = (height - frameSize) / 2f
        val right = left + frameSize
        val bottom = top + frameSize

        scanRect.set(
            left,
            top,
            right,
            bottom
        )

        // Transparent scanner area
        canvas.drawRoundRect(
            scanRect,
            frameRadius,
            frameRadius,
            clearPaint
        )

        drawScanLine(canvas)

        drawCorners(canvas)

        canvas.restoreToCount(layer)
    }

    private fun drawScanLine(
        canvas: Canvas
    ) {

        canvas.drawRoundRect(
            scanRect.left + dp(8f),
            scanY,
            scanRect.right - dp(8f),
            scanY + scanHeight,
            scanHeight,
            scanHeight,
            scanPaint
        )
    }

    private fun drawCorners(
        canvas: Canvas
    ) {

        val l = scanRect.left
        val t = scanRect.top
        val r = scanRect.right
        val b = scanRect.bottom

        val cr = cornerRadius
        val cl = cornerLength

        // ---------- TOP LEFT ----------

        val tl = Path()

        tl.moveTo(l + cl, t)

        tl.lineTo(l + cr, t)

        tl.quadTo(
            l,
            t,
            l,
            t + cr
        )

        tl.lineTo(
            l,
            t + cl
        )

        canvas.drawPath(
            tl,
            cornerPaint
        )

        // ---------- TOP RIGHT ----------

        val tr = Path()

        tr.moveTo(r - cl, t)

        tr.lineTo(r - cr, t)

        tr.quadTo(
            r,
            t,
            r,
            t + cr
        )

        tr.lineTo(
            r,
            t + cl
        )

        canvas.drawPath(
            tr,
            cornerPaint
        )

        // ---------- BOTTOM LEFT ----------

        val bl = Path()

        bl.moveTo(l + cl, b)

        bl.lineTo(l + cr, b)

        bl.quadTo(
            l,
            b,
            l,
            b - cr
        )

        bl.lineTo(
            l,
            b - cl
        )

        canvas.drawPath(
            bl,
            cornerPaint
        )

        // ---------- BOTTOM RIGHT ----------

        val br = Path()

        br.moveTo(r - cl, b)

        br.lineTo(r - cr, b)

        br.quadTo(
            r,
            b,
            r,
            b - cr
        )

        br.lineTo(
            r,
            b - cl
        )

        canvas.drawPath(
            br,
            cornerPaint
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

//        startAnimations()
    }

    override fun onDetachedFromWindow() {

        scanAnimator?.cancel()
        glowAnimator?.cancel()

        super.onDetachedFromWindow()
    }

    private fun startAnimations() {

        scanAnimator?.cancel()

        scanAnimator = ValueAnimator.ofFloat(
            0f,
            1f
        ).apply {

            duration = 1800
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = android.view.animation.LinearInterpolator()

            addUpdateListener {

                if (scanRect.isEmpty) return@addUpdateListener
                val fraction = it.animatedFraction
                scanY = lerp(
                    scanRect.top + dp(8f),
                    scanRect.bottom - scanHeight - dp(8f),
                    fraction
                )

                invalidate()
            }

            start()
        }

        glowAnimator?.cancel()

        glowAnimator = ValueAnimator.ofFloat(
            0.65f,
            1f
        ).apply {

            duration = 900

            repeatMode = ValueAnimator.REVERSE

            repeatCount = ValueAnimator.INFINITE

            addUpdateListener {

                glowFraction = it.animatedValue as Float

                cornerPaint.alpha = (255 * glowFraction).toInt()

                invalidate()
            }

            start()
        }
    }
    private fun lerp(
        start: Float,
        end: Float,
        fraction: Float
    ): Float {

        return start + (end - start) * fraction
    }

    private fun dp(
        value: Float
    ): Float {

        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value,
            resources.displayMetrics
        )
    }
}