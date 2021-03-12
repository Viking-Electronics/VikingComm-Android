package com.vikingelectronics.softphone.util

import android.content.Context
import android.media.AudioManager
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.accounts.add.AccountAddState
import com.vikingelectronics.softphone.devices.Device
import com.vikingelectronics.softphone.util.extensions.initIfNull
import com.vikingelectronics.softphone.util.extensions.invokeIfNotNull
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.linphone.LinphoneManager
import org.linphone.core.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LinphoneManager @Inject constructor(
    @ApplicationContext val context: Context,
    val factory: Factory,
    val core: Core
) {

    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun login(
        username: String,
        password: String,
        domain: String,
        transport: TransportType,
        userId: String = "",
        displayName: String = "",
    ) {

        Factory.instance().createAuthInfo(
            username, userId, password, null, null, domain
        ).apply {
            core.addAuthInfo(this)
        }

        val proxyConfig = core.createProxyConfig().apply {
            serverAddr = "<sip:$domain;transport=${transport.name.toLowerCase()}>"
        }

        val identityAddr = Factory.instance().createAddress(
            "sip:$username@$domain"
        )
        if (identityAddr != null) {
            identityAddr.displayName = displayName
            proxyConfig.identityAddress = identityAddr
        }

        proxyConfig.natPolicy.initIfNull {
            core.createNatPolicy()
        }.run {
            stunServer = context.getString(R.string.default_stun)
            enableStun(true)
            enableIce(true)
            core.natPolicy = this
        }

        core.addProxyConfig(proxyConfig)
        core.defaultProxyConfig = proxyConfig
    }

    fun callDevice(scope: CoroutineScope,
                   device: Device) = scope.launch(Default){
        val address = factory.createAddress(device.callAddress)
        val parameters = core.createCallParams(null)?.apply {
            enableVideo(true)
            videoDirection = MediaDirection.RecvOnly
            setAudioBandwidthLimit(0)
        }
        audioManager.isSpeakerphoneOn = true

        invokeIfNotNull(address, parameters) { addr, params ->
            core.inviteAddressWithParams(addr, params)
        }
    }
}