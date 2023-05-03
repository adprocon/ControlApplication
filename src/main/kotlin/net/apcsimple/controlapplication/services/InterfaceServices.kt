package net.apcsimple.controlapplication.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import net.apcsimple.controlapplication.converters.ObjectJsonConverter
import net.apcsimple.controlapplication.daoservices.InterfaceDbService
import net.apcsimple.controlapplication.model.communication.*
import net.apcsimple.controlapplication.model.communication.modbus.*
import net.apcsimple.controlapplication.model.communication.udp.UdpServer
import net.apcsimple.controlapplication.model.datapoints.Tag
import net.apcsimple.controlapplication.model.datapoints.TagList
import org.springframework.stereotype.Service
import java.util.*

@Service
class InterfaceServices(
    private val interfaceDbService: InterfaceDbService,
    private var tagList: TagList
) {

    fun addInterface(newInt: ProcessInterface, interfaceList: InterfaceList): Boolean {
        if (interfaceList.findName(newInt.name) != null) {
            return false
        }
        return try {
            saveInterface(newInt)
            interfaceList.list.add(newInt)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun editInterface(updatedInt: ProcessInterface, interfaceList: InterfaceList): Boolean {
        val inter: ProcessInterface? = interfaceList.findName(updatedInt.name)
        if (inter != null) {
            if (inter.type != updatedInt.type) {
                return false
            }
            if (inter.id != updatedInt.id) {
                return false
            }
        }

        val existingInt = findInterfaceById(updatedInt.id!!, interfaceList) ?: return false
        if (existingInt.running) return false

        return try {
            existingInt.name = updatedInt.name
            existingInt.structure = updatedInt.structure
            existingInt.structure?.processInterface = existingInt
            existingInt.structure?.tagList = tagList
            saveInterface(existingInt)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteInterface(id: Long, interfaceList: InterfaceList): Boolean {
        val existingMB = findInterfaceById(id, interfaceList) ?: return false
        return try {
            interfaceDbService.deleteInterface(id)
            interfaceList.list.remove(existingMB)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun saveInterface(processInterface: ProcessInterface) {
        processInterface.structureJSON = ObjectJsonConverter.convertToJSON(processInterface.structure)
        interfaceDbService.saveInterface(processInterface)
    }

    fun findInterfaceById(id: Long, interfaceList: InterfaceList): ProcessInterface? {
        return interfaceList.list.find { it.id == id }
    }

    fun addModbusRW(procInt: ProcessInterface, typeRead: Boolean): Boolean {
//        val modbusRW = modbusRwFactory.`object`
        val modbusRW = ModbusRW()
        modbusRW.modbusNode = procInt.structure as ModbusNode
        modbusRW.read = typeRead
        val readWrite = if (typeRead) {
            (procInt.structure as ModbusNode).dataReads
        } else {
            (procInt.structure as ModbusNode).dataWrites
        }
        return try {
            readWrite.add(modbusRW)
            saveInterface(procInt)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun editModbusRW(editedModbusRW: ModbusRWJSON, procInt: ProcessInterface, id: Int): Boolean {
        val modbusRW = findModbusRWById(id, editedModbusRW.read, procInt.structure as ModbusNode) ?: return false
        return try {
            modbusRW.address = editedModbusRW.address
            modbusRW.length = editedModbusRW.length
            modbusRW.name = editedModbusRW.name
            modbusRW.slaveID = editedModbusRW.slaveID
            modbusRW.type = editedModbusRW.type
            saveInterface(procInt)
//            modbusDbService.saveModbusRW(modbusRW)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteModbusRW(procInt: ProcessInterface, read: Boolean, id: Int): Boolean {
//        val modbusRW = findModbusRWById(id, read, procInt.structure as ModbusNode) ?: return false
        val readWrite = if (read) {
            (procInt.structure as ModbusNode).dataReads
        } else {
            (procInt.structure as ModbusNode).dataWrites
        }
        val modbusRW = readWrite[id]
        return try {
            modbusRW.length = 0
            readWrite.remove(modbusRW)
            saveInterface(procInt)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun findModbusRWById(rwId: Int, read: Boolean, modbusNode: ModbusNode): ModbusRW? {

        val dataRW = if (read) {
            modbusNode.dataReads
        } else {
            modbusNode.dataWrites
        }
        return dataRW[rwId]
    }

    fun addModbusRwItem(item: ModbusRWItem): Boolean {
        return try {
//            interfaceDbService.saveModbusRWItem(item)
            item.noderw?.mbRwTagList?.add(item)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun editModbusRwItem(editedItem: ModbusRWItemJSON, modbusRW: ModbusRW, id: Int): Boolean {
        return try {
            val item = findItemById(id, modbusRW) ?: return false
            item.tagName = editedItem.tagName
            item.gain = editedItem.gain
            item.offset = editedItem.offset
            item.ieee754 = editedItem.ieee754
            modbusRW.modbusNode?.processInterface?.let { saveInterface(it) }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun editModbusRwItemsList(editedItemsList: MutableList<ModbusRWItemJSON>, modbusRW: ModbusRW): Boolean {
        return try {
            editedItemsList.forEachIndexed { index, item ->
                run {
                    editModbusRwItem(item, modbusRW, index)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteModbusRwItem(id: Int, modbusRW: ModbusRW): Boolean {
        val item = findItemById(id, modbusRW) ?: return false
        return try {
            modbusRW.mbRwTagList.remove(item)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun findItemById(id: Int, modbusRW: ModbusRW): ModbusRWItem? {
        return if (modbusRW.mbRwTagList.size > id) {
            modbusRW.mbRwTagList[id]
        } else {
            null
        }
    }

    fun loadInterfaceList(): MutableList<ProcessInterface> {
        val interfacesList = interfaceDbService.loadInterfacesFromDB()
        interfacesList.forEach { procInt ->
            run {
                procInt.interfaceServices = this
            }
        }
        return interfacesList
    }
    fun loadModbus(interfacesList: MutableList<ProcessInterface>) {
        interfacesList.forEach { procInt ->
            run {
                if (procInt.type == ProcessInterface.MODBUSMASTER || procInt.type == ProcessInterface.MODBUSSLAVE) {
                    procInt.structure =
                        ObjectJsonConverter.convertFromJSON(procInt.structureJSON, ModbusNode::class.java)
                    val modbusNode = procInt.structure as ModbusNode
                    modbusNode.processInterface = procInt
                    setModbusRwParentLinks(modbusNode.dataReads, modbusNode)
                    setModbusRwParentLinks(modbusNode.dataWrites, modbusNode)
                    modbusNode.tagList = tagList
                }

            }
        }
    }

    fun setModbusRwParentLinks(rwList: MutableList<ModbusRW>, modbusNode: ModbusNode) {
        rwList.forEach { rw ->
            run {
                rw.modbusNode = modbusNode
                rw.mbRwTagList.forEach { item ->
                    run {
                        item.noderw = rw
                    }
                }
            }
        }

    }

    fun getInterfaceFromJson(json: String): ProcessInterface {
        val procInt: ProcessInterface
        var structure: GeneralInterface? = null
        val mapper = ObjectMapper()
        val node: ObjectNode = mapper.readValue(json, ObjectNode::class.java)
        when (node.get("type").textValue()) {
            ProcessInterface.MODBUSMASTER, ProcessInterface.MODBUSSLAVE -> {
                structure = mapper.convertValue(node.get("structure"), ModbusNode::class.java)
            }
            ProcessInterface.UDPSERVER -> {
                structure = mapper.convertValue(node.get("structure"), UdpServer::class.java)
            }
            /* Add another model types conversion here, when needed. */
            else -> {}
        }
        node.remove("structure")
        procInt = mapper.convertValue(node, ProcessInterface::class.java)
        procInt.structure = structure
        return procInt
    }

    fun loadUdpServer(interfacesList: MutableList<ProcessInterface>) {
        interfacesList.forEach { procInt ->
            run {
                if (procInt.type == ProcessInterface.UDPSERVER) {
                    procInt.structure =
                        ObjectJsonConverter.convertFromJSON(procInt.structureJSON, UdpServer::class.java)
                    val udpServer = procInt.structure as UdpServer
                    udpServer.processInterface = procInt
                    udpServer.tagList = tagList
                    for (i in udpServer.read.indices) {
                        udpServer.read[i] = udpServer.read[i].id?.let { udpServer.tagList.idList[udpServer.read[i].id] } ?:
                        Tag("Not used", Tag.TAGDOUBLE, 0.0)
                    }
                    for (i in udpServer.write.indices) {
                        udpServer.write[i] = udpServer.write[i].id?.let { udpServer.tagList.idList[udpServer.write[i].id] } ?:
                                Tag("Not used", Tag.TAGDOUBLE, 0.0)
                    }
                    udpServer.tagList = tagList
                }
            }
        }

    }

    fun udpServerRemoveTag(udpServer: UdpServer, type: String, index: Int): Boolean {
        try {
            if (type == "read") {
                udpServer.read.removeAt(index)
            } else {
                udpServer.write.removeAt(index)
            }
        } catch (e: Exception) {
            return false
        }
        saveInterface(udpServer.processInterface)
        return true
    }

    fun udpServerMoveTag(udpServer: UdpServer, type: String, index: Int, dir: String): Boolean {
        try {
            val list: List<Tag?>?
            list = if (type == "read") {
                udpServer.read
            } else {
                udpServer.write
            }
            if (dir == "up") {
                if (index > 0) {
                    Collections.swap(list, index, index - 1)
                }
            }
            if (dir == "down") {
                if (index < udpServer.read!!.size - 1) {
                    Collections.swap(list, index, index + 1)
                }
            }
            saveInterface(udpServer.processInterface)
        } catch (e: Exception) {
            return false
        }
        return true
    }

    fun udpServerUpdate(updInt: ProcessInterface, interfaceList: InterfaceList): Boolean {
        val existingInt = updInt.id?.let { findInterfaceById(it, interfaceList) }
        try {
            existingInt!!.name = updInt.name
            val existingUdpServer = existingInt.structure as UdpServer
            val updUdpServer = updInt.structure as UdpServer
            existingUdpServer.port = updUdpServer.port
            existingUdpServer.read = updUdpServer.read
            existingUdpServer.write = updUdpServer.write
            for (i in existingUdpServer.read.indices) {
                existingUdpServer.read[i] = tagList.list[existingUdpServer.read[i].tagName] ?:
                Tag("Not used", Tag.TAGDOUBLE, 0.0)
            }
            for (i in existingUdpServer.write.indices) {
                existingUdpServer.write[i] = tagList.list[existingUdpServer.write[i].tagName] ?:
                Tag("Not used", Tag.TAGDOUBLE, 0.0)
            }
            saveInterface(existingInt)
        } catch (e: Exception) {
            return false
        }
        return true
    }

    fun udpServerAddTag(existingInt: ProcessInterface, read: Boolean): Boolean {
        val udpServer = existingInt.structure as UdpServer
        if (read) {
            udpServer.read.add(Tag.newTag())
        } else {
            udpServer.write.add(Tag.newTag())
        }
        saveInterface(existingInt)
        return true
    }
}