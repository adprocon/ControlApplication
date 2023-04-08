package net.apcsimple.controlapplication.model.processcontrollers.mpc

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import net.apcsimple.controlapplication.annotations.NoArgs
import net.apcsimple.controlapplication.converters.ModelToJsonAdapter
import net.apcsimple.controlapplication.converters.ModelFromJsonAdapter
import net.apcsimple.controlapplication.math.MatrixOps.Companion.adjustMatrixSize
import net.apcsimple.controlapplication.math.MatrixOps.Companion.eye
import net.apcsimple.controlapplication.math.MatrixOps.Companion.fillMatrix
import net.apcsimple.controlapplication.math.MatrixOps.Companion.multixscal
import net.apcsimple.controlapplication.math.MatrixOps.Companion.zerix
import net.apcsimple.controlapplication.model.datapoints.Tag
import net.apcsimple.controlapplication.model.processcontrollers.GeneralController
import net.apcsimple.controlapplication.model.processmodels.ModelsList
import net.apcsimple.controlapplication.model.processmodels.ProcessModel
import net.apcsimple.controlapplication.model.processmodels.statespace.StateSpaceModel
import sklog.KotlinLogging

private var logger = KotlinLogging.logger {}

@NoArgs
class MpcController() : GeneralController {

    constructor(modelsList: ModelsList) : this() {
        this.modelsList = modelsList
    }

    @JsonIgnore
    lateinit var modelsList: ModelsList

    /**
     * State-space model for MPC computations
     */
    @JsonDeserialize(using = ModelFromJsonAdapter::class)
    @JsonSerialize(using = ModelToJsonAdapter::class)
    var model: ProcessModel? = null
        set(value) {
            if (value == null) return
            field = value
            val ssModel = model?.structure as StateSpaceModel
            observer.matrixQk =
                fillMatrix(multixscal(0.1, eye(ssModel.matrixA.size)), observer.matrixQk)
                    ?: arrayOf(
                        doubleArrayOf()
                    )
            observer.matrixRk =
                fillMatrix(multixscal(0.1, eye(ssModel.matrixC.size)), observer.matrixRk)
                    ?: arrayOf(
                        doubleArrayOf()
                    )
            observer.matrixPk =
                fillMatrix(multixscal(0.1, eye(ssModel.matrixA.size)), observer.matrixPk)
                    ?: arrayOf(
                        doubleArrayOf()
                    )
            observer.matrixKk =
                fillMatrix(multixscal(0.1, eye(ssModel.matrixA.size)), observer.matrixKk)
                    ?: arrayOf(
                        doubleArrayOf()
                    )

            try {
                matrixQ = fillMatrixQ(matrixQ)
                matrixR = fillMatrixR(matrixR)
            } catch (e: Exception) {
//                e.printStackTrace()
            }

            usedInputs = adjustMatrixSize(usedInputs, ssModel.matrixB[0].size, true)
            adjustTagListSize(inputs, ssModel.matrixB[0].size)
            adjustTagListSize(outputs, ssModel.matrixC.size)
            adjustTagListSize(setpoints, ssModel.matrixC.size)
            adjustTagListSize(optInputs, usedInputs.count {it})
            inMovesConstraints = adjustMatrixSize(inMovesConstraints, usedInputs.count {it}, doubleArrayOf(0.0, 0.0))
            inMovesConstraintsUsed =
                adjustMatrixSize(inMovesConstraintsUsed, usedInputs.count {it}, booleanArrayOf(false, false))
            inputConstraints = adjustMatrixSize(inputConstraints, usedInputs.count {it}, doubleArrayOf(0.0, 0.0))
            inputConstraintsUsed =
                adjustMatrixSize(inputConstraintsUsed, usedInputs.count {it}, booleanArrayOf(false, false))
            outputConstraints = adjustMatrixSize(outputConstraints, ssModel.matrixC.size, doubleArrayOf(0.0, 0.0))
            outputConstraintsUsed =
                adjustMatrixSize(outputConstraintsUsed, ssModel.matrixC.size, booleanArrayOf(false, false))
        }

    /**
     * Use augmented model with states of integrated output errors for better control
     */
    var augmented = false

    /**
     * State observer
     */
    val observer: KalmanObserver = KalmanObserver()

