import inputSelect from "./inputSelect";
import {TagNumberFormat} from "../utilities";
import {DataType} from "../models/DataType";
import {Tag} from "../models/Tag";

export const inputRow = (name: string,
                         value: any,
                         edit: boolean,
                         type: string,
                         errorName: string,
                         errorField: string[],
                         handleAction: any,
                         handleKeyAction?: any,
                         selectionOptions?: Array<string>,
                         index?: number,
                         ref?: any,
                         isSearchable?: boolean,
                         isClearable?: boolean
) => {
    return (
        <tr style={{height: "2.5rem"}} key={index}>
            <td style={{width: "", fontWeight: "bold"}}>{name}</td>
            <td style={{width: ""}} className="">
                <div hidden={edit}>{typeof (value) === "boolean" ? (value ? "ON" : "OFF") : value}</div>
                <div hidden={!edit}>
                    {inputSelect(value, type, errorField, errorName, name, handleAction,
                        selectionOptions, index, false, handleKeyAction, ref, isSearchable, isClearable)}
                </div>
            </td>
        </tr>
    )
}

export const matrixRow = (matrix: any,
                          name: string,
                          value: any,
                          edit: boolean,
                          type: string,
                          errorName: string,
                          errorField: string[],
                          handleAction: any,
                          handleKeyAction?: any,
                          rowHeader?: string,
                          columnHeader?: string
) => {
    let height = 1;
    if (matrix !== undefined && Array.isArray(matrix[0])) {
        height = matrix.length;
    }
    return (
        <>
            {matrix === undefined ? null : <>
                <tr style={{height: (height * 2.2) + "rem"}}>
                    <td style={{width: "", fontWeight: "bold"}}>{name}</td>
                    <td style={{width: "15rem"}} className="">
                        <div hidden={edit}>
                            {Array.isArray(matrix[0]) ? matrixRender(matrix, rowHeader, columnHeader) :
                                vectorRender(matrix)}
                        </div>
                        <div hidden={!edit}>
                            {inputSelect(value, type, errorField, errorName, name, handleAction,
                                [], 0, false, handleKeyAction)}
                        </div>
                    </td>
                </tr>
            </>
            }
        </>
    )
}

export const booleanMatrixRow = (matrix: any,
                          name: string,
                          value: any,
                          edit: boolean,
                          type: string,
                          errorName: string,
                          errorField: string[],
                          handleAction: any,
                          handleKeyAction?: any,
                          rowHeader?: string,
                          columnHeader?: string
) => {
    let height = 1;
    if (matrix !== undefined && Array.isArray(matrix[0])) {
        height = matrix.length;
    }
    return (
        <>
            {matrix === undefined ? null : <>
                <tr style={{height: (height * 2.2) + "rem"}}>
                    <td style={{width: "", fontWeight: "bold"}}>{name}</td>
                    <td style={{width: "15rem"}} className="">
                        <div hidden={edit}>
                            {Array.isArray(matrix[0]) ? matrixRender(matrix, rowHeader, columnHeader) :
                                vectorRender(matrix)}
                        </div>
                        <div hidden={!edit}>
                            {inputSelect(value, type, errorField, errorName, name, handleAction,
                                [], 0, false, handleKeyAction)}
                        </div>
                    </td>
                </tr>
            </>
            }
        </>
    )
}

export const matrixStaticRow = (matrix: any,
                                name: string,
                                rowHeader?: string,
                                columnHeader?: string
) => {
    let height = 1;
    if (matrix !== undefined && Array.isArray(matrix[0])) {
        height = matrix.length;
    }
    return (
        <>
            {matrix === undefined ? null : <>
                <tr style={{height: (height * 2.2) + "rem"}}>
                    <td style={{width: "", fontWeight: "bold"}}>{name}</td>
                    <td style={{width: "15rem"}} className="">
                        <div>
                            {Array.isArray(matrix[0]) ? matrixRender(matrix, rowHeader, columnHeader) :
                                vectorRender(matrix)}
                        </div>
                    </td>
                </tr>
            </>
            }
        </>
    )
}

