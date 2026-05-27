/*
 * Created by menion on 25.05.2026.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
package locus.api.android.features.sensorAdapter

/**
 * Versioning + result-code constants for the Locus sensor-adapter contract. The
 * AIDL surface returns plain `int` result codes because AIDL has no enums; the
 * named constants here give them meaning on both sides.
 *
 * `ConnectionType` and `CharacteristicMode` are full Kotlin `enum class`es — they
 * are XML-only (adapter declares them as string literals in `res/xml/locus_adapter.xml`,
 * Locus parses to the enum via `valueOf`), not part of the AIDL surface, so the
 * `enum class` doesn't cross the binder boundary.
 *
 * A new `VERSION` value ships only with backward-incompatible AIDL or XML-format
 * changes — additive XML attributes or new result codes do not bump it because old
 * adapters keep working.
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
     * Adapter needs one-time user setup before it can bind (credentials, a runtime permission,
     * or in-app device pairing). Locus launches the adapter's
     * [ILocusSensorAdapterParser.getIntentForSettings] activity and re-attempts bind after it returns.
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
     * Transport Locus uses to reach the adapter's device. Declared on
     * `<deviceType connectionType="…">` in the manifest XML; the string matches the case name.
     */
    enum class ConnectionType {

        /** Bluetooth Low Energy GATT — Locus owns the BLE stack; the adapter parses bytes. */
        BT4,

        // Reserved for later parser-side transport expansions: BT3, USB, ANT, GNSS_NMEA.
        // Add cases below as each lands; XML uses the case `name` directly.
    }

    //*************************************************
    // CHARACTERISTIC MODES (BT4 only)
    //*************************************************

    /**
     * How Locus should drive each BT4 GATT characteristic declared on
     * `<characteristic mode="…">` in the adapter manifest XML — the XML string
     * matches the enum case name. Locus subscribes / polls / writes per the declared
     * mode; the adapter receives parsed bytes via
     * [ILocusSensorAdapterParser.parseCharacteristic].
     */
    enum class CharacteristicMode {

        /** Subscribe to GATT NOTIFY and feed each received frame to the parser. */
        NOTIFY,

        /**
         * Issue a GATT READ on a fixed interval (`<characteristic pollIntervalMs="…">`)
         * and feed each response to the parser.
         */
        READ_POLLED,

        /**
         * Locus performs WRITE on demand via [SensorValueBatch.writeBacks].
         * No subscription; the characteristic is only used as an ACK / control channel.
         */
        WRITE,
    }
}
