package com.example.izinmu.ui.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.example.izinmu.R

/**
 * Token warna dibaca dari res/values/colors.xml saat aplikasi dibuat.
 * Tidak ada nilai warna yang ditulis langsung di kode Compose.
 */
private object Palet {
    var navy: Color = Color.Unspecified
    var navyDark: Color = Color.Unspecified
    var navySoft: Color = Color.Unspecified
    var blueContainer: Color = Color.Unspecified
    var blueContainerStrong: Color = Color.Unspecified
    var background: Color = Color.Unspecified
    var surface: Color = Color.Unspecified
    var text: Color = Color.Unspecified
    var muted: Color = Color.Unspecified
    var outline: Color = Color.Unspecified
    var success: Color = Color.Unspecified
    var successContainer: Color = Color.Unspecified
    var warning: Color = Color.Unspecified
    var warningContainer: Color = Color.Unspecified
    var danger: Color = Color.Unspecified
    var dangerContainer: Color = Color.Unspecified
    var instructor: Color = Color.Unspecified
    var instructorContainer: Color = Color.Unspecified
    var onDark: Color = Color.Unspecified
    var transparent: Color = Color.Unspecified
}

object Warna {
    fun muat(context: Context) {
        Palet.navy = ContextCompat.getColor(context, R.color.izin_navy).let(::Color)
        Palet.navyDark = ContextCompat.getColor(context, R.color.izin_navy_dark).let(::Color)
        Palet.navySoft = ContextCompat.getColor(context, R.color.izin_navy_soft).let(::Color)
        Palet.blueContainer = ContextCompat.getColor(context, R.color.izin_blue_container).let(::Color)
        Palet.blueContainerStrong = ContextCompat.getColor(context, R.color.izin_blue_container_strong).let(::Color)
        Palet.background = ContextCompat.getColor(context, R.color.izin_background).let(::Color)
        Palet.surface = ContextCompat.getColor(context, R.color.izin_surface).let(::Color)
        Palet.text = ContextCompat.getColor(context, R.color.izin_text).let(::Color)
        Palet.muted = ContextCompat.getColor(context, R.color.izin_muted).let(::Color)
        Palet.outline = ContextCompat.getColor(context, R.color.izin_outline).let(::Color)
        Palet.success = ContextCompat.getColor(context, R.color.izin_success).let(::Color)
        Palet.successContainer = ContextCompat.getColor(context, R.color.izin_success_container).let(::Color)
        Palet.warning = ContextCompat.getColor(context, R.color.izin_warning).let(::Color)
        Palet.warningContainer = ContextCompat.getColor(context, R.color.izin_warning_container).let(::Color)
        Palet.danger = ContextCompat.getColor(context, R.color.izin_danger).let(::Color)
        Palet.dangerContainer = ContextCompat.getColor(context, R.color.izin_danger_container).let(::Color)
        Palet.instructor = ContextCompat.getColor(context, R.color.izin_instructor).let(::Color)
        Palet.instructorContainer = ContextCompat.getColor(context, R.color.izin_instructor_container).let(::Color)
        Palet.onDark = ContextCompat.getColor(context, R.color.izin_on_dark).let(::Color)
        Palet.transparent = ContextCompat.getColor(context, R.color.izin_transparent).let(::Color)
    }
}

val IzinNavy: Color get() = Palet.navy
val IzinNavyDark: Color get() = Palet.navyDark
val IzinNavySoft: Color get() = Palet.navySoft
val IzinBlueContainer: Color get() = Palet.blueContainer
val IzinBlueContainerStrong: Color get() = Palet.blueContainerStrong
val IzinBackground: Color get() = Palet.background
val IzinSurface: Color get() = Palet.surface
val IzinText: Color get() = Palet.text
val IzinMuted: Color get() = Palet.muted
val IzinOutline: Color get() = Palet.outline
val IzinSuccess: Color get() = Palet.success
val IzinSuccessContainer: Color get() = Palet.successContainer
val IzinWarning: Color get() = Palet.warning
val IzinWarningContainer: Color get() = Palet.warningContainer
val IzinDanger: Color get() = Palet.danger
val IzinDangerContainer: Color get() = Palet.dangerContainer
val IzinInstructor: Color get() = Palet.instructor
val IzinInstructorContainer: Color get() = Palet.instructorContainer
val IzinOnDark: Color get() = Palet.onDark
val IzinTransparent: Color get() = Palet.transparent
