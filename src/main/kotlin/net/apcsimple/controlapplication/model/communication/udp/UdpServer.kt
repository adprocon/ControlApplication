package net.apcsimple.controlapplication.model.communication.udp

import com.fasterxml.jackson.annotation.JsonIgnore
import net.apcsimple.controlapplication.model.communication.GeneralInterface
import net.apcsimple.controlapplication.model.datapoints.Tag
import net.apcsimple.controlapplication.model.processmodels.GeneralModel
import java.net.DatagramSocket
import java.util.*

class UdpServer(
    var port: Int = 0,
    var read: MutableList<Tag> = ArrayList<Tag>(),
    var write: MutableList<Tag> = ArrayList<Tag>()
) : GeneralInterface() {

    @JsonIgnore
    var socket: DatagramSocket? = null

    override fun startInterface() {
        val runnable = UdpServerRunnable(processInterface, tagList)
        Thread(runnable).start()
    }

    override fun stopInterface() {
        socket?.close();
    }

    override fun copy(): GeneralModel {
        TODO("Not yet implemented")
    }
}
