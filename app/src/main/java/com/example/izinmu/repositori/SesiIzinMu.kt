package com.example.izinmu.repositori

import android.content.Context

class SesiIzinMu(context: Context) {
    private val prefs = context.getSharedPreferences("izinmu_session", Context.MODE_PRIVATE)

    @Synchronized
    fun accessToken(): String? = prefs.getString(KEY_ACCESS, null)

    @Synchronized
    fun refreshToken(): String? = prefs.getString(KEY_REFRESH, null)

    @Synchronized
    fun simpan(auth: AuthDto) {
        prefs.edit()
            .putString(KEY_ACCESS, auth.accessToken)
            .putString(KEY_REFRESH, auth.refreshToken)
            .apply()
    }

    @Synchronized
    fun perbaruiToken(accessToken: String, refreshToken: String?) {
        prefs.edit()
            .putString(KEY_ACCESS, accessToken)
            .apply {
                if (!refreshToken.isNullOrBlank()) putString(KEY_REFRESH, refreshToken)
            }
            .apply()
    }

    @Synchronized
    fun hapus() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_ACCESS = "access_token"
        const val KEY_REFRESH = "refresh_token"
    }
}
