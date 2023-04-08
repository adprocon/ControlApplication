package net.apcsimple.controlapplication.model.communication.modbus

import com.intelligt.modbus.jlibmodbus.data.DataHolder
import com.intelligt.modbus.jlibmodbus.exception.IllegalDataAddressException
import com.intelligt.modbus.jlibmodbus.exception.IllegalDataValueException

class MbDataHolder(): DataHolder() {
    @Throws(IllegalDataAddressException::class, IllegalDataValueException::class)
    fun writeDiscreteInputRange(offset: Int, range: BooleanArray?) {
        discreteInputs.setRange(offset, range)
    }

    @Throws(IllegalDataAddressException::class, IllegalDataValueException::class)
    fun writeInputRegisterRange(offset: Int, range: IntArray?) {
        inputRegisters.setRange(offset, range)
    }


}