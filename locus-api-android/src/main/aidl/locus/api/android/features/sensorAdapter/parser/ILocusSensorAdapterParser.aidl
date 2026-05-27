// ILocusSensorAdapterParser.aidl
package locus.api.android.features.sensorAdapter.parser;

import android.content.Intent;
import locus.api.android.features.sensorAdapter.LocusBindContext;
import locus.api.android.features.sensorAdapter.parser.SensorValueBatch;
import locus.api.android.features.sensorAdapter.parser.ILocusSensorWriteChannel;

/**
 * Bound service interface for adapter apps: Locus owns the BT3 / BT4 / USB transport,
 * scanning, pairing, and GATT lifecycle. The adapter only declares its device types in
 * `res/xml/locus_adapter.xml` and implements byte-frame parsing here.
 *
 * All static metadata (apiVersion, id, displayName, supported refIds,
 * device-type catalogue) lives in the adapter's manifest XML so Locus can populate
 * its picker without binding. Bind happens only when the user starts a sensor
 * session with one of the adapter's device types.
 */
interface ILocusSensorAdapterParser {

    /**
     * Negotiate startup for one bound device. Establishes the session identity — every other call
     * carries just {@code deviceId}; the adapter keeps a {@code deviceId → deviceTypeId} mapping
     * itself if it needs the type later. Locus passes its identification + the refIds it understands
     * in {@code bindContext}; adapter returns one of the INIT_* result codes from {@code AdapterApi}.
     * Called once per bound device.
     *
     * @param deviceId      stable instance id Locus assigned at pairing (BLE MAC, USB id, …)
     * @param deviceTypeId  matches an `<deviceType id="...">` from the manifest XML; lets adapters
     *                      that support multiple device types pick the protocol for this device
     * @param writeChannel  callback for adapter-initiated writes (connect handshake, periodic poll,
     *                      event). Null when the device type's transport is read-only. The adapter
     *                      may call it any time the session is open; see {@code ILocusSensorWriteChannel}.
     */
    int init(
        String deviceId,
        String deviceTypeId,
        in LocusBindContext bindContext,
        @nullable ILocusSensorWriteChannel writeChannel);

    /**
     * Parse one inbound data unit from the device. For BT4 that's a single GATT notification /
     * read response keyed by a characteristic; for stream transports (BT3 / USB / NET) it's a
     * chunk of the byte stream. The adapter owns frame-reassembly state — if a logical message
     * spans multiple physical packets, it buffers across calls and returns null for the early
     * fragments.
     *
     * @param deviceId  the device this data is from (the {@code deviceTypeId} was established at
     *                  {@code init}; look it up in your own {@code deviceId → deviceTypeId} map if
     *                  you need to branch on protocol)
     * @param source    characteristic UUID for BT4; empty string for stream transports
     * @param bytes     raw payload
     * @return parsed batch, or null if consumed without producing values (e.g. partial frame)
     */
    @nullable SensorValueBatch parseData(
        String deviceId,
        String source,
        in byte[] bytes);

    /**
     * Optional intent the adapter wants Locus to launch when the user taps "Settings"
     * on [deviceId]'s row in the picker.
     */
    @nullable Intent getIntentForSettings(String deviceId);

    /**
     * Tear down per-pairing state for [deviceId]. Called by Locus on unpair / disconnect / app
     * shutdown, once per bound device (mirrors the [deviceId] of `init` / `parseData`).
     */
    void shutdown(String deviceId);
}
