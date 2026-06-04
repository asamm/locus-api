/*
 * Created by menion on 03.06.2026.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
package com.asamm.locus.api.sample.computetrack

import android.content.Intent
import locus.api.android.features.computeTrack.ComputeTrackParameters
import locus.api.android.features.computeTrack.ComputeTrackService
import locus.api.android.objects.LocusVersion
import locus.api.objects.extra.GeoDataExtra
import locus.api.objects.extra.Location
import locus.api.objects.extra.PointRteAction
import locus.api.objects.geoData.Point
import locus.api.objects.geoData.Track
import locus.api.objects.geoData.parameterRteAction
import locus.api.objects.geoData.parameterRteComputeType
import locus.api.objects.geoData.parameterRteIndex
import kotlin.math.PI
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Sample Locus compute-track (routing) provider. Locus binds this service over AIDL and calls
 * [computeTrack] to turn the user's via-points into a navigable [Track].
 *
 * It is intentionally a "dumb router": between consecutive via-points it draws a deliberately
 * **wavy** line (a sine wiggle around the straight segment) rather than following any road network.
 * The wiggle has no navigational meaning — it's there so that on the map you can tell at a glance
 * the route was *computed* by a provider and isn't just a straight line drawn between the points.
 *
 * What a real provider copies is the contract around that, not the geometry:
 *
 * - declare capabilities ([attribution], [trackTypes], [numOfTransitPoints], [intentForSettings]);
 * - build the track geometry as a list of [Location] in [Track.points];
 * - emit turn-by-turn instructions as [Point] waypoints carrying the maneuver ([PointRteAction])
 *   and the geometry index it happens at ([Point.parameterRteIndex]).
 *
 * It deliberately does **not** compute track statistics or per-waypoint distance / time: Locus
 * rebuilds those from the geometry when it validates the received track (see [computeTrack]).
 *
 * A production router (GraphHopper, BRouter, Valhalla, an online service, …) swaps out only the
 * geometry-and-maneuver computation in [computeTrack]; everything around it stays the same.
 */
class WavyRouteService : ComputeTrackService() {

    override val attribution: String
        get() = "Routed by <b>Wavy sample</b> — a wiggly demo line, not for real navigation"

    /**
     * Profiles this provider advertises. Locus shows these in the routing-source picker and passes
     * the chosen one back as [ComputeTrackParameters.type]. The values are stable IDs from
     * [GeoDataExtra]; see `docs/android/reference/locus-variables.md` for the catalogue.
     */
    override val trackTypes: IntArray
        get() = intArrayOf(
            GeoDataExtra.VALUE_RTE_TYPE_FOOT_01,
            GeoDataExtra.VALUE_RTE_TYPE_CYCLE,
            GeoDataExtra.VALUE_RTE_TYPE_CAR,
        )

    /**
     * Advertises that this provider accepts via-points between start and end. Overridable only
     * because the base member is `open` (it was a final `val` in earlier releases) — a provider
     * that supports transit points could not declare it otherwise.
     */
    override val numOfTransitPoints: Int
        get() = MAX_TRANSIT_POINTS

    override val intentForSettings: Intent
        get() = Intent(this, InfoActivity::class.java)

    override fun computeTrack(lv: LocusVersion?, params: ComputeTrackParameters): Track? {
        val vias = params.locations
        // Locus guarantees >= 2 via-points (start + end), but a provider must never trust the
        // caller blindly — a malformed request should yield null, not a crash inside the binder.
        if (vias.size < 2) {
            return null
        }

        // Geometry — connect each leg with a wavy line. maneuverIndices[i] records where input
        // via-point i landed in the densified list, so instructions can point back into it.
        val geometry = ArrayList<Location>()
        val maneuverIndices = IntArray(vias.size)
        geometry.add(vias[0])
        maneuverIndices[0] = 0
        for (i in 1 until vias.size) {
            geometry.addAll(wavySegment(vias[i - 1], vias[i]))
            geometry.add(vias[i])
            maneuverIndices[i] = geometry.size - 1
        }

        val track = Track().apply {
            name = "Wavy route (${vias.size} points)"
            points = geometry
            parameterRteComputeType = params.type
        }

        // Instructions — only when Locus asked for them. One waypoint per input via-point.
        //
        // Note what this provider does NOT set: track statistics (TrackStats) and per-waypoint
        // distance / time. Locus recomputes all of that from the geometry when it validates the
        // received track, overwriting anything supplied here — so computing it would be wasted
        // work. A provider owns the geometry, the route type, and the maneuvers. (If you want an
        // ETA, set PAR_RTE_SPEED_F per waypoint as a seed; Locus derives per-segment time from it.)
        if (params.isComputeInstructions) {
            track.waypoints = buildInstructions(vias, maneuverIndices, params)
        }
        return track
    }

