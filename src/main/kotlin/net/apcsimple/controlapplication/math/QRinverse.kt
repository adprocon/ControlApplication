package net.apcsimple.controlapplication.math

import net.apcsimple.controlapplication.math.MatrixOps.Companion.eye
import net.apcsimple.controlapplication.math.MatrixOps.Companion.multix
import net.apcsimple.controlapplication.math.MatrixOps.Companion.shift
import net.apcsimple.controlapplication.math.MatrixOps.Companion.slice
import net.apcsimple.controlapplication.math.MatrixOps.Companion.transpix
import net.apcsimple.controlapplication.math.MatrixOps.Companion.zerix

class QRinverse {

    var A: Array<DoubleArray> = arrayOf(doubleArrayOf(0.0))
    var y: Array<DoubleArray> = arrayOf(doubleArrayOf(0.0))
    var Q: Array<DoubleArray> = arrayOf(doubleArrayOf(0.0))
    var x: Array<DoubleArray> = arrayOf(doubleArrayOf(0.0))

    fun QRinverse(AA: Array<DoubleArray>, yy: Array<DoubleArray>) {
        A = AA
        y = yy
        val sz_v: Int = A.size
        val sz_h: Int = A[0].size
        Q = eye(sz_v)
        triangolize()
        val Qt: Array<DoubleArray> = transpix(Q)
        y = multix(Qt, y)!!
        x = Array(sz_h) { DoubleArray(1) }
        x[sz_h - 1][0] = y[sz_h - 1][0] / A[sz_h - 1][sz_h - 1]
        for (i in 2 until sz_h + 1) {
            for (j in 1 until i) {
                y[sz_h - i][0] = y[sz_h - i][0] - A[sz_h - i][sz_h - j] * x[sz_h - j][0]
            }
            x[sz_h - i][0] = y[sz_h - i][0] / A[sz_h - i][sz_h - i]
        }
    }

    fun triangolize() {
        val sz_v = A.size // matrix vertical dimension
        val sz_h = A[0].size // matrix horizontal dimension
        var hd: Int // horizontal dimension
        var vd: Int // vertical dimension
        var Am: Array<DoubleArray>
        val m_min = Math.min(sz_v, sz_h)
        for (k in 0 until m_min) {
            hd = sz_h - k
            vd = sz_v - k
            if (vd > 0) {
                Am = slice(A, k, k, vd, hd)
                val u1 = calc_u(Am)
                val h = calc_H(u1)
                var Hm: Array<DoubleArray> = eye(sz_v)
                Hm = shift(Hm, h, k, k)
                Q = multix(Q, Hm)!!
                A = multix(Hm, A)!!
            }
        }
    }

    fun calc_u(A: Array<DoubleArray>): DoubleArray {
        var A = A
        A = transpix(A)
        val U = A[0]
        U[0] = U[0] + Math.signum(U[0]) * normV(U)
        val u_norm = normV(U)
        for (i in A[0].indices) {
            if (u_norm > 0) {
                U[i] = U[i] / u_norm
            } else {
                U[i] = 0.0
            }
        }
        return U
    }

    fun calc_H(U: DoubleArray): Array<DoubleArray> {
        val n = U.size
        val H: Array<DoubleArray> = zerix(n, n)
        for (i in 0 until n) {
            for (j in 0 until n) {
                H[i][j] = H[i][j] - 2 * U[i] * U[j]
            }
            H[i][i] = H[i][i] + 1
        }
        return H
    }

    fun normV(A: DoubleArray): Double {
        var x = 0.0
        for (i in A.indices) {
            x = x + A[i] * A[i]
        }
        x = Math.sqrt(x)
        return x
    }

    fun GetX(): Array<DoubleArray> {
        return x
    }
}