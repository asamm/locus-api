// ILocusSensorWriteChannel.aidl
package locus.api.android.features.sensorAdapter.parser;

import locus.api.android.features.sensorAdapter.parser.AdapterWrite;

/**
 * Callback Locus hands the adapter in {@code init(...)} so the adapter can initiate writes to the
 * device independent of {@code parseData(...)} — e.g. a connect-time handshake, a periodic poll, or
 * an event-driven command. Complements the reactive {@code SensorValueBatch.writeBacks} return path.
 *
 * {@code oneway}: fire-and-forget, never blocks the adapter. Locus drops a write when the session
 * is closed or the device type's transport is read-only. Provided as null when the transport can't
 * write (e.g. NET).
 */
oneway interface ILocusSensorWriteChannel {

    /**
     * Write to the device the adapter is bound for. {@code deviceId} matches the {@code deviceId}
     * the session was opened with at {@code init(...)} (and {@code parseData}'s {@code deviceId});
     * Locus drops the call when it doesn't match the session.
     */
    void writeData(String deviceId, in List<AdapterWrite> writes);
}
