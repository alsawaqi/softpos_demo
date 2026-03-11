package com.example.softpos_demo

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.cardtek.softpos.constants.TransactionType
import com.cardtek.softpos.interfaces.CheckPOSServiceListener
import com.cardtek.softpos.interfaces.InitializeListener
import com.cardtek.softpos.interfaces.RegisterListener
import com.cardtek.softpos.interfaces.TransactionListener
import com.cardtek.softpos.kernel.BeepType
import com.cardtek.softpos.results.SoftPosError
import com.cardtek.softpos.results.TransactionResult
import com.example.softpos_demo.softpos.SoftPosConfig
import com.example.softpos_demo.softpos.SoftPosManager
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    private val methodChannelName = "softpos/methods"
    private val eventChannelName = "softpos/events"

    private var eventSink: EventChannel.EventSink? = null
    private var pendingInitializeAfterPermission = false

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        EventChannel(flutterEngine.dartExecutor.binaryMessenger, eventChannelName)
            .setStreamHandler(object : EventChannel.StreamHandler {
                override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                    eventSink = events
                }

                override fun onCancel(arguments: Any?) {
                    eventSink = null
                }
            })

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, methodChannelName)
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "setupSdk" -> {
                        try {
                            SoftPosManager.setup(applicationContext)
                            result.success(true)
                        } catch (e: Exception) {
                            result.error("SETUP_ERROR", e.message, null)
                        }
                    }

                    "initializeSdk" -> {
                        initializeSdk(result)
                    }

                    "registerSdk" -> {
                        registerSdk(result)
                    }

                    "checkPosService" -> {
                        checkPosService(result)
                    }

                    "startSale" -> {
                        val amount = call.argument<Int>("amount")?.toLong() ?: 0L
                        val timeoutMs = call.argument<Int>("timeoutMs") ?: SoftPosConfig.SALE_TIMEOUT_MS
                        startSale(amount, timeoutMs, result)
                    }

                    "cancelTransaction" -> {
                        val ok = runCatching {
                            SoftPosManager.getService().cancelTransaction()
                        }.getOrDefault(false)
                        result.success(ok)
                    }

                    else -> result.notImplemented()
                }
            }
    }

    private fun initializeSdk(result: MethodChannel.Result) {
        SoftPosManager.getService().initialize(object : InitializeListener {
            override fun onPOSReady() {
                emit("onPOSReady", null)
                result.success("POS_READY")
            }

            override fun onRegisterNeed() {
                emit("onRegisterNeed", null)
                result.success("REGISTER_NEEDED")
            }

            override fun onPermissionNeed(missingPermission: ArrayList<String>) {
                emit("onPermissionNeed", mapOf("permissions" to missingPermission))
                requestPermissionsIfNeeded(missingPermission)
                pendingInitializeAfterPermission = true
                result.success("PERMISSION_NEEDED")
            }

            override fun onInitializeError(error: SoftPosError) {
                emit("onInitializeError", errorMap(error))
                result.error("INIT_ERROR", error.getErrorMessage(), errorMap(error))
            }
        })
    }

    private fun registerSdk(result: MethodChannel.Result) {
        val merchantId = SoftPosConfig.MERCHANT_ID
        val terminalId = SoftPosConfig.TERMINAL_ID
        val activationCode = SoftPosConfig.ACTIVATION_CODE

        if (merchantId.isBlank() || terminalId.isBlank() || activationCode.isBlank()) {
            result.error("REGISTER_CONFIG_MISSING", "Merchant/Terminal/Activation values are missing", null)
            return
        }

        SoftPosManager.getService().register(
            merchantId,
            terminalId,
            activationCode,
            object : RegisterListener {
                override fun onRegisterSuccess() {
                    emit("onRegisterSuccess", null)
                    result.success(true)
                }

                override fun onRegisterError(error: SoftPosError) {
                    emit("onRegisterError", errorMap(error))
                    result.error("REGISTER_ERROR", error.getErrorMessage(), errorMap(error))
                }
            }
        )
    }

    private fun checkPosService(result: MethodChannel.Result) {
        SoftPosManager.getService().checkPOSService(object : CheckPOSServiceListener {
            override fun onCheckPOSSuccess() {
                emit("onCheckPOSSuccess", null)
                result.success(true)
            }

            override fun onCheckPOSError(error: SoftPosError) {
                emit("onCheckPOSError", errorMap(error))
                result.error("CHECK_POS_ERROR", error.getErrorMessage(), errorMap(error))
            }
        })
    }

    private fun startSale(amount: Long, timeoutMs: Int, result: MethodChannel.Result) {
        SoftPosManager.getService().startTransaction(
            amount,
            TransactionType.SALE,
            timeoutMs,
            "0100",
            this,
            object : TransactionListener {
    override fun onCardDetected() {
        emit("onCardDetected", null)
    }

    override fun onCardReadSuccess() {
        emit("onCardReadSuccess", null)
    }

    override fun onCardReadFail() {
        emit("onCardReadFail", null)
    }

    override fun onGoOnline(cardType: com.cardtek.softpos.constants.CardType?) {
        emit("onGoOnline", mapOf("cardType" to cardType?.name))
    }

    override fun onPlaySound(type: BeepType?) {
        emit("onPlaySound", mapOf("type" to type?.name))
    }

    override fun onCompleted(resultData: TransactionResult) {
        val payload = mapOf(
            "isEMVAccepted" to resultData.isEMVAccepted(),
            "refusalCode" to resultData.getRefusalCode(),
            "maskedPan" to resultData.getMaskedPan(),
            "transactionId" to resultData.getTransactionId(),
            "applicationLabel" to resultData.getApplicationLabel(),
            "applicationPreferredName" to resultData.getApplicationPreferredName(),
            "scaNeeded" to resultData.isSCANeeded()
        )
        emit("onCompleted", payload)
    }

    override fun onTimeout() {
        emit("onTimeout", null)
    }

    override fun onStartTransactionError(error: SoftPosError) {
        emit("onStartTransactionError", errorMap(error))
    }
}
        )

        result.success(true)
    }

    private fun requestPermissionsIfNeeded(permissions: List<String>) {
        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing, 2001)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 2001 && pendingInitializeAfterPermission) {
            pendingInitializeAfterPermission = false
            SoftPosManager.getService().initialize(object : InitializeListener {
                override fun onPOSReady() {
                    emit("onPOSReady", null)
                }

                override fun onRegisterNeed() {
                    emit("onRegisterNeed", null)
                }

                override fun onPermissionNeed(missingPermission: ArrayList<String>) {
                    emit("onPermissionNeed", mapOf("permissions" to missingPermission))
                }

                override fun onInitializeError(error: SoftPosError) {
                    emit("onInitializeError", errorMap(error))
                }
            })
        }
    }

    override fun onStop() {
        super.onStop()
        runCatching { SoftPosManager.getService().cancelTransaction() }
    }

    private fun emit(event: String, data: Any?) {
        eventSink?.success(mapOf("event" to event, "data" to data))
    }

    private fun errorMap(error: SoftPosError): Map<String, Any?> {
        return mapOf(
            "errorCode" to error.getErrorCode(),
            "errorMessage" to error.getErrorMessage(),
            "internalErrorCode" to error.getInternalErrorCode(),
            "internalErrorSubCode" to error.getInternalErrorSubCode(),
            "internalErrorMessage" to error.getInternalErrorMessage()
        )
    }
}