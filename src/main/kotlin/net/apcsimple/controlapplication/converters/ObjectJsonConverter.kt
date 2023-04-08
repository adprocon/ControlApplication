package net.apcsimple.controlapplication.converters

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import sklog.KotlinLogging
import java.io.IOException

private var logger = KotlinLogging.logger {}

open class ObjectJsonConverter() {
    companion object {
        fun <T> convertToJSON(attribute: T): String {
            val objectMapper = ObjectMapper()
            var obj = ""
            try {
                obj = objectMapper.writeValueAsString(attribute)
            } catch (e: JsonProcessingException) {
                logger.error("JSON writing error")
            }
            return obj
        }

        fun <T> convertFromJSON(data: String, cls: Class<T>): T? {
            val objectMapper = ObjectMapper()
            var obj: T? = null
            try {
                obj = objectMapper.readValue(data, cls)
            } catch (e: IOException) {
                logger.error("JSON reading error")
                e.printStackTrace()
            }
            return obj
        }
    }
}