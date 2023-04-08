package net.apcsimple.controlapplication.converters

import org.ejml.simple.SimpleMatrix

class MatrixConverter {

    companion object {
        fun fromSimpleMatrix(mat: SimpleMatrix?): Array<DoubleArray> {
            if (mat === null) return arrayOf(doubleArrayOf(0.0))
            val matrix: Array<DoubleArray> = Array(mat.numRows()) { _ -> DoubleArray(mat.numCols()) }
            mat.ddrm.data.forEachIndexed { i, el ->
                matrix[i / mat.numCols()][i % mat.numCols()] = el
            }
            return matrix
        }
    }
}