/*
 * Created by menion on 25.05.2026.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
package locus.api.android.features.sensorAdapter

/**
 * Versioning + result-code constants for the Locus sensor-adapter contract. The
 * AIDL surface returns plain `int`s because AIDL has no enums; these names give
 * them meaning on both sides.
 *
 * A new `VERSION` value ships only with backward-incompatible AIDL changes —
 * additive XML attributes or new result codes do not bump it because old adapters
 * keep working.
 */
object AdapterApi {

    /**
     * Current adapter contract version. Adapters declare the version they're built
     * against as `<adapter apiVersion="…">` in their manifest XML so Locus can skip
     * incompatible adapters before binding. Locus also re-checks at bind time via
     * [LocusBindContext.locusApiVersion]; a mismatch the XML check missed surfaces
     * as [INIT_INCOMPATIBLE_API].
     */
    const val VERSION = 1

    //*************************************************
    // init() RESULT CODES
    //*************************************************

    /**
     * Initialization completed; Locus may start driving the transport.
     */
    const val INIT_OK = 0

    /**
     * Adapter needs the user to complete one-time setup before it can bind — e.g. enter
     * API credentials, grant a runtime permission the adapter needs (BLE, BODY_SENSORS),
     * or finish device pairing in the adapter's own UI. Locus launches the adapter's
     * [ILocusSensorAdapterParser.getIntentForSettings] activity and re-attempts bind
     * once it returns. Adapters that need a permission grant should host the request
     * flow in that settings activity.
     */
    const val INIT_NEED_USER_ACTION = 1

    /**
     * Adapter and Locus speak different [VERSION]s and can't negotiate. Locus
     * surfaces an error to the user and skips this adapter until one side updates.
     */
    const val INIT_INCOMPATIBLE_API = 2

    /**
     * Generic failure. Adapter logs the cause; Locus shows a generic "adapter
     * unavailable" message. Prefer [INIT_NEED_USER_ACTION] or [INIT_INCOMPATIBLE_API]
     * when applicable.
     */
    const val INIT_ERROR = 3

    //*************************************************
    // CONNECTION TYPES
    //*************************************************

    /**
     * Which transport Locus uses to talk to the adapter's underlying device.
     * Declared on `<deviceType connectionType="…">` in the adapter manifest XML.
     * Only meaningful for the parser adapter style — the push adapter style
     * (deferred) doesn't use this because the adapter owns its own transport.
     *
     * New types added in later PRs (BT3 / USB / ANT / GNSS) get higher integer
     * values; existing values are stable.
     */
    object ConnectionType {

        // Value 0 reserved as "unspecified / future sentinel" — see issue tracker before
        // assigning. Concrete connection types start at 1.

        /** Bluetooth Low Energy GATT — Locus owns the BLE stack; the adapter parses bytes. */
        const val BT4 = 1

        // Reserved values 2..10 for later parser-side transport expansions
        // (BT3, USB, ANT, GNSS_NMEA, ...).
    }

    //*************************************************
    // CHARACTERISTIC MODES (BT4 only)
    //*************************************************

    /**
     * How Locus should drive each BT4 GATT characteristic declared on
     * `<characteristic mode="…">` in the adapter manifest XML. Locus subscribes
     * / polls / writes per the declared mode; the adapter receives parsed bytes
     * via [ILocusSensorAdapterParser.parseCharacteristic].
     */
    object CharacteristicMode {

        /** Subscribe to GATT NOTIFY and feed each received frame to the parser. */
        const val NOTIFY = 0

        /**
         * Issue a GATT READ on a fixed interval (`<characteristic pollIntervalMs="…">`)
         * and feed each response to the parser.
         */
        const val READ_POLLED = 1

        /**
         * Locus performs WRITE on demand via [SensorValueBatch.writeBacks].
         * No subscription; the characteristic is only used as an ACK / control channel.
         */
        const val WRITE = 2
    }
}
