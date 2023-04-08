import {Constant} from "../constants/Constant";
import {useHttp} from "../hooks/http.hook";
import {useEffect, useState} from "react";
import {useNavigate, useParams} from "react-router-dom";
import * as yup from "yup";
import Loader from "./Loader";
import {StateSpace} from "../models/StateSpace";
import {ProcessModel} from "../models/ProcessModel";
import {
    matrixADimension,
    matrixBDimension,
    matrixCDimension,
    MatrixFromString, MatrixRegEx,
    MatrixToString,
} from "../utilities";
import {inputRow, matrixRow, textRow} from "../forms";

const StateSpaceModel = () => {

    const url = Constant.beurl;
    const {request} = useHttp();

    //>>>>>>>>>>>>>>>>>>>> Local states

    const initModel = new ProcessModel();
    initModel.structure = new StateSpace();
    const [model, setModel] = useState<ProcessModel>(initModel);
    const [updModel, setUpdModel] = useState<ProcessModel>(initModel);
    const [matrixA, setMatrixA] = useState("")
    const [matrixB, setMatrixB] = useState("")
    const [matrixC, setMatrixC] = useState("")
    const [matrixD, setMatrixD] = useState("")

    // const [name, setName] = useState("");
    // const [type, setType] = useState("");
    const [edit, setEdit] = useState(false);
    const [loaded, setLoaded] = useState(false);

    const [error, setError] = useState('');
    const [errorField, setErrorField] = useState(new Array<string>());

    let {id} = useParams();
    const navigate = useNavigate();

    //>>>>>>>>>>>>>>>>>>>> Hooks

    useEffect(() => {
        getModel();
    }, [])

    const resetMatrices = () => {
        const structure = updModel.structure as StateSpace;
        setMatrixA(MatrixToString(structure.matrixA));
        setMatrixB(MatrixToString(structure.matrixB));
        setMatrixC(MatrixToString(structure.matrixC));
        setMatrixD(MatrixToString(structure.matrixD));
    }

    useEffect(() => {
        resetMatrices();
    }, [updModel])

    /* >>>>>>>>>>>>>>>>>>>> Validation */
    const modelSchema = yup.object().shape({
        name: yup.string().required('Name required'),
        structure: yup.object({
            matrixA: yup.string().trim()
                // .matches(/^\[((((\d+\.?\d*) ?)+);? ?)+(((\d+\.?\d*) ?)+)]$/g, "Wrong matrix A format.")
                .matches(MatrixRegEx, "Wrong matrix A format.")
                .test("test-matrixA-dimension", "Matrix A has wrong dimension.", function (value) {
                    return matrixADimension(value ? value : "");
                }),
            matrixB: yup.string().trim()
                .matches(MatrixRegEx, "Wrong matrix B format.")
                .test("test-matrixB-dimension", "Matrix B has wrong dimension.", function (value) {
                    return matrixBDimension(value ? value : "", matrixA);
                }),
            matrixC: yup.string().trim().matches(MatrixRegEx, "Wrong matrix C format.")
                .test("test-matrixC-dimension", "Matrix C has wrong dimension.", function (value) {
                    return matrixCDimension(value ? value : "", matrixA);
                }),
            /* Uncomment, if matrix D to be used */
            // matrixD: yup.string().trim().matches(/^\[((((\d+\.?\d*) ?)+);? ?)+(((\d+\.?\d*) ?)+)]$/g, "Wrong matrix D format"),
        })
    })

    const validateModel = (model: StringSSModel, setError: any, setErrorField: any): boolean => {
        try {
            modelSchema.validateSync(model, {abortEarly: false});
            console.log("Validation passed");
            setError('');
            setErrorField([]);
            return true;
        } catch (err: any) {
            console.log(err.inner);
            setError(err.inner.map((er: any) => er.message).join(' '));
            setErrorField(err.inner.map((er: any) => er.path));
            return false;
        }
    }

    /* >>>>>>>>>>>>>>>>>>>> Model loading and updating */
    const getModel = () => {
        const urlTale = 'modelapi/model';
        request(url + urlTale + '/' + id)
            .then(data => {
                setModel(data);
                setUpdModel(data);
                setLoaded(true);
                // console.log(data);
            })
            .catch(() => {
                setError('Failed to load model');
                setLoaded(false);
            })
    }

    const updateModel = () => {
        const strStruct = new StringStructure(matrixA, matrixB, matrixC, matrixD);
        const strModel = new StringSSModel(updModel.name, strStruct);
        const valid = validateModel(strModel, setError, setErrorField);
        if (valid) {
            const urlTale = 'modelapi/modeledit';
            const mdl = new ProcessModel();
            mdl.id = model.id;
            mdl.name = updModel.name;
            mdl.type = updModel.type;
            const structure = new StateSpace();
            structure.matrixA = MatrixFromString(matrixA, "number") as number[][];
            structure.matrixB = MatrixFromString(matrixB, "number") as number[][];
            structure.matrixC = MatrixFromString(matrixC, "number") as number[][];

            /* Uncomment, if matrix D to be used */
            // structure.matrixD = MatrixFromString(matrixD);
            mdl.structure = structure;
            request(url + urlTale, "POST", JSON.stringify(mdl))
                .then(data => {
                    getModel();
                    setEdit(false);
                    // console.log(data);
                })
                .catch(() => setError('Failed to update model'))
        }
    }

    const onCancel = () => {
        setEdit(false);
        setUpdModel(model);
        resetMatrices();
        setError('');
        setErrorField([]);
    }

    /* >>>>>>>>>>>>>>>>>>>> Interface modifications */

    const handleUpdName = (name: string) => {
        setUpdModel({...updModel, name});
    }

    const handleUpdMatrixA = (mAstring: string) => {
        setMatrixA(mAstring);
    }

    const handleUpdMatrixB = (mBstring: string) => {
        setMatrixB(mBstring);
    }

    const handleUpdMatrixC = (mCstring: string) => {
        setMatrixC(mCstring);
    }

    /* Uncomment if matrix D to be used */
    // const handleUpdMatrixD = (mDstring: string) => {
    //     setMatrixD(mDstring);
    // }

    const handleKeyUp = (key: string) => {
        switch (key) {
            case "Enter":
                updateModel();
                break;
            case "Escape":
                onCancel();
                break;
            default:
        }

    }

    /* >>>>>>>>>>>>>>>>>>>> Rendering */
    return (
        <div className="m-auto">
            <div style={{display: "flex", justifyContent: "space-between"}}>
                <div className="mb-3 mt-3 btn btn-link -i-cursor" onClick={() => navigate('/ui/models/')}>
                    {'<<BACK'}
                </div>
                <div className="mb-3 mt-3 btn btn-link -i-cursor"
                     onClick={() => navigate('/ui/models/statespacesimulation/' + id)}>
                    {'SIMULATION>>'}
                </div>
            </div>
            {loaded ?
                <div style={{overflow: "auto", width: "100%"}}>
                    <table className="paramsTable">
                        <tbody>

                        {/* Columns width definition */}
                        <tr>
                            <th style={{width: "30%", minWidth: "6rem"}}></th>
                            <th style={{maxWidth: "50%", minWidth: "20rem"}}></th>
                        </tr>

                        {/* Model name render */}
                        {inputRow("Name", updModel.name, edit, "input", "name", errorField,
                            handleUpdName, handleKeyUp)}

                        {/* Type render */}
                        {textRow("Type", updModel.type)}

                        {/*Matrix A render*/}
                        {model.structure != null ? matrixRow((model.structure as StateSpace).matrixA,
                            "Matrix A", matrixA, edit, "input", "structure.matrixA",
                            errorField, handleUpdMatrixA, handleKeyUp) : null}

                        {/*Matrix B render*/}
                        {model.structure != null ? matrixRow((model.structure as StateSpace).matrixB,
                            "Matrix B", matrixB, edit, "input", "structure.matrixB",
                            errorField, handleUpdMatrixB, handleKeyUp) : null}

                        {/*Matrix C render*/}
                        {model.structure != null ? matrixRow((model.structure as StateSpace).matrixC,
                            "Matrix C", matrixC, edit, "input", "structure.matrixC",
                            errorField, handleUpdMatrixC, handleKeyUp) : null}

                        {/* Uncomment, if matrix D to be used */}
                        {/*{model.structure != null ? MatrixRow((model.structure as StateSpace).matrixD, "Matrix D", matrixD,*/}
                        {/*    "input", "structure.matrixD", handleUpdMatrixD) : null}*/}

                        </tbody>
                    </table>
                    <div className="mt-3"></div>
                    <div className="paramsTable" style={{display: "flex"}}>

                        {/* Edit button */}
                        <div hidden={edit}>
                            <button typeof="button" className="btn btn-primary" onClick={() => setEdit(true)}
                                    style={{width: "5rem"}}>Edit
                            </button>
                        </div>

                        <div hidden={!edit}>
                            {/* Cancel button*/}
                            <button typeof="button" className="btn btn-primary" onClick={() => onCancel()}
                                    style={{width: "5rem"}}>Cancel
                            </button>

                            {/* Save button */}
                            <button typeof="button" className="btn btn-success" onClick={() => updateModel()}
                                    style={{width: "5rem"}}>Save
                            </button>
                        </div>
                        <div hidden={!edit}>
                            {error !== '' ? (
                                <div className="localtooltip" style={{verticalAlign: "text-top"}}>
                                    <span className="tooltiptext">{error}</span>
                                </div>
                            ) : null}
                        </div>
                    </div>

                </div> :
                <Loader/>
            }
        </div>
    )
}

/* These are needed for yup validation */
class StringSSModel {
    name: string;
    structure: StringStructure;

    constructor(name: string, structure: StringStructure) {
        this.name = name;
        this.structure = structure;
    }
}

class StringStructure {
    matrixA: string;
    matrixB: string;
    matrixC: string;
    matrixD: string;

    constructor(matrixA: string, matrixB: string, matrixC: string, matrixD: string) {
        this.matrixA = matrixA;
        this.matrixB = matrixB;
        this.matrixC = matrixC;
        this.matrixD = matrixD;
    }
}

export default StateSpaceModel;
