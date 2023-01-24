/**
 * Created by menion on 23. 6. 2014.
 * Asamm Software, s. r. o.
 */
package locus.api.objects.extra

import locus.api.objects.Storable
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian
import java.io.IOException
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Precise statistics for the [locus.api.objects.geoData.Track] class.
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

    fun getLength(onlyWithMove: Boolean): Float {
        return if (onlyWithMove) {
            totalLengthMove
        } else {
            totalLength
        }
    }

    // TOTAL TIME

    /**
     * Total time of route (in ms).
     */
    var totalTime: Long = 0L
        set(value) {
            field = abs(value)
        }

    fun addTotalTime(add: Long) {
        this.totalTime += abs(add)
    }

    /**
     * Total track time with speed (in ms).
     */
    var totalTimeMove: Long = 0L
        set(value) {
            field = abs(value)
        }

    fun addTotalTimeMove(add: Long) {
        this.totalTimeMove += abs(add)
    }

    /**
     * Get duration of the track
     */
    fun getTime(onlyWithMove: Boolean): Long {
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
        val time = getTime(onlyWithMove) / 1000.0f
        return if (time > 0) {
            getLength(onlyWithMove) / time
        } else {
            0.0f
        }
    }

    // ELEVATION & CHANGES

    /**
     * Maximum altitude on the track (in m).
     */
    var altitudeMax: Float = 0.0f

    /**
     * Minimum altitude on the track (in m).
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
     * Check if the track has some elevation values.
     */
    fun hasElevationValues(): Boolean {
        return (altitudeMin != Float.POSITIVE_INFINITY && altitudeMin != 0.0f)
                || (altitudeMax != Float.NEGATIVE_INFINITY && altitudeMax != 0.0f)
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
     * Maximum cadence value.
     */
    var cadenceMax: Int = 0
        private set

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
        cadenceMax = max(cadenceMax, revMeasured)
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
     * Average value of heart rate.
     */
    val heartRateAverage: Int
        get() = if (heartRateBeats > 0 && heartRateTime > 0L) {
            val minutes = heartRateTime / (60.0 * 1000.0)
            (heartRateBeats / minutes).toInt()
        } else {
            0
        }

    /**
     * Maximum HRM value.
     */
    var heartRateMax: Int = 0
        private set

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
        heartRateMax = max(heartRateMax, hrmMeasured)
    }

    // POWER

    /**
     * Sum of the average power counter (power measurement W * time in sec).
     */
    private var powerAvgSum: Double = 0.0

    /**
     * Time during which were power measured (in millis).
     */
    private var powerTime: Long = 0L

    /**
     * Average value of power (W).
     */
    val powerAverage: Int
        get() = if (powerAvgSum > 0.0 && powerTime > 0L) {
            val isSeconds = powerTime / 1000.0
            (powerAvgSum / isSeconds).toInt()
        } else {
            0
        }

    /**
     * Maximum power value (W).
     */
    var powerMax: Int = 0
        private set

    /**
     * Add measured power values.
     *
     * @param powerMeasured measured power value (in W)
     * @param powerAvgSegment average power value in segment (in W)
     * @param timeSegment time of segment (in millis)
     */
    fun addPowerMeasure(powerMeasured: Int, powerAvgSegment: Int, timeSegment: Long) {
        // store values
        val inSeconds = timeSegment / 1000.0
        powerAvgSum += powerAvgSegment * inSeconds
        powerTime += timeSegment
        powerMax = max(powerMax, powerMeasured)
    }

    // STRIDES

    /**
     * Total number of strides..
     */
    var numOfStrides: Int = 0

    // TEMPERATURE

    /**
     * Flag if temperature values are available.
     */
    var hasTemperature: Boolean = false

    /**
     * Minimum measured temperature (in °C).
     */
    var temperatureMin: Float = Float.POSITIVE_INFINITY

    /**
     * Maximum measured temperature (in °C).
     */
    var temperatureMax: Float = Float.NEGATIVE_INFINITY

    /**
     * Add temperature to current track statistics.
     */
    fun addTemperature(temp: Float) {
        hasTemperature = true
        temperatureMin = min(temperatureMin, temp)
        temperatureMax = max(temperatureMax, temp)
    }

    //*************************************************
    // OTHER TOOLS
    //*************************************************

    init {
        // other variables
        resetStatistics()
    }

    /**
     * Reset all parameters of the track statistics.
     */
    fun resetStatistics() {
        // basic statistics variables
        totalLength = 0.0f
        totalLengthMove = 0.0f
        totalTime = 0L
        totalTimeMove = 0L
        speedMax = 0.0f
        resetStatisticsAltitude()

        cadenceNumber = 0.0
        cadenceTime = 0L
        cadenceMax = 0
        energy = 0
        heartRateBeats = 0.0
        heartRateTime = 0L
        heartRateMax = 0
        numOfStrides = 0
        powerAvgSum = 0.0
        powerTime = 0L
        powerMax = 0
        hasTemperature = false
        temperatureMin = Float.POSITIVE_INFINITY
        temperatureMax = Float.NEGATIVE_INFINITY
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
        this.startTime = min(startTime, stats.startTime)
        this.stopTime = max(stopTime, stats.stopTime)
        this.totalLength += stats.totalLength
        this.totalLengthMove += stats.totalLengthMove
        this.totalTime += stats.totalTime
        this.totalTimeMove += stats.totalTimeMove
        this.speedMax = max(speedMax, stats.speedMax)

        this.altitudeMax = max(altitudeMax, stats.altitudeMax)
        this.altitudeMin = min(altitudeMin, stats.altitudeMin)
        this.eleNeutralDistance += stats.eleNeutralDistance
        this.eleNeutralHeight += stats.eleNeutralHeight
        this.elePositiveDistance += stats.elePositiveDistance
        this.elePositiveHeight += stats.elePositiveHeight
        this.eleNegativeDistance += stats.eleNegativeDistance
        this.eleNegativeHeight += stats.eleNegativeHeight

        this.cadenceNumber += stats.cadenceNumber
        this.cadenceTime += stats.cadenceTime
        this.cadenceMax = max(this.cadenceMax, stats.cadenceMax)
        this.energy += stats.energy
        this.heartRateBeats += stats.heartRateBeats
        this.heartRateTime += stats.heartRateTime
        this.heartRateMax = max(this.heartRateMax, stats.heartRateMax)
        this.powerAvgSum += stats.powerAvgSum
        this.powerTime += stats.powerTime
        this.powerMax = max(this.powerMax, stats.powerMax)
        this.hasTemperature = this.hasTemperature || stats.hasTemperature
        this.temperatureMin = min(this.temperatureMin, stats.temperatureMin)
        this.temperatureMax = max(this.temperatureMax, stats.temperatureMax)
        this.numOfStrides += stats.numOfStrides
    }

    //*************************************************
    // STORABLE
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
            heartRateMax = dr.readInt()
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

        // V4
        if (version >= 4) {
            powerAvgSum = dr.readDouble()
            powerTime = dr.readLong()
            powerMax = dr.readInt()
            hasTemperature = dr.readBoolean()
            temperatureMin = dr.readFloat()
            temperatureMax = dr.readFloat()
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
        dw.writeInt(heartRateMax)
        dw.writeInt(energy)

        // V2
        dw.writeDouble(heartRateBeats)
        dw.writeDouble(cadenceNumber)
        dw.writeLong(cadenceTime)
        dw.writeInt(cadenceMax)

        // V3
        dw.writeInt(numOfStrides)

        // V4
        dw.writeDouble(powerAvgSum)
        dw.writeLong(powerTime)
        dw.writeInt(powerMax)
        dw.writeBoolean(hasTemperature)
        dw.writeFloat(temperatureMin)
        dw.writeFloat(temperatureMax)
    }
}