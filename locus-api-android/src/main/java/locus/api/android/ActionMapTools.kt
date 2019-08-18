/**
 * Created by menion on 22/01/2018.
 * This code is part of Locus project from Asamm Software, s. r. o.
 */
package locus.api.android

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import locus.api.android.objects.LocusVersion
import locus.api.android.objects.VersionCode
import locus.api.android.utils.LocusConst
import locus.api.android.utils.closeQuietly
import locus.api.android.utils.exceptions.RequiredVersionMissingException
import locus.api.objects.Storable
import locus.api.objects.extra.Location
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian
import locus.api.utils.Logger
import java.io.IOException

object ActionMapTools {

    // tag for logger
    const val TAG = "ActionMapTools"

    /**
     * Generate map preview for a defined [params]. This method returns container with loaded data as well
     * as number if incorrectly loaded map tiles (useful for repeated request with same [params]).
     */
    @JvmStatic
    @Throws(RequiredVersionMissingException::class)
    fun getMapPreview(ctx: Context, lv: LocusVersion, params: MapPreviewParams): MapPreviewResult? {

        // get scheme if valid Locus is available
        val scheme = ActionBasics.getProviderUri(lv,
                VersionCode.UPDATE_14,
                LocusConst.CONTENT_PROVIDER_AUTHORITY_MAP_TOOLS,
                LocusConst.CONTENT_PROVIDER_PATH_MAP_PREVIEW)

        // prepare base query with parameters
        val sbQuery = params.generateQuery()

        // get data
        var cursor: Cursor? = null
        try {
            cursor = ActionBasics.queryData(ctx, scheme, sbQuery)
            if (cursor == null || !cursor.moveToFirst()) {
                return null
            }

            // load data
            var img: ByteArray? = null
            var notYetLoadedTiles = 0
            for (i in 0 until cursor.count) {
                cursor.moveToPosition(i)
                val key = String(cursor.getBlob(0))
                if (key == LocusConst.VALUE_MAP_PREVIEW) {
                    img = cursor.getBlob(1)
                } else if (key == LocusConst.VALUE_MAP_PREVIEW_MISSING_TILES) {
                    notYetLoadedTiles = cursor.getInt(1)
                }
            }

            // return result
            return MapPreviewResult(img, notYetLoadedTiles)
        } catch (e: Exception) {
            Logger.logE(TAG, "getMapPreview()", e)
            return MapPreviewResult(null, 0)
        } finally {
            cursor?.closeQuietly()
        }
    }
}

/**
 * Defined parameters for request on map preview.
 */
class MapPreviewParams {

    // center location of request
    var locCenter: Location? = null
    // offset [px] in X axis
    var offsetX = 0
    // offset [px] in Y axis
    var offsetY = 0
    // zoom level value [0..19+]
    var zoom = 0
    // required width [px] of final preview
    var widthPx = 0
    // required height [px] of final preview
    var heightPx: Int = 0
    // density in DPI of device that send request (set '0' to let app default)
    var densityDpi = 0
    // required rotation of final screenshot [°]. Needs to be used together with [radius]
    var rotation = 0
    // radius parameter of device [px] used for rotation. Usually it is "diagonal" of screen, so
    // biggest possible radius of map that may be visible on screen
    var radius = 0

    /**
     * Generate required query for a preview request.
     */
    fun generateQuery(): String {
        return "lon=${(locCenter?.longitude ?: "")}," +
                "lat=${locCenter?.latitude ?: ""}," +
                "offsetX=$offsetX," +
                "offsetY=$offsetY," +
                "zoom=$zoom," +
                "width=$widthPx," +
                "height=$heightPx," +
                "densityDpi=$densityDpi," +
                "rotation=$rotation," +
                "radius=$radius"
    }
}

/**
 * Result container for screenshot request.
 */
class MapPreviewResult() : Storable() {

    // loaded image
    private var imgData: ByteArray? = null
    // number of not yet loaded tiles
    var numOfNotYetLoadedTiles = 0

    constructor(imgData: ByteArray?, numOfNotYetLoadedTiles: Int) : this() {
        this.imgData = imgData
        this.numOfNotYetLoadedTiles = numOfNotYetLoadedTiles
    }

    /**
     * Check if loaded result has valid image.
     * @return `true` if image is valid
     */
    fun isValid(): Boolean {
        return imgData?.isNotEmpty() ?: false
    }

    fun getAsImage(): Bitmap? {
        return imgData?.takeIf { isValid() }?.let {
            BitmapFactory.decodeByteArray(it, 0, it.size)
        }
    }

    // STORABLE PART

    override fun getVersion(): Int {
        return 0
    }

    @Throws(IOException::class)
    override fun readObject(version: Int, dr: DataReaderBigEndian) {
        val size = dr.readInt()
        if (size > 0) {
            imgData = ByteArray(size).apply {
                dr.readBytes(this)
            }
        }
        numOfNotYetLoadedTiles = dr.readInt()
    }

    @Throws(IOException::class)
    override fun writeObject(dw: DataWriterBigEndian) {
        imgData?.takeIf { isValid() }?.let {
            dw.writeInt(it.size)
            dw.write(imgData)
        } ?: dw.writeInt(0)
        dw.writeInt(numOfNotYetLoadedTiles)
    }
}