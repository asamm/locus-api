/**
 * Created by menion on 23. 6. 2014.
 * Asamm Software, s. r. o.
 */
package locus.api.objects.extra

import locus.api.objects.Storable
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian
import java.io.IOException

/**
 * Precise statistics for [Track] class.
 */
class TrackStats : Storable() {

    /**
     * Number of points (useful when points itself are not loaded).
     */
    var numOfPoints: Int = 0

    /**
     * Track start time (time of first point) (in ms).
     */
    var startTime: Long = -1L

    /**
     * Track stop time (time of last point) (in ms).
     */
    var stopTime: Long = -1L

    // TOTAL LENGTH

    /**
     * Total length of done route (in m).
     */
    var totalLength: Float = 0.0f

    fun addTotalLength(add: Float) {
        this.totalLength += add
    }

    /**
     * Total track distance with speed (in m).
     */
    var totalLengthMove: Float = 0.0f

    fun addTotalLengthMove(add: Float) {
        this.totalLengthMove += add
    }

    fun getTrackLength(onlyWithMove: Boolean): Float {
        return if (onlyWithMove) {
            totalLengthMove
        } else {
            totalLength
        }
    }

    // TOTAL TIME

    /**
     * Total time of route (in s).
     */
    var totalTime: Long = 0L
        set(value) {
            field = Math.abs(value)
        }

    fun addTotalTime(add: Long) {
        this.totalTime += Math.abs(add)
    }

    /**
     * Total track time with speed (in s).
     */
    var totalTimeMove: Long = 0L
        set(value) {
            field = Math.abs(value)
        }

    fun addTotalTimeMove(add: Long) {
        this.totalTimeMove += Math.abs(add)
    }

    fun getTrackTime(onlyWithMove: Boolean): Long {
        return if (onlyWithMove) {
            totalTimeMove
        } else {
            totalTime
        }
    }

    // SPEED

    /**
     * Maximal speed of this route (in m/s).
     */
    var speedMax: Float = 0.0f

    /**
     * Get average speed value for a track.
     *
     * @param onlyWithMove `true` to get AVG. speed only during movement
     * @return average speed in m/s
     */
    fun getSpeedAverage(onlyWithMove: Boolean): Float {
        val trackTime = getTrackTime(onlyWithMove) / 1000.0f
        return if (trackTime > 0) {
            getTrackLength(onlyWithMove) / trackTime
        } else {
            0.0f
        }
    }

    // ELEVATION & CHANGES

    /**
     * Maximum altitude on track (in m).
     */
    var altitudeMax: Float = 0.0f

    /**
     * Minimum altitude on track (in m).
     */
    var altitudeMin: Float = 0.0f

    /**
     * Neutral grade (distance) (in m).
     */
    var eleNeutralDistance: Float = 0.0f

    fun addEleNeutralDistance(add: Float) {
        this.eleNeutralDistance += add
    }

    /**
     * Neutral grade (elevation) (in m).
     */
    var eleNeutralHeight: Float = 0.0f

    /**
     * Add elevation value for "neutral height"
     */
    fun addEleNeutralHeight(add: Float) {
        this.eleNeutralHeight += add
    }

    /**
     * Positive grade (distance)
     */
    var elePositiveDistance: Float = 0.0f

    fun addElePositiveDistance(add: Float) {
        this.elePositiveDistance += add
    }

    /**
     * Positive grade (elevation)
     */
    var elePositiveHeight: Float = 0.0f

    /**
     * Add elevation value for "positive height"
     */
    fun addElePositiveHeight(add: Float) {
        this.elePositiveHeight += add
    }

    /**
     * Negative grade (distance)
     */
    var eleNegativeDistance: Float = 0.0f

    fun addEleNegativeDistance(add: Float) {
        this.eleNegativeDistance += add
    }

    /**
     * Negative grade (elevation)
     */
    var eleNegativeHeight: Float = 0.0f

    /**
     * Add elevation value for "negative height"
     */
    fun addEleNegativeHeight(add: Float) {
        this.eleNegativeHeight += add
    }

    /**
     * Check if track has some elevation values.
     */
    fun hasElevationValues(): Boolean {
        return (altitudeMin != Float.POSITIVE_INFINITY && altitudeMin != 0.0f)
                || (altitudeMax != Float.NEGATIVE_INFINITY && altitudeMax != 0.0f)
    }

