package net.apcsimple.controlapplication.controllers

import net.apcsimple.controlapplication.model.communication.*
import net.apcsimple.controlapplication.model.communication.modbus.*
import net.apcsimple.controlapplication.services.InterfaceServices
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import sklog.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Interfaces List REST Controller. Used for the Front-End communication.
 */
@RestController
@RequestMapping("/intapi")
@CrossOrigin(origins = ["*"], allowedHeaders = ["*"])
class InterfaceController(
    val interfaceList: InterfaceList,
    val interfaceServices: InterfaceServices
) {

    @GetMapping("/interfaces")
    fun getInterfaces(): MutableList<ProcessInterface> {
        return interfaceList.list
    }

    @PostMapping("/interfaceadd")
    fun addInterface(@RequestBody intrfc: ProcessInterface) {
        if (!interfaceList.addNewInterface(intrfc)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Failed to add interface.")
        }
    }

    @DeleteMapping("/interfacedelete/{id}")
    fun deleteInterface(@PathVariable id: Long) {
        if (!interfaceList.deleteInterface(id)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Failed to delete interface.")
        }
    }

    @PostMapping("interfaceswitch/{id}")
    fun switchInterface(@PathVariable id: Long) {
        if (!interfaceList.switchInterface(id)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Failed to switch interface.")
        }
    }

    @GetMapping("/interface/{id}")
    fun getModbusNode(@PathVariable id: Long): ProcessInterface? {
        return interfaceServices.findInterfaceById(id, interfaceList)
    }

    @PostMapping("/intupdate")
    fun updateInterface(@RequestBody json: String) {
//        logger.warning(json)
        if (!interfaceServices.editInterface(interfaceServices.getInterfaceFromJson(json), interfaceList)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Failed to delete interface.")
        }
    }

    @PostMapping("/modbusrwadd/{id}/{type}")
    fun addModbusRw(@PathVariable id: Long, @PathVariable type: String) {
//        logger.warning("Type equals to ${type}. ${type == "read"}")
        val modbusNode = interfaceServices.findInterfaceById(id, interfaceList)
        if ((modbusNode == null) || !interfaceServices.addModbusRW(modbusNode, type == "read")) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Failed to delete interface.")
        }
    }

    @PostMapping("/modbusrwupdate/{id}/{rwid}")
    fun updateModbusRw(@PathVariable id: Long, @PathVariable rwid: Int, @RequestBody mbRw: ModbusRWJSON) {
        val modbusNode = interfaceServices.findInterfaceById(id, interfaceList)
        if ((modbusNode == null) || !interfaceServices.editModbusRW(mbRw, modbusNode, rwid)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Failed to delete interface.")
        }
    }

    @DeleteMapping("/modbusrwdelete/{id}/{type}/{rwid}")
    fun deleteModbusRw(@PathVariable id: Long, @PathVariable type: String, @PathVariable rwid: Int) {
        logger.warning("Modbus ID is ${id}, RW type is ${type}, RW ID is ${rwid}. ${type == "read"}")
        val modbusNode = interfaceServices.findInterfaceById(id, interfaceList)
        if ((modbusNode == null) || !interfaceServices.deleteModbusRW(modbusNode, type == "read", rwid)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Failed to delete interface.")
        }
    }

    @GetMapping("/modbusrw/{id}/{type}/{rwid}")
    fun getModbusRwList(
        @PathVariable id: Long,
        @PathVariable rwid: Int,
        @PathVariable type: String
    ): MutableList<ModbusRWItem> {
        val procInt: ProcessInterface = interfaceServices.findInterfaceById(id, interfaceList)
            ?: throw ResponseStatusException(HttpStatus.CONFLICT, "Wrong Modbus node ID.")
        val modbusRw: ModbusRW =
            interfaceServices.findModbusRWById(rwid, type == "read", procInt.structure as ModbusNode)
                ?: throw ResponseStatusException(HttpStatus.CONFLICT, "Wrong Modbus RW node ID.")
        return modbusRw.mbRwTagList
    }

    @PostMapping("/modbusrwupdate/{id}/{type}/{rwid}")
    fun updateModbusRwItems(
        @PathVariable id: Long,
        @PathVariable rwid: Int,
        @PathVariable type: String,
        @RequestBody mbRwList: MutableList<ModbusRWItemJSON>
    ): Boolean {
        val procInt: ProcessInterface = interfaceServices.findInterfaceById(id, interfaceList)
            ?: throw ResponseStatusException(HttpStatus.CONFLICT, "Wrong Modbus node ID.")
        val modbusRw: ModbusRW =
            interfaceServices.findModbusRWById(rwid, type == "read", procInt.structure as ModbusNode)
                ?: throw ResponseStatusException(HttpStatus.CONFLICT, "Wrong Modbus RW node ID.")
        if (modbusRw.mbRwTagList.size != mbRwList.size) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Array has wrong size")
        }
        return interfaceServices.editModbusRwItemsList(mbRwList, modbusRw)
    }

    @PostMapping("/udpaddtag/{id}/{type}")
    fun addUdpTag(@PathVariable id: Long, @PathVariable type: String) {
        val udpServer = interfaceServices.findInterfaceById(id, interfaceList)
        if ((udpServer == null) || !interfaceServices.udpServerAddTag(udpServer, type == "read")) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Failed to delete interface.")
        }
    }


}