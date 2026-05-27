// ILocusSensorAdapterParser.aidl
package locus.api.android.features.sensorAdapter.parser;

import android.content.Intent;
import locus.api.android.features.sensorAdapter.LocusBindContext;
import locus.api.android.features.sensorAdapter.parser.SensorValueBatch;

/**
 * Bound service interface for adapter apps: Locus owns the BT/USB/ANT/... transport,
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
     * Negotiate startup. Locus passes its own identification + the set of refIds it
     * understands; adapter returns one of the INIT_* result codes from {@code AdapterApi}.
     * Called once per bind.
     */
    int init(in LocusBindContext bindContext);

    /**
     * Parse one inbound data unit from the device. For BT4 that's a single GATT notification /
     * read response keyed by a characteristic; for stream transports (BT3 / USB / NET) it's a
     * chunk of the byte stream. The adapter owns frame-reassembly state — if a logical message
     * spans multiple physical packets, it buffers across calls and returns null for the early
     * fragments.
     *
     * @param deviceId      stable instance id Locus assigned at pairing (BLE MAC, USB id, …)
     * @param deviceTypeId  matches an `<deviceType id="...">` from the manifest XML;
     *                      lets adapters that support multiple device types switch on
     *                      it for protocol-specific parsing
     * @param source        characteristic UUID for BT4; empty string for stream transports
     * @param bytes         raw payload
     * @return parsed batch, or null if consumed without producing values (e.g. partial frame)
     */
    @nullable SensorValueBatch parseData(
        String deviceId,
        String deviceTypeId,
        String source,
        in byte[] bytes);

    /**
     * Optional intent the adapter wants Locus to launch when the user taps "Settings"
     * on the adapter row in the picker.
     */
    @nullable Intent getIntentForSettings();

    /**
     * Tear down per-pairing state. Called by Locus on unpair / app shutdown.
     */
    void shutdown();
}