export const matrixRender = (matrix: number[][] | boolean[][], rowHeader?: string, columnHeader?: string, width?: number) => {
    return (
        <table>
            <tbody>
            {(columnHeader !== null && columnHeader !== undefined) ?
                <tr>
                    {(rowHeader !== null && rowHeader !== undefined) ?
                        <td></td> : null}
                    {matrix[0].map((item, index) => (
                        <td key={index} className="matrixHeader">
                            <div>{columnHeader + (index + 1)}</div>
                        </td>
                    ))}
                </tr> : null
            }
            {matrix.map((row, index) => (
                <tr key={index} className="">
                    {(rowHeader !== null && rowHeader !== undefined) ?
                        <td key={index} className="matrixHeader">
                            <div>{rowHeader + (index + 1)}</div>
                        </td>
                        : null}
                    {Array.isArray(row) ? row.map((item, index) => (
                            <td key={index} style={width == null ? {} : {width: width + "px"}}
                                className={row.length === 1 ? "matrixcell matrixLeft matrixRight" :
                                    index === 0 ? "matrixcell matrixLeft" :
                                        index === row.length - 1 ? "matrixcell matrixRight" :
                                            "matrixcell"}>
                                {TagNumberFormat(item, typeof (item) == "number" ? DataType.double : DataType.bool)}
                            </td>
                        ))
                        :
                        <td key={index} style={width == null ? {} : {width: width + "px"}}
                            className={"matrixcell matrixLeft matrixRight"}>
                            {TagNumberFormat(row, typeof (row) == "number" ? DataType.double : DataType.bool)}
                        </td>
                    }
                </tr>
            ))}
            </tbody>
        </table>
    )
}

export const booleanMatrixRender = (
    matrix: boolean[][],
    name: string,
    errorField: string[],
    errorName: string,
    handleAction: any,
    rowHeader?: string,
    columnHeader?: string,
    width?: number
) => {
    return (
        <table>
            <tbody>
            {(columnHeader !== null && columnHeader !== undefined) ?
                <tr>
                    {(rowHeader !== null && rowHeader !== undefined) ?
                        <td></td> : null}
                    {matrix[0].map((item, index) => (
                        <td key={index} className="matrixHeader">
                            <div>{columnHeader + (index + 1)}</div>
                        </td>
                    ))}
                </tr> : null
            }
            {matrix.map((row, index) => (
                <tr key={index} className="">
                    {(rowHeader !== null && rowHeader !== undefined) ?
                        <td key={index} className="matrixHeader">
                            <div>{rowHeader + (index + 1)}</div>
                        </td>
                        : null}
                    {Array.isArray(row) ? row.map((item, index) => (
                            <td key={index} style={width == null ? {} : {width: width + "px"}}
                                className={row.length === 1 ? "matrixcell matrixLeft matrixRight" :
                                    index === 0 ? "matrixcell matrixLeft" :
                                        index === row.length - 1 ? "matrixcell matrixRight" :
                                            "matrixcell"}>
                                {inputSelect(item, "input", errorField, errorName, name, handleAction)}
                            </td>
                        ))
                        :
                        <td key={index} style={width == null ? {} : {width: width + "px"}}
                            className={"matrixcell matrixLeft matrixRight"}>
                            {TagNumberFormat(row, typeof (row) == "number" ? DataType.double : DataType.bool)}
                        </td>
                    }
                </tr>
            ))}
            </tbody>
        </table>
    )
}

export const vectorRender = (vector: number[] | boolean[]) => {
    return (
        <table>
            <tbody>
            <tr>
                {vector.map((item, index) => (
                    <td key={index} className="matrixcell">
                        {TagNumberFormat(item, typeof (item) == "number" ? DataType.double : DataType.bool)}
                    </td>
                ))}
            </tr>
            </tbody>
        </table>
    )
}

export const textRow = (name: string, value: string) => {
    return (
        <tr style={{height: "2.5rem"}}>
            <td style={{width: "", fontWeight: "bold"}}>
                {name}
            </td>
            <td>
                <div>{value}</div>
            </td>
        </tr>
    )
}

export const buttonRow = (onEdit: any, onSave: any, onCancel: any, edit: boolean) => {
    return (
        <>
            <tr>
                <td></td>
                <td>
                    <button hidden={edit} className="btn btn-outline-primary" onClick={onEdit}>Edit</button>
                    <button hidden={!edit} className="btn btn-outline-warning" onClick={onSave}>Save</button>
                    <button hidden={!edit} className="btn btn-outline-success" onClick={onCancel}>Cancel</button>
                </td>
            </tr>
            <tr style={{height: "1rem"}}></tr>
        </>
    )
}

export const header = (text: string) => {
    return (
        <tr>
            <td></td>
            <td className="tableheader">{text}</td>
        </tr>
    )
}

export const dataPointsList = (name: string, list: string[], edit: boolean, errorField: string[],
                               handleUpd: any, handleKeyUp: any, optionsList: Tag[]) => {
    const optList = optionsList.map((item) => {
        return item.tagName;
    })
    return list.length > 0 ? list.map((tagName, index) => {
        return (
            inputRow(index === 0 ? name : "", tagName, edit, "select", "modelname",
                errorField, handleUpd, handleKeyUp, optList, index, undefined, true, true)
        )
    }) : textRow(name, "");
}

export const tableDevider = () => {
    return (
        <tr style={{borderBottom: "1px dashed lightgray"}}></tr>
    )
}

export const sectionsDevider = () => {
    return (
        <div className="mb-3" style={{borderBottom: "1px dashed lightgray"}}></div>
    )
}


