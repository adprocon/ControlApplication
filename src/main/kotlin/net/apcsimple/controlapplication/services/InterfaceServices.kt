package net.apcsimple.controlapplication.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import net.apcsimple.controlapplication.converters.ObjectJsonConverter
import net.apcsimple.controlapplication.daoservices.InterfaceDbService
import net.apcsimple.controlapplication.model.communication.*
import net.apcsimple.controlapplication.model.communication.modbus.*
import net.apcsimple.controlapplication.model.datapoints.TagList
import org.springframework.stereotype.Service

@Service
class InterfaceServices(
    private val interfaceDbService: InterfaceDbService,
//    private var modbusRwFactory: ObjectFactory<ModbusRW>,
//    private var modbusRwItemFactory: ObjectFactory<ModbusRWItem>,
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

    fun loadModbus(): MutableList<ProcessInterface> {
        val interfacesList = interfaceDbService.loadInterfacesFromDB()
        interfacesList.forEach { procInt ->
            run {
                procInt.interfaceServices = this
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

//                run {
//                node.tagList = tagList
//                node.dataReads.forEach { read ->
//                    run {
//                        read.interfaceService = this
//                        read.modbusRWItemFactory = modbusRwItemFactory
//                    }
//                }
//                node.dataWrites.forEach { write ->
//                    run {
//                        write.interfaceService = this
//                        write.modbusRWItemFactory = modbusRwItemFactory
//                    }
//                }

        }
        return interfacesList
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
            /* Add another model types conversion here, when needed. */
            else -> {}
        }
        node.remove("structure")
        procInt = mapper.convertValue(node, ProcessInterface::class.java)
        procInt.structure = structure
        return procInt
    }

}