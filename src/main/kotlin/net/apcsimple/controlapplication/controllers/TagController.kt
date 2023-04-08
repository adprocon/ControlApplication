package net.apcsimple.controlapplication.controllers

import net.apcsimple.controlapplication.ControlServer
import net.apcsimple.controlapplication.model.datapoints.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import sklog.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Tag List REST Controller. Used for the Front-End communication.
 */
@RestController
@RequestMapping("/tagapi")
@CrossOrigin(origins = ["*"], allowedHeaders = ["*"])
class TagController(val controlServer: ControlServer) {

    /**
     * Provides list of all tags.
     */
    @GetMapping("/tags")
    fun listTags() = controlServer.tagList.list.values

    /**
     * Updates existing tag with the values received from the Front-End.
     */
    @PostMapping("/tagupdate")
    fun updateTag(@RequestBody tag: Tag) {
        val ok = controlServer.tagList.updateTag(tag)
        if (!ok) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "This tag already exists.")
        }
    }

    /**
     * Adds a new tag with the values received from the Front-End to the tag list.
     */
    @PostMapping("/tagadd")
    fun addTag(@RequestBody tag: Tag) {
        val ok = controlServer.tagList.addTag(tag)
        if (!ok) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "This tag already exists.")
        }
    }

    /**
     * Deletes existing tag from the tag list.
     */
    @DeleteMapping("/tagdelete/{id}")
    fun deleteTag(@PathVariable id: Int) {
        val ok = controlServer.tagList.removeTag(id)
        if (ok != null && !ok) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Tag delete failed.")
        }
    }

}