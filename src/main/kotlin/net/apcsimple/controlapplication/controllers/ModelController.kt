package net.apcsimple.controlapplication.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import net.apcsimple.controlapplication.converters.ObjectJsonConverter
import net.apcsimple.controlapplication.math.MatrixOps
import net.apcsimple.controlapplication.model.datapoints.Tag
import net.apcsimple.controlapplication.model.processmodels.ModelsList
import net.apcsimple.controlapplication.model.processmodels.ProcessModel
import net.apcsimple.controlapplication.model.processmodels.statespace.StateSpaceModel
import net.apcsimple.controlapplication.services.ModelsServices
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import sklog.KotlinLogging
import java.lang.Exception

private var logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/modelapi")
@CrossOrigin(origins = ["*"], allowedHeaders = ["*"])
class ModelController(
    val modelsList: ModelsList,
    val modelsServices: ModelsServices
) {

    @GetMapping("/models")
    fun getModels(): MutableList<ProcessModel> {
        return modelsList.list
    }

    @GetMapping("/model/{id}")
    fun getModel(@PathVariable id: Int): ProcessModel {
        return modelsList.modelsServices.findModelById(id, modelsList) ?:
        throw ResponseStatusException(HttpStatus.CONFLICT, "Failed to add new process model.")
    }

    @PostMapping("/modeladd")
    fun addModel(@RequestBody model: ProcessModel) {
        logger.info(ObjectJsonConverter.convertToJSON(model))
        if (!modelsList.addModel(model)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Failed to add new process model.")
        }
    }

    @PostMapping("/modeledit")
    fun editModel(@RequestBody json: String) {
        val model = modelsServices.getModelFromJson(json)
        logger.info(ObjectJsonConverter.convertToJSON(model))
        logger.warning(model.structureJSON)
        if (!modelsList.editModel(model)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Failed to edit process model " + model.name + ".")
        }
    }

    @DeleteMapping("/modeldelete/{id}")
    fun deleteModel(@PathVariable id: Int) {
        if (!modelsList.deleteModel(id)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Failed to delete process model.")
        }
    }

    @PostMapping("/simulationedit/{id}")
    fun updateSimulation(@PathVariable id: Int, @RequestBody json: String) {
        val model = modelsList.modelsServices.findModelById(id, modelsList)
        if (model !== null && model.type == ProcessModel.STATE_SPACE) {
            try {
                val structure = model.structure as StateSpaceModel
                val mapper = ObjectMapper()
                val node: ObjectNode = mapper.readValue(json, ObjectNode::class.java)
                structure.initialStates = MatrixOps.fillVector(
                    structure.initialStates,
                    mapper.convertValue(node.get("initialStates"), Array<Double>::class.java)
                ) ?: structure.initialStates
                structure.inputs =
                    mapper.convertValue(node.get("inputs"), Array<Tag>::class.java).toMutableList()
                structure.outputs =
                    mapper.convertValue(node.get("outputs"), Array<Tag>::class.java).toMutableList()
                model.simulationCycle =
                    mapper.convertValue(node.get("simulationCycle"), Int::class.java)
                modelsList.editModel(model)
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        throw ResponseStatusException(HttpStatus.CONFLICT, "Failed to edit process controller.")

    }

    @PostMapping("/runsimulation/{id}")
    fun runSimulation(@PathVariable id: Int,
//                      @RequestBody json: String
    ) {
        val model = modelsList.modelsServices.findModelById(id, modelsList)
        if (model !== null && model.type == ProcessModel.STATE_SPACE) {
            try {
                model.simulationRunning = !model.simulationRunning
//                logger.info("Model ${model.name} simulation status is ${model.simulationRunning}.")
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        throw ResponseStatusException(HttpStatus.CONFLICT, "Failed to edit process controller.")

    }


}