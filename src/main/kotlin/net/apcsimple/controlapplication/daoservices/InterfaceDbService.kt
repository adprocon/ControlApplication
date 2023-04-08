package net.apcsimple.controlapplication.daoservices

import net.apcsimple.controlapplication.model.communication.ProcessInterface
import net.apcsimple.controlapplication.model.communication.modbus.ModbusNode

interface InterfaceDbService {
    fun saveInterface(node: ProcessInterface)
    fun deleteInterface(id: Long)
//    fun saveModbusRW(rw: ModbusRW)
//    fun deleteModbusRW(id: Int)
//    fun saveModbusRWItem(item: ModbusRWItem)
//    fun deleteModbusRWItem(id: Int)
    fun loadInterfacesFromDB(): MutableList<ProcessInterface>
}