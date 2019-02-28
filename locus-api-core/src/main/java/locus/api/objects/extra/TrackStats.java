package locus.api.objects.extra;

import java.io.IOException;

import locus.api.objects.Storable;
import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;

/**
 * Created by menion on 23. 6. 2014.
 * Asamm Software, s. r. o.
 */
public class TrackStats extends Storable {

    // number of points (useful when points itself are not loaded)
    private int mNumOfPoints;
    // track start time (time of first point)
    private long mStartTime;
    // track stop time (time of last point)
    private long mStopTime;

    // total length of done route
    private float mTotalLength;
    // total track distance with speed
    private float mTotalLengthMove;
    // total time of route
    private long mTotalTime;
    // total track time with speed
    private long mTotalTimeMove;
    // maximal speed of this route
    private float mSpeedMax;

    // maximum altitude on track
    private float mAltitudeMax;
    // minimum altitude on track
    private float mAltitudeMin;

    // neutral grade (distance)
    private float mEleNeutralDistance;
    // neutral grade (elevation)
    private float mEleNeutralHeight;
    // positive grade (distance)
    private float mElePositiveDistance;
    // positive grade (elevation)
    private float mElePositiveHeight;
    // negative grade (distance)
    private float mEleNegativeDistance;
    // negative grade (elevation)
    private float mEleNegativeHeight;
    // total grade (distance)
    @Deprecated
    private float mEleTotalAbsDistance;
    // total grade (elevation)
    @Deprecated
    private float mEleTotalAbsHeight;

    // number of measured beats
    private double mHeartRateBeats;
    // time during which were beats measured
    private long mHeartRateTime;
    // maximum HRM value
    private int mHeartRateMax;

    // number of measured cadence revolutions
    private double mCadenceNumber;
    // time during which were cadence measured
    private long mCadenceTime;
    // maximum cadence value
    private int mCadenceMax;

    // burned energy (in joule)
    private int mEnergy;

    // number of steps
    private int mNumOfStrides;

    /**
     * Default empty constructor.
     */
    public TrackStats() {
        mNumOfPoints = 0;
        mStartTime = -1L;
        mStopTime = -1L;

        // other variables
        resetStatistics();
    }

    //*************************************************
    // GETTERS & SETTERS
    //*************************************************

    // NUMBER OF POINTS

    /**
     * Number of points in track.
     *
     * @return number of trackpoints
     */
    public int getNumOfPoints() {
        return mNumOfPoints;
    }

    /**
     * Set number of trackpoints in current track.
     *
     * @param numOfPoints number of trackpoints
     */
    public void setNumOfPoints(int numOfPoints) {
        this.mNumOfPoints = numOfPoints;
    }

    // START TIME

    /**
     * Get time when current track started.
     *
     * @return time of start (in ms)
     */
    public long getStartTime() {
        return mStartTime;
    }

    /**
     * Set new value to start time parameter.
     *
     * @param startTime time when track started (in ms)
     */
    public void setStartTime(long startTime) {
        this.mStartTime = startTime;
    }

    // STOP TIME

    /**
     * Get current defined stop time of track.
     *
     * @return stop time
     */
    public long getStopTime() {
        return mStopTime;
    }

    /**
     * Set new time when track record was stopped (time of last trackpoint).
     *
     * @param stopTime stop time
     */
    public void setStopTime(long stopTime) {
        this.mStopTime = stopTime;
    }

    // TOTAL LENGTH

    public float getTotalLength() {
        return mTotalLength;
    }

    public void setTotalLength(float totalLength) {
        this.mTotalLength = totalLength;
    }

    public void addTotalLength(float add) {
        this.mTotalLength += add;
    }

    // TOTAL LENGTH (MOVE)

    public float getTotalLengthMove() {
        return mTotalLengthMove;
    }

    public void setTotalLengthMove(float totalLengthMove) {
        this.mTotalLengthMove = totalLengthMove;
    }

    public void addTotalLengthMove(float add) {
        this.mTotalLengthMove += add;
    }

