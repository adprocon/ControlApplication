package net.apcsimple.controlapplication.controllers

import com.fasterxml.jackson.annotation.JsonView
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import net.apcsimple.controlapplication.converters.BasicData
import net.apcsimple.controlapplication.converters.ObjectJsonConverter
import net.apcsimple.controlapplication.math.MatrixOps
import net.apcsimple.controlapplication.math.MatrixOps.Companion.fillMatrix
import net.apcsimple.controlapplication.math.MatrixOps.Companion.fillVector
import net.apcsimple.controlapplication.math.MatrixOps.Companion.zerix
import net.apcsimple.controlapplication.model.datapoints.Tag
import net.apcsimple.controlapplication.model.processcontrollers.ControllersList
import net.apcsimple.controlapplication.model.processcontrollers.GeneralController
import net.apcsimple.controlapplication.model.processcontrollers.ProcessController
import net.apcsimple.controlapplication.model.processcontrollers.ProcessControllerJson
import net.apcsimple.controlapplication.model.processcontrollers.mpc.MpcController
import net.apcsimple.controlapplication.model.processcontrollers.mpc.MpcDiagnostics
import net.apcsimple.controlapplication.model.processmodels.statespace.StateSpaceModel
import net.apcsimple.controlapplication.services.ModelsServices
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import sklog.KotlinLogging
import java.lang.Exception

