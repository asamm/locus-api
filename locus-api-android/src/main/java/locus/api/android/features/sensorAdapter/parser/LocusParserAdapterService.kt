/*
 * Created by menion on 25.05.2026.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
package locus.api.android.features.sensorAdapter.parser

import android.app.Service
import android.content.Intent
import android.os.IBinder
import locus.api.android.features.sensorAdapter.AdapterApi
import locus.api.android.features.sensorAdapter.LocusBindContext

/**
 * Base class for **parser-style** adapter apps — the simpler of the two adapter
 * models. Locus owns the BT/USB/ANT/... transport and lifecycle; your adapter
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
 *     override fun init(bindContext: LocusBindContext): Int = AdapterApi.INIT_OK
 *     override fun parseData(
 *         deviceId: String,
 *         deviceTypeId: String,
 *         source: String,
 *         bytes: ByteArray,
 *     ): SensorValueBatch? = …
 * }
 * ```
 */
abstract class LocusParserAdapterService : Service() {

    private val binder = object : ILocusSensorAdapterParser.Stub() {

        override fun init(bindContext: LocusBindContext): Int {
            return this@LocusParserAdapterService.init(bindContext)
        }

        override fun parseData(
            deviceId: String,
            deviceTypeId: String,
            source: String,
            bytes: ByteArray,
        ): SensorValueBatch? {
            return this@LocusParserAdapterService.parseData(
                deviceId, deviceTypeId, source, bytes,
            )
        }

        override fun getIntentForSettings(): Intent? {
            return this@LocusParserAdapterService.getIntentForSettings()
        }

        override fun shutdown() {
            this@LocusParserAdapterService.shutdown()
        }
    }

    final override fun onBind(intent: Intent): IBinder {
        return binder
    }

    /**
     * Negotiate startup. See [ILocusSensorAdapterParser.init]. Return one of the
     * [AdapterApi] `INIT_*` constants.
     */
    protected abstract fun init(bindContext: LocusBindContext): Int

    /**
     * Parse one inbound data unit — a GATT frame (BT4) or a byte-stream chunk (BT3 / USB / NET).
     * Return `null` if consumed without producing values (e.g. partial frame buffered for
     * reassembly). See [ILocusSensorAdapterParser.parseData].
     */
    protected abstract fun parseData(
        deviceId: String,
        deviceTypeId: String,
        source: String,
        bytes: ByteArray,
    ): SensorValueBatch?

    /**
     * Optional settings activity intent. See
     * [ILocusSensorAdapterParser.getIntentForSettings]. Default implementation returns
     * `null` (no settings UI).
     */
    protected open fun getIntentForSettings(): Intent? {
        return null
    }

    /**
     * Tear down per-pairing state. See [ILocusSensorAdapterParser.shutdown]. Default
     * implementation is a no-op; override for adapters that hold device handles or
     * background work.
     */
    protected open fun shutdown() {
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