    /**
     * One navigation [Point] per via-point. Each carries the maneuver to perform there and the
     * index into [Track.points] where it happens.
     */
    private fun buildInstructions(
        vias: Array<Location>,
        maneuverIndices: IntArray,
        params: ComputeTrackParameters,
    ): MutableList<Point> {
        val waypoints = ArrayList<Point>(vias.size)
        for (i in vias.indices) {
            val action = when (i) {
                // Departure. If Locus supplied the user's current heading, the first instruction
                // is the turn from that heading onto leg 0; otherwise just "head out".
                0 -> {
                    if (params.hasDirection) {
                        PointRteAction.getActionByAngle(
                            normalizeDeg(vias[0].bearingTo(vias[1]) - params.currentDirection),
                        )
                    } else {
                        PointRteAction.CONTINUE_STRAIGHT
                    }
                }
                // Arrival.
                vias.size - 1 -> PointRteAction.ARRIVE_DEST
                // Interior via-point: turn from the incoming leg onto the outgoing leg.
                else -> PointRteAction.getActionByAngle(
                    normalizeDeg(vias[i].bearingTo(vias[i + 1]) - vias[i - 1].bearingTo(vias[i])),
                )
            }

            // The two fields Locus keeps verbatim: the maneuver, and the index of the trackpoint
            // it happens at. Everything else on the waypoint is recomputed, so nothing else is set.
            waypoints.add(
                Point("Maneuver $i", vias[i]).apply {
                    parameterRteIndex = maneuverIndices[i]
                    parameterRteAction = action
                },
            )
        }
        return waypoints
    }

    /**
     * Intermediate points strictly between [from] and [to] forming a wavy line: the straight
     * segment plus a perpendicular sine offset. The `sin(t·π)` envelope keeps the offset zero at
     * both via-points, so legs join cleanly; the offset scales with leg length so the wiggle is
     * visible at any distance. Endpoints are excluded so legs chain without duplicating via-points.
     */
    private fun wavySegment(from: Location, to: Location): List<Location> {
        val dLat = to.latitude - from.latitude
        val dLon = to.longitude - from.longitude
        val length = hypot(dLat, dLon)
        if (length == 0.0) {
            return emptyList()
        }

        // unit vector perpendicular to the straight leg, in lat/lon space (good enough for a demo)
        val perpLat = -dLon / length
        val perpLon = dLat / length
        val amplitude = length * WAVE_AMPLITUDE_FRACTION

        val result = ArrayList<Location>(WAVE_STEPS - 1)
        for (i in 1 until WAVE_STEPS) {
            val t = i.toDouble() / WAVE_STEPS
            val offset = amplitude * sin(t * PI) * sin(t * PI * WAVE_COUNT)
            result.add(
                Location(
                    from.latitude + t * dLat + perpLat * offset,
                    from.longitude + t * dLon + perpLon * offset,
                ),
            )
        }
        return result
    }

    /**
     * Normalise a heading delta to the `0..360` range [PointRteAction.getActionByAngle] expects
     * (it reads 30–180 as right turns, 180–330 as left turns).
     */
    private fun normalizeDeg(angle: Float): Float {
        var a = angle % 360f
        if (a < 0f) {
            a += 360f
        }
        return a
    }

    companion object {

        // samples generated per leg (excluding endpoints) — enough for a smooth wave
        private const val WAVE_STEPS = 64

        // number of sine oscillations along a leg
        private const val WAVE_COUNT = 7

        // peak perpendicular offset as a fraction of the leg's straight length
        private const val WAVE_AMPLITUDE_FRACTION = 0.06

        // advertised cap on via-points between start and end
        private const val MAX_TRANSIT_POINTS = 100
    }
}
