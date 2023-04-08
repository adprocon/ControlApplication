package net.apcsimple.controlapplication.model.processcontrollers.mpc

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import net.apcsimple.controlapplication.converters.SimpleMatrixToJsonArrayAdapter
import net.apcsimple.controlapplication.model.processmodels.statespace.StateSpaceModel
import org.ejml.simple.SimpleMatrix

class MpcDiagnostics(
    @JsonIgnore
    val mpc: MpcController
) {

    @JsonSerialize(using = SimpleMatrixToJsonArrayAdapter::class)
    var freeResponseStates = SimpleMatrix((mpc.model?.structure as StateSpaceModel).matrixA.size * mpc.hp, 1)
    @JsonSerialize(using = SimpleMatrixToJsonArrayAdapter::class)
    var freeResponseOut = SimpleMatrix((mpc.model?.structure  as StateSpaceModel).matrixC.size * mpc.hp, 1)
    @JsonSerialize(using = SimpleMatrixToJsonArrayAdapter::class)
    var freeResponseError = SimpleMatrix((mpc.model?.structure  as StateSpaceModel).matrixC.size  * mpc.hp, 1)
    @JsonSerialize(using = SimpleMatrixToJsonArrayAdapter::class)
    var trajectory = SimpleMatrix((mpc.model?.structure  as StateSpaceModel).matrixC.size  * mpc.hp, 1)
    @JsonSerialize(using = SimpleMatrixToJsonArrayAdapter::class)
    var prediction = SimpleMatrix((mpc.model?.structure  as StateSpaceModel).matrixC.size  * mpc.hp, 1)
    @JsonSerialize(using = SimpleMatrixToJsonArrayAdapter::class)
    var states = SimpleMatrix((mpc.model?.structure  as StateSpaceModel).matrixA.size * mpc.hp, 1)
    @JsonSerialize(using = SimpleMatrixToJsonArrayAdapter::class)
    var restoredStates = SimpleMatrix((mpc.model?.structure  as StateSpaceModel).matrixA.size * mpc.hp, 1)
    @JsonSerialize(using = SimpleMatrixToJsonArrayAdapter::class)
    var outputs = SimpleMatrix(0,0)
    @JsonSerialize(using = SimpleMatrixToJsonArrayAdapter::class)
    var computedOutputs = SimpleMatrix(0,0)
    @JsonSerialize(using = SimpleMatrixToJsonArrayAdapter::class)
    var inputs = SimpleMatrix(0,0)
    @JsonSerialize(using = SimpleMatrixToJsonArrayAdapter::class)
    var optimalInputsHc = SimpleMatrix(mpc.usedInputs.count { it } * mpc.hc, 1)
    @JsonSerialize(using = SimpleMatrixToJsonArrayAdapter::class)
    var optimalMovesHc = SimpleMatrix(mpc.usedInputs.count { it } * mpc.hc, 1)
    @JsonSerialize(using = SimpleMatrixToJsonArrayAdapter::class)
    var dUMin = SimpleMatrix(0, 0)
    @JsonSerialize(using = SimpleMatrixToJsonArrayAdapter::class)
    var dUMax = SimpleMatrix(0, 0)
    @JsonSerialize(using = SimpleMatrixToJsonArrayAdapter::class)
    var dUMindU = SimpleMatrix(0, 0)
    @JsonSerialize(using = SimpleMatrixToJsonArrayAdapter::class)
    var dUMaxdU = SimpleMatrix(0, 0)
    @JsonSerialize(using = SimpleMatrixToJsonArrayAdapter::class)
    var dUMinU = SimpleMatrix(0, 0)
    @JsonSerialize(using = SimpleMatrixToJsonArrayAdapter::class)
    var dUMaxU = SimpleMatrix(0, 0)
    @JsonSerialize(using = SimpleMatrixToJsonArrayAdapter::class)
    var dUMinZ = SimpleMatrix(0, 0)
    @JsonSerialize(using = SimpleMatrixToJsonArrayAdapter::class)
    var dUMaxZ = SimpleMatrix(0, 0)

}