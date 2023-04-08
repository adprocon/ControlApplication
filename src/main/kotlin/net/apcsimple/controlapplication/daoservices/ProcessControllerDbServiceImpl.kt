package net.apcsimple.controlapplication.daoservices

import net.apcsimple.controlapplication.daorepositories.ProcessControllerRepository
import net.apcsimple.controlapplication.model.processcontrollers.ProcessController
import org.springframework.stereotype.Service

@Service
class ProcessControllerDbServiceImpl(
    val processControllerRepository: ProcessControllerRepository
) : ProcessControllerDbService {

    override fun saveController(controller: ProcessController) {
        processControllerRepository.save(controller)
    }

    override fun deleteController(id: Int) {
        processControllerRepository.deleteById(id)
    }

    override fun loadControllersFromDB(): MutableList<ProcessController> {
        return processControllerRepository.findAll()
    }
}