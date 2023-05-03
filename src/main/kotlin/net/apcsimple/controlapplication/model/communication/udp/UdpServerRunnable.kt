package net.apcsimple.controlapplication.model.communication.udp

import net.apcsimple.controlapplication.model.communication.ProcessInterface
import net.apcsimple.controlapplication.model.datapoints.Tag
import net.apcsimple.controlapplication.model.datapoints.TagList
import sklog.KotlinLogging
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException
import java.nio.ByteBuffer

private var logger = KotlinLogging.logger {}

class UdpServerRunnable(
    private val proInt: ProcessInterface,
    private val tagList: TagList
) : Runnable {

    var udpServer = proInt.structure as UdpServer
    override fun run() {
        udpServer.socket = try {
            DatagramSocket(udpServer.port)
        } catch (se: SocketException) {
            logger.error("Socket error.")
            proInt.running = false
            proInt.status = ProcessInterface.STOPPED
            return
        }
        var clientAddress: InetAddress
        var clientPort: Int
        logger.info("Strating UDP Server " + proInt.name + ".")
//        logger.info("Start UDP Server listening.")
        proInt.status = ProcessInterface.LISTENING
        while (proInt.running) {
            val request = DatagramPacket(ByteArray(sizeInBytes(udpServer.read)), sizeInBytes(udpServer.read))
            try {
                udpServer.socket!!.receive(request)
                //                log.info("UDP Server request received.");
                writeToTags(request.data)
                clientAddress = request.address
                clientPort = request.port
                val response = DatagramPacket(
                    listToBytes(udpServer.write),
                    sizeInBytes(udpServer.write),
                    clientAddress,
                    clientPort
                )
                udpServer.socket!!.send(response)
                //                log.info("UDP request/response handled.");
            } catch (e: IOException) {
//                logger.error("Request IO exception error.")
                proInt.running = false
//                return
            }
        }
        logger.info("UDP server stopped.")
        proInt.status = ProcessInterface.STOPPED
    }

    private fun sizeInBytes(list: List<Tag>): Int {
        var size = 0
        for (tag in list) {
            when (tag.dataType) {
                "double" -> {
                    size = size + 8
                }

                "int" -> {
                    size = size + 4
                }

                "bool" -> {
                    size = size + 1
                }
            }
        }
        return size
    }

    private fun listToBytes(list: List<Tag>): ByteArray {
        val byteBuffer = ByteBuffer.allocate(sizeInBytes(list))
        for (tag in list) {
            when (tag.dataType) {
                "double" -> {
                    byteBuffer.putDouble(tagList.list[tag.tagName]?.value as Double)
                }

                "int" -> {
                    byteBuffer.putInt(tagList.list[tag.tagName]?.value as Int)
                }

                "bool" -> {
                    byteBuffer.put((if (tagList.list[tag.tagName]?.value as Boolean) 1 else 0).toByte())
                }
            }
        }
        return byteBuffer.array()
    }

    private fun writeToTags(buffer: ByteArray) {
        var index = 0
        for (tag in udpServer.read) {
            when (tag.dataType) {
                "double" -> {
                    try {
                        tagList.list[tag.tagName]?.value = ByteBuffer.wrap(buffer).getDouble(index)
                        index += 8
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                "int" -> {
                    try {
                        tagList.list[tag.tagName]?.value = ByteBuffer.wrap(buffer).getInt(index)
                        index += 4
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                "bool" -> {
                    try {
                        tagList.list[tag.tagName]?.value = ByteBuffer.wrap(buffer)[index].toInt() == 1
                        index++
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}