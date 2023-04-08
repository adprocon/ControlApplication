package net.apcsimple.controlapplication.model.processmodels.statespace

import net.apcsimple.controlapplication.model.datapoints.Tag
import net.apcsimple.controlapplication.model.processmodels.GeneralModel

class StateSpaceModel(): GeneralModel {

    constructor(
        matrixA: Array<DoubleArray> = arrayOf(doubleArrayOf()),
        matrixB: Array<DoubleArray> = arrayOf(doubleArrayOf()),
        matrixC: Array<DoubleArray> = arrayOf(doubleArrayOf()),
        matrixD: Array<DoubleArray> = arrayOf(doubleArrayOf()),
        ): this() {
        this.matrixA = matrixA
        this.matrixB = matrixB
        this.matrixC = matrixC
        this.matrixD = matrixD
    }

    /* Data points used in Simulation */
    /**
     * List of data points of simulation model inputs
     */
    var inputs: MutableList<Tag> = mutableListOf()

    /**
     * List of data points of simulation model outputs
     */
    var outputs: MutableList<Tag> = mutableListOf()

    var initialStates: Array<Double> = arrayOf()

    var matrixA: Array<DoubleArray> = arrayOf(doubleArrayOf())
        set(value) {
            field = value
            this.initialStates = adjustMatrixSize(initialStates, value.size, 0.0)
        }
    var matrixB: Array<DoubleArray> = arrayOf(doubleArrayOf())
        set(value) {
            field = value
            adjustTagListSize(inputs, value[0].size)
        }
    var matrixC: Array<DoubleArray> = arrayOf(doubleArrayOf())
        set(value) {
            field = value
            adjustTagListSize(outputs, value.size)
        }
    var matrixD: Array<DoubleArray> = arrayOf(doubleArrayOf())


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StateSpaceModel

        if (!matrixA.contentDeepEquals(other.matrixA)) return false
        if (!matrixB.contentDeepEquals(other.matrixB)) return false
        if (!matrixC.contentDeepEquals(other.matrixC)) return false
        if (!matrixD.contentDeepEquals(other.matrixD)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = matrixA.contentDeepHashCode()
        result = 31 * result + matrixB.contentDeepHashCode()
        result = 31 * result + matrixC.contentDeepHashCode()
        result = 31 * result + matrixD.contentDeepHashCode()
        return result
    }

    override fun copy(): GeneralModel {
        val structure = StateSpaceModel()
        structure.matrixA = matrixA.clone()
        structure.matrixB = matrixB.clone()
        structure.matrixC = matrixC.clone()
        structure.matrixD = matrixD.clone()
        return structure
    }

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

    /**
     * Adjust matrix size to predefined dimensions
     *
     * @param matrix is input matrix that should be adjusted
     * @param size is the amount of rows that output matrix should have
     * @param defElem is a default element for the row, it also defines number of columns of the matrix
     *
     * @return is a matrix of predefined size
     */
    inline fun <reified T> adjustMatrixSize(matrix: Array<T>, size: Int, defElem: T): Array<T> {
        val arr: Array<T> = Array(size) { defElem }
        if (matrix.size < size) {
            System.arraycopy(matrix, 0, arr, 0, matrix.size)
        } else {
            System.arraycopy(matrix, 0, arr, 0, arr.size)
        }
        return arr
    }
}