/*
 * Copyright 2012, Asamm Software, s. r. o.
 *
 * This file is part of LocusAPI.
 *
 * LocusAPI is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * LocusAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the Lesser GNU General Public
 * License along with LocusAPI. If not, see
 * <http://www.gnu.org/licenses/lgpl.html/>.
 */
package locus.api.objects

import com.asamm.loggerV2.logE
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.*

/**
 * Default empty constructor.
 */
abstract class Storable {

    /**
     * Object version used for storing.
     */
    protected abstract fun getVersion(): Int

    /**
     * Create precise copy of current object.
     * Method is that object is stored into byte stream and then restored
     * as a new object.
     *
     * @return exact clone of this object
     */
    val copy: Storable
        @Throws(IOException::class, InstantiationException::class, IllegalAccessException::class)
        get() {
            return read(this.javaClass, DataReaderBigEndian(asBytes))
        }

    /**
     * Get whole object serialized into byte array.
     *
     * @return serialized object
     */
    val asBytes: ByteArray?
        get() {
            return try {
                DataWriterBigEndian().apply {
                    write(this)
                }.toByteArray()
            } catch (e: IOException) {
                logE(tag = TAG, ex = e) { "asBytes()" }
                null
            }
        }

    /*
     * Container for inner data
     */
    private class BodyContainer {

        // current item version
        var version: Int = 0

        // data in item
        var data: ByteArray? = null
    }

    //*************************************************
    // READ PART
    //*************************************************

    /**
     * Read content of certain item from byte array.
     *
     * @param data array with data
     */
    @Throws(IOException::class)
    fun read(data: ByteArray) {
        val dr = DataReaderBigEndian(data)
        read(dr)
    }

    /**
     * Read content of certain item from existing stream.
     *
     * @param dr stream to read for
     */
    @Throws(IOException::class)
    fun read(dr: DataReaderBigEndian) {
        // read header
        val bc = readHeader(dr)

        // read body
        readObject(bc.version, DataReaderBigEndian(bc.data))
    }

    /**
     * Read content of object from stream.
     *
     * @param input input stream
     */
    @Throws(IOException::class)
    fun read(input: DataInputStream) {
        // read header
        val bc = readHeader(input)

        // read body
        readObject(bc.version, DataReaderBigEndian(bc.data))
    }

    /**
     * This function is called from [.read] function. Do not call it directly until you know,
     * what exactly are you doing.
     *
     * @param version version of loading content
     * @param dr data reader with content
     */
    @Throws(IOException::class)
    protected abstract fun readObject(version: Int, dr: DataReaderBigEndian)

    //*************************************************
    // WRITE PART
    //*************************************************

    /**
     * Write current object into writer.
     *
     * @param dw data writer
     */
    @Throws(IOException::class)
    fun write(dw: DataWriterBigEndian) {
        // write version
        dw.writeInt(getVersion())

        // save position and write empty size
        dw.writeInt(0)
        val startSize = dw.size()

        // write object itself
        writeObject(dw)

        // return back and write 'totalSize'
        val totalSize = dw.size() - startSize
        if (totalSize > 0) {
            dw.storePosition()
            dw.moveTo(startSize - 4)
            dw.writeInt(totalSize)
            dw.restorePosition()
        }
    }

    /**
     * This function is called from [.write] function. Do not call it directly until you know,
     * what exactly are you doing.
     *
     * @param dw data writer class
     */
    @Throws(IOException::class)
    protected abstract fun writeObject(dw: DataWriterBigEndian)

