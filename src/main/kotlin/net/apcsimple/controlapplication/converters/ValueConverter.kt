package net.apcsimple.controlapplication.converters

import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class ValueConverter : AttributeConverter<Any, String> {
    override fun convertToDatabaseColumn(attribute: Any): String {
        return attribute.toString()
    }

    override fun convertToEntityAttribute(dbData: String): Any {
        return dbData
    }
}
