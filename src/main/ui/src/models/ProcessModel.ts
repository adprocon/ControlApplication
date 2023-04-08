import {ModelType} from "./ModelType";
import {GeneralModel} from "./GeneralModel";

export class ProcessModel {
    id: number;
    name: string;
    type: ModelType;
    structure: GeneralModel;
    simulationCycle: number;
    simulationRunning: boolean;
}