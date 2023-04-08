package net.apcsimple.controlapplication.daoservices

import net.apcsimple.controlapplication.daorepositories.ProcessModelRepository
import net.apcsimple.controlapplication.model.processmodels.ProcessModel
import org.springframework.stereotype.Service

@Service
class ProcessModelDbServiceImpl(
    val processModelRepository: ProcessModelRepository
): ProcessModelDbService {
    override fun saveModel(model: ProcessModel) {
        processModelRepository.save(model)
    }

    override fun deleteModel(id: Int) {
        processModelRepository.deleteById(id)
    }

    override fun loadModelsFromDB(): MutableList<ProcessModel> {
        return processModelRepository.findAll()
    }
}