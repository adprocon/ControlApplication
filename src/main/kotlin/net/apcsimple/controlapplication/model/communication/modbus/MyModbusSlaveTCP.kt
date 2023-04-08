package net.apcsimple.controlapplication.model.communication.modbus

import com.intelligt.modbus.jlibmodbus.net.ModbusConnection
import com.intelligt.modbus.jlibmodbus.slave.ModbusSlaveTCP
import com.intelligt.modbus.jlibmodbus.tcp.TcpParameters

class MyModbusSlaveTCP(private val tcp: TcpParameters): ModbusSlaveTCP(tcp) {
//    override val connectionList: List<ModbusConnection> = ArrayList()
}