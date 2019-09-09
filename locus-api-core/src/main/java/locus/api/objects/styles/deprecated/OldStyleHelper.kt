package locus.api.objects.styles.deprecated

import locus.api.objects.styles.LineStyle

/**
 * Helper for tasks related to old styles.
 */
internal object OldStyleHelper {

    /**
     * Convert old line and poly styles to new system.
     *
     * @param ls old line style container
     * @param ps old poly style container
     * @return new converted style
     */
    fun convertToNewLineStyle(ls: LineStyleOld?, ps: PolyStyleOld?): LineStyle? {
        // check objects
        if (ls == null && ps == null) {
            return null
        }

        // convert to new container
        val lsNew = LineStyle()

        // re-use line style
        if (ls != null) {
            lsNew.drawBase = true
            lsNew.colorBase = ls.color
            if (ls.lineType == LineStyleOld.LineType.NORMAL) {
                lsNew.drawSymbol = false
            } else {
                lsNew.drawSymbol = true
                lsNew.symbol = LineStyle.Symbol.valueOf(ls.lineType.name)
            }
            lsNew.coloring = LineStyle.Coloring.valueOf(ls.colorStyle.name)
            lsNew.width = ls.width
            lsNew.units = LineStyle.Units.valueOf(ls.units.name)
            lsNew.drawOutline = ls.drawOutline
            lsNew.colorOutline = ls.colorOutline
        } else {
            lsNew.drawBase = false
        }

        // re-use poly style
        if (ps != null) {
            lsNew.drawFill = ps.fill
            lsNew.colorFill = ps.color
        } else {
            lsNew.drawFill = false
        }

        // return new style
        return lsNew
    }
}
