package net.apcsimple.controlapplication.model.processcontrollers.mpc

import net.apcsimple.controlapplication.math.MatrixOps.Companion.eye
import net.apcsimple.controlapplication.math.MatrixOps.Companion.simpleMatrixToArray
import net.apcsimple.controlapplication.math.MatrixOps.Companion.toDiagonal
import net.apcsimple.controlapplication.model.processcontrollers.ProcessController
import net.apcsimple.controlapplication.model.processmodels.statespace.StateSpaceModel
import net.apcsimple.controlapplication.services.ControllerServices
import org.ejml.simple.SimpleMatrix
import sklog.KotlinLogging

private var logger = KotlinLogging.logger {}

/**
 * Mpc runnable to run the MPC controller.
 *
 * @property controller General controller object.
 * @property service Controller services.
 * @property settings MPC parameters inside controller object.
 * @property cycleTime MPC execution cycle tyme in milliseconds.
 *
 * @constructor Create empty Mpc runnable.
 *
 * @property
 */
class MpcRunnable(
    val controller: ProcessController,
    val service: ControllerServices,
) : Runnable {

    private val generalServices = service.generalServices
    var name = controller.name

    /* Controller structure */
    val settings = controller.structure as MpcController
    val cycleTime = controller.cycleTime
    var hp = settings.hp
    var hc = settings.hc
    var matrixQ = SimpleMatrix(toDiagonal(settings.matrixQ))
    var matrixR = SimpleMatrix(toDiagonal(settings.matrixR))
    var inMoveConstr = settings.inMovesConstraints.clone()
    var inMoveConstrUsed = settings.inMovesConstraintsUsed.clone()
    var inConstr = settings.inputConstraints.clone()
    var inConstrUsed = settings.inputConstraintsUsed.clone()
    var outConstr = settings.outputConstraints.clone()
    var outConstrUsed = settings.outputConstraintsUsed.clone()

    /* State observer - Kalman filter */
    var matrixQk = SimpleMatrix(settings.observer.matrixQk)
    var matrixRk = SimpleMatrix(settings.observer.matrixRk)
    var matrixPk = SimpleMatrix(settings.observer.matrixPk)
    var matrixKk = SimpleMatrix(settings.observer.matrixKk)
    var usedInputs = settings.usedInputs.clone()
    var mpcinuse = false

    /* Used state-space model */
    var model = settings.model?.structure as StateSpaceModel
    var matrixA = SimpleMatrix(model.matrixA)
    var matrixB = SimpleMatrix(model.matrixB)
    var matrixC = SimpleMatrix(model.matrixC)

    /* Model dimensions */
    var allInAmount = matrixB.numCols()
    var modelDim = matrixA.numRows()
    var outAmount = matrixC.numRows()

    /* Model inputs, states and outputs */
    var contrInAmount = usedInputs.count { it }
    var inputs = SimpleMatrix(allInAmount, 1)
    var outputs = SimpleMatrix(outAmount, 1)
    var setpoints = SimpleMatrix(outAmount, 1)
    var optInputs = SimpleMatrix(contrInAmount, 1)
    var states = SimpleMatrix(modelDim, 1)

    //    var freeResponseStates = SimpleMatrix(modelDim * Hp, 1)
//    var freeResponseOut = SimpleMatrix(outAmount * Hp, 1)
//    var freeResponseError = SimpleMatrix(outAmount * Hp, 1)
//    var prediction = SimpleMatrix(outAmount * Hp, 1)
//    var optimalInputsHc = SimpleMatrix(controllableInputsAmount * Hc, 1)
//
    var augmented = settings.augmented

    /* MPC constants computed once at controller starting */
    lateinit var mpcConst: MpcConstants

    /* Diagnostics information */
    val diagnostics = MpcDiagnostics(controller.structure as MpcController)

//    var debugLevel = 0

    override fun run() {
        runMpc()
    }

    /**
     * This is the main MPC runnable function.
     */
    private fun runMpc() {

        var startTime: Long
        var elapsedTime: Long
        var waitingTime: Long

        settings.diagnostics = diagnostics

        /* Initialization */
        inputs = generalServices.readFromTagList(settings.inputs)
//        outputs = generalServices.readFromTagList(settings.outputs)
//        setpoints = generalServices.readFromTagList(settings.setpoints)
        states = initStates(300)
        var augStates = if (augmented) {
            states.combine(states.numRows(), 0, SimpleMatrix(outAmount, 1))
        } else {
            states
        }

        val usedInputs = if (augmented) {
            this.usedInputs + Array(outAmount) { false }
        } else {
            this.usedInputs
        }

//        optInputs = readControllableInputsValuesFromProcess(inputs, usedInputs, controllableInputsAmount)
        var computedOutputs: SimpleMatrix

        /* Compute MPC constants */
        mpcConst = computeMpcConstants(usedInputs)

        val matrixA = mpcConst.A
        val matrixB = mpcConst.B
        val matrixC = mpcConst.C

        try {
            while (controller.running) {

                /* Start timer to count execution time */
                startTime = System.nanoTime()

                /* Read current values from the process */
                inputs = generalServices.readFromTagList(settings.inputs)
                outputs = generalServices.readFromTagList(settings.outputs)
                setpoints = generalServices.readFromTagList(settings.setpoints)
                diagnostics.inputs = inputs.copy()
                diagnostics.outputs = outputs.copy()

                /* Reduce set point to the constrained area, if it violates constraints */
                val minZ = SimpleMatrix(outConstr).extractVector(false, 0)
                val maxZ = SimpleMatrix(outConstr).extractVector(false, 1)
                for (i in 0 until setpoints.numRows()) {
                    if (setpoints.get(i) > maxZ.get(i) && outConstrUsed[i][1]) {
                        setpoints.set(i, maxZ.get(i))
                    }
                    if (setpoints.get(i) < minZ.get(i) && outConstrUsed[i][0]) {
                        setpoints.set(i, minZ.get(i))
                    }
                }

                /* Augment inputs vector, if needed */
                val augInputs = if (augmented) {
                    inputs.combine(inputs.numRows(), 0, outputs.minus(setpoints))
                } else {
                    inputs
                }

                /* TODO Check if MPC is used by the process - USAGE TO BE CLARIFIED */
                mpcinuse = generalServices.readFromTagList(settings.mpcinuse) as Boolean
//                if (!mpcinuse) {
//                    optInputs = readControllableInputsValuesFromProcess(inputs, usedInputs, controllableInputsAmount)
//                }

                /* Compute model states and outputs */
                augStates = matrixA.mult(augStates).plus(matrixB.mult(augInputs))
                diagnostics.states = augStates.copy()
                computedOutputs = matrixC.mult(augStates)
                diagnostics.computedOutputs = computedOutputs.copy()

                states = if (augmented) {
                    augStates.extractMatrix(0, modelDim, 0, 1)
                } else {
                    augStates.copy()
                }

                /* Restore model states with Kalman observer */
                states = restoreStates(states, computedOutputs, outputs)
                augStates.insertIntoThis(0, 0, states)
                diagnostics.restoredStates = augStates.copy()

                /* Compute MPC */
                optInputs = computeMPC(augStates, augInputs, usedInputs)

                /* Write optimized process inputs to related data points */
                generalServices.writeToTagList(settings.optInputs, optInputs)

                elapsedTime = (System.nanoTime() - startTime) / 1000
                generalServices.logError(logger, 2, "Controller execution time is ${elapsedTime} ns.", 1)
                waitingTime = cycleTime - elapsedTime / 1000
                generalServices.logError(logger, 2, "Controller waiting time is ${waitingTime} ms.", 1)

                if (waitingTime < 0) {
                    generalServices.logError(logger, 2, "Execution time exceeded for ${-waitingTime} ms.", 3)
                } else {
                    try {
                        Thread.sleep(waitingTime)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }

                elapsedTime = (System.nanoTime() - startTime) / 1000000
                generalServices.logError(logger, 2, "Controller cycle time is ${elapsedTime} ms.", 1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            controller.running = false
        }
    }


    /**
     * This function is used to initialize used model states.
     *
     * @param iterations    number of iterations to be used for initialization.
     */
    fun initStates(iterations: Int): SimpleMatrix {
        var out = SimpleMatrix(matrixA.numCols(), 1)
        for (i in 1..iterations) {
            out = matrixA.mult(out).plus(matrixB.mult(inputs))
        }
        return out
    }

    /**
     * This function copies real inputs to optimized inputs, if MPC is not in use.
     *
     * @param inputs                inputs values.
     * @param usedInputs            vector that identifies used inputs.
     * @param amountOfUsedInputs    amount of inputs used for control.
     *
     * @return                      vector of values with size of used inputs amount.
     */
    fun readContrInValuesFromProcess(
        inputs: SimpleMatrix,
        usedInputs: Array<Boolean>,
        amountOfUsedInputs: Int
    ): SimpleMatrix {
        val out = SimpleMatrix(amountOfUsedInputs, 1)
        var k = 0
        usedInputs.forEachIndexed { index, elem ->
            if (elem) {
                out[k] = inputs[index]
                k++
            }
        }
        return out
    }

    /**
     * This function restores process states from process outputs using Kalman filter.
     *
     * @param states            is a states vector that should be restored according to the process and model outputs difference.
     * @param computedOutputs   is a vector of the model computed outputs.
     * @param realOutputs       is a vector of the real process outputs.
     *
     * @return                  a vector of the restored process states.
     */
    private fun restoreStates(
        states: SimpleMatrix,
        computedOutputs: SimpleMatrix,
        realOutputs: SimpleMatrix
    ): SimpleMatrix {

        matrixPk = matrixA.mult(matrixPk).mult(matrixA.transpose()).plus(matrixQk)
        matrixKk = matrixPk.mult(matrixC.transpose()).mult(
            matrixC.mult(matrixPk).mult(matrixC.transpose()).plus(matrixRk).pseudoInverse()
        )
        matrixPk = SimpleMatrix.identity(matrixA.numRows()).minus(matrixKk.mult(matrixC)).mult(matrixPk)

        settings.observer.matrixKk = simpleMatrixToArray(matrixKk)
        settings.observer.matrixPk = simpleMatrixToArray(matrixPk)
        return states.plus(matrixKk.mult(realOutputs.minus(computedOutputs)))

    }

    /**
     * This function computes outputs prediction and optimal values of process inputs.
     *
     * @param states        model current states.
     * @param inputs        current process inputs.
     * @param usedInputs    boolean vector with true value for controlled inputs and false values for measured disturbances.
     *
     * @return              optimal values of process inputs.
     */
    fun computeMPC(
        states: SimpleMatrix,
        inputs: SimpleMatrix,
        usedInputs: Array<Boolean>,
    ): SimpleMatrix {
        /* Initialize parameters */
        var out = SimpleMatrix(contrInAmount, 1)
        val A = mpcConst.A
        val B = mpcConst.B
        val Bm = mpcConst.Bm
        val C = mpcConst.C
        val Ax = mpcConst.Ax
        val Bx = mpcConst.Bx
        val ABx = mpcConst.ABx
        val Cz = mpcConst.Cz
        val H = mpcConst.H
        var G = mpcConst.G

        try {
            /* Fill in trajectory with the set points */
            val trajectory = SimpleMatrix(hp * outAmount, 1)
            for (i in 0 until hp) {
                trajectory.insertIntoThis(i * outAmount, 0, setpoints)
            }
            diagnostics.trajectory = vectorToMatrix(trajectory, outAmount)

            if (contrInAmount > 0) {
                /* Compute prediction from states */
                val vectorAx = Ax.mult(states)

                /* Compute prediction from inputs */
                val vectorBx = Bx.mult(inputs)

                /* Compute states free response (dU = 0) */
                val freeResponseStates = vectorAx.plus(vectorBx)
                diagnostics.freeResponseStates = vectorToMatrix(freeResponseStates, outAmount)

                /* Compute outputs free response */
                val freeResponseOut = Cz.mult(freeResponseStates)
                diagnostics.freeResponseOut = vectorToMatrix(freeResponseOut, outAmount)

                /* Compute free response error */
                val freeResponseError = trajectory.minus(freeResponseOut)
                diagnostics.freeResponseError = vectorToMatrix(freeResponseError, outAmount)
                G = G.mult(freeResponseError)

                /* Control moves vector declaration */
                var dUhc: SimpleMatrix

                /* Current controllable inputs values */
                val contrIn = readContrInValuesFromProcess(inputs, usedInputs, contrInAmount)

                if (inMoveConstrUsed.sumOf { sub -> sub.count { it } }
                    + inConstrUsed.sumOf { sub -> sub.count { it } }
                    + outConstrUsed.sumOf { sub -> sub.count { it } } == 0) {
                    /* Unconstrained MPC */
                    dUhc = H.solve(G).scale(0.5)
                } else {
                    /* Interior point method optimization */
                    /* IPM constants */
                    val iterations = 10
                    val zita = 0.1          // reduce mu in each iteration by this value
                    val nyu = 0.999999999   // how close is allowed to approach constraints (1 = on the constraints)
                    var mu = 100.0          // barrier weight reduced by zita at each iteration

                    /* Create control moves constraints vectors for the whole control horizon */
                    val mindUhc = fillInVector(SimpleMatrix(inMoveConstr).extractVector(false, 0), hc)
                    val maxdUhc = fillInVector(SimpleMatrix(inMoveConstr).extractVector(false, 1), hc)

                    /* Create input constraints vectors for the whole control horizon */
                    val minUhc = fillInVector(SimpleMatrix(inConstr).extractVector(false, 0), hc)
                    val maxUhc = fillInVector(SimpleMatrix(inConstr).extractVector(false, 1), hc)
                    val minU = SimpleMatrix(inConstr).extractVector(false, 0)
                    val maxU = SimpleMatrix(inConstr).extractVector(false, 1)

                    /* Create output constraints vectors for the whole control horizon */
//                    val minZhc = fillInVector(SimpleMatrix(outConstr).extractVector(false, 0), hp)
//                    val maxZhc = fillInVector(SimpleMatrix(outConstr).extractVector(false, 1), hp)
                    val minZ = SimpleMatrix(outConstr).extractVector(false, 0)
                    val maxZ = SimpleMatrix(outConstr).extractVector(false, 1)

                    /* Ei is a low triangular matrix, where non-zero part consists of identity matrices of contrInAmount dimension */
                    val matrixEi = SimpleMatrix(hc * contrInAmount, hc * contrInAmount)
                    val identity = SimpleMatrix.identity(contrInAmount)
                    for (i in 0 until hc) {
                        for (j in 0 until hc - i) {
                            matrixEi.insertIntoThis(
                                j * contrInAmount,
                                j * contrInAmount, identity
                            )
                        }
                    }

                    /* Current inputs vector for the whole control horizon */
                    val inputsHc = SimpleMatrix(hc * contrInAmount, 1)
                    for (i in 0 until hc) {
                        inputsHc.insertIntoThis(i * contrInAmount, 0, contrIn)
                    }

                    /* Control moves constraints */
                    val dUlimMin = SimpleMatrix(contrInAmount * hc, 1)
                    dUlimMin.fill(-9999999999.0)
                    mindUhc.ddrm.data.mapIndexed { index, d ->
                        if (inMoveConstrUsed[index % contrInAmount][0]) dUlimMin.set(index, d)
                    }
                    val dUlimMax = SimpleMatrix(contrInAmount * hc, 1)
                    dUlimMax.fill(9999999999.0)
                    maxdUhc.ddrm.data.mapIndexed { index, d ->
                        if (inMoveConstrUsed[index % contrInAmount][1]) dUlimMax.set(index, d)
                    }
                    diagnostics.dUMindU = vectorToMatrix(dUlimMin.copy(), contrInAmount)
                    diagnostics.dUMaxdU = vectorToMatrix(dUlimMax.copy(), contrInAmount)

                    /* Compute input constraints effect to control moves constraints */
                    val dUlimMinU = matrixEi.solve(minUhc.minus(inputsHc))
                    dUlimMinU.ddrm.data.mapIndexed { index, d ->
                        if (inConstrUsed[index % contrInAmount][0]
                            && (d > dUlimMin.get(index)) && (d < dUlimMax.get(index))) {
                            dUlimMin.set(index, d)
                        }
                    }
                    val dUlimMaxU = matrixEi.solve(maxUhc.minus(inputsHc))
                    dUlimMaxU.ddrm.data.mapIndexed { index, d ->
                        if (inConstrUsed[index % contrInAmount][1]
                            && (d < dUlimMax.get(index)) && (d > dUlimMin.get(index))) {
                            dUlimMax.set(index, d)
                        }
                    }
                    diagnostics.dUMinU = vectorToMatrix(dUlimMinU.copy(), contrInAmount)
                    diagnostics.dUMaxU = vectorToMatrix(dUlimMaxU.copy(), contrInAmount)

                    try {
                        /* Compute output constraints effect to control moves constraints */
                        val computedOutputs = C.mult(A).mult(states).plus(C.mult(B).mult(inputs))
                        val tempMinZ = minZ.minus(computedOutputs)
                        val tempMaxZ = maxZ.minus(computedOutputs)
                        val dUlimMaxZ = computeOutputConstraint(tempMaxZ, Bm, C, false)
                        val dUlimMinZ = computeOutputConstraint(tempMinZ, Bm, C, true)

                        diagnostics.dUMinZ = vectorToMatrix(dUlimMinZ.copy(), contrInAmount)
                        diagnostics.dUMaxZ = vectorToMatrix(dUlimMaxZ.copy(), contrInAmount)

                        for (i in 0 until contrInAmount * hc) {
                            if (dUlimMax.get(i, 0) > dUlimMaxZ.get(i % contrInAmount, 0) &&
                                outConstrUsed[i % contrInAmount][1]
                            ) {
                                /* Apply output constraints, if these are feasible */
                                if (dUlimMaxZ.get(i % contrInAmount, 0) > dUlimMinZ.get(i % contrInAmount, 0)
                                ) {
                                    dUlimMax.set(i, 0, dUlimMaxZ.get(i % contrInAmount, 0))
                                }
                            }
                            if (dUlimMin.get(i, 0) < dUlimMinZ.get(i % contrInAmount, 0) &&
                                outConstrUsed[i % contrInAmount][0]
                            ) {
                                /* Apply output constraints, if these are feasible */
                                if (dUlimMinZ.get(i % contrInAmount, 0) < dUlimMaxZ.get(i % contrInAmount, 0)
                                ) {
                                    dUlimMin.set(i, 0, dUlimMinZ.get(i % contrInAmount, 0))
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    diagnostics.dUMin = vectorToMatrix(dUlimMin.copy(), contrInAmount)
                    diagnostics.dUMax = vectorToMatrix(dUlimMax.copy(), contrInAmount)

//                    /* Initialize control moves to be in the middle of the constrained space */
//                    dUhc = dUlimMax.minus((dUlimMax.minus(dUlimMin)).scale(0.5))

                    /* TODO Initialize control moves to be in the middle of the constrained space - TO BE CHECKED */
                    dUhc = SimpleMatrix(hc * contrInAmount, 1)
                    val dU = maxU.plus(minU).scale(0.5).minus(contrIn)
                    dUhc.insertIntoThis(0, 0, dU)

                    for (iteration in 0 until iterations) {

                        /* Compute barriers */
                        val barrierMin = dUhc.minus(dUlimMin).elementPower(-1.0)
                        val barrierMax = dUlimMax.minus(dUhc).elementPower(-1.0)
                        // Check, if division by 0 occurred
                        for (i in 0 until barrierMin.numElements) {
                            if (barrierMin.get(i).isInfinite() || barrierMin.get(i).isNaN()) {
                                barrierMin.set(i, 0.0)
                            }
                        }
                        for (i in 0 until barrierMax.numElements) {
                            if (barrierMax.get(i).isInfinite() || barrierMax.get(i).isNaN()) {
                                barrierMax.set(i, 0.0)
                            }
                        }
                        val barrierMinSq = barrierMin.elementPower(2.0)
                        val barrierMaxSq = barrierMax.elementPower(2.0)

                        /* Compute gradient and Hessian */
                        val gradient = SimpleMatrix(hc * contrInAmount, 1)
                        val hessian = H.copy().scale(2.0)
                        val Hu = H.mult(dUhc).scale(2.0)
                        for (i in 0 until hc * contrInAmount) {
                            val useMinConst = if (
                                inConstrUsed[i % contrInAmount][0] ||
                                (outConstrUsed.count { it[0] } > 0)
                            ) 1.0 else 0.0
                            val useMaxConst = if (inConstrUsed[i % contrInAmount][1] ||
                                (outConstrUsed.count { it[1] } > 0)
                            ) 1.0 else 0.0
                            gradient.set(
                                i, Hu.get(i) - G.get(i) +
                                        mu * useMaxConst * barrierMax.get(i)
                                        - mu * useMinConst * barrierMin.get(i)
                            )
                            hessian.set(
                                i, i, hessian.get(i, i) +
                                        mu * useMaxConst * barrierMaxSq.get(i)
                                        + mu * useMinConst * barrierMinSq.get(i)
                            )
                        }

                        /* Update dU update rate ro */
                        val ro = hessian.solve(gradient)
//                    val ro = (hessian.pseudoInverse()).mult(gradient)
//                        val qr = DecompositionFactory_DDRM.qr(hessian.numRows(), hessian.numCols())
//                        qr.decompose(hessian.ddrm)
//                        var R = DMatrixRMaj()
//                        qr.getR(R, true)
//                        var Q = DMatrixRMaj()
//                        qr.getQ(Q, true)
//                        val ro = solveQR(SimpleMatrix(R), SimpleMatrix(Q).transpose().mult(gradient))
//                        printMatrix("ro", simpleMatrixToArray(ro))

                        for (i in 0 until hc * contrInAmount) {
                            val useMinConst = inConstrUsed[i % contrInAmount][0] ||
                                    (outConstrUsed.count { it[0] } > 0)
                            val useMaxConst = inConstrUsed[i % contrInAmount][1] ||
                                    (outConstrUsed.count { it[1] } > 0)
                            if (ro[i] < (dUhc[i] - dUlimMax[i]) && useMaxConst) {
                                ro[i] = nyu * (dUhc[i] - dUlimMax[i])
                            }
                            if (ro[i] > (dUhc[i] - dUlimMin[i]) && useMinConst) {
                                ro[i] = nyu * (dUhc[i] - dUlimMin[i])
                            }
                        }

                        /**
                         * Compute optimal step size alpha
                         * As solution is constrained by previous step, then unconstrained cost function can be used here
                         * V(k) = const - (du - alpha*ro)T*G + (du - alpha*ro)T*H*(du - alpha*ro)
                         * dV(k)/dalpha = 0
                         * alpha = (2*roT*H*du - roT*G) / (2*roT*H*ro)
                         */
                        val roT = ro.transpose()
                        val roTH = roT.mult(H)
                        val alpha = ((2 * roTH.mult(dUhc).get(0) - roT.mult(G).get(0)) / (2 * roTH.mult(ro).get(0)))

                        /* Update dU */
//                        dUhc.minus(ro)

                        /* Update dU */
                        dUhc = if (alpha < 0) {
                            dUhc.minus(ro)
                        } else {
                            dUhc.minus(ro.scale(alpha.coerceAtMost(1.0)))
                        }

                        /* Update barrier weight */
                        mu *= zita
                    }
                }

                diagnostics.prediction = vectorToMatrix(Cz.mult(freeResponseStates.plus(ABx.mult(dUhc))), outAmount)
                diagnostics.optimalMovesHc = vectorToMatrix((dUhc), contrInAmount)

                val optimalInputsHc = SimpleMatrix(usedInputs.count { it } * hc, 1)
                var optimalInputs = contrIn.copy()
                for (i in 0 until hc) {
                    optimalInputs = optimalInputs.plus(
                        dUhc.extractMatrix(
                            i * contrInAmount,
                            (i + 1) * contrInAmount, 0, 1
                        )
                    )
                    optimalInputsHc.insertIntoThis(i * contrInAmount, 0, optimalInputs)
                }

                diagnostics.optimalInputsHc = vectorToMatrix(optimalInputsHc, contrInAmount)
                out = optimalInputsHc.extractMatrix(0, contrInAmount, 0, 1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return out
    }

    /**
     * This function raises a matrix to the predefined power.
     *
     * @param A     is a matrix to be raised to the power.
     * @param power is a power to raise the matrix to.
     *
     * @return      matrix A in the power of power.
     */
    private fun matrixPower(A: SimpleMatrix, power: Int): SimpleMatrix {
        var out = SimpleMatrix(eye(A.numRows()))
        for (i in 1..power) {
            out = out.mult(A)
        }
        return out
    }

    /**
     * This function creates a bigger vector by repeating a smaller one predefined amount of times.
     *
     * @param vector    is a smaller vector.
     * @param amount    is an amount of times to repeat the smaller vector.
     *
     * @return          resulting bigger vector.
     */
    private fun fillInVector(vector: SimpleMatrix, amount: Int): SimpleMatrix {
        val out = SimpleMatrix(vector.numRows() * amount, 1)
        for (i in 0 until amount) {
            out.insertIntoThis(i * contrInAmount, 0, vector)
        }
        return out
    }

    /**
     * Compute mpc constants
     *
     * @param usedIns
     * @return
     */
    fun computeMpcConstants(
        usedIns: Array<Boolean>,
    ): MpcConstants {

        /* Model augmentation */
        val matA: SimpleMatrix
        val matB: SimpleMatrix
        val matC: SimpleMatrix
        val usedInputs: Array<Boolean>

        if (augmented) {
            matA = SimpleMatrix.identity(modelDim + outAmount).combine(0, 0, matrixA)
            matB = SimpleMatrix(modelDim + outAmount, allInAmount + outAmount)
                .combine(0, 0, matrixB)
                .combine(modelDim, allInAmount, SimpleMatrix.identity(outAmount).scale(0.1))
            matC = SimpleMatrix(outAmount, modelDim + outAmount)
                .combine(0, 0, matrixC).combine(0, modelDim, SimpleMatrix.identity(outAmount))
            usedInputs = usedIns + Array(outAmount) { false }
        } else {
            matA = matrixA
            matB = matrixB
            matC = matrixC
            usedInputs = usedIns
        }


        val modelDim = matA.numCols()
        val allInputsAmount = matB.numCols()
        val controllableInputsAmount = usedInputs.count { it }
        val outAmount = matC.numRows()
        val matrixAx = SimpleMatrix(modelDim * hp, modelDim)
        val matrixBx = SimpleMatrix(modelDim * hp, matB.numCols())
        val matrixABx = SimpleMatrix(modelDim * hp, hc * controllableInputsAmount)
        val matrixCz = SimpleMatrix(outAmount * hp, modelDim * hp)
        var matrixG = SimpleMatrix(0, 0)
        var matrixH = SimpleMatrix(0, 0)
        var matrixBm = SimpleMatrix(0, 0)

        if (controllableInputsAmount > 0) {
            /* Create matrix Ax to compute prediction from states */
            for (i in 0 until hp) {
                matrixAx.insertIntoThis(i * modelDim, 0, matrixPower(matA, i + 1))
            }

            /* Create matrix Bx to compute prediction from inputs */
            for (i in 0 until hp) {
                var tmpB = SimpleMatrix(modelDim, allInputsAmount)
                for (j in 0..i) {
                    tmpB = matrixPower(matA, j).mult(matB).plus(tmpB)
                }
                matrixBx.insertIntoThis(modelDim * i, 0, tmpB)
            }

            /* Create matrix Bm for controllable inputs only */
            for (i in 0 until allInputsAmount) {
                if (usedInputs[i]) {
                    matrixBm = matrixBm.concatColumns(matB.cols(i, i + 1))
                }
            }

            /* Create Matrix ABx to compute control moves effect to the states prediction */
            for (i in 0 until hp) {
                var tmpB = SimpleMatrix(modelDim, controllableInputsAmount)
                for (j in 0..i) {
                    tmpB = matrixPower(matA, j).mult(matrixBm).plus(tmpB)
                }
                for (j in 0 until hc.coerceAtMost(hp - i)) {
                    matrixABx.insertIntoThis((i + j) * modelDim, j * controllableInputsAmount, tmpB)
                }
            }

            /* Create big outputs prediction computation matrix C */
            for (i in 0 until hp) {
                matrixCz.insertIntoThis(i * outAmount, i * modelDim, matC)
            }

            /* Compute matrices G and H for MPC computation */
            val theta = matrixCz.mult(matrixABx)
            matrixG = theta.transpose().mult(matrixQ)
            matrixH = matrixG.mult(theta).plus(matrixR)
            matrixG = matrixG.scale(2.0)

        }

        return MpcConstants(
            matA,
            matB,
            matrixBm,
            matC,
            matrixAx,
            matrixBx,
            matrixABx,
            matrixCz,
            matrixG,
            matrixH
        )

    }

    /**
     * This function converts vector to the matrix with the same elements, but split onto few columns. This is
     * needed to present vector of alternate values of different process inputs or outputs as a matrix, where
     * each column is a vector of the value of the same input or output through control or prediction horizon.
     *
     * @param vector    is initial vector to be split onto columns.
     * @param cols      is amount of columns of generated matrix.
     *
     * @return          a resulting matrix.
     */
    private fun vectorToMatrix(vector: SimpleMatrix, cols: Int): SimpleMatrix {
        val out = SimpleMatrix(vector.numRows() / cols, cols)
        var index = 0
        for (i in 0 until vector.numRows() / cols) {
            for (j in 0 until cols) {
                out.set(i, j, vector.get(index))
                index++
            }
        }
        return out
    }

    /** This function solves equation Ax = y in relation to x after A QR decomposition.
     *
     * @param R     is an R matrix of QR decomposition.
     * @param qY    is a Qt*y multiplication after QR decomposition.
     *
     * @return      x vector as a solution of the equation.
     */
    private fun solveQR(R: SimpleMatrix, qY: SimpleMatrix): SimpleMatrix {
        val dim = R.numRows()
        val x = SimpleMatrix(dim, 1)
        for (i in 0 until dim) {
            var temp = 0.0
            for (j in 0 until i) {
                temp += x.get(dim - j - 1) * R.get(dim - i - 1, dim - j - 1)
            }
            x.set(dim - i - 1, (qY.get(dim - i - 1) - temp) / R.get(dim - i - 1, dim - i - 1))
        }
        return x
    }

    /**
     * This function is used to compute MPC output constraints effect onto control moves constraints for the first
     * step only.
     *
     * @param outputLimit   is a vector of output minimum or maximum values.
     * @param Bm            is a state-space model B matrix excluding columns of measured disturbances.
     * @param C             is a state-space model C matrix.
     * @param min           is a boolean parameters defining whether min (true) or max (false) vector is computed.
     *
     * @return              is a resulting vector of the control moves constraints.
     */
    private fun computeOutputConstraint(
        outputLimit: SimpleMatrix,
        Bm: SimpleMatrix,
        C: SimpleMatrix,
        min: Boolean
    ): SimpleMatrix {
        var sign = 1.0
        if (min) {
            sign = -1.0
        }
        var outputConstraints: SimpleMatrix? = null
        var tempLimZ: SimpleMatrix
        for (i in 0 until outAmount) {
            val tempC = C.extractVector(true, i)
            tempLimZ = (tempC.mult(Bm).pseudoInverse().mult(outputLimit.extractVector(true, i)))
            if (outputConstraints == null) {
                outputConstraints = tempLimZ
            } else {
                for (j in 0 until tempLimZ.numRows()) {
                    if (tempLimZ.get(j) * sign < outputConstraints.get(j) * sign) {
                        outputConstraints.set(j, tempLimZ.get(j))
                    }
                }
            }
        }
        return outputConstraints!!
    }

    /**
     * Compute output constraints.
     *
     * @param outputLimit
     * @param ABx
     * @param Cz
     * @param Hp
     * @param controllableInputsAmount
     * @param min
     * @return
     */
    private fun computeOutputConstraints(
        outputLimit: SimpleMatrix,
        ABx: SimpleMatrix,
        Cz: SimpleMatrix,
        Hp: Int,
        controllableInputsAmount: Int,
        min: Boolean
    ): SimpleMatrix {
        var sign = 1.0
        if (min) {
            sign = -1.0
        }
        var outputConstraints: SimpleMatrix? = null
        var tempLimZ: SimpleMatrix
        for (i in 0 until outAmount) {
            var tempC = SimpleMatrix(0, 0)
            var tempOutputLimit = SimpleMatrix(0, 0)
            for (j in 0 until Hp) {
                tempC = tempC.concatRows(Cz.extractVector(true, i + j * controllableInputsAmount))
                tempOutputLimit =
                    tempOutputLimit.concatRows(outputLimit.extractVector(true, i + j * controllableInputsAmount))
            }
            tempLimZ = tempC.mult(ABx).pseudoInverse().mult(tempOutputLimit)
            if (outputConstraints == null) {
                outputConstraints = tempLimZ
            } else {
                for (j in 0 until tempLimZ.numRows()) {
                    if (tempLimZ.get(j) * sign < outputConstraints.get(j) * sign) {
                        outputConstraints.set(j, tempLimZ.get(j))
                    }
                }
            }
        }
        return outputConstraints!!
    }
}

/**
 * Mpc constants
 *
 * @property A Matrix A of the state-space model.
 * @property B Matrix B of the state-space model.
 * @property Bm Matrix B of the state-space model excluding columns of measured disturbances.
 * @property C Matrix C of the state-space model.
 * @property Ax Matrix to compute states effect to states within prediction horizon.
 * @property Bx Matrix to compute inputs effect to states within prediction horizon.
 * @property ABx Matrix to compute control moves effect to states within prediction horizon.
 * @property Cz Combination of the state-space model C matrices to compute outputs prediction
 * from the states' prediction.
 * @property G MPC constants for optimal control moves computation.
 * @property H MPC constants for optimal control moves computation.
 * @constructor Create empty Mpc constants object.
 */
class MpcConstants(
    val A: SimpleMatrix,
    val B: SimpleMatrix,
    val Bm: SimpleMatrix,
    val C: SimpleMatrix,
    val Ax: SimpleMatrix,
    val Bx: SimpleMatrix,
    val ABx: SimpleMatrix,
    val Cz: SimpleMatrix,
    val G: SimpleMatrix,
    val H: SimpleMatrix
) {}