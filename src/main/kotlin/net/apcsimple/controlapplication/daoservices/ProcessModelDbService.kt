package net.apcsimple.controlapplication.daoservices

import net.apcsimple.controlapplication.model.processmodels.ProcessModel

interface ProcessModelDbService {

    fun saveModel(model: ProcessModel)

    fun deleteModel(id: Int)

    fun loadModelsFromDB(): MutableList<ProcessModel>
}