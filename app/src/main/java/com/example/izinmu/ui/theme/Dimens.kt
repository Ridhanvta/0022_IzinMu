package com.example.izinmu.ui.theme

import android.content.Context
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.izinmu.R

/** Token ukuran dibaca dari res/values/dimens.xml. */
object Dimens {
    var s0: Dp = 0.dp
    var s1: Dp = 0.dp
    var s2: Dp = 0.dp
    var s3: Dp = 0.dp
    var s4: Dp = 0.dp
    var s5: Dp = 0.dp
    var s6: Dp = 0.dp
    var s7: Dp = 0.dp
    var s8: Dp = 0.dp
    var s9: Dp = 0.dp
    var s10: Dp = 0.dp
    var s12: Dp = 0.dp
    var s13: Dp = 0.dp
    var s14: Dp = 0.dp
    var s16: Dp = 0.dp
    var s17: Dp = 0.dp
    var s18: Dp = 0.dp
    var s19: Dp = 0.dp
    var s20: Dp = 0.dp
    var s22: Dp = 0.dp
    var s24: Dp = 0.dp
    var s26: Dp = 0.dp
    var s28: Dp = 0.dp
    var s30: Dp = 0.dp
    var s34: Dp = 0.dp
    var s36: Dp = 0.dp
    var s38: Dp = 0.dp
    var s40: Dp = 0.dp
    var s42: Dp = 0.dp
    var s44: Dp = 0.dp
    var s46: Dp = 0.dp
    var s48: Dp = 0.dp
    var s50: Dp = 0.dp
    var s52: Dp = 0.dp
    var s54: Dp = 0.dp
    var s64: Dp = 0.dp
    var s76: Dp = 0.dp
    var s90: Dp = 0.dp
    var s96: Dp = 0.dp
    var s100: Dp = 0.dp
    var s150: Dp = 0.dp
    var s210: Dp = 0.dp
    var s240: Dp = 0.dp
    var s600: Dp = 0.dp
    var n60: Dp = 0.dp
    var n70: Dp = 0.dp
    var t4: TextUnit = 0.sp
    var tNeg04: TextUnit = 0.sp
    var tNeg03: TextUnit = 0.sp
    var t04: TextUnit = 0.sp
    var t11: TextUnit = 0.sp
    var t12: TextUnit = 0.sp
    var t14: TextUnit = 0.sp
    var t16: TextUnit = 0.sp
    var t18: TextUnit = 0.sp
    var t20: TextUnit = 0.sp
    var t21: TextUnit = 0.sp
    var t22: TextUnit = 0.sp
    var t24: TextUnit = 0.sp
    var t26: TextUnit = 0.sp
    var t28: TextUnit = 0.sp
    var t32: TextUnit = 0.sp
    var t38: TextUnit = 0.sp

    fun muat(context: Context) {
        val density = context.resources.displayMetrics.density
        val scaled = context.resources.displayMetrics.scaledDensity
        fun dp(id: Int): Dp = (context.resources.getDimension(id) / density).dp
        fun sp(id: Int): TextUnit = (context.resources.getDimension(id) / scaled).sp
        s0 = dp(R.dimen.dp_0)
        s1 = dp(R.dimen.dp_1)
        s2 = dp(R.dimen.dp_2)
        s3 = dp(R.dimen.dp_3)
        s4 = dp(R.dimen.dp_4)
        s5 = dp(R.dimen.dp_5)
        s6 = dp(R.dimen.dp_6)
        s7 = dp(R.dimen.dp_7)
        s8 = dp(R.dimen.dp_8)
        s9 = dp(R.dimen.dp_9)
        s10 = dp(R.dimen.dp_10)
        s12 = dp(R.dimen.dp_12)
        s13 = dp(R.dimen.dp_13)
        s14 = dp(R.dimen.dp_14)
        s16 = dp(R.dimen.dp_16)
        s17 = dp(R.dimen.dp_17)
        s18 = dp(R.dimen.dp_18)
        s19 = dp(R.dimen.dp_19)
        s20 = dp(R.dimen.dp_20)
        s22 = dp(R.dimen.dp_22)
        s24 = dp(R.dimen.dp_24)
        s26 = dp(R.dimen.dp_26)
        s28 = dp(R.dimen.dp_28)
        s30 = dp(R.dimen.dp_30)
        s34 = dp(R.dimen.dp_34)
        s36 = dp(R.dimen.dp_36)
        s38 = dp(R.dimen.dp_38)
        s40 = dp(R.dimen.dp_40)
        s42 = dp(R.dimen.dp_42)
        s44 = dp(R.dimen.dp_44)
        s46 = dp(R.dimen.dp_46)
        s48 = dp(R.dimen.dp_48)
        s50 = dp(R.dimen.dp_50)
        s52 = dp(R.dimen.dp_52)
        s54 = dp(R.dimen.dp_54)
        s64 = dp(R.dimen.dp_64)
        s76 = dp(R.dimen.dp_76)
        s90 = dp(R.dimen.dp_90)
        s96 = dp(R.dimen.dp_96)
        s100 = dp(R.dimen.dp_100)
        s150 = dp(R.dimen.dp_150)
        s210 = dp(R.dimen.dp_210)
        s240 = dp(R.dimen.dp_240)
        s600 = dp(R.dimen.dp_600)
        n60 = dp(R.dimen.dp_neg_60)
        n70 = dp(R.dimen.dp_neg_70)
        t4 = sp(R.dimen.sp_4)
        tNeg04 = sp(R.dimen.sp_neg_0_4)
        tNeg03 = sp(R.dimen.sp_neg_0_3)
        t04 = sp(R.dimen.sp_0_4)
        t11 = sp(R.dimen.sp_11)
        t12 = sp(R.dimen.sp_12)
        t14 = sp(R.dimen.sp_14)
        t16 = sp(R.dimen.sp_16)
        t18 = sp(R.dimen.sp_18)
        t20 = sp(R.dimen.sp_20)
        t21 = sp(R.dimen.sp_21)
        t22 = sp(R.dimen.sp_22)
        t24 = sp(R.dimen.sp_24)
        t26 = sp(R.dimen.sp_26)
        t28 = sp(R.dimen.sp_28)
        t32 = sp(R.dimen.sp_32)
        t38 = sp(R.dimen.sp_38)
    }
}
