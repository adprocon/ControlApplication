package net.apcsimple.controlapplication.daoservices

import net.apcsimple.controlapplication.model.datapoints.Tag

interface TagListDbService {
    fun saveTag(tag: Tag)
    fun deleteTag(id: Int)
    fun existsById(id: Int): Boolean
    fun findAll(): List<Tag>
}