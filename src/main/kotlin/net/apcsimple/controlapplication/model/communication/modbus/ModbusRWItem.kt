package net.apcsimple.controlapplication.model.communication.modbus

import com.fasterxml.jackson.annotation.JsonIgnore

//@Entity
class ModbusRWItem(
    var modbusAddress: Int = 0,
    tagName: String = DEFAULTTAGNAME,
    gain: Double = 1.0,
    offset: Double = 0.0,
    blocked: Boolean = false,

//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    override var id: Int = 0
) {

    //    @ManyToOne
//    @JoinColumn(name = "rwid")
    @JsonIgnore
    var noderw: ModbusRW? = null

    companion object {
        val DEFAULTTAGNAME = "Not used"
    }

    //>>>>>>>>>>>>>> tagName
    var tagName: String = tagName
    set(value) {
        if (blocked) {
            field = DEFAULTTAGNAME
        } else {
            field = value
        }
    }

    //>>>>>>>>>>>>>> gain
    var gain: Double = gain
        set(value) {
            if (blocked) {
                field = 1.0
            } else {
                field = value
            }
        }

    //>>>>>>>>>>>>>> offset
    var offset: Double = offset
        set(value) {
            if (blocked) {
                field = 0.0
            } else {
                field = value
            }
        }

    //>>>>>>>>>>>>>> blocked
    var blocked: Boolean = blocked
        set(value) {
            if (value) {
                tagName = DEFAULTTAGNAME
                gain = 1.0
                offset = 0.0
                field = value
            } else {
                field = value
            }
        }

    //>>>>>>>>>>>>>> ieee754
    var ieee754: Boolean = false
        set(value) {
            if (blocked) {
                field = false
            } else {
                try {
                    if (field == value) return
                    field = value
                    val index: Int = noderw?.mbRwTagList?.indexOf(this) ?: return
                    if (noderw!!.length <= index + 1) return
                    if (value) {
                        blockTagItem()
                    } else {
                        unBlockTagItem()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    fun blockTagItem() {
        try {
            val index: Int = noderw?.mbRwTagList?.indexOf(this)!!
            val tagToBlock = noderw?.mbRwTagList?.get(index + 1)
            tagToBlock?.blocked = true
            tagToBlock?.ieee754 = false
            tagToBlock?.gain = 1.0
            tagToBlock?.offset = 0.0
            tagToBlock?.tagName = DEFAULTTAGNAME
//            noderw?.modbusNode?.processInterface?.interfaceService?.editModbusRwItem(tagToBlock!!, noderw!!, index)
            noderw?.modbusNode?.processInterface?.let {noderw?.modbusNode?.processInterface?.interfaceServices?.saveInterface(it)}
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun unBlockTagItem() {
        val index: Int = noderw?.mbRwTagList?.indexOf(this) ?: return
        val tagToUnblock = noderw?.mbRwTagList?.get(index + 1) ?: return
        tagToUnblock.blocked = false
//        noderw?.modbusNode?.processInterface?.interfaceService?.editModbusRwItem(tagToUnblock, noderw!!, index)
        noderw?.modbusNode?.processInterface?.let {noderw?.modbusNode?.processInterface?.interfaceServices?.saveInterface(it)}
    }

}