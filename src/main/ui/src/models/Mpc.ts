import {KalmanObserver} from "./KalmanObserver";
import {Tag} from "./Tag";
import {ProcessModel} from "./ProcessModel";

export class Mpc {

    model: ProcessModel = new ProcessModel();
    augmented: boolean = false;
    observer: KalmanObserver = new KalmanObserver();
    hp: number = 1;
    hc: number = 1;
    matrixQ: number[][];
    matrixR: number[][];
    inputs: Tag[] = [];
    setpoints: Tag[] = [];
    outputs: Tag[] = [];
    optInputs: Tag[] = [];
    usedInputs: boolean[] = [];
    inMovesConstraints: number[][];
    inMovesConstraintsUsed: boolean[][] = [];
    inputConstraints: number[][];
    inputConstraintsUsed: boolean[][] = [];
    outputConstraints: number[][];
    outputConstraintsUsed: boolean[][] = [];
    watchdogTag: Tag;
    mpcinuse: Tag;
    tagList: Map<String, Tag>;
    freeResponse: number[][];
    prediction: number[][];
    optimalU: number[][];
    states: number[][];
    statesK: number[][];
    watchdog = false

}