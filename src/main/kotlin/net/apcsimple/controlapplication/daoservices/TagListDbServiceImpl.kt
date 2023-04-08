package net.apcsimple.controlapplication.daoservices

import net.apcsimple.controlapplication.model.datapoints.Tag
import net.apcsimple.controlapplication.daorepositories.TagRepository
import org.springframework.stereotype.Service

@Service
class TagListDbServiceImpl(val tagRepository: TagRepository): TagListDbService {
    override fun saveTag(tag: Tag) {
        tagRepository.save(tag)
    }

    override fun deleteTag(id: Int) {
        tagRepository.deleteById(id)
    }

    override fun existsById(id: Int): Boolean {
        return tagRepository.existsById(id)
    }

    override fun findAll(): List<Tag> {
        return tagRepository.findAll()
    }
}