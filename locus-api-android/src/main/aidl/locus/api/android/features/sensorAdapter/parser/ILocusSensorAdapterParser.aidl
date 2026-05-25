// ILocusSensorAdapterParser.aidl
package locus.api.android.features.sensorAdapter.parser;

import android.content.Intent;
import locus.api.android.features.sensorAdapter.LocusBindContext;
import locus.api.android.features.sensorAdapter.parser.SensorValueBatch;

/**
 * Bound service interface for **parser-style** adapter apps — the simpler of the two
 * adapter models: Locus owns the BT/USB/ANT/... transport, scanning, pairing, and
 * GATT lifecycle. The adapter only declares its device types in
 * `res/xml/locus_adapter.xml` and implements byte-frame parsing here.
 *
 * All static metadata (apiVersion, schemaVersion, id, displayName, supported refIds,
 * device-type catalogue) lives in the adapter's manifest XML so Locus can populate
 * its picker without binding. Bind happens only when the user starts a sensor
 * session with one of the adapter's device types.
 *
 * The companion "push-style" adapter API (adapter owns its own connection
 * lifecycle, Locus only signals start/stop) is a separate AIDL shape, deferred
 * until a real push-style adapter use case drives its design.
 */
interface ILocusSensorAdapterParser {

    /**
     * Negotiate startup. Locus passes its own identification + the set of refIds it
     * understands; adapter returns one of the INIT_* result codes from {@code AdapterApi}.
     * Called once per bind.
     */
    int init(in LocusBindContext bindContext);

    /**
     * Parse one BT4 GATT notification / read response. Adapter owns frame-reassembly
     * state — if a logical message spans multiple physical packets, the adapter buffers
     * across calls and returns null for the early fragments.
     *
     * @param deviceId      stable instance id Locus assigned at pairing (BLE MAC etc.)
     * @param deviceTypeId  matches an `<deviceType id="...">` from the manifest XML;
     *                      lets adapters that support multiple device types switch on
     *                      it for protocol-specific parsing
     * @param charUuid      characteristic UUID the frame was received on
     * @param bytes         raw notification payload
     * @return parsed batch, or null if the frame was consumed without producing values
     *         (e.g. partial frame buffered for later)
     */
    @nullable SensorValueBatch parseCharacteristic(
        String deviceId,
        String deviceTypeId,
        String charUuid,
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
