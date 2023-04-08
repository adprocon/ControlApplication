package net.apcsimple.controlapplication.model.communication.modbus

import com.fasterxml.jackson.annotation.JsonIgnore

//@Entity
class ModbusRW(

    // Is this RW read or write
    var read: Boolean = true,

    var name: String = "Instance",
    var slaveID: Int = 1,
    address: Int = 0,
    var type: String = HOLDINGREGISTER,

//    @OneToMany(fetch = FetchType.EAGER, mappedBy = "noderw")
//    @JsonIgnore
    val mbRwTagList: MutableList<ModbusRWItem> = mutableListOf(),

//    @ManyToOne
//    @JoinColumn(name = "id")
    @JsonIgnore
    var modbusNode: ModbusNode? = null,

    /**
     * Generated unique identifier for the database.
     */
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    val rwid: Int? = null
) {

//    @Transient
//    @JsonIgnore
//    var interfaceService: InterfaceService? = null

//    @Transient
//    @JsonIgnore
//    var modbusRWItemFactory: ObjectFactory<ModbusRWItem>? = null


    var address: Int = address
        set(value) {
            field = value
            for (i in mbRwTagList.indices) {
                mbRwTagList[i].modbusAddress = value + i
            }
        }

    /**
     * Amount of Modbus elements to be exchanged in this ReadWrite
     * If amount changes than elements are added or removed
     */
    var length: Int = 0
        set(value) {
            if (value > field) {
                for (i in field until value) {
                    val item = ModbusRWItem()
                    item.modbusAddress = address + i
                    item.noderw = this
                    modbusNode?.processInterface?.interfaceServices?.addModbusRwItem(item)
                }
            } else if (value < field) {
                for (i in field - 1 downTo value) {
                    modbusNode?.processInterface?.interfaceServices?.deleteModbusRwItem(i, this)
                }
            }
            modbusNode?.processInterface?.let { modbusNode?.processInterface?.interfaceServices?.saveInterface(it) }
            field = value
        }

    companion object {
        // Registers/coils types
        val INPUTCOIL = "Input Coil"
        val HOLDINGCOIL = "Holding Coil"
        val INPUTREGISTER = "Input Register"
        val HOLDINGREGISTER = "Holding Register"
    }


//    fun addModbusRwItem(item: ModbusRWItem): Boolean {
//        return try {
//            tagList.add(item)
////            modbusNode?.modbusDbService?.saveModbusRWItem(item)
//            true
//        } catch (e: Exception) {
//            false
//        }
//    }
//
//    fun editModbusRwItem(editedItem: ModbusRWItem): Boolean {
//        return try {
//            val item = findItemById(editedItem.id) ?: return false
//            item.tagName = editedItem.tagName
//            item.gain = editedItem.gain
//            item.offset = editedItem.offset
//            item.ieee754 = editedItem.ieee754
////            modbusNode?.modbusDbService?.saveModbusRWItem(item)
//            true
//        } catch (e: Exception) {
//            false
//        }
//    }
//
//    fun deleteModbusRwItem(id: Int): Boolean {
//        return try {
////            modbusNode?.modbusDbService?.deleteModbusRWItem(id)
//            tagList.remove(findItemById(id))
//            true
//        } catch (e: Exception) {
//            false
//        }
//
//    }
//
//    fun findItemById(id: Int): ModbusRWItem? {
//        return tagList.find { item -> item.id == id }
//    }
}