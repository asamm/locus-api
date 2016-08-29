package locus.api.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import locus.api.objects.Storable;

@SuppressWarnings("PointlessBitwiseExpression")
public class DataWriterBigEndian {
   
	/**
     * The buffer where data is stored.
     */
	private byte mBuf[];

    /**
     * The number of valid bytes in the buffer.
     */
    private int mCount;
    /**
     * 
     */
    private int mCurrentPos;
    /**
     * 
     */
    private int mSavedPos;
    
    /**
     * Creates a new data array output stream. The buffer capacity is
     * initially 32 bytes, though its size increases if necessary.
     */
    public DataWriterBigEndian() {
        this(256);
    }
    
    /**
     * Creates a new data array output stream, with a buffer capacity of
     * the specified size, in bytes.
     *
     * @param capacity the initial size.
     * @exception  IllegalArgumentException if size is negative.
     */
	public DataWriterBigEndian(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Negative initial size: " + capacity);
        }
        mBuf = new byte[capacity];
        reset();
	}
	
	/**
	 * Resets the <code>count</code> field of this byte array output
	 * stream to zero, so that all currently accumulated output in the
	 * output stream is discarded. The output stream can be used again,
	 * reusing the already allocated buffer space.
	 *
	 * @see     java.io.ByteArrayInputStream#count
	 */
	public synchronized void reset() {
		mCount = 0;
		mCurrentPos = 0;
		mSavedPos = 0;
	}
	

    /**
     * Increases the capacity if necessary to ensure that it can hold
     * at least the number of elements specified by the minimum
     * capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     * @throws OutOfMemoryError if {@code minCapacity < 0}.  This is
     * interpreted as a request for the unsatisfiably large capacity
     * {@code (long) Integer.MAX_VALUE + (minCapacity - Integer.MAX_VALUE)}.
     */
    private void ensureCapacity(int minCapacity) {
        if (minCapacity - mBuf.length > 0) {
            grow(minCapacity);
        }
    }
    
    /**
     * Increases the capacity to ensure that it can hold at least the
     * number of elements specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     */
    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = mBuf.length;
        int newCapacity = oldCapacity << 1;
        if (newCapacity - minCapacity < 0) {
            newCapacity = minCapacity;
        }

        if (newCapacity < 0) {
            if (minCapacity < 0) {// overflow
                throw new OutOfMemoryError();
            }
            newCapacity = Integer.MAX_VALUE;
        }
        mBuf = Utils.copyOf(mBuf, newCapacity);
    }
    
    private void setNewPositions(int bytesWrote) {
    	if ((mCurrentPos + bytesWrote) < mCount) {
    		// we are somewhere in the middle, only position moves
    		mCurrentPos += bytesWrote;
    	} else {
    		// we wrote to end, or now we are at the end
    		mCurrentPos += bytesWrote;
    		mCount = mCurrentPos;
    	}
    }

    // WORK WITH POSITION
    
    /**
     * Save current position. 
     */
	public void storePosition() {
		mSavedPos = mCurrentPos;
	}
	
	public void restorePosition() {
		mCurrentPos = mSavedPos;
	}
	
	public void moveTo(int index) {
		// check index
		if (index < 0 || index > mCount) {
			throw new IllegalArgumentException(
					"Invalid move index:" + index + ", count:" + mCount);
		}
		
		// set current location to index
		mCurrentPos = index;
	}
    
	// WRITE FUNCTIONS
	
    private byte mWriteBuffer[] = new byte[8];
    
    /**
     * Writes the specified byte to this byte array output stream.
     *
     * @param   b   the byte to be written.
     */
    public synchronized void write(int b) {
        ensureCapacity(mCurrentPos + 1);
        mBuf[mCurrentPos] = (byte) b;
        setNewPositions(1);
    }
    
    public synchronized void write(byte b[]) {
    	write(b, 0, b.length);
    }
    
    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this byte array output stream.
     *
     * @param   b     the data.
     * @param   off   the start offset in the data.
     * @param   len   the number of bytes to write.
     */
    public synchronized void write(byte b[], int off, int len) {
        if ((off < 0) || (off > b.length) || (len < 0) ||
        		((off + len) - b.length > 0)) {
        	throw new IndexOutOfBoundsException();
        }
        ensureCapacity(mCurrentPos + len);
        System.arraycopy(b, off, mBuf, mCurrentPos, len);
        setNewPositions(len);
    }
    
    /**
     * Writes a <code>boolean</code> to the underlying output stream as
     * a 1-byte value. The value <code>true</code> is written out as the
     * value <code>(byte)1</code>; the value <code>false</code> is
     * written out as the value <code>(byte)0</code>. If no exception is
     * thrown, the counter <code>written</code> is incremented by
     * <code>1</code>.
     *
     * @param      v   a <code>boolean</code> value to be written.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterOutputStream#out
     */
    public final void writeBoolean(boolean v) throws IOException {
        write(v ? 1 : 0);
    }
    
    /**
     * Writes a <code>short</code> to the underlying output stream as two
     * bytes, high byte first. If no exception is thrown, the counter
     * <code>written</code> is incremented by <code>2</code>.
     *
     * @param      v   a <code>short</code> to be written.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterOutputStream#out
     */
    public final void writeShort(int v) throws IOException {
        write((v >>> 8) & 0xFF);
        write((v >>> 0) & 0xFF);
    }
    
    /**
     * Writes an <code>int</code> to the underlying output stream as four
     * bytes, high byte first. If no exception is thrown, the counter
     * <code>written</code> is incremented by <code>4</code>.
     *
     * @param      v   an <code>int</code> to be written.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterOutputStream#out
     */
    public final void writeInt(int v) throws IOException {
        mWriteBuffer[0] = (byte) ((v >>> 24) & 0xFF);
        mWriteBuffer[1] = (byte) ((v >>> 16) & 0xFF);
        mWriteBuffer[2] = (byte) ((v >>>  8) & 0xFF);
        mWriteBuffer[3] = (byte) ((v >>>  0) & 0xFF);
        write(mWriteBuffer, 0, 4);
    }
    
    /**
     * Writes a <code>long</code> to the underlying output stream as eight
     * bytes, high byte first. In no exception is thrown, the counter
     * <code>written</code> is incremented by <code>8</code>.
     *
     * @param      v   a <code>long</code> to be written.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterOutputStream#out
     */
    public final void writeLong(long v) throws IOException {
    	mWriteBuffer[0] = (byte)(v >>> 56);
        mWriteBuffer[1] = (byte)(v >>> 48);
        mWriteBuffer[2] = (byte)(v >>> 40);
        mWriteBuffer[3] = (byte)(v >>> 32);
        mWriteBuffer[4] = (byte)(v >>> 24);
        mWriteBuffer[5] = (byte)(v >>> 16);
        mWriteBuffer[6] = (byte)(v >>>  8);
        mWriteBuffer[7] = (byte)(v >>>  0);
        write(mWriteBuffer, 0, 8);
    }
    
    /**
     * Converts the float argument to an <code>int</code> using the
     * <code>floatToIntBits</code> method in class <code>Float</code>,
     * and then writes that <code>int</code> value to the underlying
     * output stream as a 4-byte quantity, high byte first. If no
     * exception is thrown, the counter <code>written</code> is
     * incremented by <code>4</code>.
     *
     * @param      v   a <code>float</code> value to be written.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterOutputStream#out
     * @see        java.lang.Float#floatToIntBits(float)
     */
    public final void writeFloat(float v) throws IOException {
        writeInt(Float.floatToIntBits(v));
    }

    /**
     * Converts the double argument to a <code>long</code> using the
     * <code>doubleToLongBits</code> method in class <code>Double</code>,
     * and then writes that <code>long</code> value to the underlying
     * output stream as an 8-byte quantity, high byte first. If no
     * exception is thrown, the counter <code>written</code> is
     * incremented by <code>8</code>.
     *
     * @param      v   a <code>double</code> value to be written.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterOutputStream#out
     * @see        java.lang.Double#doubleToLongBits(double)
     */
    public final void writeDouble(double v) throws IOException {
        writeLong(Double.doubleToLongBits(v));
    }
    
	public final void writeString(String string) throws IOException {
		if (string == null || string.length() == 0) {
			writeInt(0);
		} else {
			byte[] bytes = string.getBytes("UTF-8");
			writeInt(bytes.length);
			write(bytes, 0, bytes.length);
		}
	}
	
	@Deprecated
	public final void writeStringDos(String string) throws IOException {
		if (string == null || string.length() == 0) {
			writeShort(0);
		} else {
			byte[] bytes = string.getBytes("UTF-8");
			writeShort(bytes.length);
			write(bytes, 0, bytes.length);
		}
	}
	
	public final void writeStorable(Storable obj) throws IOException {
		obj.write(this);
	}
	
	// LIST TOOLS
	
	public void writeListString(List<String> objs) throws IOException {
		// write '0' if no data are available
		if (objs == null || objs.size() == 0) {
			writeInt(0);
			return;
		}
		
		// write data. Count first
		int size = objs.size();
		writeInt(size);

		// write objects
		for (int i = 0, n = objs.size(); i < n; i++) {
			writeString(objs.get(i));
		}
	}
	
	public void writeListStorable(List<? extends Storable> objs) throws IOException {
		// get size of list
		int size;
		if (objs == null) {
			size = 0;
		} else {
			size = objs.size();
		}

		// write size of list
		writeInt(size);
		if (size == 0) {
			return;
		}

		// write objects
		for (int i = 0, n = objs.size(); i < n; i++) {
			objs.get(i).write(this);
		}
	}

	// VARIOUS TOOLS
	
    /**
     * Writes the complete contents of this byte array output stream to
     * the specified output stream argument, as if by calling the output
     * stream's write method using <code>out.write(buf, 0, count)</code>.
     *
     * @param      out   the output stream to which to write the data.
     * @exception  IOException  if an I/O error occurs.
     */
    public synchronized void writeTo(OutputStream out) throws IOException {
        out.write(mBuf, 0, mCount);
    }

    /**
     * Creates a newly allocated byte array. Its size is the current
     * size of this output stream and the valid contents of the buffer
     * have been copied into it.
     *
     * @return  the current contents of this output stream, as a byte array.
     * @see     java.io.ByteArrayOutputStream#size()
     */
    public synchronized byte toByteArray()[] {
        return Utils.copyOf(mBuf, mCount);
    }

    /**
     * Returns the current size of the buffer.
     *
     * @return  the value of the <code>count</code> field, which is the number
     *          of valid bytes in this output stream.
     * @see     java.io.ByteArrayOutputStream#count
     */
    public synchronized int size() {
        return mCount;
    }
}
