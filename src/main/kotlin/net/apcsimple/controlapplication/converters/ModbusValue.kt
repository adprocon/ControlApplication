package net.apcsimple.controlapplication.converters

import java.lang.Float.floatToIntBits
import java.lang.Float.intBitsToFloat

class ModbusValue {

    companion object {
        fun registersToDouble(msB: Int, lsB: Int): Double {
            val reg = lsB.toShort().toInt() and 0xFFFF or (msB.toShort().toInt() and 0xFFFF shl 16)
            return intBitsToFloat(reg).toDouble()
        }

        fun doubleToRegisters(out_values: Double): IntArray {
            val msB = (floatToIntBits(out_values.toFloat()) shr 16).toUShort()
            val lsB = floatToIntBits(out_values.toFloat()).toUShort()
            val reg = IntArray(2)
            reg[0] = msB.toInt()
            reg[1] = lsB.toInt()
            return reg
        }

        fun registersToInt32(msB: Int, lsB: Int): Int {
            return lsB.toShort().toInt() and 0xFFFF or (msB.toShort().toInt() and 0xFFFF shl 16)
        }

        fun int32ToRegisters(input: Int):IntArray {
            val msB = ((input shr 16) and 0xFFFF).toShort()
            val lsB = (input and 0xFFFF).toShort()
            val reg = IntArray(2)
            reg[0] = msB.toInt()
            reg[1] = lsB.toInt()
            return reg
        }
    }
}