    companion object {

        // tag for logger
        private const val TAG = "Storable"

        // maximal size of Storable item
        private const val MAX_SIZE = 50 * 1024 * 1024

        /**
         * Read header of object from stream.
         *
         * @param dr input stream
         * @return read data container
         */
        @Throws(IOException::class)
        private fun readHeader(dr: DataReaderBigEndian): BodyContainer {
            // initialize container
            val bc = BodyContainer()

            // read basic data
            bc.version = dr.readInt()
            val size = dr.readInt()

            // check size to prevent OOE
            if (size < 0 || size > MAX_SIZE) {
                throw IOException("item size too big, size:$size, max: 50MB")
            }

            // read object data
            bc.data = dr.readBytes(size)

            // return filled container
            return bc
        }

        /**
         * Read header of object from stream.
         *
         * @param dis input stream
         * @return read data container
         */
        @Throws(IOException::class)
        private fun readHeader(dis: DataInputStream): BodyContainer {
            // initialize container
            val bc = BodyContainer()

            // read basic data
            bc.version = dis.readInt()
            val size = dis.readInt()

            // check size to prevent OOE
            if (size < 0 || size > MAX_SIZE) {
                throw IOException("item size too big, size:$size, max: 10MB")
            }

            // read object data
            bc.data = ByteArray(size)

            dis.read(bc.data!!)

            // return filled container
            return bc
        }

        //*************************************************
        // STATIC TOOLS
        //*************************************************

        // TOOLS

        /**
         * Read certain class from input.
         *
         * @param claz class to instantiate and read
         * @param dr   reader with data
         * @return read class
         */
        @Throws(IOException::class, InstantiationException::class, IllegalAccessException::class)
        fun <E : Storable> read(claz: Class<E>, dr: DataReaderBigEndian): E {
            // read header
            val bc = readHeader(dr)

            // now initialize object. Data are already loaded, so error will not break data flow
            val storable = claz.newInstance()
            storable.readObject(bc.version, DataReaderBigEndian(bc.data))
            return storable
        }

        /**
         * Allows to read object, that is not known.
         *
         * @param dr instance of data reader
         */
        @Throws(IOException::class)
        fun readUnknownObject(dr: DataReaderBigEndian) {
            // read header. This also allow to skip body of object
            readHeader(dr)
        }

        // LIST READING/WRITING

        /**
         * Read list of certain classes from input stream.
         *
         * @param claz class to instantiate and read
         * @param data byte array with pack data
         * @return loaded list of items
         */
        @Throws(IOException::class)
        fun <E : Storable> readList(claz: Class<E>, data: ByteArray): List<E> {
            return DataReaderBigEndian(data).readListStorable(claz)
        }

        /**
         * Read list of certain classes from input stream.
         *
         * @param claz class to instantiate and read
         * @param dis  input stream with data
         * @return loaded list of items
         */
        @Throws(IOException::class)
        fun <E : Storable> readList(
            claz: Class<E>,
            dis: DataInputStream
        ): List<E> {
            // prepare container
            val objs = ArrayList<E>()

            // read size
            val count = dis.readInt()
            if (count == 0) {
                return objs
            }

            // read locations
            for (i in 0 until count) {
                try {
                    val item = claz.newInstance()
                    item.read(dis)
                    objs.add(item)
                } catch (e: InstantiationException) {
                    logE(tag = TAG, ex = e) { "readList($claz, $dis)" }
                } catch (e: IllegalAccessException) {
                    logE(tag = TAG, ex = e) { "readList($claz, $dis)" }
                }

            }
            return objs
        }

        // WRITE LIST PART

        /**
         * Get list of items as byte array.
         *
         * @param data list of storable items
         * @return generated byte array with items
         */
        fun getAsBytes(data: List<Storable>): ByteArray? {
            return try {
                DataWriterBigEndian().apply {
                    writeListStorable(data)
                }.toByteArray()
            } catch (e: Exception) {
                logE(tag = TAG, ex = e) { "getAsBytes($data)" }
                null
            }
        }

        /**
         * Write certain list into output stream.
         *
         * @param objs list of storable items
         * @param dos  output stream where to write items
         */
        @Throws(IOException::class)
        fun writeList(objs: List<Storable>, dos: DataOutputStream) {
            // get size of list
            val size = objs.size

            // write size of list
            dos.writeInt(size)
            if (size == 0) {
                return
            }

            // write objects
            for (obj in objs) {
                dos.write(obj.asBytes!!)
            }
        }
    }
}
