import {DataType} from "../models/DataType";

export const TagNumberFormat = (value: any, type: DataType) => {
    return type === DataType.bool || type === DataType.int ? value.toString() :
        // Number(value) < 99999999 && Number(value) > 0.001 ? Number(value).toLocaleString() :
        Number(value) < 99999999 && Number(value) > -9999999 ?
            Number(value) < 0.001 && Number(value) > -0.001 && Number(value) !== 0 ?
                Number(value).toExponential(3).replace(/0*(?=e)/, "") :
                Number(value).toLocaleString("en").replace(",", " ") :
            Number(value).toExponential(3).replace(/0*(?=e)/, "");
}

export const MatrixRegEx = /^\[(( *-?\d+\.?\d*) *;?)+ *]$/g;
export const MatrixBooleanRegEx = /^\[(((true|false) ?)+;? ?)*]$/;
export const VectorRegEx = /^\[(( *-?\d+\.?\d*) *)+]$/g;
export const VectorBooleanRegEx = /^\[((true|false) ?)+]$/g;

export const MatrixFromString = (strMatrix: string, type: string): number[][] | boolean[][] => {
    let stringMatrix = "" + strMatrix;
    // if (stringMatrix.match(/^\[((((\d+\.?\d*) ?)+);? ?)+(((\d+\.?\d*) ?)+)]$/g)) {
    if (stringMatrix.match(MatrixRegEx) || stringMatrix.match(MatrixBooleanRegEx)) {
        stringMatrix = stringMatrix
            .replace(/^.|.$/g, "")
            .replace(/ +/g, " ")
            .replace(/ $/g, "")
            .replace(/;$/g, "");
        let rows = stringMatrix.split(";");
        let matrix: any = [];
        rows.forEach(row => {
            matrix.push(row.trim().split(" ").map(item => {
                if (type === "number") {
                    return +item;
                } else if (type === "boolean") {
                    return item === "true";
                }
            }));
        })
        return matrix;
    }
    return [];
}

export const VectorFromString = (strVector: string, type: string): number[] | boolean[] => {
    let stringVector = "" + strVector;
    if (stringVector.match(VectorRegEx) || stringVector.match(VectorBooleanRegEx)) {
        stringVector = stringVector
            .replace(/^.|.$/g, "")
            .replace(/ +/g, " ")
            .replace(/ $/g, "")
            .replace(/;$/g, "");
        let vector: any = (stringVector.trim().split(" ").map(item => {
            if (type === "number") {
                return +item;
            } else if (type === "boolean") {
                return item === "true";
            }
        }));
        return vector;
    }
    return [];
}

export const MatrixToString = (matrix: number[][] | boolean[][]) => {
    let stringMatrix = "[";
    matrix.forEach((row, index) => {
        stringMatrix += row.join(" ");
        if (index < matrix.length - 1) {
            stringMatrix += "; ";
        }
    })
    stringMatrix += "]"
    return stringMatrix
}

export const VectorToString = (vector: number[] | boolean[]) => {
    return `[${vector.join(" ")}]`;
}

export const matrixADimension = (data: string) => {
    const mat = MatrixFromString(data as string, "number");
    if (mat === undefined || mat.length === 0) return false;
    const width = mat[0].length;
    const height = mat.length;
    let pass = width === height;
    for (let i = 1; i < height; i++) {
        if (mat[i].length !== width) {
            pass = false;
        }
    }
    return pass;
}

export const matrixBDimension = (data: string, matrixA: string) => {
    const matB = MatrixFromString(data as string, "number");
    const matA = MatrixFromString(matrixA as string, "number");
    if (matB === undefined || matB.length === 0 || matA === undefined || matA.length === 0) return false;
    const width = matB[0].length;
    const widthA = matA[0].length;
    const height = matB.length;
    let pass = widthA === height;
    for (let i = 1; i < height; i++) {
        if (matB[i].length !== width) {
            pass = false;
        }
    }
    return pass;
}

export const matrixCDimension = (data: string, matrixA: string) => {
    const matC = MatrixFromString(data as string, "number");
    const matA = MatrixFromString(matrixA as string, "number");
    if (matC === undefined || matC.length === 0 || matA === undefined || matA.length === 0) return false;
    const width = matC[0].length;
    const height = matC.length;
    const heightA = matA.length;
    let pass = heightA === width;
    for (let i = 1; i < height; i++) {
        if (matC[i].length !== width) {
            pass = false;
        }
    }
    return pass;
}

export const transposeArray = (ar: number[][]) => {
    // const out = Array(ar[0].length, ar.length);
    // for (let i = 0; i < ar.length; i++) {
    //     for (let j = 0; j < ar[0].length; j++) {
    //
    //     }
    // }
    return ar[0].map((_, colIndex) => ar.map(row => row[colIndex]));
}

export const deepCopy = (data: any) => {
    return JSON.parse(JSON.stringify(data));
}

// export const deepCopy = (data: any) => {
//     let newObj = Object.assign({}, data);
//     for (let key in newObj) {
//         if (typeof newObj[key] === "object" && newObj[key] !== null) {
//             newObj[key] = deepCopy(newObj[key]);
//         }
//     }
//     return newObj;
// }
