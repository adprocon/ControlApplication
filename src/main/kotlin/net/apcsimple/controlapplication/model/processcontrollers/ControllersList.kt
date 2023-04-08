package net.apcsimple.controlapplication.model.processcontrollers

import net.apcsimple.controlapplication.services.ControllerServices
import org.springframework.stereotype.Component

@Component("controllersList")
class ControllersList(val controllerServices: ControllerServices) {
    val list: MutableList<ProcessController> = mutableListOf()

    fun load() {
        list.addAll(controllerServices.loadControllers())
    }

    fun addController(controller: ProcessControllerJson): Boolean {
        return controllerServices.addController(controller, this)
    }

    fun editController(controller: ProcessController): Boolean {
        return controllerServices.editController(controller, this)
    }

    fun editControllerName(name: String, id: Int): Boolean {
        return controllerServices.editControllerName(name, id, this)
    }

    fun deleteController(id: Int?): Boolean {
        return controllerServices.deleteController(id ?: 0, this)
    }

    fun switchController(id: Int?): Boolean {
        return controllerServices.switchController(id ?: 0, this)
    }
}