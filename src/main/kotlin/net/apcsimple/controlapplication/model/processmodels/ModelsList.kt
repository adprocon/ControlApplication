package net.apcsimple.controlapplication.model.processmodels

import net.apcsimple.controlapplication.services.ModelsServices
import org.springframework.stereotype.Component

@Component("modelsList")
class ModelsList(
    val modelsServices: ModelsServices
) {
    val list: MutableList<ProcessModel> = mutableListOf()

    fun load() {
        list.addAll(modelsServices.loadModels())
    }

    fun addModel(model: ProcessModel): Boolean {
        return modelsServices.addModel(model, this)
    }

    fun editModel(model: ProcessModel): Boolean {
        return modelsServices.editModel(model, this)
    }

    fun deleteModel(id: Int?): Boolean {
        return modelsServices.deleteModel(id ?: 0, this)
    }
}