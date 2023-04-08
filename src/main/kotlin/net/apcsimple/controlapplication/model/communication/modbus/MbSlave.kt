package net.apcsimple.controlapplication.model.communication.modbus

import com.intelligt.modbus.jlibmodbus.data.DataHolder
import com.intelligt.modbus.jlibmodbus.data.ModbusCoils
import com.intelligt.modbus.jlibmodbus.data.ModbusHoldingRegisters
import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException
import com.intelligt.modbus.jlibmodbus.exception.ModbusNumberException
import com.intelligt.modbus.jlibmodbus.exception.ModbusProtocolException
import com.intelligt.modbus.jlibmodbus.net.ModbusConnection
import com.intelligt.modbus.jlibmodbus.slave.ModbusSlave
import com.intelligt.modbus.jlibmodbus.slave.ModbusSlaveFactory
import com.intelligt.modbus.jlibmodbus.slave.ModbusSlaveTCP
import com.intelligt.modbus.jlibmodbus.tcp.TcpParameters
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.util.concurrent.DefaultThreadFactory
import net.apcsimple.controlapplication.model.communication.ProcessInterface
import net.apcsimple.controlapplication.model.communication.modbus.ModbusUtility.Companion.collectCoilsFromTagList
import net.apcsimple.controlapplication.model.communication.modbus.ModbusUtility.Companion.collectRegistersFromTagList
import net.apcsimple.controlapplication.model.communication.modbus.ModbusUtility.Companion.switchRegsBytes
import net.apcsimple.controlapplication.model.communication.modbus.ModbusUtility.Companion.writeCoilsToTagList
import net.apcsimple.controlapplication.model.communication.modbus.ModbusUtility.Companion.writeRegistersToTagList
import sklog.KotlinLogging
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible


private var logger = KotlinLogging.logger {}

/**
 * Modbus Slave class. Includes slave functionality as static method.
 */
class MbSlave {