    /**
     * Prediction horizon
     */
    var hp = 1
        set(value) {
            field = value
            if (model == null) return
//            val ssModel = model?.structure as StateSpaceModel
            try {
                val lnght = matrixQ[0].size
                matrixQ = fillMatrixQ(matrixQ)
                for (i in matrixQ.indices) {
                    for (j in lnght until matrixQ[0].size) {
                        matrixQ[i][j] = matrixQ[i][lnght - 1]
                    }
                }
            } catch (e: Exception) {
//                e.printStackTrace()
            }
            logger.info("Prediction horizon is ${hp}")
        }

    /**
     * Control horizon
     */
    var hc = 1
        set(value) {
            field = value
            if (model == null) return
            val ssModel = model?.structure as StateSpaceModel
            try {
                val lnght = matrixR[0].size
                matrixR = fillMatrixR(matrixR)
                for (i in matrixR.indices) {
                    for (j in lnght until matrixR[0].size) {
                        matrixR[i][j] = matrixR[i][lnght - 1]
                    }
                }
            } catch (e: Exception) {
//                e.printStackTrace()
            }
            logger.info("Control horizon is $hc")
        }

    /**
     * Weights matrix for output deviations from the set point.
     */
    var matrixQ: Array<DoubleArray> = arrayOf(doubleArrayOf())

    /**
     * Weights matrix for input moves.
     */
    var matrixR: Array<DoubleArray> = arrayOf(doubleArrayOf())

    /* Data points used in MPC */
    /**
     * List of data points of process inputs
     */
    var inputs: MutableList<Tag> = mutableListOf()

    /**
     * List of data points of process outputs trajectory (set points)
     */
    var setpoints: MutableList<Tag> = mutableListOf()

    /**
     * List of data points of process outputs
     */
    var outputs: MutableList<Tag> = mutableListOf()

    /**
     * List of data points for optimal process inputs computed by MPC
     */
    var optInputs: MutableList<Tag> = mutableListOf()

    /**
     * Boolean matrix of used (true) or not used (false) inputs.
     */
    var usedInputs: Array<Boolean> = arrayOf()

    /**
     * Numerical constraints for process inputs
     */
    var inMovesConstraints: Array<DoubleArray> = arrayOf()

    /**
     * Use (true) or do not use (false) inputs constraints
     */
    var inMovesConstraintsUsed: Array<BooleanArray> = arrayOf()

    /**
     * Numerical constraints for process inputs
     */
    var inputConstraints: Array<DoubleArray> = arrayOf()

    /**
     * Use (true) or do not use (false) inputs constraints
     */
    var inputConstraintsUsed: Array<BooleanArray> = arrayOf()

    /**
     * Numerical constraints for process inputs
     */
    var outputConstraints: Array<DoubleArray> = arrayOf()

    /**
     * Use (true) or do not use (false) outputs constraints
     */
    var outputConstraintsUsed: Array<BooleanArray> = arrayOf()

    /**
     * Data point for binary watchdog pulse to/from DCS/PLC
     */
    var watchdogTag: Tag = Tag(Tag.DEFAULT_NAME, Tag.TAGBOOLEAN, false)


    /**
     * Data point to define, if MPC is used for process control or not.
     */
    var mpcinuse: Tag = Tag(Tag.DEFAULT_NAME, Tag.TAGBOOLEAN, false)

    /**
     * Application general data points list for drop-down selection
     */
    private var tagList: Map<String, Tag> = mapOf()

    /**
     * Application general data points list for drop-down selection
     */
    @JsonIgnore
    private var updateSettings: Boolean = false

    @JsonIgnore
    var diagnostics: MpcDiagnostics? = null

    /*>>>>>>>>>>>>>>>>>>>>>>>> Methods **********************************/
    /**
     * Adjust tag list according to model dimensions
     */
    fun adjustTagListSize(tagList: MutableList<Tag>, size: Int) {
        if (tagList.size > size) {
            for (i in size until tagList.size) {
                tagList.removeLast()
            }
        } else if (tagList.size < size) {
            for (i in tagList.size until size) {
                tagList.add(Tag(Tag.DEFAULT_NAME, Tag.TAGDOUBLE, 0.0))
            }
        }
    }

    override fun copy() {
        TODO("Not yet implemented")
    }

    fun fillMatrixQ(matrixQ: Array<DoubleArray>): Array<DoubleArray> {
        return fillMatrix(zerix((model?.structure as StateSpaceModel).matrixC.size, hp), matrixQ)
            ?: arrayOf(doubleArrayOf())
    }

    fun fillMatrixR(matrixR: Array<DoubleArray>): Array<DoubleArray> {
        return fillMatrix(zerix(usedInputs.count { it }, hc), matrixR) ?: arrayOf(doubleArrayOf())
    }


}