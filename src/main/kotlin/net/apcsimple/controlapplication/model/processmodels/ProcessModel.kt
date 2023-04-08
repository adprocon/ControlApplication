package net.apcsimple.controlapplication.model.processmodels

import com.fasterxml.jackson.annotation.JsonIgnore
import net.apcsimple.controlapplication.converters.ObjectJsonConverter
import net.apcsimple.controlapplication.services.ModelsServices
import sklog.KotlinLogging
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

private var logger = KotlinLogging.logger {}

@Entity
data class ProcessModel(
    var name: String = "",
    var type: String? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int? = null,
) {
    @Transient
    @JsonIgnore
    var modelsServices: ModelsServices? = null

    @Transient
    var structure: GeneralModel? = null
        set(value) {
            structureJSON = ObjectJsonConverter.convertToJSON(value)
            field = value
        }

    @JsonIgnore
    var structureJSON: String = ""

    /**
     * Simulation running status.
     */
    @Transient
    var simulationRunning: Boolean = false
        set(value) {
            field = value
            if (value) {
                if (type == STATE_SPACE) {
                    val runnable = modelsServices?.simulationRunnable(this)
                    Thread(runnable).start()
                }
            }
//            logger.info("Simulation started.")
        }

    var simulationCycle: Int = 1000

    companion object {
        val STATE_SPACE ="State-Space"
        val POLYNOMIAL ="Polynomial"
        val TRANSFER_FUNCTION = "Transfer Function"
    }
}