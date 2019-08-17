/****************************************************************************
 *
 * Created by menion on 17.08.2019.
 * Copyright (c) 2019. All rights reserved.
 *
 * This file is part of the Asamm team software.
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 ***************************************************************************/

package locus.api.objects.styles

import locus.api.objects.Storable
import locus.api.utils.DataReaderBigEndian
import locus.api.utils.DataWriterBigEndian
import java.io.IOException

class ListStyle : Storable() {

    enum class ListItemType {
        CHECK, CHECK_OFF_ONLY, CHECK_HIDE_CHILDREN, RADIO_FOLDER
    }

    class ItemIcon {

        var state = State.OPEN
        var href = ""

        enum class State {
            OPEN, CLOSED, ERROR, FETCHING0, FETCHING1, FETCHING2
        }
    }

    var listItemType: ListItemType = ListItemType.CHECK
    var bgColor: Int = GeoDataStyle.WHITE
    var itemIcons: MutableList<ItemIcon> = arrayListOf()

    //*************************************************
    // STORABLE
    //*************************************************

    override fun getVersion(): Int {
        return 0
    }

    @Throws(IOException::class)
    override fun readObject(version: Int, dis: DataReaderBigEndian) {
        val style = dis.readInt()
        if (style < ListStyle.ListItemType.values().size) {
            listItemType = ListStyle.ListItemType.values()[style]
        }
        bgColor = dis.readInt()
        val itemsCount = dis.readInt()
        for (i in 0 until itemsCount) {
            val itemIcon = ListStyle.ItemIcon()
            val iconStyle = dis.readInt()
            if (iconStyle < ListStyle.ItemIcon.State.values().size) {
                itemIcon.state = ListStyle.ItemIcon.State.values()[iconStyle]
            }
            itemIcon.href = dis.readString()
            itemIcons.add(itemIcon)
        }
    }

    @Throws(IOException::class)
    override fun writeObject(dw: DataWriterBigEndian) {
        dw.writeBoolean(true)
        dw.writeInt(listItemType.ordinal)
        dw.writeInt(bgColor)
        dw.writeInt(itemIcons.size)
        for (itemIcon in itemIcons) {
            dw.writeInt(itemIcon.state.ordinal)
            dw.writeString(itemIcon.href)
        }
    }
}