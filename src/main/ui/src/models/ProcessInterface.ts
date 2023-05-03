import {InterfaceType} from "./InterfaceType";

export class ProcessInterface {
    id?: any;
    name: string;
    type: InterfaceType;
    running: boolean;
    structure: any;
    status: string;
}