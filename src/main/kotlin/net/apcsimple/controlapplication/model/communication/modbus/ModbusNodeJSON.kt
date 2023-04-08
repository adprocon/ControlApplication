package net.apcsimple.controlapplication.model.communication.modbus

import java.net.InetAddress

data class ModbusNodeJSON(

    var name: String,
    var type: String,
    var running: Boolean = false,
    var ipAddress: InetAddress? = InetAddress.getByName("127.0.0.0"),
    var port: Int = 0,
    val master: Boolean = true,
    var readWriteCycle: Int = 1000,
    var switchBytes: Boolean = false,
    var switchRegisters: Boolean = false,
    val id: Int? = null

){
}