    // TOTAL TIME

    public long getTotalTime() {
        return mTotalTime;
    }

    public void addTotalTime(long add) {
        this.mTotalTime += Math.abs(add);
    }

    public void setTotalTime(long totalTime) {
        this.mTotalTime = Math.abs(totalTime);
    }

    // TOTAL TIME (MOVE)

    public long getTotalTimeMove() {
        return mTotalTimeMove;
    }

    public void setTotalTimeMove(long totalTimeMove) {
        this.mTotalTimeMove = Math.abs(totalTimeMove);
    }

    public void addTotalTimeMove(long add) {
        this.mTotalTimeMove += Math.abs(add);
    }

    // SPEED MAX

    /**
     * Get maximal speed value received from GPS.
     *
     * @return maximal speed value (in m/s)
     */
    public float getSpeedMax() {
        return mSpeedMax;
    }

    /**
     * Set maximal speed value.
     *
     * @param speedMax max. speed value (in m/s)
     */
    public void setSpeedMax(float speedMax) {
        this.mSpeedMax = speedMax;
    }

    // ALTITUDE MAX

    public float getAltitudeMax() {
        return mAltitudeMax;
    }

    public void setAltitudeMax(float altitudeMax) {
        this.mAltitudeMax = altitudeMax;
    }

    // ALTITUDE MIN

    public float getAltitudeMin() {
        return mAltitudeMin;
    }

    public void setAltitudeMin(float altitudeMin) {
        this.mAltitudeMin = altitudeMin;
    }

    // ELEVATION NEUTRAL - DISTANCE

    public float getEleNeutralDistance() {
        return mEleNeutralDistance;
    }

    public void setEleNeutralDistance(float eleNeutralDistance) {
        this.mEleNeutralDistance = eleNeutralDistance;
    }

    public void addEleNeutralDistance(float add) {
        this.mEleNeutralDistance += add;
    }

    // ELEVATION NEUTRAL - HEIGHT

    public float getEleNeutralHeight() {
        return mEleNeutralHeight;
    }

    public void setEleNeutralHeight(float eleNeutralHeight) {
        this.mEleNeutralHeight = eleNeutralHeight;
    }

    /**
     * Add elevation value for "neutral height"
     *
     * @param add value increment
     */
    public void addEleNeutralHeight(float add) {
        this.mEleNeutralHeight += add;
    }

    // ELEVATION POSITIVE - DISTANCE

    public float getElePositiveDistance() {
        return mElePositiveDistance;
    }

    public void setElePositiveDistance(float elePositiveDistance) {
        this.mElePositiveDistance = elePositiveDistance;
    }

    public void addElePositiveDistance(float add) {
        this.mElePositiveDistance += add;
    }

    // ELEVATION POSITIVE - HEIGHT

    public float getElePositiveHeight() {
        return mElePositiveHeight;
    }

    public void setElePositiveHeight(float elePositiveHeight) {
        this.mElePositiveHeight = elePositiveHeight;
    }

    /**
     * Add elevation value for "positive height"
     *
     * @param add value increment
     */
    public void addElePositiveHeight(float add) {
        this.mElePositiveHeight += add;
    }

    // ELEVATION NEGATIVE - DISTANCE

    public float getEleNegativeDistance() {
        return mEleNegativeDistance;
    }

    public void setEleNegativeDistance(float eleNegativeDistance) {
        this.mEleNegativeDistance = eleNegativeDistance;
    }

    public void addEleNegativeDistance(float add) {
        this.mEleNegativeDistance += add;
    }

    // ELEVATION NEGATIVE - HEIGHT

    public float getEleNegativeHeight() {
        return mEleNegativeHeight;
    }

    public void setEleNegativeHeight(float eleNegativeHeight) {
        this.mEleNegativeHeight = eleNegativeHeight;
    }

    /**
     * Add elevation value for "negative height"
     *
     * @param add value increment
     */
    public void addEleNegativeHeight(float add) {
        this.mEleNegativeHeight += add;
    }

