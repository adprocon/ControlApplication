package net.apcsimple.controlapplication.converters

import java.net.InetAddress
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class IpConverter : AttributeConverter<Any?, String> {
    override fun convertToDatabaseColumn(attribute: Any?): String {
        return attribute.toString().substring(1)
    }

    override fun convertToEntityAttribute(dbData: String): InetAddress? {
        try {
            return InetAddress.getByName(dbData)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