    // HEART RATE

    /**
     * Number of measured beats.
     */
    private var heartRateBeats: Double = 0.0

    /**
     * Time during which were beats measured.
     */
    private var heartRateTime: Long = 0L

    /**
     * Maximum HRM value.
     */
    var hrmMax: Int = 0
        private set

    /**
     * Average value of heart rate.
     */
    val hrmAverage: Int
        get() = if (heartRateBeats > 0 && heartRateTime > 0L) {
            val minutes = heartRateTime / (60.0 * 1000.0)
            (heartRateBeats / minutes).toInt()
        } else {
            0
        }

    /**
     * Add measured heart rate values.
     *
     * @param hrmMeasured   measured heart rate value (in bpm)
     * @param hrmAvgSegment average heart rate value in segment (in bpm)
     * @param measureTime   time of segment (in millis)
     */
    fun addHeartRateMeasure(hrmMeasured: Int, hrmAvgSegment: Int, measureTime: Long) {
        // store values
        val inMinutes = measureTime * 1.0 / (60 * 1000)
        val numOfBeats = hrmAvgSegment * inMinutes
        heartRateBeats += numOfBeats
        heartRateTime += measureTime

        // compute max. heart rate
        hrmMax = Math.max(hrmMax, hrmMeasured)
    }

    // CADENCE

    /**
     * Number of measured cadence revolutions.
     */
    private var cadenceNumber: Double = 0.0

    /**
     * Time during which were cadence measured.
     */
    private var cadenceTime: Long = 0
    /**
     * Maximum cadence value.
     */
    var cadenceMax: Int = 0
        private set

    /**
     * Get average value of cadence.
     *
     * @return average cadence value (
     */
    val cadenceAverage: Int
        get() = if (cadenceNumber > 0 && cadenceTime > 0L) {
            val minutes = cadenceTime / (60.0 * 1000.0)
            (cadenceNumber / minutes).toInt()
        } else {
            0
        }

    /**
     * Add measured cadence values.
     *
     * @param revMeasured   measured cadence value (in rpm)
     * @param revAvgSegment average revolutions value in segment (in rpm)
     * @param measureTime   time of segment (in millis)
     */
    fun addCadenceMeasure(revMeasured: Int, revAvgSegment: Int, measureTime: Long) {
        // store values
        val inMinutes = measureTime * 1.0 / (60 * 1000)
        val numOfRevolutions = revAvgSegment * inMinutes
        cadenceNumber += numOfRevolutions
        cadenceTime += measureTime

        // compute max. heart rate
        cadenceMax = Math.max(cadenceMax, revMeasured)
    }

    // ENERGY

    /**
     * Burned energy (in joule).
     */
    var energy: Int = 0
        private set

    /**
     * Add burned energy.
     *
     * @param energy burned energy (in joule)
     */
    fun addEnergy(energy: Int) {
        this.energy += energy
    }

    // STRIDES

    /**
     * Total number of strides..
     */
    var numOfStrides: Int = 0

    //*************************************************
    // OTHER TOOLS
    //*************************************************

    init {
        // other variables
        resetStatistics()
    }

    /**
     * Reset all parameters of track statistics.
     */
    fun resetStatistics() {
        // basic statistics variables
        totalLength = 0.0f
        totalLengthMove = 0.0f
        totalTime = 0L
        totalTimeMove = 0L
        speedMax = 0.0f
        heartRateBeats = 0.0
        heartRateTime = 0L
        hrmMax = 0
        cadenceNumber = 0.0
        cadenceTime = 0L
        cadenceMax = 0
        energy = 0
        numOfStrides = 0

        // reset also elevation values
        resetStatisticsAltitude()
    }

    /**
     * Reset only statistics related to elevation values.
     */
    fun resetStatisticsAltitude() {
        altitudeMax = Float.NEGATIVE_INFINITY
        altitudeMin = Float.POSITIVE_INFINITY

        // graph variables
        eleNeutralDistance = 0.0f
        eleNeutralHeight = 0.0f
        elePositiveDistance = 0.0f
        elePositiveHeight = 0.0f
        eleNegativeDistance = 0.0f
        eleNegativeHeight = 0.0f
    }

