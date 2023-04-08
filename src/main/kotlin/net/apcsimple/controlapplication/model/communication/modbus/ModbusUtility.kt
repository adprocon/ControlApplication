package net.apcsimple.controlapplication.model.communication.modbus

import net.apcsimple.controlapplication.converters.ModbusValue
import net.apcsimple.controlapplication.model.datapoints.Tag
import net.apcsimple.controlapplication.model.datapoints.TagList
import sklog.KotlinLogging

private var logger = KotlinLogging.logger {}

class ModbusUtility {

    companion object {
        /**
         * This function writes registers integer values to the data points
         * taking Modbus settings into account.
         */
        fun writeRegistersToTagList(registers: IntArray, mbRead: ModbusRW, tagList: Map<String, Tag>) {
            for ((i, mbTagItem) in mbRead.mbRwTagList.withIndex()) {
                /* If default tag name "Tag name", then skip. */
                if (mbTagItem.tagName != ModbusRWItem.DEFAULTTAGNAME) {
                    val name: String = mbTagItem.tagName
                    if (tagList.containsKey(name)) {
                        /* If data point type is DOUBLE*/
                        if (tagList[name]!!.dataType == Tag.TAGDOUBLE) {
                            if (mbRead.mbRwTagList[i].ieee754) {
                                tagList[name]!!.value = ModbusValue.registersToDouble(registers[i], registers[i + 1])
                            } else {
                                tagList[name]!!.value =
                                    registers[i] / mbRead.mbRwTagList[i].gain + mbRead.mbRwTagList[i].offset
                            }
                            /* If data point type is INTEGER */
                        } else if (tagList[name]!!.dataType == Tag.TAGINTEGER)
                            if (mbRead.mbRwTagList[i].ieee754) {
                                tagList[name]!!.value = ModbusValue.registersToInt32(registers[i], registers[i + 1])
                            } else {
                                tagList[name]!!.value =
                                    registers[i] / mbRead.mbRwTagList[i].gain + mbRead.mbRwTagList[i].offset
                            }
                        /* If data point type is different */
                        else {
                            logger.error("Tag $name has different types in tag list and in Modbus configuration.")
                        }
                    } else {
                        logger.error("No tag $name in the tag list.")
                    }

                }
            }
        }

        /**
         * This function writes coil values to the data points
         * taking Modbus settings into account.
         */
        fun writeCoilsToTagList(coils: BooleanArray, mbRead: ModbusRW, tagList: Map<String, Tag>) {
            for ((i, mbTagItem) in mbRead.mbRwTagList.withIndex()) {
                if (mbTagItem.tagName != (ModbusRWItem.DEFAULTTAGNAME)) {
                    val name: String = mbTagItem.tagName
                    if (tagList.containsKey(name)) {
                        if (tagList[name]!!.dataType == Tag.TAGBOOLEAN) {
                            tagList[name]!!.value = coils[i]
                        } else {
                            logger.error("Tag $name has different types in tag list and in Modbus configuration.")
                        }
                    } else {
                        logger.error("No tag $name in the tag list.")
                    }
                }
            }
        }

        /**
         * This function switches bytes in registers and two consequent registers, if this is configured.
         */
        fun switchRegsBytes(data: IntArray, switchRegs: Boolean, switchBytes: Boolean): IntArray {
            val output = mutableListOf<Int>();
            for ((i, number) in data.withIndex()) {
                var num: Int = number
                if (switchBytes) {
                    num = (number and 0xFF00 shr 8) + (number and 0xFF shl 8)
                }
                if (switchRegs) {
                    if (i % 2 > 0) {
                        output[i - 1] = num
                    } else {
                        output += 0
                        output += num
                    }
                } else {
                    output += num
                }
            }
            return output.toIntArray()
        }

        fun collectCoilsFromTagList(modbusWrite: ModbusRW, tagList: TagList): BooleanArray {
            val coils = BooleanArray(modbusWrite.length)
            for ((i, modbusTagItem) in modbusWrite.mbRwTagList.withIndex()) {
                try {
                    coils[i] = tagList.list[modbusTagItem.tagName]!!.value.toString()
                        .toBoolean()
                } catch (e: Exception) {
                    coils[i] = false
                }
            }
            return coils
        }

        fun collectRegistersFromTagList(modbusWrite: ModbusRW, tagList: TagList): IntArray {
            val registers = IntArray(modbusWrite.length)
            for ((i, modbusTagItem) in modbusWrite.mbRwTagList.withIndex()) {
                if (tagList.list.containsKey(modbusTagItem.tagName)) {
                    val type = tagList.list[modbusTagItem.tagName]!!.dataType
                    try {
                        if (modbusTagItem.ieee754) {
                            val writeRegisters: IntArray = when (type) {
                                Tag.TAGDOUBLE -> {
                                    ModbusValue.doubleToRegisters(
                                        tagList.list[modbusTagItem.tagName]?.value.toString().toDouble()
                                    )
                                }

                                Tag.TAGINTEGER -> {
                                    ModbusValue.int32ToRegisters(
                                        tagList.list[modbusTagItem.tagName]?.value.toString().toInt()
                                    )
                                }

                                else -> {
                                    intArrayOf(2)
                                }
                            }
                            registers[i] = writeRegisters[0]
                            registers[i + 1] = writeRegisters[1]
                        } else {
                            var value = tagList.list[modbusTagItem.tagName]?.value.toString().toDouble()
                            value = value * modbusTagItem.gain + modbusTagItem.offset
                            registers[i] = value.toInt()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            return registers
        }
    }
}