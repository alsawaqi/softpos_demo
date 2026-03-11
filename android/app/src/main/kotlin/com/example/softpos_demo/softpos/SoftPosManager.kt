package com.example.softpos_demo.softpos

import android.content.Context
import com.cardtek.softpos.SoftPosService
import com.cardtek.softpos.utils.SoftPosInfo

object SoftPosManager {
    private var service: SoftPosService? = null

    fun setup(context: Context) {
        if (service != null) return

        require(SoftPosConfig.BASE_URL.isNotBlank()) { "BASE_URL is missing" }
        require(SoftPosConfig.SAFETY_NET_API_KEY.isNotBlank()) { "SAFETY_NET_API_KEY is missing" }
        require(SoftPosConfig.ACQUIRER_ID != 0L) { "ACQUIRER_ID is missing" }
        require(SoftPosConfig.HOST_NAME.isNotBlank()) { "HOST_NAME is missing" }

        val certInput = context.assets.open(SoftPosConfig.HOST_CERT_ASSET)

        SoftPosInfo.setUrl(SoftPosConfig.BASE_URL)
        SoftPosInfo.setSafetyNetApiKey(SoftPosConfig.SAFETY_NET_API_KEY)
        SoftPosInfo.setAcquirerId(SoftPosConfig.ACQUIRER_ID)
        SoftPosInfo.setConnectionTimeoutSec(60)
        SoftPosInfo.setHostCertificate(certInput)
        SoftPosInfo.setHostName(SoftPosConfig.HOST_NAME)
        SoftPosInfo.setL2HostResponseTimeoutMs(180000)
        SoftPosInfo.setIsoDepTimeoutMs(15000)
        SoftPosInfo.enableGeoCoordinates()
        SoftPosInfo.enableForegroundDispatch()

        service = SoftPosService(context.applicationContext)
    }

    fun getService(): SoftPosService {
        return requireNotNull(service) { "SoftPosService not initialized. Call setup first." }
    }
}