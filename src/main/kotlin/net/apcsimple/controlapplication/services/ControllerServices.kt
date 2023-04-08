package net.apcsimple.controlapplication.services

import net.apcsimple.controlapplication.converters.ObjectJsonConverter
import net.apcsimple.controlapplication.daoservices.ProcessControllerDbService
import net.apcsimple.controlapplication.model.datapoints.Tag
import net.apcsimple.controlapplication.model.datapoints.TagList
import net.apcsimple.controlapplication.model.processcontrollers.ControllersList
import net.apcsimple.controlapplication.model.processcontrollers.ProcessController
import net.apcsimple.controlapplication.model.processcontrollers.ProcessControllerJson
import net.apcsimple.controlapplication.model.processcontrollers.mpc.MpcController
import net.apcsimple.controlapplication.model.processcontrollers.mpc.MpcRunnable
import net.apcsimple.controlapplication.model.processmodels.ModelsList
import org.springframework.stereotype.Service
import sklog.KotlinLogging

private var logger = KotlinLogging.logger {}

@Service
class ControllerServices(
    val processControllerDbService: ProcessControllerDbService,
    val modelsList: ModelsList,
    val tagList: TagList,
    val generalServices: GeneralServices
) {

    fun addController(controller: ProcessControllerJson, controllersList: ControllersList): Boolean {
        if (findControllerByName(controller.name, controllersList) != null) {
            logger.error("Controller name " + controller.name + " is already in use by another controller.")
            return false
        }
        val ctrl = ProcessController(controller.name, controller.type)
        ctrl.controllerServices = this
        when (controller.type) {
            ProcessController.MODEL_PREDICTIVE_CONTROLLER -> ctrl.structure = MpcController(modelsList)
            else -> ctrl.structure = null
        }
        controllersList.list.add(ctrl)
        processControllerDbService.saveController(ctrl)
        return true
    }

    fun editController(controller: ProcessController, controllersList: ControllersList): Boolean {
        val ctrl = findControllerByName(controller.name, controllersList)
        if (ctrl != null) {
            if (ctrl.id != controller.id) {
                logger.error("Controller name " + controller.name + " is already in use by another controller.")
                return false
            }
        }
        val existingController = findControllerById(controller.id ?: 0, controllersList) ?: return false
        existingController.name = controller.name
        if (existingController.type == controller.type) {
            existingController.structure = controller.structure
        }
        processControllerDbService.saveController(existingController)
        return true
    }

    fun editControllerName(name: String, id: Int, controllersList: ControllersList): Boolean {
        val ctrl = findControllerByName(name, controllersList)
        if (ctrl != null) {
            if (ctrl.id != id) {
                logger.error("Controller name $name is already in use by another controller.")
                return false
            }
        }
        val existingController = findControllerById(id, controllersList) ?: return false
        existingController.name = name
        processControllerDbService.saveController(existingController)
        return true
    }

    fun switchController(id: Int, controlersList: ControllersList): Boolean {
        val ctrl = findControllerById(id, controlersList) ?: return false
        ctrl.running = !ctrl.running
        return true
    }

    fun deleteController(id: Int, controllersList: ControllersList): Boolean {
        val existingController = findControllerById(id, controllersList) ?: return false
        controllersList.list.remove(existingController)
        processControllerDbService.deleteController(id)
        return true
    }

    fun findControllerById(id: Int, controllersList: ControllersList): ProcessController? {
        return controllersList.list.find { it.id == id }
    }

    fun findControllerByName(name: String, controllersList: ControllersList): ProcessController? {
        return controllersList.list.find { it.name == name }
    }

    fun loadControllers(): MutableList<ProcessController> {
        val controllerList = processControllerDbService.loadControllersFromDB()
        controllerList.forEach {
            it.controllerServices = this
            if (it.type == ProcessController.MODEL_PREDICTIVE_CONTROLLER) {
                var mpc = ObjectJsonConverter.convertFromJSON(it.structureJSON, MpcController::class.java)
                if (mpc == null) {
                    mpc = MpcController(modelsList)
                }
                /* Bond model with a model from the models list, as this one is created from JSON and is not bonded */
                mpc.model = modelsList.modelsServices.findModelById(mpc.model?.id, modelsList)
                /* Bond tags with tags from the tags list */
                fixTags(mpc.inputs)
                fixTags(mpc.outputs)
                fixTags(mpc.setpoints)
                fixTags(mpc.optInputs)
                it.structure = mpc
            }
        }
        return controllerList
    }

    private fun fixTags(list: MutableList<Tag>) {
        list.forEachIndexed { index, tag ->
            list[index] = tagList.idList.get(tag.id) ?: Tag(Tag.DEFAULT_NAME, Tag.TAGDOUBLE, 0.0)
        }
    }

    fun mpcRunnable(controller: ProcessController): MpcRunnable {
        return MpcRunnable(controller, this)
    }

}