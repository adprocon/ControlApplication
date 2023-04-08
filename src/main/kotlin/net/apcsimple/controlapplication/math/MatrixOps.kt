package net.apcsimple.controlapplication.math

import org.ejml.simple.SimpleMatrix
import java.util.*

class MatrixOps {

    companion object {
        /**
         * Bring matrix A to the power of n.
         *
         * @param A square matrix.
         * @param n positive number.
         * @return Power n of matrix A.
         */
        fun powerix(A: Array<DoubleArray>, n: Int): Array<DoubleArray>? {
            var A1: Array<DoubleArray>?
            val m = A.size
            A1 = eye(m)
            try {
                for (i in 0 until n) {
                    A1 = multix(A1, A)
                }
            } catch (ex: Exception) {
                println(ex)
                return null
            }
            return A1
        }

        /**
         * Create and returns copy of matrix A.
         *
         * @param A any matrix.
         * @return Copy of matrix A.
         */
        fun copy(A: Array<DoubleArray>): Array<DoubleArray> {
            val B = Array(A.size) { DoubleArray(A[0].size) }
            for (i in A.indices) {
                for (j in A[0].indices) {
                    B[i][j] = A[i][j]
                }
            }
            return B
        }

        /**
         * Multiply matrices A and B.
         *
         * @param A any matrix.
         * @param B the number of columns of B equals to the number of rows of A, and the number of rows in B equals to the number of columns in A.
         * @return Multiplication of matrices A and B.
         */
        fun multix(A: Array<DoubleArray>?, B: Array<DoubleArray>): Array<DoubleArray>? {
            if (A!![0].size != B.size) {
                println("Multix error! Mitrices dimensions are not consistent!")
                return null
            }
            val m = A.size
            val n = B[0].size
            val p = B.size
            val C = Array(m) { DoubleArray(n) }
            for (i in 0 until m) {
                for (j in 0 until n) {
                    for (k in 0 until p) {
                        C[i][j] = C[i][j] + A[i][k] * B[k][j]
                    }
                }
            }
            return C
        }

        /**
         * Multiply matrix A with scalar b.
         *
         * @param b scalar value.
         * @param A matrix.
         * @return Scalar multiplication of scalar value b and matrix A.
         */
        fun multixscal(b: Double, A: Array<DoubleArray>?): Array<DoubleArray> {
            val m = A!!.size
            val n = A[0].size
            val C = Array(m) { DoubleArray(n) }
            for (i in 0 until m) {
                for (j in 0 until n) {
                    C[i][j] = A[i][j] * b
                }
            }
            return C
        }

        /**
         * Create unity matrix.
         *
         * @param n dimension of the matrix.
         * @return Returns unity matrix of the dimension n.
         */
        fun eye(n: Int): Array<DoubleArray> {
            val A = Array(n) { DoubleArray(n) }
            for (i in 0 until n) {
                for (j in 0 until n) {
                    A[i][j] = 0.0
                }
                A[i][i] = 1.0
            }
            return A
        }

        /**
         * Create zero matrix.
         *
         * @param n number of rows in the matrix.
         * @param m number of columns in the matrix.
         * @return Zero matrix of the dimension n x m.
         */
        fun zerix(n: Int, m: Int): Array<DoubleArray> {
            val A = Array(n) { DoubleArray(m) }
            for (i in 0 until n) {
                for (j in 0 until m) {
                    A[i][j] = 0.0
                }
            }
            return A
        }

        /**
         * Add matrix B to matrix A.
         *
         * @param A matrix.
         * @param B matrix of the same dimension as A.
         * @return Sum of matrices A and B.
         */
        fun sumix(A: Array<DoubleArray>, B: Array<DoubleArray>): Array<DoubleArray>? {
            if (A.size != B.size || A[0].size != B[0].size) {
                println("Sumix error! Mitrices dimensions are not consistent!")
                return null
            }
            val m = A.size
            val n = A[0].size
            val C = Array(m) { DoubleArray(n) }
            for (i in 0 until m) {
                for (j in 0 until n) {
                    C[i][j] = A[i][j] + B[i][j]
                }
            }
            return C
        }

        /**
         * Subtruct matrix B from matrix A.
         *
         * @param A matrix.
         * @param B matrix of the same dimension as A.
         * @return Difference between matrices A and B.
         */
        fun difix(A: Array<DoubleArray>?, B: Array<DoubleArray>?): Array<DoubleArray>? {
            if (A!!.size != B!!.size || A[0].size != B[0].size) {
                println("Multix error! Mitrices dimensions are not consistent!")
                return null
            }
            val m = A.size
            val n = A[0].size
            val C = Array(m) { DoubleArray(n) }
            for (i in 0 until m) {
                for (j in 0 until n) {
                    C[i][j] = A[i][j] - B[i][j]
                }
            }
            return C
        }

        /**
         * Compute norm of matrix A.
         *
         * @param A matrix.
         * @return Norm of the matrix A.
         */
        fun norm(A: Array<DoubleArray>?): Double {
            val m = A!!.size
            val n = A[0].size
            var x = 0.0
            for (i in 0 until m) {
                for (j in 0 until n) {
                    x = x + A[i][j] * A[i][j]
                }
            }
            return Math.sqrt(x)
        }

        /**
         * Slice matrix A starting with row V and column H for vd in vertical direction and hd in horizontal direction.
         *
         * @param A  matrix.
         * @param V  number of row to start slicing.
         * @param H  number of columns to start slicing.
         * @param vd how many rows to slice.
         * @param hd how many columns to slice.
         * @return Part of matrix A of the dimension vd x hd.
         */
        fun slice(A: Array<DoubleArray>, V: Int, H: Int, vd: Int, hd: Int): Array<DoubleArray> {
            if (vd <= 0 || hd <= 0) {
                return A
            }
            val B = Array(vd) { DoubleArray(hd) }
            for (i in 0 until vd) {
                for (j in 0 until hd) {
                    B[i][j] = A[i + V][j + H]
                }
            }
            return B
        }

        /**
         * Put matrix B into matrix A starting with row V and column H.
         *
         * @param A bigger destination matrix
         * @param B smaller source matrix.
         * @param V copy values of matrix B to the matrix A starting in this row.
         * @param H copy values of matrix B to the matrix A starting in this column.
         * @return Matrix A with values of matrix B starting in row V and column H.
         */
        fun shift(A: Array<DoubleArray>, B: Array<DoubleArray>, V: Int, H: Int): Array<DoubleArray> {
            val m = B.size
            val n = B[0].size
            for (i in 0 until m) {
                for (j in 0 until n) {
                    A[i + V][j + H] = B[i][j]
                }
            }
            return A
        }

        fun removeColumn(A: Array<DoubleArray>, index: Int): Array<DoubleArray> {
            if (index < A[0].size && index >= 0) {
                val B = Array(A.size) { DoubleArray(A[0].size - 1) }
                var bRow = 0
                for (sample in A) {
                    var bColumn = 0
                    for (i in A[0].indices) {
                        if (i != index) {
                            B[bRow][bColumn] = sample[i]
                            bColumn++
                        }
                    }
                    bRow++
                }
                return B
            }
            return A.clone()
        }

        /**
         * Fill matrix A with values of matrix B. If matrix A is smaller, then part of matrix B is copied to A.
         * If matrix A is bigger, then values above B dimension are 0.
         *
         * @param A matrix.
         * @param B matrix
         * @return Matrix A filled with B values.
         */
        fun fillMatrix(A: Array<DoubleArray>, B: Array<DoubleArray>): Array<DoubleArray> {
            try {
                val rowsMin = A.size.coerceAtMost(B.size)
                val columnsMin = A[0].size.coerceAtMost(B[0].size)
                for (i in 0 until rowsMin) {
                    for (j in 0 until columnsMin) {
                        A[i][j] = B[i][j]
                    }
                }
            } catch (ne: NullPointerException) {
                ne.printStackTrace()
            }
            return A
        }

        fun fillMatrix(A: Array<BooleanArray>, B: Array<BooleanArray>): Array<BooleanArray> {
            try {
                val rowsMin = A.size.coerceAtMost(B.size)
                val columnsMin = A[0].size.coerceAtMost(B[0].size)
                for (i in 0 until rowsMin) {
                    for (j in 0 until columnsMin) {
                        A[i][j] = B[i][j]
                    }
                }
            } catch (ne: NullPointerException) {
                ne.printStackTrace()
            }
            return A
        }


        /**
         * Fill vector A with values of vector B. If vector A is smaller, then part of vector B is copied to A.
         * If vector A is bigger, then values above B dimension are 0.
         *
         * @param A vector.
         * @param B vector
         * @return Vector A filled with B values.
         */
        fun fillVector(A: Array<Double>, B: Array<Double>): Array<Double> {
            try {
                val rows_min = Math.min(A.size, B.size)
                for (i in 0 until rows_min) {
                    A[i] = B[i]
                }
            } catch (ne: NullPointerException) {
                ne.printStackTrace()
            }
            return A
        }

        fun fillVector(A: Array<Boolean>, B: Array<Boolean>): Array<Boolean> {
            try {
                val rows_min = Math.min(A.size, B.size)
                for (i in 0 until rows_min) {
                    A[i] = B[i]
                }
            } catch (ne: NullPointerException) {
                ne.printStackTrace()
            }
            return A
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


        /**
         * Transpose matrix A.
         *
         * @param A matrix.
         * @return Transpose of matrix A.
         */
        fun transpix(A: Array<DoubleArray>?): Array<DoubleArray> {
            val m = A!!.size
            val n = A[0].size
            val B = Array(n) { DoubleArray(m) }
            for (i in 0 until n) {
                for (j in 0 until m) {
                    B[i][j] = A[j][i]
                }
            }
            return B
        }

        /**
         * Make matrix A gaussian inverse. Matrix A have to be square.
         *
         * @param A square matrix.
         * @return Gaussian inverse of matrix A.
         */
        fun gaussian(A: Array<DoubleArray>): Array<DoubleArray>? {
            val sz = A.size
            if (A.size != A[0].size) {
                println("Gaussian error!  Matrix should be square!")
                return null
            }
            val B = eye(sz)
            var coef1: Double
            var coef2: Double
            var n = 0
            for (i in 0 until sz) {
                coef1 = A[n][i]
                for (j in 0 until sz) {
                    if (j != n) {
                        if (coef1 == 0.0) {
                            coef1 = 0.000001
                        }
                        coef2 = A[j][i] / coef1
                        for (k in 0 until sz) {
                            A[j][k] = A[j][k] - A[n][k] * coef2
                            B[j][k] = B[j][k] - B[n][k] * coef2
                        }
                    }
                }
                for (k in 0 until sz) {
                    A[n][k] = A[n][k] / coef1
                    B[n][k] = B[n][k] / coef1
                }
                n++
            }
            return B
        }

        /**
         * Least mean square method implementation.
         *
         * @param A      matrix double[m][n].
         * @param b      vector double[n][1].
         * @param x      vector double[n][1].
         * @param lambda suitable accuracy of the result is less than this value.
         * @param iter   maximum number of itterations of the algorithm.
         * @return Solution of the system of equations Ax = b.
         */
        fun lms(
            A: Array<DoubleArray>,
            b: Array<DoubleArray>,
            x: Array<DoubleArray>,
            lambda: Double,
            iter: Int
        ): Array<DoubleArray>? {
            // Least mean square with Newton method
            // 2017-11-28

            // Cost function
            // V = (Ax - b)(Ax - b) = xTATAx - 2xTATb + bTb
            // dV/dx = 2ATAx - 2ATb = grad
            // d2V/dx2 = 2ATA = Hesse
            var tempA: Array<DoubleArray>
            var tempB: Array<DoubleArray>
            var Hesse: Array<DoubleArray>
            var grad: Array<DoubleArray>
            var xm: Array<DoubleArray> = Array(x.size) { DoubleArray(1) }
            var norm = 100000.0
            var k = 0
            for (i in x.indices) {
                xm[i][0] = x[i][0]
            }
            while (k < iter && norm > lambda) {
                tempA = transpix(A)
                Hesse = multix(tempA, A) ?: return null
                tempA = multix(Hesse, xm) ?: return null
                Hesse = multixscal(2.0, Hesse)
                tempB = multix(transpix(A), b) ?: return null
                grad = difix(tempA, tempB) ?: return null
                grad = multixscal(2.0, grad)

                // x(k+1) = x(k) - eta x Hesse^-1 x grad
                val QR = QRinverse()
                QR.QRinverse(Hesse, grad)
                // following grad = Hesse^-1 x grad
                grad = QR.GetX()

                // Search for optimal eta
                // V = (A(x - eta * grad) - b)(A(x - eta * grad) - b) = (x - eta * grad)TATA(x - eta * grad) - 2(x - eta * grad)TATb + bTb
                // V = xTATAx - 2*eta*gradT*ATAx + eta^2 * gradT*ATA*grad - 2xTATb + 2*eta*gradT*ATb + bTb
                // V = xTATAx - 2*eta*gradT*AT(Ax - b) + eta^2 * gradT*ATA*grad + bTb
                // dV/deta = 2*eta*gradT*ATA*grad - 2*gradT*AT(Ax - b) = 0
                // eta = (gradT*ATA*grad)^-1 * gradT*AT(Ax - b)
                tempA = multix(transpix(grad), transpix(A)) ?: return null
                tempA = multix(tempA, A) ?: return null
                tempA = multix(tempA, grad) ?: return null
                tempB = multix(A, xm) ?: return null
                tempB = difix(tempB, b) ?: return null
                tempB = multix(transpix(A), tempB) ?: return null
                tempB = multix(transpix(grad), tempB) ?: return null
                //tempB[0][0] = tempB[0][0] * 2;

                // eta = tempA
                if (tempA[0][0] != 0.0) {
                    tempA[0][0] = tempB[0][0] / tempA[0][0]
                }
                tempB = multixscal(tempA[0][0], grad)
                xm = difix(xm, tempB) ?: return null
                norm = norm(difix(multix(A, xm), b))
                k++
            }
            return xm
        }

        fun printVector(name: String, A: DoubleArray) {
            var vector = ""
            try {
                for (i in A.indices) {
                    vector = vector + A[i] + " "
                }
            } catch (ne: NullPointerException) {
                ne.printStackTrace()
            }
            println("Vector $name:")
            println(vector)
        }

        fun printVector(name: String, A: Array<Boolean>) {
            var vector = ""
            try {
                for (i in A.indices) {
                    vector = vector + A[i] + " "
                }
            } catch (ne: NullPointerException) {
                ne.printStackTrace()
            }
            println("Vector $name:")
            println(vector)
        }

        fun printMatrix(name: String, A: Array<BooleanArray>) {
            val matrix = arrayOfNulls<String>(A.size)
            try {
                for (i in A.indices) {
                    matrix[i] = ""
                    for (j in A[0].indices) {
                        matrix[i] = matrix[i] + A[i][j] + " "
                    }
                }
            } catch (ne: NullPointerException) {
                ne.printStackTrace()
            }
            println("Matrix $name:")
            for (i in matrix.indices) {
                println(matrix[i])
            }
        }

        fun printMatrix(name: String, A: Array<Array<String>>) {
            val matrix = arrayOfNulls<String>(A.size)
            try {
                for (i in A.indices) {
                    matrix[i] = ""
                    for (j in A[0].indices) {
                        matrix[i] = matrix[i] + A[i][j] + " "
                    }
                }
            } catch (ne: NullPointerException) {
                ne.printStackTrace()
            }
            println("Matrix $name:")
            for (i in matrix.indices) {
                println(matrix[i])
            }
        }

        fun printMatrix(name: String, A: Array<DoubleArray>) {
            val matrix = arrayOfNulls<String>(A.size)
            try {
                for (i in A.indices) {
                    matrix[i] = ""
                    for (j in A[0].indices) {
                        matrix[i] = matrix[i] + A[i][j] + " "
                    }
                }
            } catch (ne: NullPointerException) {
                ne.printStackTrace()
            }
            println("Matrix $name:")
            for (i in matrix.indices) {
                println(matrix[i])
            }
        }

        fun matrixToString(A: Array<DoubleArray>): String {
            var matrix = "["
            try {
                for (i in A.indices) {
                    for (j in A[0].indices) {
                        matrix += A[i][j]
                        matrix += " "
                    }
                    matrix = matrix.dropLast(1)
                    matrix += ";"
                }
                matrix += "]"
            } catch (ne: NullPointerException) {
                ne.printStackTrace()
            }
            return matrix
        }

        fun toDiagonal(A: Array<DoubleArray>): Array<DoubleArray> {
            val out = Array(A.size * A[0].size) { DoubleArray(A.size * A[0].size) }
            A.forEachIndexed { indexR, row ->
                row.forEachIndexed { indexC, elem ->
                    out[indexR + A.size * indexC][indexR + A.size * indexC] = elem
                }
            }
            return out
        }


        /**
         * This function converts SimpleMatrix type to common Array of the same dimension
         * @param A: SimpleMatrix to be converted
         * @return array from matrix A
         */
        fun simpleMatrixToArray(A: SimpleMatrix): Array<DoubleArray> {
            val arA = Array(A.numRows()) { DoubleArray(0) }
            for (i in 0 until A.numRows()) {
                arA[i] = A.extractVector(true, i).ddrm.getData()
            }
            return arA
        }
    }
}