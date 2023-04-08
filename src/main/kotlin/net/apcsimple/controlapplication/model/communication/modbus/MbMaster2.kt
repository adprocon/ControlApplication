package net.apcsimple.controlapplication.model.communication.modbus

import com.digitalpetri.modbus.codec.Modbus
import com.digitalpetri.modbus.master.ModbusTcpMaster
import com.digitalpetri.modbus.master.ModbusTcpMasterConfig
import com.digitalpetri.modbus.requests.ReadHoldingRegistersRequest
import com.digitalpetri.modbus.requests.ReadInputRegistersRequest
import com.digitalpetri.modbus.responses.ReadHoldingRegistersResponse
import io.netty.buffer.ByteBufUtil
import net.apcsimple.controlapplication.model.communication.ProcessInterface
import sklog.KotlinLogging
import java.time.Duration
import java.util.concurrent.CompletableFuture

private var logger = KotlinLogging.logger {}

/**
 * Modbus Master class. Includes master functionality as static method.
 */
class MbMaster2 {

    companion object {
        fun connect(procInt: ProcessInterface) {
            val settings = procInt.structure as ModbusNode
            val config = ModbusTcpMasterConfig.Builder(settings.ipAddress?.hostAddress)
                .setPort(settings.port)
                .setTimeout(Duration.ofMillis(1000))
                .build()
            logger.info("Address is ${config.address}:${config.port}")

            val master = ModbusTcpMaster(config)

            var start: Long
            var elapsed: Int

            val modbusReads = settings.dataReads
            val modbusWrites = settings.dataWrites

            val timeout = 2000L

            logger.info("Starting Modbus Master thread.")

            var reconnectTries = 0
            var connected = false

            while (procInt.running) {

                /* Connecting to the slave */
//                while (!modbusMaster.isConnected) {
                while (!connected) {
                    try {
                        logger.info("Connecting to Modbus Slave.")
                        master.connect()
                        connected = true
//                        modbusMaster.connect()
                        break
                    } catch (e: Exception) {
                        logger.warning("Failed to connect to the slave.")
                    }
                    reconnectTries++
                    Thread.sleep(timeout)
                    if (!procInt.running) {
                        return
                    }
                }

                start = System.nanoTime()

                try {

                    /* Read data from Modbus Slave */
                    for (modbusRead in modbusReads) {
                        when (modbusRead.type) {
                            /* Read coils */
                            ModbusRW.INPUTCOIL, ModbusRW.HOLDINGCOIL -> {
                                val coilValues: BooleanArray = when (modbusRead.type) {
                                    ModbusRW.HOLDINGCOIL -> {
                                        booleanArrayOf()
//                                        modbusMaster.readCoils(
//                                            modbusRead.slaveID, modbusRead.address, modbusRead.length
//                                        )
                                    }

                                    ModbusRW.INPUTCOIL -> {
                                        booleanArrayOf()
//                                        modbusMaster.readDiscreteInputs(
//                                            modbusRead.slaveID, modbusRead.address, modbusRead.length
//                                        )
                                    }

                                    else -> {
                                        BooleanArray(modbusRead.length)
                                    }
                                }
                                ModbusUtility.writeCoilsToTagList(
                                    coilValues, modbusRead, settings.tagList.list
                                )
                            }
                            /* Read registers */
                            ModbusRW.INPUTREGISTER, ModbusRW.HOLDINGREGISTER -> {
//                                val registers = when (modbusRead.type) {
                                val request = when (modbusRead.type) {
                                    ModbusRW.HOLDINGREGISTER -> {
//                                        modbusMaster.readHoldingRegisters(
//                                            modbusRead.slaveID, modbusRead.address, modbusRead.length
//                                        )
                                        ReadHoldingRegistersRequest(modbusRead.address, modbusRead.length)
                                    }

                                    ModbusRW.INPUTREGISTER -> {
                                        ReadInputRegistersRequest(modbusRead.address, modbusRead.length)
//                                        modbusMaster.readInputRegisters(
//                                            modbusRead.slaveID, modbusRead.address, modbusRead.length
//                                        )
                                    }

                                    else -> {
                                        ReadHoldingRegistersRequest(modbusRead.address, modbusRead.length)
//                                        IntArray(modbusRead.length)
                                    }
                                }
//                                logger.warning(request.functionCode.name)

                                val future: CompletableFuture<ReadHoldingRegistersResponse> =
                                    master.sendRequest(request, modbusRead.slaveID)

                                future.whenCompleteAsync({ response, ex ->
                                    if (response != null) {
                                        val regs = ByteBufUtil.hexDump(response.registers)
                                        logger.info(regs)
                                        logger.warning(master.responseCounter.count.toString())
                                        val registers = (0 until response.registers.readableBytes() step 2)
                                            .map {response.registers.getShort(it).toInt()}
                                            .toIntArray()
                                        ModbusUtility.writeRegistersToTagList(
                                            ModbusUtility.switchRegsBytes(
                                                registers,
                                                settings.switchRegisters,
                                                settings.switchBytes
                                            ),
                                            modbusRead, settings.tagList.list
                                        )
                                        response.registers.clear()
//                                        ReferenceCountUtil.release(response)
                                    } else {
                                        logger.error("Completed exceptionally, message=${ex.localizedMessage}")
                                        master.disconnect()
                                        connected = false
                                    }
                                }, Modbus.sharedExecutor())

//                                writeRegistersToTagList(
//                                    switchRegsBytes(registers, settings.switchRegisters, settings.switchBytes),
//                                    modbusRead, settings.tagList.list
//                                )
                            }

                            else -> {
                                logger.error("Incorrect modbusRead ${modbusRead.name} type.")
                            }
                        }
                    }


                    /* Write data to Modbus Slave */
                    for (modbusWrite in modbusWrites) {
                        when (modbusWrite.type) {
                            /* Write coil */
                            ModbusRW.HOLDINGCOIL -> {
                                val coils = ModbusUtility.collectCoilsFromTagList(modbusWrite, settings.tagList)
//                                val coils = BooleanArray(modbusWrite.length)
//                                for ((i, modbusTagItem) in modbusWrite.mbRwTagList.withIndex()) {
//                                    try {
//                                        coils[i] = settings.tagList.list[modbusTagItem.tagName]!!.value.toString()
//                                            .toBoolean()
//                                    } catch (e: Exception) {
//                                        coils[i] = false
//                                    }
//                                }
//                                modbusMaster.writeMultipleCoils(modbusWrite.slaveID, modbusWrite.address, coils)
                            }
                            /* Write register */
                            ModbusRW.HOLDINGREGISTER -> {
                                /* Preparation */
                                val registers = ModbusUtility.collectRegistersFromTagList(modbusWrite, settings.tagList)
//                                val registers = IntArray(modbusWrite.length)
//                                for ((i, modbusTagItem) in modbusWrite.mbRwTagList.withIndex()) {
//                                    if (settings.tagList.list.containsKey(modbusTagItem.tagName)) {
//                                        val type = settings.tagList.list[modbusTagItem.tagName]!!.dataType
//                                        try {
//                                            if (modbusTagItem.ieee754) {
//                                                val writeRegisters: IntArray = when (type) {
//                                                    Tag.TAGDOUBLE -> {
//                                                        ModbusValue.doubleToRegisters(
//                                                            settings.tagList.list[modbusTagItem.tagName]!!.value.toString()
//                                                                .toDouble()
//                                                        )
//                                                    }
//
//                                                    Tag.TAGINTEGER -> {
//                                                        ModbusValue.int32ToRegisters(
//                                                            settings.tagList.list[modbusTagItem.tagName]!!.value.toString()
//                                                                .toInt()
//                                                        )
//                                                    }
//
//                                                    else -> {
//                                                        intArrayOf(2)
//                                                    }
//                                                }
//                                                registers[i] = writeRegisters[0]
//                                                registers[i + 1] = writeRegisters[1]
//                                            } else {
//                                                var value =
//                                                    settings.tagList.list[modbusTagItem.tagName]!!.value.toString()
//                                                        .toDouble()
//                                                value = value * modbusTagItem.gain + modbusTagItem.offset
//                                                registers[i] = value.toInt()
//                                            }
//                                        } catch (e: Exception) {
//                                            e.printStackTrace()
//                                        }
//                                    }
//                                }
                                /* Write itself to data points */
//                                modbusMaster.writeMultipleRegisters(
//                                    modbusWrite.slaveID, modbusWrite.address,
//                                    switchRegsBytes(registers, settings.switchRegisters, settings.switchBytes)
//                                )
                            }

                            else -> {
                                logger.error("Incorrect modbusWrite ${modbusWrite.name} type.")
                            }
                        }
                    }
//                    logger.info("Transaction id is ${modbusMaster.transactionId}.")

                } catch (e: Exception) {
                    e.printStackTrace()
                }

                /* Wait until end of Modbus cycle */
                elapsed = ((System.nanoTime() - start) / 1000000).toInt()
                try {
                    val timeout: Int = settings.readWriteCycle - elapsed
                    if (timeout > 0) {
                        Thread.sleep((timeout).toLong())
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                elapsed = ((System.nanoTime() - start) / 1000000).toInt()
//                logger.warning("Modbus real cycle is ${elapsed}ms.")

            }

            /* Disconnect Modbus on exit */
            master.disconnect()

            logger.info("Exit Modbus thread")

        }

    }
}