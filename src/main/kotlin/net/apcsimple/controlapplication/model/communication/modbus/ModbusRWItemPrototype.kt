package net.apcsimple.controlapplication.model.communication.modbus

interface ModbusRWItemPrototype {
    var modbusAddress: Int
    var tagName: String
    var gain: Double
    var offset: Double
    var blocked: Boolean
    var ieee754: Boolean
//    val id: Int
}