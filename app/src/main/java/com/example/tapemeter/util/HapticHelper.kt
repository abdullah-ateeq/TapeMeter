package com.example.tapemeter.util

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

object HapticHelper {
    fun performLightTick(haptic: HapticFeedback?, isEnabled: Boolean) {
        if (!isEnabled || haptic == null) return
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    fun performViewHaptic(view: View?, isEnabled: Boolean) {
        if (!isEnabled || view == null) return
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }
}