    // ELEVATION TOTAL - DISTANCE

    @Deprecated
    public float getEleTotalAbsDistance() {
        return mEleTotalAbsDistance;
    }

    public void setEleTotalAbsDistance(float eleTotalAbsDistance) {
        this.mEleTotalAbsDistance = eleTotalAbsDistance;
    }

    public void addEleTotalAbsDistance(float add) {
        this.mEleTotalAbsDistance += add;
    }

    // ELEVATION TOTAL - HEIGHT

    @Deprecated
    public float getEleTotalAbsHeight() {
        return mEleTotalAbsHeight;
    }

    public void setEleTotalAbsHeight(float eleTotalAbsHeight) {
        this.mEleTotalAbsHeight = eleTotalAbsHeight;
    }

    public void addEleTotalAbsHeight(float add) {
        this.mEleTotalAbsHeight += add;
    }

    // HEART RATE VALUES

    /**
     * Get average value of heart rate.
     *
     * @return average value (in BPM)
     */
    public int getHrmAverage() {
        if (mHeartRateBeats > 0 && mHeartRateTime > 0L) {
            double minutes = mHeartRateTime / (60.0 * 1000.0);
            return (int) (mHeartRateBeats / minutes);
        } else {
            return 0;
        }
    }

    /**
     * Get maximal value of heart rate.
     *
     * @return maximal heart rate value (in BPM)
     */
    public int getHrmMax() {
        return mHeartRateMax;
    }

    /**
     * Add measured heart rate values.
     *
     * @param hrmMeasured   measured heart rate value (in bpm)
     * @param hrmAvgSegment average heart rate value in segment (in bpm)
     * @param measureTime   time of segment (in millis)
     */
    public void addHeartRateMeasure(int hrmMeasured, int hrmAvgSegment, long measureTime) {
        // store values
        double inMinutes = measureTime * 1.0 / (60 * 1000);
        double numOfBeats = hrmAvgSegment * inMinutes;
        mHeartRateBeats += numOfBeats;
        mHeartRateTime += measureTime;

        // compute max. heart rate
        mHeartRateMax = Math.max(mHeartRateMax, hrmMeasured);
    }

    // CADENCE VALUES

    /**
     * Get average value of cadence.
     *
     * @return average cadence value (
     */
    public int getCadenceAverage() {
        if (mCadenceNumber > 0 && mCadenceTime > 0L) {
            double minutes = mCadenceTime / (60.0 * 1000.0);
            return (int) (mCadenceNumber / minutes);
        } else {
            return 0;
        }
    }

    /**
     * Get maximal value of heart rate.
     *
     * @return maximal heart rate value (in BPM)
     */
    public int getCadenceMax() {
        return mCadenceMax;
    }

    /**
     * Add measured cadence values.
     *
     * @param revMeasured   measured cadence value (in rpm)
     * @param revAvgSegment average revolutions value in segment (in rpm)
     * @param measureTime   time of segment (in millis)
     */
    public void addCadenceMeasure(int revMeasured, int revAvgSegment, long measureTime) {
        // store values
        double inMinutes = measureTime * 1.0 / (60 * 1000);
        double numOfRevolutions = revAvgSegment * inMinutes;
        mCadenceNumber += numOfRevolutions;
        mCadenceTime += measureTime;

        // compute max. heart rate
        mCadenceMax = Math.max(mCadenceMax, revMeasured);
    }

    // ENERGY BURNED

    /**
     * Get burned energy.
     *
     * @return burned energy (in joule)
     */
    public int getEnergy() {
        return mEnergy;
    }

    /**
     * Add burned energy.
     *
     * @param energy burned energy (in joule)
     */
    public void addEnergy(int energy) {
        mEnergy += energy;
    }

    // STRIDES

    /**
     * Get total number of strides.
     *
     * @return number of strides
     */
    public int getNumOfStrides() {
        return mNumOfStrides;
    }

