package net.apcsimple.controlapplication.model.processcontrollers

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonView
import net.apcsimple.controlapplication.converters.BasicData
import net.apcsimple.controlapplication.converters.ObjectJsonConverter
import net.apcsimple.controlapplication.model.processcontrollers.mpc.MpcController
import net.apcsimple.controlapplication.services.ControllerServices
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class ProcessController(

    /**
     * Used to identify the controller in the list. Must be unique.
     */
    @JsonView(BasicData::class)
    var name: String = "Name",
    /**
     * Type of the controller.
     */
    @JsonView(BasicData::class)
    val type: String? = null,
    /**
     * Controller running status.
     */
    @JsonView(BasicData::class)
    var cycleTime: Int = 1000,
    /**
     * Controller id.
     */
    @JsonView(BasicData::class)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Int? = null,
) {

    /**
     * Controller running status.
     */
    @JsonView(BasicData::class)
    var running: Boolean = false
        set(value) {
            field = value
            if (value) {
                if (type == MODEL_PREDICTIVE_CONTROLLER) {
                    val runnable = controllerServices?.mpcRunnable(this)
                    Thread(runnable).start()
                }
            } else {
                if (type == MODEL_PREDICTIVE_CONTROLLER) {
                    (structure as MpcController).diagnostics = null
                }

            }
        }

    @Transient
    var structure: GeneralController? = null
        set(value) {
            structureJSON = ObjectJsonConverter.convertToJSON(value)
            field = value
        }

    @JsonIgnore
    var structureJSON: String = ""

    @Transient
    @JsonIgnore
    var controllerServices: ControllerServices? = null

    companion object {
        val MODEL_PREDICTIVE_CONTROLLER = "Model Predictive Controller"
        val FUZZY_CONTROLLER = "Fuzzy Controller"
    }
}