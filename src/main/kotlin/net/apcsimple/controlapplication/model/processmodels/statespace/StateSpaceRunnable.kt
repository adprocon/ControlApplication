package net.apcsimple.controlapplication.model.processmodels.statespace

import net.apcsimple.controlapplication.model.processmodels.ProcessModel
import net.apcsimple.controlapplication.services.ModelsServices
import org.ejml.simple.SimpleMatrix
import sklog.KotlinLogging

private var logger = KotlinLogging.logger {}

class StateSpaceRunnable(
    val model: ProcessModel,
    val service: ModelsServices
): Runnable {
    private val generalServices = service.generalServices
    override fun run() {
        runSimulation()
    }

    private fun runSimulation() {
        val stateSpaceModel = model.structure as StateSpaceModel
        val matrixA = SimpleMatrix(stateSpaceModel.matrixA)
        val matrixB = SimpleMatrix(stateSpaceModel.matrixB)
        val matrixC = SimpleMatrix(stateSpaceModel.matrixC)
//        val matrixD = SimpleMatrix(stateSpaceModel.matrixD)
        var states = SimpleMatrix(arrayOf(stateSpaceModel.initialStates.toDoubleArray())).transpose()
        var inputs: SimpleMatrix
        var outputs: SimpleMatrix
        var simulationCycle = model.simulationCycle
        var geneeralService = service.generalServices

        var starttime: Long
        var elapsedtime: Long
        var waitingtime: Long

//        logger.info("State-space simulation started.")

        try {
            while (model.simulationRunning) {
                /* Start timer to count execution time */
                starttime = System.nanoTime()

                inputs = geneeralService.readFromTagList(stateSpaceModel.inputs)

                states = matrixA.mult(states).plus(matrixB.mult(inputs))
                outputs = matrixC.mult(states)
                geneeralService.writeToTagList(stateSpaceModel.outputs, outputs)

                elapsedtime = (System.nanoTime() - starttime) / 10e6.toLong()
                waitingtime = simulationCycle - elapsedtime

                if (waitingtime < 0) {
                    generalServices.logError(logger, 2, "Simulation computation time exceeded for ${-waitingtime} ms.", 3)
                } else {
                    try {
                        Thread.sleep(waitingtime)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}