    /**
     * Set total number of strides.
     *
     * @param numOfStrides number of strides
     */
    public void setNumOfStrides(int numOfStrides) {
        mNumOfStrides = numOfStrides;
    }

    //*************************************************
    // OTHER TOOLS
    //*************************************************

    public double getTrackLength(boolean onlyWithMove) {
        if (onlyWithMove) {
            return mTotalLengthMove;
        } else {
            return mTotalLength;
        }
    }

    public long getTrackTime(boolean onlyWithMove) {
        if (onlyWithMove) {
            return mTotalTimeMove;
        } else {
            return mTotalTime;
        }
    }

    /**
     * Get average speed value for a track.
     *
     * @param onlyWithMove {@code true} to get AVG. speed only during movement
     * @return average speed in m/s
     */
    public float getSpeedAverage(boolean onlyWithMove) {
        float trackTime = getTrackTime(onlyWithMove) / 1000.0f;
        if (trackTime > 0) {
            return (float) (getTrackLength(onlyWithMove) / trackTime);
        } else {
            return 0.0f;
        }
    }

    /**
     * Check if track has some elevation values.
     *
     * @return {@code true} if track has elevation values
     */
    public boolean hasElevationValues() {
        return (mAltitudeMin != Float.POSITIVE_INFINITY && mAltitudeMin != 0.0)
                || (mAltitudeMax != Float.NEGATIVE_INFINITY && mAltitudeMax != 0.0);
    }

    /**
     * Reset all parameters of track statistics.
     */
    public void resetStatistics() {
        // basic statistics variables
        mTotalLength = 0.0f;
        mTotalLengthMove = 0.0f;
        mTotalTime = 0L;
        mTotalTimeMove = 0L;
        mSpeedMax = 0.0f;
        mHeartRateBeats = 0.0;
        mHeartRateTime = 0L;
        mHeartRateMax = 0;
        mCadenceNumber = 0.0;
        mCadenceTime = 0L;
        mCadenceMax = 0;
        mEnergy = 0;
        mNumOfStrides = 0;

        // reset also elevation values
        resetStatisticsAltitude();
    }

    /**
     * Reset only statistics related to elevation values.
     */
    public void resetStatisticsAltitude() {
        mAltitudeMax = Float.NEGATIVE_INFINITY;
        mAltitudeMin = Float.POSITIVE_INFINITY;

        // graph variables
        mEleNeutralDistance = 0.0f;
        mEleNeutralHeight = 0.0f;
        mElePositiveDistance = 0.0f;
        mElePositiveHeight = 0.0f;
        mEleNegativeDistance = 0.0f;
        mEleNegativeHeight = 0.0f;
        mEleTotalAbsDistance = 0.0f;
        mEleTotalAbsHeight = 0.0f;
    }

    /**
     * Function that allows to merge more statistics into one.
     *
     * @param stats second statistics that will be merged into this.
     */
    public void appendStatistics(TrackStats stats) {
        this.mNumOfPoints += stats.mNumOfPoints;
        this.mStartTime = Math.min(mStartTime, stats.mStartTime);
        this.mStopTime = Math.max(mStopTime, stats.mStopTime);
        this.mTotalLength += stats.mTotalLength;
        this.mTotalLengthMove += stats.mTotalLengthMove;
        this.mTotalTime += stats.mTotalTime;
        this.mTotalTimeMove += stats.mTotalTimeMove;
        this.mSpeedMax = Math.max(mSpeedMax, stats.mSpeedMax);
        this.mAltitudeMax = Math.max(mAltitudeMax, stats.mAltitudeMax);
        this.mAltitudeMin = Math.min(mAltitudeMin, stats.mAltitudeMin);

        this.mEleNeutralDistance += stats.mEleNeutralDistance;
        this.mEleNeutralHeight += stats.mEleNeutralHeight;
        this.mElePositiveDistance += stats.mElePositiveDistance;
        this.mElePositiveHeight += stats.mElePositiveHeight;
        this.mEleNegativeDistance += stats.mEleNegativeDistance;
        this.mEleNegativeHeight += stats.mEleNegativeHeight;
        this.mEleTotalAbsDistance += stats.mEleTotalAbsDistance;
        this.mEleTotalAbsHeight += stats.mEleTotalAbsHeight;

        this.mHeartRateBeats += stats.mHeartRateBeats;
        this.mHeartRateTime += stats.mHeartRateTime;
        this.mHeartRateMax = Math.max(this.mHeartRateMax, stats.mHeartRateMax);

        this.mCadenceNumber += stats.mCadenceNumber;
        this.mCadenceTime += stats.mCadenceTime;
        this.mCadenceMax = Math.max(this.mCadenceMax, stats.mCadenceMax);

        this.mEnergy += stats.mEnergy;
        this.mNumOfStrides += stats.mNumOfStrides;
    }

