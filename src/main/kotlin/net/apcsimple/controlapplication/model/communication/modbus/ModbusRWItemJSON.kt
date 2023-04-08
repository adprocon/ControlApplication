package net.apcsimple.controlapplication.model.communication.modbus

class ModbusRWItemJSON(
    override var modbusAddress: Int = 0,
    override var tagName: String = "Not used",
    override var gain: Double = 0.0,
    override var offset: Double = 0.0,
    override var blocked: Boolean = false,
//    override val id: Int = 0,
    override var ieee754: Boolean = false
): ModbusRWItemPrototype {}