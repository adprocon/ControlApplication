import {DataType} from "./DataType";

export class Tag {
    id: number = 0;
    tagName: string = "Not used";
    dataType: DataType = DataType.double;
    value: any = 0.0;
}