package com.linkersconsulting.appaint

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds

object AdManager {

    // Cambiar a false antes de publicar en producción
    private const val USE_TEST_ADS = true

    private const val BANNER_AD_UNIT_ID      = "ca-app-pub-9878919326245430/2591785843"
    private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    @Volatile private var isInitialized = false

    private fun getAdUnitId() =
        if (USE_TEST_ADS) TEST_BANNER_AD_UNIT_ID else BANNER_AD_UNIT_ID

    fun initialize(context: Context) {
        if (isInitialized) return
        synchronized(this) {
            if (isInitialized) return
            try {
                // Marcamos como inicializado DENTRO del callback para garantizar
                // que el SDK esté listo antes de cargar cualquier anuncio
                MobileAds.initialize(context.applicationContext) { status ->
                    isInitialized = true
                    android.util.Log.d("AdManager", "AdMob inicializado: $status")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Crea un AdView desde código y lo inyecta en el contenedor dado.
     * El contenedor queda oculto hasta que el anuncio cargue exitosamente.
     */
    fun injectBannerAd(container: FrameLayout): AdView {
        container.removeAllViews()

        val adView = AdView(container.context)
        adView.adUnitId = getAdUnitId()
        adView.setAdSize(AdSize.BANNER)
        adView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        adView.adListener = object : com.google.android.gms.ads.AdListener() {
            override fun onAdLoaded() {
                android.util.Log.d("AdManager", "✅ Anuncio cargado correctamente")
                adView.visibility = android.view.View.VISIBLE
                container.visibility = android.view.View.VISIBLE
            }
            override fun onAdFailedToLoad(error: LoadAdError) {
                android.util.Log.e("AdManager", "❌ Fallo al cargar anuncio - código: ${error.code}, mensaje: ${error.message}, dominio: ${error.domain}")
                adView.visibility = android.view.View.GONE
                container.visibility = android.view.View.GONE
            }
        }

        container.addView(adView)
        container.visibility = android.view.View.GONE

        android.util.Log.d("AdManager", "Solicitando anuncio con ID: ${getAdUnitId()} (test=$USE_TEST_ADS)")
        adView.loadAd(AdRequest.Builder().build())

        return adView
    }

    fun pauseBanner(adView: AdView?)   { adView?.pause() }
    fun resumeBanner(adView: AdView?)  { adView?.resume() }
    fun destroyBanner(adView: AdView?) { adView?.destroy() }
}
