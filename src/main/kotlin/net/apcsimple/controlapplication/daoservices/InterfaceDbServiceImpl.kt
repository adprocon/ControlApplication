package net.apcsimple.controlapplication.daoservices

import net.apcsimple.controlapplication.daorepositories.InterfaceRepository
import net.apcsimple.controlapplication.model.communication.ProcessInterface
import org.springframework.stereotype.Service

@Service
class InterfaceDbServiceImpl(
    val interfaceRepository: InterfaceRepository,
//    val modbusRWRepository: ModbusRWReposit ory,
//    val modbusRWItemRepository: ModbusRWItemRepository,
): InterfaceDbService {
    override fun saveInterface(node: ProcessInterface) {
        interfaceRepository.save(node)
    }

    override fun deleteInterface(id: Long) {
        interfaceRepository.deleteById(id)
    }

//    override fun saveModbusRW(rw: ModbusRW) {
//        modbusRWRepository.save(rw)
//    }
//
//    override fun deleteModbusRW(id: Int) {
//        modbusRWRepository.deleteById(id)
//    }
//
//    override fun saveModbusRWItem(item: ModbusRWItem) {
//        modbusRWItemRepository.save(item)
//    }
//
//    override fun deleteModbusRWItem(id: Int) {
//        modbusRWItemRepository.deleteById(id)
//    }

    override fun loadInterfacesFromDB(): MutableList<ProcessInterface> {
        return interfaceRepository.findAll()
    }
}