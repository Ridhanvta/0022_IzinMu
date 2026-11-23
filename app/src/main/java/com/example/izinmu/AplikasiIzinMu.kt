package com.example.izinmu

import android.app.Application
import com.example.izinmu.repositori.ContainerApp
import com.example.izinmu.repositori.ContainerDataApp
import com.example.izinmu.ui.theme.Dimens
import com.example.izinmu.ui.theme.Warna

class AplikasiIzinMu : Application() {
    lateinit var container: ContainerApp

    override fun onCreate() {
        super.onCreate()
        Warna.muat(this)
        Dimens.muat(this)
        container = ContainerDataApp(this)
    }
}
