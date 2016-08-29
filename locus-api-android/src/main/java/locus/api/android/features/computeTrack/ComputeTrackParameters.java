package locus.api.android.features.computeTrack;

import java.io.IOException;

import locus.api.objects.Storable;
import locus.api.objects.extra.ExtraData;
import locus.api.objects.extra.Location;
import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;

/**
 * Created by menion on 10. 7. 2014.
 * Class is part of Locus project
 */
public class ComputeTrackParameters extends Storable {

    // type of track that should be compute
    private int mType;
    // <code>True</code> if user wants with navigation orders
    private boolean mComputeInstructions;
    // flag if parameters has defined starting direction
    private boolean mHasDirection;
    // direction value
    private float mDirection;
    // list of locations (via-points)
    private Location[] mLocs;

    /**
     * Empty constructor for 'Storable' class. Do not use directly
     */
    @SuppressWarnings("unused")
    public ComputeTrackParameters() {}

    public ComputeTrackParameters(int type, Location[] locs) {
        super();

        // set parameters
        this.mType = type;
        if (locs == null || locs.length < 2) {
            throw new IllegalArgumentException("'locs' parameter" +
                    " cannot be 'null' or smaller then 2");
        }
        this.mLocs = locs;
    }

    public ComputeTrackParameters(byte[] data) throws IOException {
        super(data);
    }

    public int getType() {
        return mType;
    }

    public boolean isComputeInstructions() {
        return mComputeInstructions;
    }

    public void setComputeInstructions(boolean computeInstructions) {
        this.mComputeInstructions = computeInstructions;
    }

    // STARTING DIRECTION

    /**
     * Check if direction is currently defined.
     * @return <code>true</code> if direction for first point is set
     */
    public boolean hasDirection() {
        return mHasDirection;
    }

    /**
     * Get direction angle of movement at first point.
     * @return define direction angle
     */
    public float getCurrentDirection() {
        return mDirection;
    }

    /**
     * Set direction angle of movement at first point in degress.
     * @param direction direction angle
     */
    public void setCurrentDirection(float direction) {
        this.mDirection = direction;
    }

    // LOCATIONS

    /**
     * Get list of defined via-points (location).
     * @return list of locations
     */
    public Location[] getLocations() {
        return mLocs;
    }

    /**************************************************/
    // STORABLE PART
    /**************************************************/

    @Override
    protected int getVersion() {
        return 1;
    }

    @Override
    public void reset() {
        mType = ExtraData.VALUE_RTE_TYPE_CAR;
        mComputeInstructions = true;
        mHasDirection = false;
        mDirection = 0;
        mLocs = new Location[0];
    }

    @Override
    protected void readObject(int version, DataReaderBigEndian dr) throws IOException {
        mType = dr.readInt();
        mComputeInstructions = dr.readBoolean();
        mDirection = dr.readFloat();
        mLocs = new Location[dr.readInt()];
        for (int i = 0, m = mLocs.length; i < m; i++) {
            mLocs[i] = new Location(dr);
        }

        // V1

        if (version >= 1) {
            mHasDirection = dr.readBoolean();
        }
    }

    @Override
    protected void writeObject(DataWriterBigEndian dw) throws IOException {
        dw.writeInt(mType);
        dw.writeBoolean(mComputeInstructions);
        dw.writeFloat(mDirection);
        dw.writeInt(mLocs.length);
        for (Location mLoc : mLocs) {
            dw.writeStorable(mLoc);
        }

        // V1

        dw.writeBoolean(mHasDirection);
    }
}
