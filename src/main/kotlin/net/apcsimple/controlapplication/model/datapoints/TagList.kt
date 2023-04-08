package net.apcsimple.controlapplication.model.datapoints

import net.apcsimple.controlapplication.daoservices.TagListDbService
import org.springframework.stereotype.Component
import sklog.KotlinLogging

private var logger = KotlinLogging.logger {}

/**
 * This class is used to create data points list in the main application. It has required methods to add, modify,
 * delete and load data points from the database.
 */
@Component("tagList")
class TagList(
    /**
     * This is a repository for data points database operations.
     */
    val tagListDbService: TagListDbService,
) {
    /**
     * The main list of the data points. It guarantees unique data points names.
     */
    var list: MutableMap<String, Tag> = mutableMapOf()
    /**
     * Auxiliary data points list, which is used for the operations based on data point id.
     */
    var idList: MutableMap<Int?, Tag> = mutableMapOf()
    /**
     * Error string is used to indicate in the text form any errors appearing during data points manipulations.
     */
    var error: String = ""

    /**
     * Method to add new tag to the tag list. Includes database operation.
     */
    fun addTag(tag: Tag): Boolean {
        if (list.containsKey(tag.tagName)) {
            error = "This tag name already exists."
            return false
        }
        tagListDbService.saveTag(tag)
        list[tag.tagName] = tag
        idList[tag.id] = tag
        return true
    }

    /**
     * Method to update existing tag in the tag list. Includes database operation.
     */
    fun updateTag(tag: Tag): Boolean {
        if (list.containsKey(tag.tagName)) {
            if (tag.id != list[tag.tagName]!!.id) {
                error = "This tag name already exists."
                logger.error(error)
                return false
            }
        }
        if (idList.containsKey(tag.id)) {
            val listTag = idList[tag.id]
            if (listTag != null) {
//                list.remove(listTag.tagName)
//                idList.remove(listTag.id)
//                val newTag = tag.copy()
//                list[tag.tagName] = newTag
//                idList[tag.id] = newTag
//                tagListDbService.saveTag(newTag)
                listTag.tagName = tag.tagName
                listTag.dataType = tag.dataType
                listTag.value = tag.value
                tagListDbService.saveTag(listTag)
            }
            return true
        }
        error = "No tag with this id."
        logger.error(error)
        return false
    }


    /**
     * Method to remove existing tag from the tag list and database.
     */
    fun removeTag(id: Int): Boolean? {
        return idList[id]?.let {
            list.remove(it.tagName)
            idList.remove(id)
            if (tagListDbService.existsById(id)) {
                tagListDbService.deleteTag(id)
                true
            } else {
                false
            }
        }
    }

    /**
     * Method used to load all tags from the database to the tag list.
     */
    fun load() {
        val loadedList = tagListDbService.findAll()
        for (tag in loadedList) {
            /* This expression is needed to involve setter and get proper value type */
            tag.value = tag.value
            list[tag.tagName] = tag
            idList[tag.id] = tag
        }
    }
}
