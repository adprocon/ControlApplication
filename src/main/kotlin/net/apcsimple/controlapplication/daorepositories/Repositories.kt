package net.apcsimple.controlapplication.daorepositories

import net.apcsimple.controlapplication.model.communication.ProcessInterface
import net.apcsimple.controlapplication.model.communication.modbus.ModbusNode
import net.apcsimple.controlapplication.model.communication.modbus.ModbusRW
import net.apcsimple.controlapplication.model.communication.modbus.ModbusRWItem
import net.apcsimple.controlapplication.model.datapoints.Tag
import net.apcsimple.controlapplication.model.processcontrollers.ProcessController
import net.apcsimple.controlapplication.model.processmodels.ProcessModel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

/**
 * Handles Tag related database operations.
 */
@Component
interface TagRepository: JpaRepository<Tag, Int> {}

@Component
interface InterfaceRepository: JpaRepository<ProcessInterface, Long> {}

//interface ModbusRWRepository: JpaRepository<ModbusRW, Int> {}
//
//interface ModbusRWItemRepository: JpaRepository<ModbusRWItem, Int> {}

@Component
interface ProcessModelRepository: JpaRepository<ProcessModel, Int> {}

@Component
interface ProcessControllerRepository: JpaRepository<ProcessController, Int> {}