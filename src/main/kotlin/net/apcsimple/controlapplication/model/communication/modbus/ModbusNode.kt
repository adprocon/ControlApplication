package net.apcsimple.controlapplication.model.communication.modbus

import com.fasterxml.jackson.annotation.JsonIgnore
//import net.apcsimple.controlapplication.converters.IpConverter
import net.apcsimple.controlapplication.model.communication.GeneralInterface
import net.apcsimple.controlapplication.model.communication.ProcessInterface
import net.apcsimple.controlapplication.model.datapoints.TagList
import net.apcsimple.controlapplication.model.processmodels.GeneralModel
import sklog.KotlinLogging
import java.net.InetAddress
import kotlin.jvm.Transient

private var logger = KotlinLogging.logger {}

class ModbusNode(

//    @Convert(converter = IpConverter::class)
    var ipAddress: InetAddress? = InetAddress.getByName("127.0.0.0"),
    var port: Int = 502,
    var readWriteCycle: Int = 1000,
    var switchBytes: Boolean = false,
    var switchRegisters: Boolean = false,

    val dataReads: MutableList<ModbusRW> = mutableListOf(),
    val dataWrites: MutableList<ModbusRW> = mutableListOf(),

    ): GeneralInterface() {

//    @JsonIgnore
//    lateinit var tagList: TagList

    override fun startInterface() {
        val runnable = ModbusNodeRunnable(processInterface)
        Thread(runnable).start()
    }

    override fun stopInterface() {
        TODO("Not yet implemented")
    }

    override fun copy(): GeneralModel {
        TODO("Not yet implemented")
    }

}