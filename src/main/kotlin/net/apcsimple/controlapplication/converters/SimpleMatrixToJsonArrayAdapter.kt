package net.apcsimple.controlapplication.converters

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.google.gson.Gson
import net.apcsimple.controlapplication.math.MatrixOps.Companion.simpleMatrixToArray
import org.ejml.simple.SimpleMatrix

class SimpleMatrixToJsonArrayAdapter(): StdSerializer<SimpleMatrix>(SimpleMatrix::class.java) {
    override fun serialize(value: SimpleMatrix, gen: JsonGenerator?, provider: SerializerProvider?) {
        gen?.writeStartArray()
        val gson = Gson()
        gen?.writeRaw(gson.toJson(simpleMatrixToArray(value)))
//        gen?.writeString(simpleMatrixToArray(value).joinToString(",", "[", "]") {
//            it.joinToString(",", "[", "]")
//        })
        gen?.writeEndArray()
    }
}