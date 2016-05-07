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
    private int numOfPoints;
    // track start time (time of first point)
    private long startTime;
    // track stop time (time of last point)
    private long stopTime;

    // total length of done route
    private float totalLength;
    // total track distance with speed
    private float totalLengthMove;
    // total time of route
    private long totalTime;
    // total track time with speed
    private long totalTimeMove;
    // maximal speed of this route
    private float speedMax;

    // maximum altitude on track
    private float altitudeMax;
    // minimum altitude on track
    private float altitudeMin;

    // neutral grade (distance)
    private float eleNeutralDistance;
    // neutral grade (elevation)
    private float eleNeutralHeight;
    // positive grade (distance)
    private float elePositiveDistance;
    // positive grade (elevation)
    private float elePositiveHeight;
    // negative grade (distance)
    private float eleNegativeDistance;
    // negative grade (elevation)
    private float eleNegativeHeight;
    // total grade (distance)
    private float eleTotalAbsDistance;
    // total grade (elevation)
    private float eleTotalAbsHeight;

    public TrackStats() {
        super();
    }

    public TrackStats(DataReaderBigEndian dr) throws IOException {
        super(dr);
    }

    /**************************************************/
    // GETTERS & SETTERS
    /**************************************************/

    // NUMBER OF POINTS

    public int getNumOfPoints() {
        return numOfPoints;
    }

    public void setNumOfPoints(int numOfPoints) {
        this.numOfPoints = numOfPoints;
    }

    // START TIME

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    // STOP TIME

    public long getStopTime() {
        return stopTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    // TOTAL LENGTH

    public float getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(float totalLength) {
        this.totalLength = totalLength;
    }

    public void addTotalLength(float add) {
        this.totalLength += add;
    }

    // TOTAL LENGTH (MOVE)

    public float getTotalLengthMove() {
        return totalLengthMove;
    }

    public void setTotalLengthMove(float totalLengthMove) {
        this.totalLengthMove = totalLengthMove;
    }

    public void addTotalLengthMove(float add) {
        this.totalLengthMove += add;
    }

    // TOTAL TIME

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = Math.abs(totalTime);
    }

    public void addTotalTime(long add) {
        this.totalTime += Math.abs(add);
    }

    // TOTAL TIME (MOVE)

    public long getTotalTimeMove() {
        return totalTimeMove;
    }

    public void setTotalTimeMove(long totalTimeMove) {
        this.totalTimeMove = Math.abs(totalTimeMove);
    }

    public void addTotalTimeMove(long add) {
        this.totalTimeMove += Math.abs(add);
    }

    // SPEED MAX

    public float getSpeedMax() {
        return speedMax;
    }

    public void setSpeedMax(float speedMax) {
        this.speedMax = speedMax;
    }

    // ALTITUDE MAX

    public float getAltitudeMax() {
        return altitudeMax;
    }

    public void setAltitudeMax(float altitudeMax) {
        this.altitudeMax = altitudeMax;
    }

    // ALTITUDE MIN

    public float getAltitudeMin() {
        return altitudeMin;
    }

    public void setAltitudeMin(float altitudeMin) {
        this.altitudeMin = altitudeMin;
    }

    // ELEVATION NEUTRAL - DISTANCE

    public float getEleNeutralDistance() {
        return eleNeutralDistance;
    }

    public void setEleNeutralDistance(float eleNeutralDistance) {
        this.eleNeutralDistance = eleNeutralDistance;
    }

    public void addEleNeutralDistance(float add) {
        this.eleNeutralDistance += add;
    }

    // ELEVATION NEUTRAL - HEIGHT

    public float getEleNeutralHeight() {
        return eleNeutralHeight;
    }

    public void setEleNeutralHeight(float eleNeutralHeight) {
        this.eleNeutralHeight = eleNeutralHeight;
    }

    /**
     * Add elevation value for "neutral height"
     * @param add value increment
     */
    public void addEleNeutralHeight(float add) {
        this.eleNeutralHeight += add;
    }

    // ELEVATION POSITIVE - DISTANCE

    public float getElePositiveDistance() {
        return elePositiveDistance;
    }

    public void setElePositiveDistance(float elePositiveDistance) {
        this.elePositiveDistance = elePositiveDistance;
    }

    public void addElePositiveDistance(float add) {
        this.elePositiveDistance += add;
    }

    // ELEVATION POSITIVE - HEIGHT

    public float getElePositiveHeight() {
        return elePositiveHeight;
    }

    public void setElePositiveHeight(float elePositiveHeight) {
        this.elePositiveHeight = elePositiveHeight;
    }

    /**
     * Add elevation value for "positive height"
     * @param add value increment
     */
    public void addElePositiveHeight(float add) {
        this.elePositiveHeight += add;
    }

    // ELEVATION NEGATIVE - DISTANCE

    public float getEleNegativeDistance() {
        return eleNegativeDistance;
    }

    public void setEleNegativeDistance(float eleNegativeDistance) {
        this.eleNegativeDistance = eleNegativeDistance;
    }

    public void addEleNegativeDistance(float add) {
        this.eleNegativeDistance += add;
    }

    // ELEVATION NEGATIVE - HEIGHT

    public float getEleNegativeHeight() {
        return eleNegativeHeight;
    }

    public void setEleNegativeHeight(float eleNegativeHeight) {
        this.eleNegativeHeight = eleNegativeHeight;
    }

    /**
     * Add elevation value for "negative height"
     * @param add value increment
     */
    public void addEleNegativeHeight(float add) {
        this.eleNegativeHeight += add;
    }

    // ELEVATION TOTAL - DISTANCE

    public float getEleTotalAbsDistance() {
        return eleTotalAbsDistance;
    }

    public void setEleTotalAbsDistance(float eleTotalAbsDistance) {
        this.eleTotalAbsDistance = eleTotalAbsDistance;
    }

    public void addEleTotalAbsDistance(float add) {
        this.eleTotalAbsDistance += add;
    }

    // ELEVATION TOTAL - HEIGHT

    public float getEleTotalAbsHeight() {
        return eleTotalAbsHeight;
    }

    public void setEleTotalAbsHeight(float eleTotalAbsHeight) {
        this.eleTotalAbsHeight = eleTotalAbsHeight;
    }

    public void addEleTotalAbsHeight(float add) {
        this.eleTotalAbsHeight += add;
    }

    /**************************************************/
    // OTHER TOOLS
    /**************************************************/

    public double getTrackLength(boolean onlyWithMove) {
        if (onlyWithMove) {
            return totalLengthMove;
        } else {
            return totalLength;
        }
    }

    public long getTrackTime(boolean onlyWithMove) {
        if (onlyWithMove) {
            return totalTimeMove;
        } else {
            return totalTime;
        }
    }

	/**
	 * Get average speed value for a track.
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

    public void resetStatistics() {
        // basic statistics variables
        totalLength = 0.0f;
        totalLengthMove = 0.0f;
        totalTime = 0L;
        totalTimeMove = 0L;
        speedMax = 0.0f;
        resetStatisticsAltitude();
    }

    public void resetStatisticsAltitude() {
        altitudeMax = Float.NEGATIVE_INFINITY;
        altitudeMin = Float.POSITIVE_INFINITY;

        // graph variables
        eleNeutralDistance = 0.0f;
        eleNeutralHeight = 0.0f;
        elePositiveDistance = 0.0f;
        elePositiveHeight = 0.0f;
        eleNegativeDistance = 0.0f;
        eleNegativeHeight = 0.0f;
        eleTotalAbsDistance = 0.0f;
        eleTotalAbsHeight = 0.0f;
    }

	/**
	 * Function that allows to merge more statistics into one.
	 * @param stats second statistics that will be merged into this.
	 */
	public void appendStatistics(TrackStats stats) {
		this.numOfPoints += stats.numOfPoints;
		this.startTime = Math.min(startTime, stats.startTime);
		this.stopTime = Math.max(stopTime, stats.stopTime);
		this.totalLength += stats.totalLength;
		this.totalLengthMove += stats.totalLengthMove;
		this.totalTime += stats.totalTime;
		this.totalTimeMove += stats.totalTimeMove;
		this.speedMax = Math.max(speedMax, stats.speedMax);
		this.altitudeMax = Math.max(altitudeMax, stats.altitudeMax);
		this.altitudeMin = Math.min(altitudeMin, stats.altitudeMin);

		this.eleNeutralDistance += stats.eleNeutralDistance;
		this.eleNeutralHeight += stats.eleNeutralHeight;
		this.elePositiveDistance += stats.elePositiveDistance;
		this.elePositiveHeight += stats.elePositiveHeight;
		this.eleNegativeDistance += stats.eleNegativeDistance;
		this.eleNegativeHeight += stats.eleNegativeHeight;
		this.eleTotalAbsDistance += stats.eleTotalAbsDistance;
		this.eleTotalAbsHeight += stats.eleTotalAbsHeight;
	}

    /**************************************************/
    // STORABLE PART
    /**************************************************/

    @Override
    protected int getVersion() {
        return 0;
    }

    @Override
    public void reset() {
        numOfPoints = 0;
        startTime = -1L;
        stopTime = -1L;

        // other variables
        resetStatistics();
    }

    @Override
    protected void readObject(int version, DataReaderBigEndian dr) throws IOException {
        numOfPoints = dr.readInt();
        startTime = dr.readLong();
        stopTime = dr.readLong();

        // basic variables
        totalLength = dr.readFloat();
        totalLengthMove = dr.readFloat();
        totalTime = dr.readLong();
        totalTimeMove = dr.readLong();
        speedMax = dr.readFloat();

        // altitude values
        altitudeMax = dr.readFloat();
        altitudeMin = dr.readFloat();

        // elevation variables
        eleNeutralDistance = dr.readFloat();
        eleNeutralHeight = dr.readFloat();
        elePositiveDistance = dr.readFloat();
        elePositiveHeight = dr.readFloat();
        eleNegativeDistance = dr.readFloat();
        eleNegativeHeight = dr.readFloat();
        eleTotalAbsDistance = dr.readFloat();
        eleTotalAbsHeight = dr.readFloat();
    }

    @Override
    protected void writeObject(DataWriterBigEndian dw) throws IOException {
        dw.writeInt(numOfPoints);
        dw.writeLong(startTime);
        dw.writeLong(stopTime);

        // basic variables
        dw.writeFloat(totalLength);
        dw.writeFloat(totalLengthMove);
        dw.writeLong(totalTime);
        dw.writeLong(totalTimeMove);
        dw.writeFloat(speedMax);

        // altitude values
        dw.writeFloat(altitudeMax);
        dw.writeFloat(altitudeMin);

        // elevation variables
        dw.writeFloat(eleNeutralDistance);
        dw.writeFloat(eleNeutralHeight);
        dw.writeFloat(elePositiveDistance);
        dw.writeFloat(elePositiveHeight);
        dw.writeFloat(eleNegativeDistance);
        dw.writeFloat(eleNegativeHeight);
        dw.writeFloat(eleTotalAbsDistance);
        dw.writeFloat(eleTotalAbsHeight);
    }
}
