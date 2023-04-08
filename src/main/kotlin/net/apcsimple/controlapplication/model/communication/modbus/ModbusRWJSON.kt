package net.apcsimple.controlapplication.model.communication.modbus

class ModbusRWJSON (
    var read: Boolean = true,
    var name: String = "Instance",
    var slaveID: Int = 0,
    var address: Int = 0,
    var type: String = ModbusRW.HOLDINGREGISTER,
    val rwid: Int? = null,
    var length: Int = 0
    )
    {}