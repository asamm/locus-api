package locus.api.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import locus.api.objects.Storable;

public class DataReaderBigEndian {

    // tag for logger
    private static final String TAG = "DataReaderBigEndian";

    // current position in buffer
    private int mPosition;
    // buffer with data
    private byte[] mBuffer;

    public DataReaderBigEndian(byte[] data) throws IOException {
        if (data == null) {
            throw new IOException("Invalid parameter");
        }
        this.mPosition = 0;
        this.mBuffer = data;
    }

    /**
     * Get length of current stream.
     *
     * @return length of stream
     */
    public long length() {
        return mBuffer.length;
    }

    /**
     * Get number of available bytes to read till end.
     *
     * @return number of available bytes
     */
    public int available() {
        return mBuffer.length - mPosition;
    }

    /**
     * Move cursor to certain position.
     *
     * @param pos position where to move
     */
    public void seek(int pos) {
        mPosition = pos;
    }

    // READ FUNCTIONS

    public byte readByte() {
        checkPosition(1);
        return mBuffer[mPosition - 1];
    }

    public byte[] readBytes(int count) {
        checkPosition(count);

        // create new temp array
        byte[] newData = new byte[count];
        System.arraycopy(mBuffer, mPosition - count, newData, 0, count);

        // return filled result
        return newData;
    }

    public void readBytes(byte[] data) {
        checkPosition(data.length);

        // create new temp array
        System.arraycopy(mBuffer, mPosition - data.length, data, 0, data.length);
    }

    public boolean readBoolean() {
        checkPosition(1);
        return mBuffer[mPosition - 1] != 0;
    }

    public short readShort() {
        checkPosition(2);
        return (short) ((mBuffer[mPosition - 2] & 0xff) << 8 |
                (mBuffer[mPosition - 1] & 0xff));
    }

    public int readInt() {
        checkPosition(4);
        return mBuffer[mPosition - 4] << 24 |
                (mBuffer[mPosition - 3] & 0xff) << 16 |
                (mBuffer[mPosition - 2] & 0xff) << 8 |
                (mBuffer[mPosition - 1] & 0xff);
    }

    public long readLong() {
        checkPosition(8);
        return (mBuffer[mPosition - 8] & 0xffL) << 56 |
                (mBuffer[mPosition - 7] & 0xffL) << 48 |
                (mBuffer[mPosition - 6] & 0xffL) << 40 |
                (mBuffer[mPosition - 5] & 0xffL) << 32 |
                (mBuffer[mPosition - 4] & 0xffL) << 24 |
                (mBuffer[mPosition - 3] & 0xffL) << 16 |
                (mBuffer[mPosition - 2] & 0xffL) << 8 |
                (mBuffer[mPosition - 1] & 0xffL);
    }

    public final float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    public final double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    public String readString() throws IOException {
        int textLength = readInt();
        if (textLength == 0) {
            return "";
        } else {
            checkPosition(textLength);
            return new String(mBuffer,
                    mPosition - textLength, textLength, "UTF-8");
        }
    }

    /**
     * This method simulate method from DataInputStream. Useful only for older
     * Storable instances, that wrote text with method:
     * <code>dos.writeUTF(String text);</code>
     */
    @Deprecated
    public String readStringDis() throws IOException {
        int textLength = readShort();
        if (textLength == 0) {
            return "";
        } else {
            checkPosition(textLength);
            return new String(mBuffer,
                    mPosition - textLength, textLength, "UTF-8");
        }
    }

    /**
     * Read Storable object.
     *
     * @param claz class parameter
     * @param <E>  class type
     * @return loaded Storable class
     */
    public <E extends Storable> E readStorable(Class<E> claz)
            throws InstantiationException, IllegalAccessException, IOException {
        return Storable.read(claz, this);
    }

    // LIST TOOLS

    public List<String> readListString() throws IOException {
        // prepare container
        List<String> objs = new ArrayList<>();

        // read size
        int count = readInt();
        if (count == 0) {
            return objs;
        }

        // read Strings
        for (int i = 0; i < count; i++) {
            objs.add(readString());
        }
        return objs;
    }

    public <E extends Storable> List<E> readListStorable(Class<E> claz)
            throws IOException {
        // prepare container
        List<E> objs = new ArrayList<>();

        // read size
        int count = readInt();
        if (count == 0) {
            return objs;
        }

        // read locations
        for (int i = 0; i < count; i++) {
            //noinspection TryWithIdenticalCatches
            try {
                E item = claz.newInstance();
                item.read(this);
                objs.add(item);
            } catch (InstantiationException e) {
                Logger.logE(TAG, "readList(" + claz + ")", e);
            } catch (IllegalAccessException e) {
                Logger.logE(TAG, "readList(" + claz + ")", e);
            }
        }
        return objs;
    }

    // PRIVATE TOOLS

    private void checkPosition(int increment) {
        mPosition += increment;
        if (mPosition > mBuffer.length) {
            throw new ArrayIndexOutOfBoundsException("Invalid position for data load. " +
                    "Current:" + mPosition + ", " +
                    "length:" + mBuffer.length + ", " +
                    "increment:" + increment);
        }
    }
}