    //*************************************************
    // STORABLE PART
    //*************************************************

    @Override
    protected int getVersion() {
        return 3;
    }

    @Override
    protected void readObject(int version, DataReaderBigEndian dr) throws IOException {
        mNumOfPoints = dr.readInt();
        mStartTime = dr.readLong();
        mStopTime = dr.readLong();

        // basic variables
        mTotalLength = dr.readFloat();
        mTotalLengthMove = dr.readFloat();
        mTotalTime = dr.readLong();
        mTotalTimeMove = dr.readLong();
        mSpeedMax = dr.readFloat();

        // altitude values
        mAltitudeMax = dr.readFloat();
        mAltitudeMin = dr.readFloat();

        // elevation variables
        mEleNeutralDistance = dr.readFloat();
        mEleNeutralHeight = dr.readFloat();
        mElePositiveDistance = dr.readFloat();
        mElePositiveHeight = dr.readFloat();
        mEleNegativeDistance = dr.readFloat();
        mEleNegativeHeight = dr.readFloat();
        mEleTotalAbsDistance = dr.readFloat();
        mEleTotalAbsHeight = dr.readFloat();

        // V1

        if (version >= 1) {
            mHeartRateBeats = dr.readInt();
            mHeartRateTime = dr.readLong();
            mHeartRateMax = dr.readInt();
            mEnergy = dr.readInt();
        }

        // V2

        if (version >= 2) {
            mHeartRateBeats = dr.readDouble();
            mCadenceNumber = dr.readDouble();
            mCadenceTime = dr.readLong();
            mCadenceMax = dr.readInt();
        }

        // V3

        if (version >= 3) {
            mNumOfStrides = dr.readInt();
        }
    }

    @Override
    protected void writeObject(DataWriterBigEndian dw) throws IOException {
        dw.writeInt(mNumOfPoints);
        dw.writeLong(mStartTime);
        dw.writeLong(mStopTime);

        // basic variables
        dw.writeFloat(mTotalLength);
        dw.writeFloat(mTotalLengthMove);
        dw.writeLong(mTotalTime);
        dw.writeLong(mTotalTimeMove);
        dw.writeFloat(mSpeedMax);

        // altitude values
        dw.writeFloat(mAltitudeMax);
        dw.writeFloat(mAltitudeMin);

        // elevation variables
        dw.writeFloat(mEleNeutralDistance);
        dw.writeFloat(mEleNeutralHeight);
        dw.writeFloat(mElePositiveDistance);
        dw.writeFloat(mElePositiveHeight);
        dw.writeFloat(mEleNegativeDistance);
        dw.writeFloat(mEleNegativeHeight);
        dw.writeFloat(mEleTotalAbsDistance);
        dw.writeFloat(mEleTotalAbsHeight);

        // V1

        dw.writeInt((int) mHeartRateBeats);
        dw.writeLong(mHeartRateTime);
        dw.writeInt(mHeartRateMax);
        dw.writeInt(mEnergy);

        // V2

        dw.writeDouble(mHeartRateBeats);
        dw.writeDouble(mCadenceNumber);
        dw.writeLong(mCadenceTime);
        dw.writeInt(mCadenceMax);

        // V3

        dw.writeInt(mNumOfStrides);
    }
}
