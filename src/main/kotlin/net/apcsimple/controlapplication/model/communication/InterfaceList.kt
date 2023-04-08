package net.apcsimple.controlapplication.model.communication

import net.apcsimple.controlapplication.model.communication.modbus.ModbusNode
import net.apcsimple.controlapplication.services.InterfaceServices
import org.springframework.beans.factory.ObjectFactory
import org.springframework.stereotype.Component

@Component("interfaceList")
class InterfaceList(
    val interfaceServices: InterfaceServices,
//    val modbusObjectFactory: ObjectFactory<ModbusNode>
) {
    val list: MutableList<ProcessInterface> = mutableListOf()

    fun load() {
        list.addAll(interfaceServices.loadModbus())
    }

    fun addNewInterface(intrfc: ProcessInterface): Boolean {
        return if (intrfc.type == ProcessInterface.MODBUSMASTER || intrfc.type == ProcessInterface.MODBUSSLAVE) {
            val newInt = ProcessInterface(intrfc.name, intrfc.type)
            newInt.interfaceServices = interfaceServices
            val newMbNode = ModbusNode()
            newMbNode.processInterface = newInt
            newInt.structure = newMbNode
//            newInt.name = intrfc.name
//            newInt.type = intrfc.type
//            if (intrfc.type == GeneralInterface.MODBUSSLAVE) newModbusNode.master = false
            interfaceServices.addInterface(newInt, this)
            true
        }
        else {
            false
        }
    }

    fun deleteInterface(id: Long): Boolean {
        val intrfc = list.find { item -> item.id == id } ?: return false
        return interfaceServices.deleteInterface(intrfc.id ?: 0, this)
    }

    fun switchInterface(id: Long): Boolean {
        val intrfc = list.find { item -> item.id == id } ?: return false
        return if (intrfc.type == ProcessInterface.MODBUSMASTER || intrfc.type == ProcessInterface.MODBUSSLAVE) {
            val procInt = interfaceServices.findInterfaceById(id, this) ?: return false
            procInt.running = !(procInt.running)
//            interfaceService.saveInterface(modbusNode)
            true
        } else {
            false
        }
    }


    fun findName(name: String): ProcessInterface? {
        return list.find { item -> item.name == name }
    }

    fun findById(id: Long): ProcessInterface? {
        return list.find { item -> item.id == id }
    }

}