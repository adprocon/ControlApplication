export class ModbusRwItem {
    modbusAddress: number;
    tagName: string;
    gain: number;
    offset: number;
    id: number;
    ieee754: boolean;
    blocked: boolean;
}