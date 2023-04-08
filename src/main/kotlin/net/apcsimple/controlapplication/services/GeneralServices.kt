package net.apcsimple.controlapplication.services

import net.apcsimple.controlapplication.model.datapoints.Tag
import net.apcsimple.controlapplication.model.datapoints.TagList
import net.apcsimple.controlapplication.model.general.GeneralComponent
import org.ejml.simple.SimpleMatrix
import org.springframework.stereotype.Service

@Service
class GeneralServices(
    val generalComponent: GeneralComponent,
    val tagList: TagList,
    ) {

    fun logError(logger: sklog.Logger, debugLevel: Int, msg: String, type: Int) {
        if (generalComponent.debugLevel >= debugLevel) {
            when (type) {
                1 -> logger.info(msg)
                2 -> logger.warning(msg)
                3 -> logger.error(msg)
            }
        }
    }

    /**
     *  This function is used to read values from the data points
     *
     *  @param list      list of data points
     */
    fun readFromTagList(list: List<Tag>): SimpleMatrix {
        val out = SimpleMatrix(list.size, 1)
        list.forEachIndexed { index, tag -> out[index] = tagList.list[tag.tagName]?.value as Double }
        return out
    }

    /**
     *  This function is used to read values from the data points
     *
     *  @param tagList      list of data points
     */
    fun readFromTagList1(list: List<Tag>): Array<DoubleArray> {
        val out = Array(list.size) {DoubleArray(1)}
        list.forEachIndexed { index, tag -> out[index][0] = tagList.list[tag.tagName]?.value as Double }
        return out
    }

    /**
     *  This function is used to read values from the data points
     *
     *  @param tag      list of data points
     */
    fun readFromTagList(tag: Tag): Any? {
        return tagList.list[tag.tagName]?.value
    }

    /**
     *  This function is used to write values to the data points
     *
     *  @param list      list of data points
     */
    fun writeToTagList(list: List<Tag>, values: SimpleMatrix) {
        list.forEachIndexed { index, tag -> tagList.list[tag.tagName]?.value = values[index] }
    }

    /**
     *  This function is used to write values to the data points
     *
     *  @param list      list of data points
     */
    fun writeToTagList1(list: List<Tag>, values: Array<DoubleArray>) {
        list.forEachIndexed { index, tag -> tagList.list[tag.tagName]?.value = values[index][0] }
    }

    /**
     * Adjust tag list according to model dimensions
     */
    fun adjustTagListSize(tagList: MutableList<Tag>, size: Int) {
        if (tagList.size > size) {
            for (i in size until tagList.size) {
                tagList.removeLast()
            }
        } else if (tagList.size < size) {
            for (i in tagList.size until size) {
                tagList.add(Tag(Tag.DEFAULT_NAME, Tag.TAGDOUBLE, 0.0))
            }
        }
    }
}