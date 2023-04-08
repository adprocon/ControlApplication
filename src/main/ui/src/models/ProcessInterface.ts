import {InterfaceType} from "./InterfaceType";
import ModbusNode from "./ModbusNode";

export class ProcessInterface {
    id?: any;
    name: string;
    type: InterfaceType;
    running: boolean;
    structure: ModbusNode;
    status: string;
}