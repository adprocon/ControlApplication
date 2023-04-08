package net.apcsimple.controlapplication.converters

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import net.apcsimple.controlapplication.model.processmodels.ProcessModel

class ModelToJsonAdapter(): StdSerializer<ProcessModel>(ProcessModel::class.java) {
    override fun serialize(value: ProcessModel?, gen: JsonGenerator?, provider: SerializerProvider?) {
        gen?.writeStartObject()
        gen?.writeStringField("id", value?.id.toString())
        gen?.writeStringField("name", value?.name)
        gen?.writeEndObject()
    }


}