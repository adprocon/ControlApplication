package net.apcsimple.controlapplication.model.communication.modbus

import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException
import com.intelligt.modbus.jlibmodbus.exception.ModbusNumberException
import com.intelligt.modbus.jlibmodbus.exception.ModbusProtocolException
import com.intelligt.modbus.jlibmodbus.master.ModbusMaster
import com.intelligt.modbus.jlibmodbus.master.ModbusMasterFactory
import com.intelligt.modbus.jlibmodbus.tcp.TcpParameters
import net.apcsimple.controlapplication.model.communication.ProcessInterface
import net.apcsimple.controlapplication.model.communication.modbus.ModbusUtility.Companion.collectCoilsFromTagList
import net.apcsimple.controlapplication.model.communication.modbus.ModbusUtility.Companion.collectRegistersFromTagList
import net.apcsimple.controlapplication.model.communication.modbus.ModbusUtility.Companion.switchRegsBytes
import net.apcsimple.controlapplication.model.communication.modbus.ModbusUtility.Companion.writeCoilsToTagList
import net.apcsimple.controlapplication.model.communication.modbus.ModbusUtility.Companion.writeRegistersToTagList
import sklog.KotlinLogging


private var logger = KotlinLogging.logger {}

/**
 * Modbus Master class. Includes master functionality as static method.
 */
class MbMaster {

    companion object {
        fun connect(procInt: ProcessInterface) {
            val settings = procInt.structure as ModbusNode
            val tcpParameters = TcpParameters();
            tcpParameters.host = settings.ipAddress
            tcpParameters.port = settings.port
            tcpParameters.isKeepAlive = true

            val modbusMaster = ModbusMasterFactory.createModbusMasterTCP(tcpParameters)
            modbusMaster.setResponseTimeout(1000)

            var start: Long
            var elapsed: Int

            val modbusReads = settings.dataReads
            val modbusWrites = settings.dataWrites

            val timeout = 1000L

            logger.info("Starting Modbus Master thread.")

//            var reconnectTries = 0

            if (modbusMaster.isConnected) {
                procInt.status = ProcessInterface.CONNECTED
            }

            while (procInt.running) {

                /* Connecting to the slave */
                while (!modbusMaster.isConnected) {
                    procInt.status = ProcessInterface.CONNECTING
                    try {
                        logger.info("Connecting to Modbus Slave.")
                        modbusMaster.connect()
                        procInt.status = ProcessInterface.CONNECTED
                        break
                    } catch (e: Exception) {
                        logger.warning("Failed to connect to the slave.")
                    }
//                    reconnectTries++
                    Thread.sleep(timeout)
                    if (!procInt.running) {
                        procInt.status = ProcessInterface.STOPPED
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
                                        modbusMaster.readCoils(
                                            modbusRead.slaveID, modbusRead.address, modbusRead.length
                                        )
                                    }

                                    ModbusRW.INPUTCOIL -> {
                                        booleanArrayOf()
                                        modbusMaster.readDiscreteInputs(
                                            modbusRead.slaveID, modbusRead.address, modbusRead.length
                                        )
                                    }

                                    else -> {
                                        BooleanArray(modbusRead.length)
                                    }
                                }
                                writeCoilsToTagList(
                                    coilValues, modbusRead, settings.tagList.list
                                )
                            }
                            /* Read registers */
                            ModbusRW.INPUTREGISTER, ModbusRW.HOLDINGREGISTER -> {
                                val registers = when (modbusRead.type) {
                                    ModbusRW.HOLDINGREGISTER -> {
                                        modbusMaster.readHoldingRegisters(
                                            modbusRead.slaveID, modbusRead.address, modbusRead.length
                                        )
                                    }

                                    ModbusRW.INPUTREGISTER -> {
                                        modbusMaster.readInputRegisters(
                                            modbusRead.slaveID, modbusRead.address, modbusRead.length
                                        )
                                    }

                                    else -> {
                                        IntArray(modbusRead.length)
                                    }
                                }

                                writeRegistersToTagList(
                                    switchRegsBytes(registers, settings.switchRegisters, settings.switchBytes),
                                    modbusRead, settings.tagList.list
                                )
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
                                val coils = collectCoilsFromTagList(modbusWrite, settings.tagList)
                                modbusMaster.writeMultipleCoils(modbusWrite.slaveID, modbusWrite.address, coils)
                            }
                            /* Write register */
                            ModbusRW.HOLDINGREGISTER -> {
                                /* Preparation */
                                val registers = collectRegistersFromTagList(modbusWrite, settings.tagList)
                                /* Write itself to data points */
                                modbusMaster.writeMultipleRegisters(
                                    modbusWrite.slaveID, modbusWrite.address,
                                    switchRegsBytes(registers, settings.switchRegisters, settings.switchBytes)
                                )
                            }

                            else -> {
                                logger.error("Incorrect modbusWrite ${modbusWrite.name} type.")
                            }
                        }
                    }

                } catch (e: ModbusProtocolException) {
//                    e.printStackTrace()
                    exceptionAction(modbusMaster, procInt);
                } catch (e: ModbusNumberException) {
//                    e.printStackTrace()
                    exceptionAction(modbusMaster, procInt);
                } catch (e: ModbusIOException) {
//                    e.printStackTrace()
                    exceptionAction(modbusMaster, procInt);
                } catch (e: Exception) {
//                    e.printStackTrace()
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
//                elapsed = ((System.nanoTime() - start) / 1000000).toInt()
//                logger.warning("Modbus real cycle is ${elapsed}ms.")

            }

            /* Disconnect Modbus on exit */
            if (modbusMaster.isConnected) {
                try {
                    procInt.status = ProcessInterface.STOPPED
                    modbusMaster.disconnect()
                } catch (e: ModbusIOException) {
                    e.printStackTrace()
                }
            }

            logger.info("Exit Modbus thread")

        }

        /**
         * Action performed on any exception.
         */
        private fun exceptionAction(master: ModbusMaster, procInt: ProcessInterface) {
            logger.error("Connection problems. Resetting connection...")
            try {
                master.disconnect()
                procInt.status = ProcessInterface.ERROR
            } catch (e: ModbusIOException) {
                e.printStackTrace()
            }
        }

    }
}
