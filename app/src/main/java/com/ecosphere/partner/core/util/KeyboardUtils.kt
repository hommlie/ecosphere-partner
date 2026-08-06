package com.ecosphere.partner.core.util

import android.app.Activity
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment

object KeyboardUtils {

    fun hideKeyboard(activity: Activity) {
        val view = activity.currentFocus ?: View(activity)
        val imm = activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun Fragment.hideKeyboard() {
        view?.let { activity?.let { act -> hideKeyboard(act) } }
    }

    fun closeKeyboardTouchedOutside(context: Activity,event: MotionEvent){
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = context.currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    hideKeyboard(v)
                }
            }
        }
    }
    fun setupHideKeyboardOnTouch(activity: Activity, root: View,showCursor : Boolean?=false,editText: EditText?=null) {

        if (root !is EditText) {

            root.setOnTouchListener(object : View.OnTouchListener {

                private var downX = 0f
                private var downY = 0f

                override fun onTouch(v: View?, event: MotionEvent): Boolean {

                    when (event.action) {

                        MotionEvent.ACTION_DOWN -> {
                            downX = event.rawX
                            downY = event.rawY
                        }

                        MotionEvent.ACTION_UP -> {

                            val deltaX = Math.abs(event.rawX - downX)
                            val deltaY = Math.abs(event.rawY - downY)

                            // Agar movement 20px se kam hai = real click
                            if (deltaX < 20 && deltaY < 20) {

                                activity.currentFocus?.let { view ->

                                    if (view is EditText) {

                                        val outRect = Rect()
                                        view.getGlobalVisibleRect(outRect)

                                        if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                                            view.clearFocus()
                                            hideKeyboard(activity, view)
                                            editText?.apply {
                                                isCursorVisible = true
                                                requestFocus()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    return false
                }
            })
        }

        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                setupHideKeyboardOnTouch(activity, root.getChildAt(i), editText = editText)
            }
        }
    }

    fun hideKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // usage
    // from activity   KeyboardUtils.hideKeyboard(this)
    // from fragment   KeyboardUtils.hideKeyboard()
    // from view        KeyboardUtils.hideKeyboard(yourview)  e.g.  KeyboardUtils.hideKeyboard(binding.edtMobile)

}
