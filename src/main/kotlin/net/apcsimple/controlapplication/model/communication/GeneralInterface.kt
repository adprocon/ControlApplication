package net.apcsimple.controlapplication.model.communication

import com.fasterxml.jackson.annotation.JsonIgnore
import net.apcsimple.controlapplication.model.datapoints.TagList
import net.apcsimple.controlapplication.model.processmodels.GeneralModel

abstract class GeneralInterface {

    @JsonIgnore
    lateinit var processInterface: ProcessInterface

    @JsonIgnore
    lateinit var tagList: TagList

    abstract fun startInterface()
    abstract fun copy(): GeneralModel
}