package locus.api.utils

/**
 * An abstract map that uses a byte as the key and an arbitrary type [V] as the value.
 *
 * This map is designed for scenarios where keys are single bytes, offering potentially
 * better performance and memory efficiency compared to using larger integer types as keys.
 *
 * **Thread Safety:**  This class is not thread-safe!
 *
 * @param V The type of the values stored in the map.
 * @param initialCapacity The initial capacity of the map.  Defaults to 1.
 *   The map will automatically resize as needed.
 */
abstract class ByteValueMap<V>(initialCapacity: Int = 1) {

    protected var keys = ByteArray(initialCapacity)

    internal abstract fun getValue(index: Int): V

    internal abstract fun setValue(index: Int, value: V)

    internal abstract fun resizeValueCapacity(capacity: Int)

    var size: Byte = 0
        private set

    val isEmpty: Boolean
        get() = size == 0.toByte()

    fun get(key: Byte): V? {
        val index = indexOfKey(key)
        return if (index >= 0) getValue(index) else null
    }

    fun put(key: Byte, value: V) {
        val index = indexOfKey(key)
        if (index >= 0) {
            setValue(index, value)
        } else {
            ensureCapacity(size + 1)
            keys[size.toInt()] = key
            setValue(size.toInt(), value)
            size++
        }
    }

    fun remove(key: Byte) {
        val index = indexOfKey(key)
        if (index >= 0) {
            for (i in index until size - 1) {
                keys[i] = keys[i + 1]
                setValue(i, getValue(i + 1))
            }
            size--
            shrinkCapacity()
        }
    }

    fun containsKey(key: Byte): Boolean {
        return indexOfKey(key) >= 0
    }

    fun keyAt(index: Int): Byte {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException("Index: $index, Size: $size")
        }
        return keys[index]
    }

    fun valueAt(index: Int): V {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException("Index: $index, Size: $size")
        }
        return getValue(index)
    }

    /**
     * Performs the given [action] on each key-value pair in the map.
     *
     * @param action The action to be performed on each key-value pair.
     * The first parameter of the action is the key, and the second is the value.
     */
    fun forEach(action: (Byte, V) -> Unit) {
        for (i in 0 until size) {
            action(keys[i], getValue(i))
        }
    }

    /**
     * Returns a new list containing the results of applying the given [transform]
     * function to each key-value pair in the map.
     *
     * @param R The type of elements in the resulting list.
     * @param transform The function to apply to each key-value pair.
     *   The first parameter of the transform is the key, and the second is the value.
     * @return A new list containing the transformed elements.
     */
    fun <R> map(transform: (Byte, V) -> R): List<R> {
        val result = ArrayList<R>(size.toInt())
        for (i in 0 until size) {
            result.add(transform(keys[i], getValue(i)))
        }
        return result
    }

    private fun ensureCapacity(minCapacity: Int) {
        if (minCapacity > keys.size) {
            val newCapacity = maxOf(keys.size * 2, 1)
            keys = keys.copyOf(newCapacity)
            resizeValueCapacity(newCapacity)
        }
    }

    private fun shrinkCapacity() {
        if (size < keys.size / 4 && keys.size > 1) {
            val newCapacity = maxOf(keys.size / 2, 1)
            keys = keys.copyOf(newCapacity)
            resizeValueCapacity(newCapacity)
        }
    }

    private fun indexOfKey(key: Byte): Int {
        for (i in 0 until size) {
            if (keys[i] == key) return i
        }
        return -1
    }
}

//*****************************************************
// IMPLEMENTATION
//*****************************************************

class ByteByteMap(initialCapacity: Int = 1) : ByteValueMap<ByteArray>(initialCapacity) {

    private var byteValues: Array<ByteArray?> = arrayOfNulls(initialCapacity)

    override fun getValue(index: Int): ByteArray = byteValues[index] ?: byteArrayOf()

    override fun setValue(index: Int, value: ByteArray) { byteValues[index] = value }

    override fun resizeValueCapacity(capacity: Int) {
        byteValues = byteValues.copyOf(capacity)
    }
}


class ByteDoubleMap(initialCapacity: Int = 1) : ByteValueMap<Double>(initialCapacity) {

    private var doubleValues: DoubleArray = DoubleArray(initialCapacity)

    override fun getValue(index: Int): Double = doubleValues[index]

    override fun setValue(index: Int, value: Double) { doubleValues[index] = value }

    override fun resizeValueCapacity(capacity: Int) {
        doubleValues = doubleValues.copyOf(capacity)
    }
}

class ByteFloatMap(initialCapacity: Int = 1) : ByteValueMap<Float>(initialCapacity) {

    private var floatValues: FloatArray = FloatArray(initialCapacity)

    override fun getValue(index: Int): Float = floatValues[index]

    override fun setValue(index: Int, value: Float) { floatValues[index] = value }

    override fun resizeValueCapacity(capacity: Int) {
        floatValues = floatValues.copyOf(capacity)
    }
}

class ByteIntMap(initialCapacity: Int = 1) : ByteValueMap<Int>(initialCapacity) {

    private var intValues: IntArray = IntArray(initialCapacity)

    override fun getValue(index: Int): Int = intValues[index]

    override fun setValue(index: Int, value: Int) { intValues[index] = value }

    override fun resizeValueCapacity(capacity: Int) {
        intValues = intValues.copyOf(capacity)
    }
}

class ByteLongMap(initialCapacity: Int = 1) : ByteValueMap<Long>(initialCapacity) {

    private var longValues: LongArray = LongArray(initialCapacity)

    override fun getValue(index: Int): Long = longValues[index]

    override fun setValue(index: Int, value: Long) { longValues[index] = value }

    override fun resizeValueCapacity(capacity: Int) {
        longValues = longValues.copyOf(capacity)
    }
}

class ByteShortMap(initialCapacity: Int = 1) : ByteValueMap<Short>(initialCapacity) {

    private var shortValues: ShortArray = ShortArray(initialCapacity)

    override fun getValue(index: Int): Short = shortValues[index]

    override fun setValue(index: Int, value: Short) { shortValues[index] = value }

    override fun resizeValueCapacity(capacity: Int) {
        shortValues = shortValues.copyOf(capacity)
    }
}

class ByteStringMap(initialCapacity: Int = 1) : ByteValueMap<String>(initialCapacity) {

    private var stringValues: Array<String?> = arrayOfNulls(initialCapacity)

    override fun getValue(index: Int): String = stringValues[index] ?: ""

    override fun setValue(index: Int, value: String) { stringValues[index] = value }

    override fun resizeValueCapacity(capacity: Int) {
        stringValues = stringValues.copyOf(capacity)
    }
}
