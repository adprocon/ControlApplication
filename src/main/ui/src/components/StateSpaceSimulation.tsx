import {ProcessModel} from "../models/ProcessModel";
import {StateSpace} from "../models/StateSpace";
import {useEffect, useState} from "react";
import {Constant} from "../constants/Constant";
import {useHttp} from "../hooks/http.hook";
import {useNavigate, useParams} from "react-router-dom";
import {
    buttonRow, dataPointsList,
    header,
    inputRow,
    matrixRow,
    matrixStaticRow,
    sectionsDevider,
    tableDevider,
    textRow
} from "../forms";
import Loader from "./Loader";
import {MatrixFromString, MatrixToString, VectorFromString, VectorToString} from "../utilities";
import {DataType} from "../models/DataType";
import {Tag} from "../models/Tag";

const StateSpaceSimulation = () => {

    const url = Constant.beurl;
    const {request} = useHttp();

    const initModel = new ProcessModel();
    initModel.structure = new StateSpace();
    const [model, setModel] = useState<ProcessModel>(initModel);
    const [updModel, setUpdModel] = useState<ProcessModel>(initModel);
    const [initialStates, setInitialStates] = useState("")
    const [edit, setEdit] = useState(false);
    const [loaded, setLoaded] = useState(false);
    const [error, setError] = useState('');
    const [errorField, setErrorField] = useState(new Array<string>());
    const [inputsS, setInputsS] = useState<string[]>([]);
    const [outputsS, setOutputsS] = useState<string[]>([]);
    const [tagList, setTagList] = useState<Tag[]>([]);

    let {id} = useParams();
    const navigate = useNavigate();


    useEffect(() => {
        getModel();
        getTagList();
        // const interval = setInterval(() => {
        //     getModel();
        // }, 5000);
        // return () => clearInterval(interval);

    }, [])

    useEffect(() => {
        resetMatrices();
        setInputsS((updModel.structure as StateSpace).inputs.map((item) => item.tagName));
        setOutputsS((updModel.structure as StateSpace).outputs.map((item) => item.tagName));
    }, [updModel])

    const resetMatrices = () => {
        const structure = updModel.structure as StateSpace;
        setInitialStates(VectorToString(structure.initialStates));
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

    function getTagList() {
        const urlTale = 'tagapi/tags';
        request(url + urlTale)
            .then(data => {
                const list: Tag[] = data;
                setTagList(list
                    // .filter((item) => {
                    //     return item.dataType === DataType.double;
                    // })
                    .sort((a, b) => {
                        return a.tagName.localeCompare(b.tagName);
                    })
                );
            })
            .catch(() => {
                setError('Could not fetch tag list.');
            })
    }

    const updateModel = () => {
        const urlTale = 'modelapi/simulationedit/';
        const inputs = inputsS.map((item, index) => handleDataPoint(item, index));
        const outputs = outputsS.map((item, index) => handleDataPoint(item, index));

        request(url + urlTale + id, "POST", JSON.stringify({
            "simulationCycle": updModel.simulationCycle,
            "initialStates": VectorFromString(initialStates, "number"),
            inputs,
            outputs
        }))
            .then(data => {
                getModel();
                setEdit(false);
                // console.log(data);
            })
            .catch(() => setError('Failed to update model'))
    }

    const onEdit = () => {
        setEdit(true);
    }

    const onCancel = () => {
        setEdit(false);
        setUpdModel(model);
        resetMatrices();
        setError('');
        setErrorField([]);
    }

    const runSimulation = () => {
        const urlTale = 'modelapi/runsimulation/';
        // const structure = new StateSpace();
        // console.log('Run simulation');

        request(url + urlTale + id, "POST")
            // , JSON.stringify({
            // "simulationRunning": !updModel.simulationRunning
        // }))
            .then(() => {
                setEdit(false);
                // console.log('Model simultation status change');
            })
            .catch(() => setError('Failed to start simulation'))
            .finally(() => {
                getModel();
                // console.log('Get model');
            })
    }

    const handleCycle = (cycle: number) => {
        setUpdModel({...updModel, simulationCycle: cycle});
    }

    const handleInitialStates = (init: string) => {
        setInitialStates(init);
        // let structure = updModel.structure as StateSpace;
        // structure.initialStates = VectorFromString(init, "number") as number[];
        // setUpdModel({...updModel, structure});
    }

    const handleInputs = (value: string, index: number) => {
        handleDataPointS(value, index, inputsS, setInputsS);
    }

    const handleOutputs = (value: string, index: number) => {
        handleDataPointS(value, index, outputsS, setOutputsS);
    }

    const handleDataPointS = (value: string, index: number, dataPoints: string[], setAction: any) => {
        const list = [...dataPoints];
        list[index] = value;
        setAction(list);
    }

    const handleDataPoint = (value: string, index: number) => {
        const list: Tag[] = [];
        const tag = tagList.find((item) => {
            return item.tagName === value;
        }) as Tag;
        list[index] = tag;
        if (tag !== undefined) {
            return tag;
        }
        return new Tag();
    }

    const handleDataPointsKeyUp = (key: string) => {
        if (key === "Enter") {
            updateModel();
        } else if (key === "Escape") {
            onCancel();
        }
    }


    return (
        <div className="m-auto">
            <div style={{display: "flex", justifyContent: "space-between"}}>
                <div className="mb-3 mt-3 btn btn-link -i-cursor" onClick={() => navigate('/ui/models/statespace/' + id)}>
                    {'<<BACK TO MODEL'}
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
                        {textRow("Name", updModel.name)}

                        {/* Type render */}
                        {textRow("Type", updModel.type)}

                        {/*Matrix A render*/}
                        {model.structure != null ? matrixStaticRow((model.structure as StateSpace).matrixA,
                            "Matrix A") : null}

                        {/*Matrix B render*/}
                        {model.structure != null ? matrixStaticRow((model.structure as StateSpace).matrixB,
                            "Matrix B") : null}

                        {/*Matrix C render*/}
                        {model.structure != null ? matrixStaticRow((model.structure as StateSpace).matrixC,
                            "Matrix C") : null}

                        {/* Uncomment, if matrix D to be used */}
                        {/*{model.structure != null ? MatrixRow((model.structure as StateSpace).matrixD, "Matrix D", matrixD,*/}
                        {/*    "input", "structure.matrixD", handleUpdMatrixD) : null}*/}

                        {tableDevider()}
                        {header("Simulation parameters")}
                        {inputRow("Simulation cycle", updModel.simulationCycle, edit, "input", error, errorField, handleCycle)}
                        {matrixRow((updModel.structure as StateSpace).initialStates, "Initial states", initialStates,
                            edit, "input", error, errorField, handleInitialStates)}
                        {header("Data points")}
                        {dataPointsList("Inputs", inputsS, edit, errorField,
                            handleInputs, handleDataPointsKeyUp, tagList)}
                        {dataPointsList("Outputs", outputsS, edit, errorField,
                            handleOutputs, handleDataPointsKeyUp, tagList)}
                        {buttonRow(onEdit, updateModel, onCancel, edit)}
                        {tableDevider()}
                        {header("Simulation")}
                        {textRow("Simulation running", model.simulationRunning ? "ON" : "OFF")}
                        {/* Simulation button */}
                        <tr>
                            <td></td>
                            <td>
                                <button
                                    typeof="button"
                                    className={updModel.simulationRunning ? "btn btn-outline-success" : "btn btn-outline-warning"}
                                    onClick={runSimulation}
                                    style={{width: "8rem", fontSize: "12px"}}>{updModel.simulationRunning ? 'Stop simulation' : 'Start simulation'}
                                </button>
                            </td>
                        </tr>
                        <tr>
                            <td></td>
                            <td>
                                <div hidden={!edit}>
                                    {error !== '' ? (
                                        <div className="localtooltip" style={{verticalAlign: "text-top"}}>
                                            <span className="tooltiptext">{error}</span>
                                        </div>
                                    ) : null}
                                </div>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                    <div className="mt-3"></div>
                </div> :
                <Loader/>
            }
        </div>
    )
}

export default StateSpaceSimulation;