import ModbusRW from "./ModbusRW";

class ModbusNode {
    ipAddress: string = '127.0.0.1';
    port: number = 502;
    readWriteCycle: number = 1000;
    switchBytes: boolean;
    switchRegisters: boolean;
    dataReads: Array<ModbusRW>;
    dataWrites: Array<ModbusRW>;
}

export default ModbusNode;