    companion object {
        fun connect(procInt: ProcessInterface) {
            val settings = procInt.structure as ModbusNode
            val tcpParameters = TcpParameters();
            tcpParameters.host = settings.ipAddress
            tcpParameters.port = settings.port
            tcpParameters.isKeepAlive = true

//            val modbusSlave = MyModbusSlaveTCP(tcpParameters)
            val modbusSlave = ModbusSlaveFactory.createModbusSlaveTCP(tcpParameters)
            modbusSlave.readTimeout = 60000
            modbusSlave.serverAddress = 1
//            Modbus.setLogLevel(Modbus.LogLevel.LEVEL_DEBUG);

            val dataHolder = MbDataHolder()
            dataHolder.inputRegisters = ModbusHoldingRegisters(1000)
            dataHolder.holdingRegisters = ModbusHoldingRegisters(1000)
            dataHolder.discreteInputs = ModbusCoils(1000)
            dataHolder.coils = ModbusCoils(1000)

            modbusSlave.dataHolder = dataHolder

            var start: Long
            var elapsed: Int

            val modbusReads = settings.dataReads
            val modbusWrites = settings.dataWrites

            logger.info("Starting Modbus Slave thread.")
            procInt.status = ProcessInterface.STARTING

            /* Get active connections count */
            val connectionListProperty = ModbusSlave::class.memberProperties.find { it.name == "connectionList" }
            var connectionList: List<ModbusConnection>? = null
            connectionListProperty?.let {
                it.isAccessible = true
                connectionList = it.get(modbusSlave) as List<ModbusConnection>
            }

            while (procInt.running) {

                start = System.nanoTime()

                try {
                    while (!modbusSlave.isListening) {
                        try {
                            logger.info("Modbus Slave: start listening.")
                            modbusSlave.listen()
                            procInt.status = ProcessInterface.LISTENING
                        } catch (e: ModbusIOException) {
                            e.printStackTrace()
                            logger.warning("Failed to start listening.")
                        }
                    }

                    if (connectionList?.isNotEmpty() == true) {
                        procInt.status = ProcessInterface.CONNECTED
                    } else {
                        procInt.status = ProcessInterface.LISTENING
                    }
//                    logger.info(connectionList!!.size.toString())



                    /* Read data from Modbus Table */
                    for (modbusRead in modbusReads) {
                        when (modbusRead.type) {
                            /* Read coils */
                            ModbusRW.INPUTCOIL, ModbusRW.HOLDINGCOIL -> {
                                val coilValues: BooleanArray = when (modbusRead.type) {
                                    ModbusRW.HOLDINGCOIL -> {
                                        modbusSlave.dataHolder.readCoilRange(
                                            modbusRead.address, modbusRead.length
                                        )
                                    }

                                    ModbusRW.INPUTCOIL -> {
                                        modbusSlave.dataHolder.readDiscreteInputRange(
                                            modbusRead.address, modbusRead.length
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
                                val registers: IntArray = when (modbusRead.type) {
                                    ModbusRW.HOLDINGREGISTER -> {
                                        modbusSlave.dataHolder.readHoldingRegisterRange(
                                            modbusRead.address, modbusRead.length
                                        )
                                    }

                                    ModbusRW.INPUTREGISTER -> {
                                        modbusSlave.dataHolder.readInputRegisterRange(
                                            modbusRead.address, modbusRead.length
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
                            ModbusRW.HOLDINGCOIL, ModbusRW.INPUTCOIL -> {
                                val coils = collectCoilsFromTagList(modbusWrite, settings.tagList)

                                when (modbusWrite.type) {
                                    ModbusRW.HOLDINGCOIL -> {
                                        modbusSlave.dataHolder.writeCoilRange(modbusWrite.address - 1, coils)
                                    }

                                    ModbusRW.INPUTCOIL -> {
                                        (modbusSlave.dataHolder as MbDataHolder).writeDiscreteInputRange(
                                            modbusWrite.address - 1, coils
                                        )
                                    }
                                }
                            }
                            /* Write register */
                            ModbusRW.HOLDINGREGISTER, ModbusRW.INPUTREGISTER -> {
                                val registers = collectRegistersFromTagList(modbusWrite, settings.tagList)

                                when (modbusWrite.type) {
                                    ModbusRW.HOLDINGREGISTER -> {
                                        modbusSlave.dataHolder.writeHoldingRegisterRange(
                                            modbusWrite.address,
                                            switchRegsBytes(registers, settings.switchRegisters, settings.switchBytes)
                                        )
                                    }

                                    ModbusRW.INPUTREGISTER -> {
                                        (modbusSlave.dataHolder as MbDataHolder).writeInputRegisterRange(
                                            modbusWrite.address,
                                            switchRegsBytes(registers, settings.switchRegisters, settings.switchBytes)
                                        )
                                    }
                                }
                            }

                            else -> {
                                logger.error("Incorrect modbusWrite ${modbusWrite.name} type.")
                            }
                        }
                    }

                } catch (e: ModbusProtocolException) {
                    e.printStackTrace();
                    exceptionAction(modbusSlave, procInt);
                } catch (e: ModbusNumberException) {
                    e.printStackTrace();
                    exceptionAction(modbusSlave, procInt);
                } catch (e: ModbusIOException) {
                    e.printStackTrace();
                    exceptionAction(modbusSlave, procInt);
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
            if (modbusSlave.isListening) {
                try {
                    modbusSlave.dataHolder = DataHolder()
                    modbusSlave.shutdown()
                    procInt.status = ProcessInterface.STOPPED
                } catch (e: ModbusIOException) {
                    e.printStackTrace()
                }
            }

            logger.info("Exit Modbus Slave thread")

        }

        /**
         * Action performed on any exception.
         */
        private fun exceptionAction(slave: ModbusSlave, procInt: ProcessInterface) {
            logger.error("Connection problems. Restarting interface...")
            try {
                slave.dataHolder = DataHolder()
                slave.shutdown()
                procInt.status = ProcessInterface.ERROR
            } catch (e: ModbusIOException) {
                e.printStackTrace()
            }
        }

    }

}

