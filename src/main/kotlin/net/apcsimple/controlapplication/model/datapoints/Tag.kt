package net.apcsimple.controlapplication.model.datapoints

import sklog.KotlinLogging
import javax.persistence.*

private val logger = KotlinLogging.logger {}

/**
 * This class provides model for data points used in the application. Data points are inputs and outputs for process
 * that are sent over communication interface and used in the controllers.
 */
@Entity
class Tag(
    /**
     * TagName is used as a name of a data point. Must be unique.
     */
    @Column(nullable = false, name = "tagname")
    var tagName: String,
    /**
     * Three data types are available for data points: integer, double and boolean.
     */
    @Column(nullable = false, name = "datatype")
    var dataType: String,
    value: Any,
    /**
     * Generated unique identifier for the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int? = null,
) {
    // Setters
    /**
     * Value have to be correct for the selected types. It is checked and corrected
     * automatically with correctValueToType() method.
     */
    @Column(nullable = false)
    @Convert(converter = net.apcsimple.controlapplication.converters.ValueConverter::class)
    var value: Any = correctValueToType(value) ?: this.correctValueToType(0)!!
        set(value) {
            field = correctValueToType(value) ?: this.correctValueToType(0)!!
        }

    // Static variables
    companion object {
        val TAGDOUBLE ="double"
        val TAGINTEGER ="int"
        val TAGBOOLEAN = "bool"
        val UNKNOWNTYPE = "N/A"

        val DEFAULT_NAME = "Not used"

        fun newTag(): Tag {
            return Tag(Tag.DEFAULT_NAME, Tag.TAGDOUBLE, 0.0)
        }
    }

    // Methods
    /**
     * As value can be of different types, it has to be checked and corrected (if needed) before modification.
     */
    fun correctValueToType(value: Any): Any? {
//        logger.info("Value is " + value + ".")
        when (dataType) {
            TAGDOUBLE -> {
                return when (value) {
                    "true" -> 1.0
                    "false" -> 0.0
                    is Boolean -> if (value) 1.0 else 0.0
                    is Int -> value.toDouble()
                    is Double -> value
                    is String -> value.toDouble()
                    else -> 0.0
                }
            }
            TAGINTEGER -> {
                return when (value) {
                    "true" -> 1
                    "false" -> 0
                    is Boolean -> if (value) 1 else 0
                    is Int -> value
                    is Double -> value.toInt()
                    is String -> value.toInt()
                    else -> 0
                }
            }
            TAGBOOLEAN -> {
                return when (value) {
                    "true" -> true
                    "false" -> false
                    is Boolean -> value
                    is Int -> value != 0
                    is Double -> value != 0.0
                    else -> false
                }
            }
        }
        return UNKNOWNTYPE
    }

    /**
     * Method to create a copy of the object.
     */
    fun copy(): Tag {
        return Tag(this.tagName, this.dataType, this.value, this.id)
    }

    // Overridden methods
    /**
     * Overridden equals method.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as Tag
        if (id != that.id) return false
        return true
    }

    /**
     * Overridden hashCode method.
     */
    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    /**
     * Overridden toString method.
     */
    override fun toString(): String {
        return "Tagname is $tagName, data type is $dataType, value is $value, id is $id."
    }
}

