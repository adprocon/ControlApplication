import {render, screen} from "@testing-library/react";
import {matrixADimension, MatrixFromString} from "./index";
import StateSpaceModel from "../components/StateSpaceModel";

test('convert string matrix to number array', () => {
    const stringMatrix = "[1 2;   3    4]";
    const matrix = [[1, 2],[3, 4]];
    const convertedMatrix = MatrixFromString(stringMatrix, "number");
    console.log(convertedMatrix);
    let condition = true;
    try {
        matrix.forEach((row, i) => {
            row.forEach((item, j) => {
                if (item !== convertedMatrix[i][j]) {
                    condition = false;
                }
            })
        })
    } catch (e) {
        condition = false;
    }
    expect(condition).toBe(true);
});


test("test-matrix-A-validation", () => {
    const stringMatrix = "[1 2; 3 4 ]";
    const pass = matrixADimension(stringMatrix);
    console.log(pass);
})