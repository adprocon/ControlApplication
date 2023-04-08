package net.apcsimple.controlapplication.model.communication

import com.fasterxml.jackson.annotation.JsonIgnore
import net.apcsimple.controlapplication.services.InterfaceServices
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

/**
 * General interface class to combine all available interfaces into one list. Should not be used to create any entity.
 */
@Entity
class ProcessInterface (
    /**
     * Used to identify the interface in the list. Must be unique.
     */
    var name: String = "Name",
    /**
     * Type of the communication interface.
     */
    var type: String = MODBUSMASTER,
    /**
     * Interface id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long? = null,
        ) {

    @Transient
    var structure: GeneralInterface? = null
//        set(value) {
//            structureJSON = ObjectJsonConverter.convertToJSON(value)
//            field = value
//        }

    @JsonIgnore
    var structureJSON: String = ""

    @Transient
    @JsonIgnore
    var interfaceServices: InterfaceServices? = null

    /**
     * Interface running status.
     */
    @Transient
    var running: Boolean = false
        set(value) {
            field = value
            if (value) {
                structure?.startInterface()
            }
        }

    @Transient
    var status: String = STOPPED

    /* Static values */
    companion object {
        val MODBUSMASTER ="Modbus Master"
        val MODBUSSLAVE ="Modbus Slave"
        val UDP = "UDP"
        val OPCUAclient = "OPC UA Client"

        const val DISCONNECTED = "Disconnected"
        const val CONNECTED = "Connected"
        const val CONNECTING = "Connecting..."
        const val ERROR = "Error"
        const val OFF = "Off"
        const val LISTENING = "Listening"
        const val READY = "Ready"
        const val STARTING = "Starting"
        const val STOPPED = "Stopped"
    }

    override fun toString(): String {
        return "Interface name is ${name}, type is ${type}, running status is ${running}."
    }
}