private var logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/conapi")
@CrossOrigin(origins = ["*"], allowedHeaders = ["*"])
class ProConController(
    val controllersList: ControllersList,
    val modelsServices: ModelsServices
) {

    @JsonView(BasicData::class)
    @GetMapping("/controllers")
    fun getControllers(): MutableList<ProcessController> {
        return controllersList.list
    }

    @GetMapping("/controller/{id}")
    fun getController(@PathVariable id: Int): ProcessController {
        return controllersList.controllerServices.findControllerById(id, controllersList)
            ?: throw ResponseStatusException(HttpStatus.CONFLICT, "Failed to get the process controller.")
    }

    @PostMapping("/controlleradd")
    fun addController(@RequestBody controller: ProcessControllerJson) {
        logger.info(ObjectJsonConverter.convertToJSON(controller))
        if (!controllersList.addController(controller)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Failed to add new process controller.")
        }
    }

    @GetMapping("/condiag/{id}")
    fun getControllerDiagnostics(@PathVariable id: Int): MpcDiagnostics {
        return (controllersList.controllerServices.findControllerById(id, controllersList)?.structure as MpcController)
            .diagnostics
            ?: throw ResponseStatusException(HttpStatus.CONFLICT, "Failed to get the process controller.")
    }

    @PostMapping("/controlleredit")
    fun editController(@RequestBody json: String) {
        val controller: ProcessController
        var structure: GeneralController? = null
        val mapper = ObjectMapper()
        val node: ObjectNode = mapper.readValue(json, ObjectNode::class.java)
//        logger.warning(json)
        when (node.get("type").textValue()) {
            ProcessController.MODEL_PREDICTIVE_CONTROLLER -> {
                structure = mapper.convertValue(node.get("structure"), MpcController::class.java)
            }
            /* Add another model types conversion here, when needed. */
            else -> {}
        }
        node.remove("structure")
        controller = mapper.convertValue(node, ProcessController::class.java)
        controller.structure = structure
        if (!controllersList.editController(controller)) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Failed to edit process controller " + controller.name + "."
            )
        }
    }

    @PostMapping("controllerswitch/{id}")
    fun switchStatus(@PathVariable id: Int) {
        if (!controllersList.switchController(id)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Failed to switch controller.")
        }
    }

    @DeleteMapping("/controllerdelete/{id}")
    fun deleteController(@PathVariable id: Int) {
        if (!controllersList.deleteController(id)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Failed to delete process controller.")
        }
    }

    @PostMapping("modelselection/{id}")
    fun updateModel(@PathVariable id: Int, @RequestBody json: String) {
        val controller = controllersList.controllerServices.findControllerById(id, controllersList)
        try {
//            val model = modelsServices.getModelFromJson(json)
            val mpc = (controller?.structure as MpcController)
            val mapper = ObjectMapper()
            val node: ObjectNode = mapper.readValue(json, ObjectNode::class.java)
//            logger.warning(node.get("model").toString())
            mpc.model = modelsServices.getModelFromJson(node.get("model").toString())
            mpc.augmented = mapper.convertValue(node.get("augmented"), Boolean::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if ((controller == null) || !controllersList.editController(controller)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Failed to update controller's model.")
        }
    }

    @PostMapping("controllernameupdate/{id}")
    fun updateName(@PathVariable id: Int, @RequestBody json: String) {
        val controller = controllersList.controllerServices.findControllerById(id, controllersList)
        if (controller != null && controller.type == ProcessController.MODEL_PREDICTIVE_CONTROLLER) {
            val structure = controller.structure as MpcController
            try {
                val mapper = ObjectMapper()
                val node: ObjectNode = mapper.readValue(json, ObjectNode::class.java)
                controller.name = mapper.convertValue(node.get("name"), String::class.java)
                controller.cycleTime = mapper.convertValue(node.get("executionCycle"), Int::class.java)
                controllersList.editController(controller)
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    @PostMapping("controllerstatuschange/{id}")
    fun statusChange(@PathVariable id: Int) {
        val controller = controllersList.controllerServices.findControllerById(id, controllersList)
        if (controller != null) {
            controller.running = !controller.running
        } else {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Failed to change controller status.")
        }
    }

    @PostMapping("updateusedinputs/{id}")
    fun editUsedInputs(@PathVariable id: Int, @RequestBody usedInputs: Array<Boolean>) {
        val controller = controllersList.controllerServices.findControllerById(id, controllersList)
        if (controller != null && controller.type == ProcessController.MODEL_PREDICTIVE_CONTROLLER) {
            val structure = controller.structure as MpcController
            try {
                val usedInputsAmount = usedInputs.count {it}
                structure.usedInputs = fillVector(structure.usedInputs, usedInputs)
                structure.matrixR = structure.fillMatrixR(structure.matrixR)
                structure.inputConstraints = MatrixOps.adjustMatrixSize(structure.inputConstraints,
                    usedInputsAmount, doubleArrayOf(0.0, 0.0))
                structure.inputConstraintsUsed = MatrixOps.adjustMatrixSize(structure.inputConstraintsUsed,
                    usedInputsAmount, booleanArrayOf(false, false))
                structure.adjustTagListSize(structure.optInputs, usedInputsAmount)
                controllersList.editController(controller)
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        throw ResponseStatusException(HttpStatus.CONFLICT, "Failed to update used inputs.")
    }

    @PostMapping("/edithorizons/{id}")
    fun editHorizons(@PathVariable id: Int, @RequestBody json: String) {
        val controller = controllersList.controllerServices.findControllerById(id, controllersList)
        if (controller !== null && controller.type == ProcessController.MODEL_PREDICTIVE_CONTROLLER) {
            try {
                val structure = controller.structure as MpcController
                val mapper = ObjectMapper()
                val node: ObjectNode = mapper.readValue(json, ObjectNode::class.java)
                structure.hp =
                    mapper.convertValue(node.get("Hp"), Int::class.java)
                structure.hc =
                    mapper.convertValue(node.get("Hc"), Int::class.java)
                controllersList.editController(controller)
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        throw ResponseStatusException(HttpStatus.CONFLICT, "Failed to edit process controller parameters.")
    }

    @PostMapping("/editparameters/{id}")
    fun editParameters(@PathVariable id: Int, @RequestBody json: String) {
        val controller = controllersList.controllerServices.findControllerById(id, controllersList)
        if (controller !== null && controller.type == ProcessController.MODEL_PREDICTIVE_CONTROLLER) {
            try {
                val structure = controller.structure as MpcController
                val model = structure.model?.structure as StateSpaceModel
                val mapper = ObjectMapper()
                val node: ObjectNode = mapper.readValue(json, ObjectNode::class.java)
                structure.matrixQ = structure.fillMatrixQ(mapper.convertValue(node.get("Q"), Array<DoubleArray>::class.java))
                structure.matrixR = structure.fillMatrixR(mapper.convertValue(node.get("R"), Array<DoubleArray>::class.java))

                controllersList.editController(controller)
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        throw ResponseStatusException(HttpStatus.CONFLICT, "Failed to edit process controller parameters.")
    }

    @PostMapping("/editconstraints/{id}")
    fun editConstraints(@PathVariable id: Int, @RequestBody json: String) {
        val controller = controllersList.controllerServices.findControllerById(id, controllersList)
        if (controller !== null && controller.type == ProcessController.MODEL_PREDICTIVE_CONTROLLER) {
            try {
                val structure = controller.structure as MpcController
                val model = structure.model?.structure as StateSpaceModel
                val mapper = ObjectMapper()
                val node: ObjectNode = mapper.readValue(json, ObjectNode::class.java)
                structure.inMovesConstraints = fillMatrix(zerix(
                    structure.usedInputs.count {it}, 2),
                    mapper.convertValue(node.get("inMovesConstraints"), Array<DoubleArray>::class.java)) ?:
                    arrayOf(doubleArrayOf())
                structure.inMovesConstraintsUsed = fillMatrix(Array(structure.usedInputs.count {it}) {BooleanArray(2)},
                    mapper.convertValue(node.get("inMovesConstraintsUsed"), Array<BooleanArray>::class.java)) ?:
                    arrayOf(booleanArrayOf())
                structure.inputConstraints = fillMatrix(zerix(
                    structure.usedInputs.count {it}, 2),
                    mapper.convertValue(node.get("inputConstraints"), Array<DoubleArray>::class.java)) ?:
                    arrayOf(doubleArrayOf())
                structure.inputConstraintsUsed = fillMatrix(Array(structure.usedInputs.count {it}) {BooleanArray(2)},
                    mapper.convertValue(node.get("inputConstraintsUsed"), Array<BooleanArray>::class.java)) ?:
                    arrayOf(booleanArrayOf())
                structure.outputConstraints = fillMatrix(zerix(
                    model.matrixC.size, 2),
                    mapper.convertValue(node.get("outputConstraints"), Array<DoubleArray>::class.java)) ?:
                    arrayOf(doubleArrayOf())
                structure.outputConstraintsUsed = fillMatrix(Array(model.matrixC.size) {BooleanArray(2)},
                    mapper.convertValue(node.get("outputConstraintsUsed"), Array<BooleanArray>::class.java)) ?:
                    arrayOf(booleanArrayOf())

                controllersList.editController(controller)
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        throw ResponseStatusException(HttpStatus.CONFLICT, "Failed to edit process controller.")
    }

    @PostMapping("/editdatapoints/{id}")
    fun editDataPoints(@PathVariable id: Int, @RequestBody json: String) {
        val controller = controllersList.controllerServices.findControllerById(id, controllersList)
        if (controller !== null && controller.type == ProcessController.MODEL_PREDICTIVE_CONTROLLER) {
            try {
                val structure = controller.structure as MpcController
                val mapper = ObjectMapper()
                val node: ObjectNode = mapper.readValue(json, ObjectNode::class.java)
                structure.inputs =
                    mapper.convertValue(node.get("inputs"), Array<Tag>::class.java).toMutableList()
                structure.outputs =
                    mapper.convertValue(node.get("outputs"), Array<Tag>::class.java).toMutableList()
                structure.setpoints =
                    mapper.convertValue(node.get("trajectory"), Array<Tag>::class.java).toMutableList()
                structure.optInputs =
                    mapper.convertValue(node.get("optInputs"), Array<Tag>::class.java).toMutableList()
                structure.mpcinuse = mapper.convertValue(node.get("mpcinuse"), Array<Tag>::class.java)[0]
                controllersList.editController(controller)
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        throw ResponseStatusException(HttpStatus.CONFLICT, "Failed to edit process controller.")
    }

    @PostMapping("/editobserver/{id}")
    fun editObserver(@PathVariable id: Int, @RequestBody json: String) {
        val controller = controllersList.controllerServices.findControllerById(id, controllersList)
        if (controller !== null && controller.type == ProcessController.MODEL_PREDICTIVE_CONTROLLER) {
            try {
                val structure = controller.structure as MpcController
                val observer = structure.observer
                val model = structure.model?.structure as StateSpaceModel
                val mapper = ObjectMapper()
                val node: ObjectNode = mapper.readValue(json, ObjectNode::class.java)
                observer.matrixQk = fillMatrix(zerix(
                    model.matrixA.size, model.matrixA.size),
                    mapper.convertValue(node.get("matrixQk"), Array<DoubleArray>::class.java)) ?: arrayOf(doubleArrayOf())
                observer.matrixRk = fillMatrix(zerix(
                    model.matrixC.size, model.matrixC.size),
                    mapper.convertValue(node.get("matrixRk"), Array<DoubleArray>::class.java)) ?: arrayOf(doubleArrayOf())
                observer.matrixPk = fillMatrix(zerix(
                    model.matrixA.size, model.matrixA.size),
                    mapper.convertValue(node.get("matrixPk"), Array<DoubleArray>::class.java)) ?: arrayOf(doubleArrayOf())
                observer.matrixKk = fillMatrix(zerix(
                    model.matrixA.size, model.matrixA.size),
                    mapper.convertValue(node.get("matrixKk"), Array<DoubleArray>::class.java)) ?: arrayOf(doubleArrayOf())
                controllersList.editController(controller)
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        throw ResponseStatusException(HttpStatus.CONFLICT, "Failed to edit process controller.")
    }


}