    /**
     * Function that allows to merge more statistics into one.
     *
     * @param stats second statistics that will be merged into this.
     */
    fun appendStatistics(stats: TrackStats) {
        this.numOfPoints += stats.numOfPoints
        this.startTime = Math.min(startTime, stats.startTime)
        this.stopTime = Math.max(stopTime, stats.stopTime)
        this.totalLength += stats.totalLength
        this.totalLengthMove += stats.totalLengthMove
        this.totalTime += stats.totalTime
        this.totalTimeMove += stats.totalTimeMove
        this.speedMax = Math.max(speedMax, stats.speedMax)
        this.altitudeMax = Math.max(altitudeMax, stats.altitudeMax)
        this.altitudeMin = Math.min(altitudeMin, stats.altitudeMin)

        this.eleNeutralDistance += stats.eleNeutralDistance
        this.eleNeutralHeight += stats.eleNeutralHeight
        this.elePositiveDistance += stats.elePositiveDistance
        this.elePositiveHeight += stats.elePositiveHeight
        this.eleNegativeDistance += stats.eleNegativeDistance
        this.eleNegativeHeight += stats.eleNegativeHeight

        this.heartRateBeats += stats.heartRateBeats
        this.heartRateTime += stats.heartRateTime
        this.hrmMax = Math.max(this.hrmMax, stats.hrmMax)

        this.cadenceNumber += stats.cadenceNumber
        this.cadenceTime += stats.cadenceTime
        this.cadenceMax = Math.max(this.cadenceMax, stats.cadenceMax)

        this.energy += stats.energy
        this.numOfStrides += stats.numOfStrides
    }

    //*************************************************
    // STORABLE PART
    //*************************************************

    override fun getVersion(): Int {
        return 3
    }

    @Throws(IOException::class)
    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        numOfPoints = dr.readInt()
        startTime = dr.readLong()
        stopTime = dr.readLong()

        // basic variables
        totalLength = dr.readFloat()
        totalLengthMove = dr.readFloat()
        totalTime = dr.readLong()
        totalTimeMove = dr.readLong()
        speedMax = dr.readFloat()

        // altitude values
        altitudeMax = dr.readFloat()
        altitudeMin = dr.readFloat()

        // elevation variables
        eleNeutralDistance = dr.readFloat()
        eleNeutralHeight = dr.readFloat()
        elePositiveDistance = dr.readFloat()
        elePositiveHeight = dr.readFloat()
        eleNegativeDistance = dr.readFloat()
        eleNegativeHeight = dr.readFloat()
        dr.readFloat() // eleTotalAbsDistance
        dr.readFloat() // eleTotalAbsHeight

        // V1
        if (version >= 1) {
            heartRateBeats = dr.readInt().toDouble()
            heartRateTime = dr.readLong()
            hrmMax = dr.readInt()
            energy = dr.readInt()
        }

        // V2
        if (version >= 2) {
            heartRateBeats = dr.readDouble()
            cadenceNumber = dr.readDouble()
            cadenceTime = dr.readLong()
            cadenceMax = dr.readInt()
        }

        // V3
        if (version >= 3) {
            numOfStrides = dr.readInt()
        }
    }

    @Throws(IOException::class)
    override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeInt(numOfPoints)
        dw.writeLong(startTime)
        dw.writeLong(stopTime)

        // basic variables
        dw.writeFloat(totalLength)
        dw.writeFloat(totalLengthMove)
        dw.writeLong(totalTime)
        dw.writeLong(totalTimeMove)
        dw.writeFloat(speedMax)

        // altitude values
        dw.writeFloat(altitudeMax)
        dw.writeFloat(altitudeMin)

        // elevation variables
        dw.writeFloat(eleNeutralDistance)
        dw.writeFloat(eleNeutralHeight)
        dw.writeFloat(elePositiveDistance)
        dw.writeFloat(elePositiveHeight)
        dw.writeFloat(eleNegativeDistance)
        dw.writeFloat(eleNegativeHeight)
        dw.writeFloat(0.0f) // eleTotalAbsDistance
        dw.writeFloat(0.0f) // eleTotalAbsHeight

        // V1
        dw.writeInt(heartRateBeats.toInt())
        dw.writeLong(heartRateTime)
        dw.writeInt(hrmMax)
        dw.writeInt(energy)

        // V2
        dw.writeDouble(heartRateBeats)
        dw.writeDouble(cadenceNumber)
        dw.writeLong(cadenceTime)
        dw.writeInt(cadenceMax)

        // V3
        dw.writeInt(numOfStrides)
    }
}