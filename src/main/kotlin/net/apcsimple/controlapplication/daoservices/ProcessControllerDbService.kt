package net.apcsimple.controlapplication.daoservices

import net.apcsimple.controlapplication.model.processcontrollers.ProcessController

interface ProcessControllerDbService {

    fun saveController(controller: ProcessController)

    fun deleteController(id: Int)

    fun loadControllersFromDB(): MutableList<ProcessController>
}