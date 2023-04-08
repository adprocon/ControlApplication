import {GeneralController} from "./GeneralController";
import {ControllerType} from "./ControllerType";

export class ProcessController {
    id: number = 0;
    name: string = "New controller";
    type: ControllerType;
    running: boolean = false;
    structure: GeneralController;
    cycleTime: number = 1000;
}