package net.apcsimple.controlapplication.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import net.apcsimple.controlapplication.converters.ObjectJsonConverter
import net.apcsimple.controlapplication.daoservices.ProcessModelDbService
import net.apcsimple.controlapplication.model.processmodels.GeneralModel
import net.apcsimple.controlapplication.model.processmodels.ModelsList
import net.apcsimple.controlapplication.model.processmodels.ProcessModel
import net.apcsimple.controlapplication.model.processmodels.statespace.StateSpaceModel
import net.apcsimple.controlapplication.model.processmodels.statespace.StateSpaceRunnable
import org.springframework.stereotype.Service
import sklog.KotlinLogging

private var logger = KotlinLogging.logger {}

@Service
class ModelsServices(
    val modelsDbService: ProcessModelDbService,
    val generalServices: GeneralServices
) {

    fun addModel(model: ProcessModel, modelsList: ModelsList): Boolean {
        if (findModelByName(model.name, modelsList) != null) {
            logger.error("Model name " + model.name + " is already in use by another model.")
            return false
        }
        val mdl = ProcessModel(model.name, model.type)
        mdl.modelsServices = this
        if (model.structure != null) {
            mdl.structure = model.structure
        } else {
            when (model.type) {
                ProcessModel.STATE_SPACE -> mdl.structure = StateSpaceModel()
                else -> mdl.structure = null
            }
        }
        modelsList.list.add(mdl)
        modelsDbService.saveModel(mdl)
        return true
    }

    fun editModel(model: ProcessModel, modelsList: ModelsList): Boolean {
        val mdl = findModelByName(model.name, modelsList)
        if (mdl != null) {
            if (mdl.id != model.id) {
                logger.error("Model name ${model.name} is already in use by another model.")
                return false
            }
        }
        val existingModel = findModelById(model.id, modelsList) ?: return false
        existingModel.name = model.name
        if (existingModel.type == model.type) {
            if (model.type == ProcessModel.STATE_SPACE) {
                val structure = model.structure as StateSpaceModel
                val existingStructure = existingModel.structure as StateSpaceModel
                existingStructure.matrixA = structure.matrixA
                existingStructure.matrixB = structure.matrixB
                existingStructure.matrixC = structure.matrixC
            } else {
                existingModel.structure = model.structure
            }
        }
        modelsDbService.saveModel(existingModel)
        return true
    }

//    fun editStructure(oldStructure: GeneralModel?, newStructure: GeneralModel?, type: String?): GeneralModel? {
//        if (type == ProcessModel.STATE_SPACE) {
//            val old = oldStructure as StateSpaceModel
//            val new = newStructure as StateSpaceModel
//            old.matrixA = new.matrixA
//            old.matrixB = new.matrixB
//            old.matrixC = new.matrixC
//            old.matrixD = new.matrixD
//            return old
//        }
//        return newStructure
//    }

    fun deleteModel(id: Int, modelsList: ModelsList): Boolean {
        val existingModel = findModelById(id, modelsList) ?: return false
        modelsList.list.remove(existingModel)
        modelsDbService.deleteModel(id)
        return true
    }

    fun findModelById(id: Int?, modelsList: ModelsList): ProcessModel? {
        return modelsList.list.find { it.id == id }
    }

    fun findModelByName(name: String, modelsList: ModelsList): ProcessModel? {
        return modelsList.list.find { it.name == name }
    }

    fun loadModels(): MutableList<ProcessModel> {
        val modelsList = modelsDbService.loadModelsFromDB()
        modelsList.forEach { model ->
            run {
                model.modelsServices = this
                if (model.type == ProcessModel.STATE_SPACE) {
                    model.structure =
                        ObjectJsonConverter.convertFromJSON(model.structureJSON, StateSpaceModel::class.java)
                }

            }
        }
        return modelsList
    }

    fun getModelFromJson(json: String): ProcessModel {
        val model: ProcessModel
        var structure: GeneralModel? = null
        val mapper = ObjectMapper()
        val node: ObjectNode = mapper.readValue(json, ObjectNode::class.java)
        when (node.get("type").textValue()) {
            ProcessModel.STATE_SPACE -> {
                structure = mapper.convertValue(node.get("structure"), StateSpaceModel::class.java)
            }
            /* Add another model types conversion here, when needed. */
            else -> {}
        }
        node.remove("structure")
        model = mapper.convertValue(node, ProcessModel::class.java)
        model.structure = structure
        return model
    }

    fun simulationRunnable(model: ProcessModel): StateSpaceRunnable {
        return StateSpaceRunnable(model, this)
    }

}