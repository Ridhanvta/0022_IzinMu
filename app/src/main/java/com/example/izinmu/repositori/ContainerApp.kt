package com.example.izinmu.repositori

import android.content.Context

interface ContainerApp {
    val repositoriIzinMu: RepositoriIzinMu
}

class ContainerDataApp(private val context: Context) : ContainerApp {
    override val repositoriIzinMu: RepositoriIzinMu by lazy {
        RestRepositoriIzinMu(context.applicationContext)
    }
}
