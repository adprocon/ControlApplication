import {Tag} from "./Tag";

export class StateSpace {
    matrixA: number[][] = [[0]];
    matrixB: number[][] = [[0]];
    matrixC: number[][] = [[0]];
    matrixD: number[][] = [[0]];
    initialStates: number[] = [0];
    inputs: Tag[] = [];
    outputs: Tag[] = [];
}