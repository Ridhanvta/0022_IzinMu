package com.example.izinmu.repositori

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private val JSON = "application/json; charset=utf-8".toMediaType()

class PenyegarToken(
    private val baseUrl: String,
    private val sesi: SesiIzinMu,
) : Authenticator {
    private val gson = Gson()
    private val klien = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath
        if (path.endsWith("/auth/login") || path.endsWith("/auth/refresh")) return null
        if (jumlahPercobaan(response) >= 2) return null

        val refresh = sesi.refreshToken() ?: return null
        return synchronized(this) {
            val terbaru = sesi.accessToken()
            val header = response.request.header("Authorization")
            if (!terbaru.isNullOrBlank() && header != "Bearer $terbaru") {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $terbaru")
                    .build()
            }

            val body = gson.toJson(RefreshBody(refresh)).toRequestBody(JSON)
            val request = Request.Builder()
                .url(baseUrl + "auth/refresh")
                .post(body)
                .build()

            val hasil = runCatching {
                klien.newCall(request).execute().use { jawaban ->
                    if (!jawaban.isSuccessful) return@synchronized null
                    val tipe = object : TypeToken<ApiEnvelope<AuthDto>>() {}.type
                    val envelope: ApiEnvelope<AuthDto> =
                        gson.fromJson(jawaban.body?.charStream(), tipe) ?: return@synchronized null
                    envelope.data
                }
            }.getOrNull()

            if (hasil == null) {
                sesi.hapus()
                return null
            }

            sesi.perbaruiToken(hasil.accessToken, hasil.refreshToken)
            response.request.newBuilder()
                .header("Authorization", "Bearer ${hasil.accessToken}")
                .build()
        }
    }

    private fun jumlahPercobaan(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}

class InterceptorSesi(private val sesi: SesiIzinMu) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        if (path.endsWith("/auth/login") || path.endsWith("/auth/refresh")) {
            return chain.proceed(request)
        }
        val token = sesi.accessToken()
        val denganToken = if (token.isNullOrBlank()) {
            request
        } else {
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(denganToken)
    }
}

object PembuatApi {
    fun buat(baseUrl: String, sesi: SesiIzinMu): ApiIzinMu {
        val dasar = if (baseUrl.endsWith('/')) baseUrl else "$baseUrl/"
        val klien = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(InterceptorSesi(sesi))
            .authenticator(PenyegarToken(dasar, sesi))
            .build()

        return Retrofit.Builder()
            .baseUrl(dasar)
            .client(klien)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiIzinMu::class.java)
    }
}
