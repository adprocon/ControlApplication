import modbusRwType from "./ModbusRwType";
import ModbusRwType from "./ModbusRwType";
import {ModbusRwItem} from "./ModbusRwItem";

class ModbusRW {
    // rwid?: number = 0;
    read: boolean = true;
    name: string = 'Instance';
    slaveID: number = 1;
    address: number = 0;
    type: modbusRwType = ModbusRwType.HOLDINGREGISTER;
    tagList: Array<ModbusRwItem>;
    length: number = 0;
}

export default ModbusRW;