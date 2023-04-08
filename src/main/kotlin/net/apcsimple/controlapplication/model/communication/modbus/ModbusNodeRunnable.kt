package net.apcsimple.controlapplication.model.communication.modbus

import net.apcsimple.controlapplication.model.communication.ProcessInterface

class ModbusNodeRunnable(
    private val proInt: ProcessInterface
) : Runnable {

    override fun run() {
        if (proInt.type == ProcessInterface.MODBUSMASTER) {
            MbMaster.connect(proInt);
        } else {
            MbSlave.connect(proInt);
        }
    }

}