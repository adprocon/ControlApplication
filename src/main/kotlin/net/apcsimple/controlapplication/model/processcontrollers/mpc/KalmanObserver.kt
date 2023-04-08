package net.apcsimple.controlapplication.model.processcontrollers.mpc

data class KalmanObserver(
    var matrixQk: Array<DoubleArray> = arrayOf(doubleArrayOf(0.1)),
    var matrixRk: Array<DoubleArray> = arrayOf(doubleArrayOf(0.1)),
    var matrixPk: Array<DoubleArray> = arrayOf(doubleArrayOf(0.1)),
    var matrixKk: Array<DoubleArray> = arrayOf(doubleArrayOf(0.1)),
    ) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KalmanObserver

        if (!matrixQk.contentDeepEquals(other.matrixQk)) return false
        if (!matrixRk.contentDeepEquals(other.matrixRk)) return false
        if (!matrixPk.contentDeepEquals(other.matrixPk)) return false
        if (!matrixKk.contentDeepEquals(other.matrixKk)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = matrixQk.contentDeepHashCode()
        result = 31 * result + matrixRk.contentDeepHashCode()
        result = 31 * result + matrixPk.contentDeepHashCode()
        result = 31 * result + matrixKk.contentDeepHashCode()
        return result
    }
}