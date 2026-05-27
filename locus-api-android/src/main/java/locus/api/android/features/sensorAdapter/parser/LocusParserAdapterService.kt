/*
 * Created by menion on 25.05.2026.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
package locus.api.android.features.sensorAdapter.parser

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.RemoteException
import locus.api.android.features.sensorAdapter.AdapterApi
import locus.api.android.features.sensorAdapter.LocusBindContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Base class for **parser-style** adapter apps — the simpler of the two adapter
 * models. Locus owns the BT3 / BT4 / USB transport and lifecycle; your adapter
 * declares its device types in `res/xml/locus_adapter.xml` and implements byte-frame
 * parsing here. Locus drives the wire and hands you data via [parseData]; you turn it into
 * typed values via [SensorValueBatchBuilder].
 *
 * Wires the [ILocusSensorAdapterParser] AIDL stub to abstract Kotlin methods so
 * adapter authors don't write any binder bookkeeping. Static metadata
 * (apiVersion, id, displayName, icon, device-type catalogue) is read from the
 * manifest XML + `<service>` attributes — Locus never binds the service to build
 * its picker.
 *
 * Adapter author manifest declaration (typical):
 *
 * ```xml
 * <service
 *     android:name=".MyAdapterService"
 *     android:exported="true"
 *     android:icon="@drawable/ic_my_adapter"
 *     android:permission="com.asamm.locus.permission.SENSOR_ADAPTER">
 *     <intent-filter>
 *         <action android:name="locus.api.android.ACTION_SENSOR_ADAPTER_PARSER" />
 *     </intent-filter>
 *     <meta-data
 *         android:name="locus.api.android.SENSOR_ADAPTER"
 *         android:resource="@xml/locus_adapter" />
 * </service>
 * ```
 *
 * Adapter author class:
 *
 * ```kotlin
 * class MyAdapterService : LocusParserAdapterService() {
 *     override fun init(deviceId: String, deviceTypeId: String, bindContext: LocusBindContext): Int =
 *         AdapterApi.INIT_OK
 *     override fun parseData(deviceId: String, source: String, bytes: ByteArray): SensorValueBatch? = …
 * }
 * ```
 */
abstract class LocusParserAdapterService : Service() {

    /**
     * Per-device write channels Locus handed us at `init`, keyed by `deviceId`. Used by
     * [writeData]; a dead channel (Locus session gone) evicts itself on the next failed call.
     */
    private val writeChannels = ConcurrentHashMap<String, ILocusSensorWriteChannel>()

    private val binder = object : ILocusSensorAdapterParser.Stub() {

        override fun init(
            deviceId: String,
            deviceTypeId: String,
            bindContext: LocusBindContext,
            writeChannel: ILocusSensorWriteChannel?,
        ): Int {
            // null for read-only transports; store per device so multi-device adapters address
            // the right session in writeData
            writeChannel?.let { writeChannels[deviceId] = it }
            return this@LocusParserAdapterService.init(deviceId, deviceTypeId, bindContext)
        }

        override fun parseData(
            deviceId: String,
            source: String,
            bytes: ByteArray,
        ): SensorValueBatch? {
            return this@LocusParserAdapterService.parseData(deviceId, source, bytes)
        }

        override fun getIntentForSettings(deviceId: String): Intent? {
            return this@LocusParserAdapterService.getIntentForSettings(deviceId)
        }

        override fun shutdown(deviceId: String) {
            writeChannels.remove(deviceId)
            this@LocusParserAdapterService.shutdown(deviceId)
        }
    }

    final override fun onBind(intent: Intent): IBinder {
        return binder
    }

    /**
     * Negotiate startup for one bound device. See [ILocusSensorAdapterParser.init]. Return one of
     * the [AdapterApi] `INIT_*` constants. Keep a `deviceId → deviceTypeId` mapping here if you
     * need the type in later [parseData] calls (they carry only `deviceId`).
     *
     * The write channel (if the transport is writable) is already stored by the time this runs, so
     * an adapter may call [writeData] from here for a connect-time handshake.
     */
    protected abstract fun init(
        deviceId: String,
        deviceTypeId: String,
        bindContext: LocusBindContext,
    ): Int

    /**
     * Parse one inbound data unit — a GATT frame (BT4) or a byte-stream chunk (BT3 / USB / NET).
     * Return `null` if consumed without producing values (e.g. partial frame buffered for
     * reassembly). See [ILocusSensorAdapterParser.parseData].
     */
    protected abstract fun parseData(
        deviceId: String,
        source: String,
        bytes: ByteArray,
    ): SensorValueBatch?

    /**
     * Initiate a write to [deviceId], independent of [parseData] — for a connect-time handshake, a
     * periodic poll, or an event-driven command. Complements returning `writeBacks` from a
     * [parseData] batch (the reactive path). No-op when the device's transport is read-only or its
     * session has ended.
     */
    protected fun writeData(deviceId: String, writes: List<AdapterWrite>) {
        if (writes.isEmpty()) {
            return
        }
        val channel = writeChannels[deviceId] ?: return
        try {
            channel.writeData(deviceId, writes)
        } catch (_: RemoteException) {
            // Locus side gone — drop the dead channel
            writeChannels.remove(deviceId)
        }
    }

    /**
     * Optional settings activity intent for [deviceId]. See
     * [ILocusSensorAdapterParser.getIntentForSettings]. Default implementation returns
     * `null` (no settings UI).
     */
    protected open fun getIntentForSettings(deviceId: String): Intent? {
        return null
    }

    /**
     * Tear down per-pairing state for [deviceId]. See [ILocusSensorAdapterParser.shutdown]. The base
     * class already drops [deviceId]'s write channel before this runs; override to release your own
     * per-device state (parsers, buffers, handles). Default is a no-op.
     */
    protected open fun shutdown(deviceId: String) {
    }

    companion object {

        /**
         * Intent-filter action adapter apps declare on their service so Locus can
         * discover them via `PackageManager.queryIntentServices`.
         */
        const val ACTION_BIND = "locus.api.android.ACTION_SENSOR_ADAPTER_PARSER"

        /**
         * `<meta-data>` key pointing to the adapter's manifest XML resource (device-type
         * catalogue). See `docs/android/guides/adapter-apps/manifest-schema.md`.
         */
        const val META_DATA_KEY = "locus.api.android.SENSOR_ADAPTER"

        /**
         * Custom permission Locus declares and adapter services require, so that only
         * Locus can bind to an adapter and the adapter knows the caller is legitimate.
         */
        const val PERMISSION = "com.asamm.locus.permission.SENSOR_ADAPTER"
    }
}
