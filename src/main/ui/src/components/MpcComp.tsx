import {useHttp} from "../hooks/http.hook";
import {useEffect, useRef, useState} from "react";
import {useNavigate, useParams} from "react-router-dom";
import Loader from "./Loader";
import * as yup from "yup";
import {Constant} from "../constants/Constant";
import {buttonRow, dataPointsList, header, inputRow, matrixRow, tableDevider, textRow} from "../forms";
import {ProcessController} from "../models/ProcessController";
import {Mpc} from "../models/Mpc";
import {ProcessModel} from "../models/ProcessModel";
import {MatrixFromString, MatrixToString, VectorFromString, VectorToString} from "../utilities";
import {Tag} from "../models/Tag";
import {DataType} from "../models/DataType";

const MpcComp = () => {

    const url = Constant.beurl;
    const {request} = useHttp();

    //>>>>>>>>>>>>>>>>>>>> Local states

    const [mpc, setMpc] = useState<ProcessController>(new ProcessController());
    const [updStructure, setUpdStructure] = useState<Mpc>(new Mpc());
    const [loaded, setLoaded] = useState(false);

    const [error, setError] = useState('');
    const [errorField, setErrorField] = useState(new Array<string>());

    let {id} = useParams();
    const navigate = useNavigate();

    //>>>>>>>>>>>>>>>>>>>> Hooks

    useEffect(() => {
        const structure = mpc.structure as Mpc;
        if (structure !== undefined) {
            setUpdStructure(structure);
            if (structure.model !== null) {
                setModelName(structure.model.name);
                setAugmented(structure.augmented);
            }
            setHp(structure.hp);
            setHc(structure.hc);
            setInputsS(structure.inputs.map((item) => item.tagName));
            setOutputsS(structure.outputs.map((item) => item.tagName));
            setTrajectoryS(structure.setpoints.map((item) => item.tagName));
            setOptInputsS(structure.optInputs.map((item) => item.tagName));
            setOptInputsS(structure.optInputs.map((item) => item.tagName));
            setMpcInUse(structure.mpcinuse.tagName);
        }
        setControllerName(mpc.name);
        setExecutionCycle(mpc.cycleTime);
    }, [mpc])

    useEffect(() => {
        getMpc();
    }, [])

    //>>>>>>>>>>>>>>>>>>>> Validation

    const mpcSchema = yup.object().shape({
        name: yup.string().required('Name required'),
        ipAddress: yup.string().matches(/^((25[0-5]|2[0-4]\d|1\d\d|[1-9]\d|\d)(\.(?!$)|$)){4}$/gm, 'Incorrect IP address'),
        port: yup.number().required().integer().min(1).max(65535).typeError('Incorrect port value.'),
        readWriteCycle: yup.number().required().integer().min(1).typeError('Incorrect cycle value.')
    })

    // const validateMpc = (mpc: ProcessController, setError: any, setErrorField: any) => {
    //     try {
    //         mpcSchema.validateSync(mpc, {abortEarly: false});
    //         setError('');
    //         setErrorField([]);
    //     } catch (err: any) {
    //         console.log(err.inner);
    //         setError(err.inner.map((er: any) => er.message).join(' '));
    //         setErrorField(err.inner.map((er: any) => er.path));
    //     }
    // }

    /*
         >>>>>>>>>>>>>>>>>>>> Mpc data loaded
    */

    const getMpc = () => {
        const urlTale = 'conapi/controller';
        request(url + urlTale + '/' + id)
            .then(data => {
                setMpc(data);
                setLoaded(true);
                // console.log(data);
            })
            .catch(() => {
                setError('Failed to load MPC');
                setLoaded(false);
            })
    }

    /*
        >>>>>>>>>>>>>>>>>>>> Basic parameters section
    */
    const [controllerName, setControllerName] = useState("")
    const [executionCycle, setExecutionCycle] = useState(0)
    const [editBasicParameters, setEditBasicParameters] = useState(false);

    const updateControllerName = () => {
        const urlTale = 'conapi/controllernameupdate/';
        request(url + urlTale + id, "POST",
            JSON.stringify({"name": controllerName, "executionCycle": executionCycle}))
            .then(data => {
            })
            .catch(() => setRowError('Failed to update controller name.'))
            .finally(() => {
                getMpc();
                setEditBasicParameters(false);
            });
    }

    const onEditBasicSettings = () => {
        setControllerName(mpc.name);
        setExecutionCycle(mpc.cycleTime)
        setEditBasicParameters(true);
    }

    const onCancelBasicSettings = () => {
        setControllerName(mpc.name);
        setExecutionCycle(mpc.cycleTime)
        setEditBasicParameters(false);
    }

    const handleNameEdit = (name: string) => {
        setControllerName(name);
    }

    const handleNameKeyUp = (key: string) => {
        if (key === "Enter") {
            updateControllerName();
        }
    }

    const handleCycleEdit = (cycle: number) => {
        setExecutionCycle(cycle);
    }

    const handleCycleKeyUp = (key: string) => {
        if (key === "Enter") {
            updateControllerName();
        }
    }

    /*
        >>>>>>>>>>>>>>>>>>>> Status section
    */

    const changeControllerStatus = () => {
        const urlTale = 'conapi/controllerstatuschange/';
        request(url + urlTale + id, "POST")
            .then(data => {
            })
            .catch(() => setRowError('Failed to change controller status.'))
            .finally(() => {
                getMpc();
            });
    }

    /*
        >>>>>>>>>>>>>>>>>>>> Model selection section
    */
    const [modelsList, setModelsList] = useState(new Array<ProcessModel>())
    const [modelName, setModelName] = useState('');
    const [augmented, setAugmented] = useState(false);
    const [rowError, setRowError] = useState('');
    const [editModel, setEditModel] = useState(false);
    const modelRef = useRef(null);

    const getModelList = () => {
        const urlTale = 'modelapi/models';
        request(url + urlTale)
            .then(data => {
                setModelsList(data);
                setRowError('');
                console.log(data);
            })
            .catch(() => setRowError('Failed to load models list.'))
    }


    const updateMpcModel = () => {
        const urlTale = 'conapi/modelselection/';
        // console.log(modelsList.find(item => item.name === modelName));
        // console.log(modelName);
        request(url + urlTale + id, "POST", JSON.stringify({
            "model": modelsList.find(item => item.name === modelName),
            "augmented": augmented
        }))
            .then(() => {
                getMpc();
                setRowError("");
            })
            .catch(() => {
                setRowError("Couldn't update model.")
                setModelName(updStructure.model.name);
            })
            .finally(() => {
                setEditModel(false);
            })
    }

    const onEditModel = () => {
        getModelList();
        setEditModel(true);
    }

    const onCancelModel = () => {
        setEditModel(false);
        setModelName(updStructure.model.name);
    }

    const handleNewModel = (name: string) => {
        // console.log(name);
        setModelName(name);
    }

    const handleNewAugmented = (augmented: boolean) => {
        setAugmented(augmented);
    }

    const handleModelKeyUp = (key: string) => {
        if (key === "Enter") {
            updateMpcModel();
        }
    }

    /*
        >>>>>>>>>>>>>>>>>>>> Structure section
    */
    const [usedInputs, setUsedInputs] = useState("");
    const [editUsedInputs, setEditUsedInputs] = useState(false);

    const updateUsedInputs = () => {
        const urlTale = 'conapi/updateusedinputs/';
        request(url + urlTale + id, "POST", JSON.stringify(VectorFromString(usedInputs, "boolean")))
            .then()
            .catch(() => setRowError('Failed to update controller\'s used inputs.'))
            .finally(() => {
                getMpc();
                setEditUsedInputs(false);
            });
    }

    const onEditUsedInputs = () => {
        setUsedInputs(VectorToString(updStructure.usedInputs));
        setEditUsedInputs(true);
    }

    const onCancelUsedInputs = () => {
        setEditUsedInputs(false);
    }

    const handleUsedInputs = (value: string) => {
        setUsedInputs(value);
    }

    const handleUsedInputsKeyUp = (key: string) => {
        if (key === "Enter") {
            updateUsedInputs();
        } else if (key === "Escape") {
            onCancelUsedInputs();
        }
    }

    /*
        >>>>>>>>>>>>>>>>>>> Tuning parameters
    */

    const [Hp, setHp] = useState(1);
    const [Hc, setHc] = useState(1);
    const [Q, setQ] = useState("");
    const [R, setR] = useState("");
    const [editParameters, setEditParameters] = useState(false);
    const [editHorizon, setEditHorizon] = useState(false);

    const updateHorizons = () => {
        const urlTale = 'conapi/edithorizons/';
        request(url + urlTale + id, "POST",
            JSON.stringify({"Hp": Hp, "Hc": Hc})
        )
            .then()
            .catch(() => setRowError('Failed to update controller\'s horizons.'))
            .finally(() => {
                getMpc();
                setEditHorizon(false);
            });
    }

    const updateTuningParameters = () => {
        const urlTale = 'conapi/editparameters/';
        console.log(urlTale)
        request(url + urlTale + id, "POST",
            JSON.stringify({
                "Q": MatrixFromString(Q, "number") as number[][],
                "R": MatrixFromString(R, "number") as number[][]
            }))
            .then()
            .catch(() => setRowError('Failed to update controller\'s parameters.'))
            .finally(() => {
                getMpc();
                setEditParameters(false);
            });
    }

    const onEditHorizon = () => {
        setHp(updStructure.hp);
        setHc(updStructure.hc);
        setEditHorizon(true);
    }

    const onCancelHorizon = () => {
        setEditHorizon(false);
    }

    const onEditParameters = () => {
        setQ(MatrixToString(updStructure.matrixQ));
        setR(MatrixToString(updStructure.matrixR));
        setEditParameters(true);
    }

    const onCancelParameters = () => {
        setEditParameters(false);
    }

    const handleHpChange = (value: number) => {
        setHp(value);
    }

    const handleHcChange = (value: number) => {
        setHc(value);
    }

    const handleQChange = (value: string) => {
        setQ(value);
    }

    const handleRChange = (value: string) => {
        setR(value);
    }

    const handleHorizonKeyUp = (key: string) => {
        if (key === "Enter") {
            updateHorizons();
        } else if (key === "Escape") {
            onCancelHorizon();
        }
    }

    const handleParametersKeyUp = (key: string) => {
        if (key === "Enter") {
            updateTuningParameters();
        } else if (key === "Escape") {
            onCancelParameters();
        }
    }

    /*
        >>>>>>>>>>>>>>>>>>> Constraints section
    */

    const [inMovesConstraints, setInMovesConstraints] = useState("");
    const [inMovesConstraintsUsed, setInMovesConstraintsUsed] = useState("");
    const [inputConstraints, setInputConstraints] = useState("");
    const [inputConstraintsUsed, setInputConstraintsUsed] = useState("");
    const [outputConstraints, setOutputConstraints] = useState("");
    const [outputConstraintsUsed, setOutputConstraintsUsed] = useState("");
    const [editConstraints, setEditConstraints] = useState(false);

    const updateConstraints = () => {
        const urlTale = 'conapi/editconstraints/';
        request(url + urlTale + id, "POST",
            JSON.stringify({
                "inMovesConstraints": MatrixFromString(inMovesConstraints, "number") as number[][],
                "inMovesConstraintsUsed": MatrixFromString(inMovesConstraintsUsed, "boolean") as boolean[][],
                "inputConstraints": MatrixFromString(inputConstraints, "number") as number[][],
                "inputConstraintsUsed": MatrixFromString(inputConstraintsUsed, "boolean") as boolean[][],
                "outputConstraints": MatrixFromString(outputConstraints, "number") as number[][],
                "outputConstraintsUsed": MatrixFromString(outputConstraintsUsed, "boolean") as boolean[][]
            }))
            .then()
            .catch(() => setRowError('Failed to update controller\'s constraints.'))
            .finally(() => {
                getMpc();
                setEditConstraints(false);
            });
    }

    const onEdistConstraints = () => {
        setInMovesConstraints(MatrixToString(updStructure.inMovesConstraints));
        setInMovesConstraintsUsed(MatrixToString(updStructure.inMovesConstraintsUsed));
        setInputConstraints(MatrixToString(updStructure.inputConstraints));
        setInputConstraintsUsed(MatrixToString(updStructure.inputConstraintsUsed));
        setOutputConstraints(MatrixToString(updStructure.outputConstraints));
        setOutputConstraintsUsed(MatrixToString(updStructure.outputConstraintsUsed));
        setEditConstraints(true);
    }

    const onCancelConstraints = () => {
        setEditConstraints(false);
    }

    const handleInMovesConstraints = (value: string) => {
        setInMovesConstraints(value);
    }

    const handleInMovesConstraintsUsed = (value: string) => {
        setInMovesConstraintsUsed(value);
    }

    const handleInputConstraints = (value: string) => {
        setInputConstraints(value);
    }

    const handleInputConstraintsUsed = (value: string) => {
        setInputConstraintsUsed(value);
    }

    const handleOutputConstraints = (value: string) => {
        setOutputConstraints(value);
    }

    const handleOutputConstraintsUsed = (value: string) => {
        setOutputConstraintsUsed(value);
    }

    const handleConstraintsKeyUp = (key: string) => {
        if (key === "Enter") {
            updateConstraints();
        } else if (key === "Escape") {
            onCancelConstraints();
        }
    }

    /*
        >>>>>>>>>>>>>>>>>>> Kalman observer section
    */

    const [kalmanQk, setKalmanQk] = useState("");
    const [kalmanRk, setKalmanRk] = useState("");
    const [kalmanPk, setKalmanPk] = useState("");
    const [kalmanKk, setKalmanKk] = useState("");
    const [editKalman, setEditKalman] = useState(false);

    const updateKalman = () => {
        const urlTale = 'conapi/editobserver/';
        request(url + urlTale + id, "POST",
            JSON.stringify({
                "matrixQk": MatrixFromString(kalmanQk, "number"),
                "matrixRk": MatrixFromString(kalmanRk, "number"),
                "matrixPk": MatrixFromString(kalmanPk, "number"),
                "matrixKk": MatrixFromString(kalmanKk, "number"),
            })
        )
            .then()
            .catch(() => setRowError('Failed to update controller\'s observer.'))
            .finally(() => {
                getMpc();
                setEditKalman(false);
            });
    }

    const onEdistKalman = () => {
        setKalmanQk(MatrixToString(updStructure.observer.matrixQk));
        setKalmanRk(MatrixToString(updStructure.observer.matrixRk));
        setKalmanPk(MatrixToString(updStructure.observer.matrixPk));
        setKalmanKk(MatrixToString(updStructure.observer.matrixKk));
        setEditKalman(true);
    }

    const onCancelKalman = () => {
        setEditKalman(false);
    }

    const handleKalmanQk = (value: string) => {
        setKalmanQk(value);
    }

    const handleKalmanRk = (value: string) => {
        setKalmanRk(value);
    }

    const handleKalmanPk = (value: string) => {
        setKalmanPk(value);
    }

    const handleKalmanKk = (value: string) => {
        setKalmanKk(value);
    }

    const handleKalmanKeyUp = (key: string) => {
        if (key === "Enter") {
            updateKalman();
        } else if (key === "Escape") {
            onCancelKalman();
        }
    }

    /*
        >>>>>>>>>>>>>>>>>>>> Data points section
    */

    const [inputsS, setInputsS] = useState<string[]>([]);
    const [outputsS, setOutputsS] = useState<string[]>([]);
    const [trajectoryS, setTrajectoryS] = useState<string[]>([]);
    const [optInputsS, setOptInputsS] = useState<string[]>([]);
    const [mpcInUse, setMpcInUse] = useState<string>("");
    const [tagList, setTagList] = useState<Tag[]>([]);
    const [editDataPoints, setEditDataPoints] = useState(false);

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

    // class DataPoints {
    //     inputs: Tag[];
    //     outputs: Tag[];
    //     trajectory: Tag[];
    //     optInputs: Tag[];
    //
    //
    //     constructor(inputs: Tag[], outputs: Tag[],
    //                 trajectory: Tag[], optInputs: Tag[]) {
    //         this.inputs = inputs;
    //         this.outputs = outputs;
    //         this.trajectory = trajectory;
    //         this.optInputs = optInputs;
    //     }
    // }

    const updateDataPoints = () => {
        const inputs = inputsS.map((item, index) => handleDataPoint(item, index));
        const outputs = outputsS.map((item, index) => handleDataPoint(item, index));
        const trajectory = trajectoryS.map((item, index) => handleDataPoint(item, index));
        const optInputs = optInputsS.map((item, index) => handleDataPoint(item, index));
        const mpcinuse = [mpcInUse].map((item, index) => handleDataPoint(item, index));
        const urlTale = 'conapi/editdatapoints/';
        request(url + urlTale + id, "POST",
            // JSON.stringify(new DataPoints(inputs, outputs, trajectory, optInputs)))
            JSON.stringify({inputs, outputs, trajectory, optInputs, mpcinuse}))
            .then()
            .catch(() => setRowError('Failed to update controller\'s constraints.'))
            .finally(() => {
                getMpc();
                setEditDataPoints(false);
            });
    }

    const onEditDataPoints = () => {
        getTagList();
        setEditDataPoints(true);
    }

    const onCancelDataPoints = () => {
        setEditDataPoints(false);
    }

    const handleInputs = (value: string, index: number) => {
        handleDataPointS(value, index, inputsS, setInputsS);
    }

    const handleOutputs = (value: string, index: number) => {
        handleDataPointS(value, index, outputsS, setOutputsS);
    }

    const handleTrajectory = (value: string, index: number) => {
        handleDataPointS(value, index, trajectoryS, setTrajectoryS);
    }

    const handleOptInputs = (value: string, index: number) => {
        handleDataPointS(value, index, optInputsS, setOptInputsS);
    }

    const handleMpcInUse = (value: string) => {
        setMpcInUse(value);
    }

    const handleDataPointS = (value: string, index: number, dataPoints: string[], setAction: any) => {
        const list = [...dataPoints];
        list[index] = value;
        setAction(list);
    }
    const handleDataPoint = (value: string, index: number) => {//, dataPoints: Tag[], setAction: any) => {
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
            updateDataPoints();
        } else if (key === "Escape") {
            onCancelDataPoints();
        }
    }


    /*  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        >>>>>>>>>>>>>>>>>>>> Component Rendering
        >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> */

    return (
        <div className="m-auto">
            <div style={{display: "flex", justifyContent: "space-between"}}>
                <div className="mb-3 mt-3 btn btn-link -i-cursor" onClick={() => navigate('/ui/controllers/')}>
                    {'<<BACK'}
                </div>
                <div className="mb-3 mt-3 btn btn-link -i-cursor"
                     onClick={() => navigate('/ui/controller/diagnostics/' + id)}>
                    {'DIAGNOSTICS>>'}
                </div>
            </div>
            {loaded ?
                <div style={{overflow: "auto", width: "100%"}}>
                    <table className="paramsTable">
                        <tbody>
                        {/* Columns width definition */}
                        <tr>
                            <th style={{width: "30%", minWidth: "12rem"}}></th>
                            <th style={{maxWidth: "70%", minWidth: "20rem"}}></th>
                        </tr>

                        {/* Basic parameters section */}
                        {inputRow("Name", controllerName, editBasicParameters, "input", "name",
                            errorField, handleNameEdit, handleNameKeyUp)}
                        {textRow("Type", mpc.type)}
                        {inputRow("Execution cycle", executionCycle, editBasicParameters, "input", "cycle",
                            errorField, handleCycleEdit, handleCycleKeyUp)}
                        {buttonRow(onEditBasicSettings, updateControllerName, onCancelBasicSettings, editBasicParameters)}
                        {tableDevider()}
                        {textRow("Status", mpc.running ? "ON" : "OFF")}
                        <tr>
                            <td></td>
                            <td>
                                <button
                                    className={mpc.running ? "btn btn-outline-success" : "btn btn-outline-warning"}
                                    onClick={changeControllerStatus}>{mpc.running ? "Stop" : "Start"}</button>
                            </td>
                        </tr>
                        <tr style={{height: "1rem"}}></tr>
                        {tableDevider()}

                        {/* Model selection section */}
                        {inputRow("Model", modelName, editModel, "select", "modelname",
                            errorField, handleNewModel, handleModelKeyUp, modelsList.map((item) => item.name),
                            0, modelRef, true, true)}
                        {inputRow("Augmented model", augmented, editModel, "input", "augmented",
                            errorField, handleNewAugmented, handleModelKeyUp)}
                        {buttonRow(onEditModel, updateMpcModel, onCancelModel, editModel)}
                        {tableDevider()}

                        {/* Structure parameters section */}

                        {header("Structure parameter")}
                        {matrixRow(updStructure.usedInputs, "Used inputs", usedInputs, editUsedInputs,
                            "input", "r", errorField, handleUsedInputs, handleUsedInputsKeyUp)}
                        {buttonRow(onEditUsedInputs, updateUsedInputs, onCancelUsedInputs, editUsedInputs)}
                        {tableDevider()}

                        {/* Tuning parameters section */}
                        {header("Horizons")}
                        {inputRow("Prediction horizon", Hp, editHorizon, "input", "hp",
                            errorField, handleHpChange, handleHorizonKeyUp)}
                        {inputRow("Control horizon", Hc, editHorizon, "input", "hc",
                            errorField, handleHcChange, handleHorizonKeyUp)}
                        {buttonRow(onEditHorizon, updateHorizons, onCancelHorizon, editHorizon)}
                        {tableDevider()}
                        {header("Weighing matrices for outputs and control moves")}
                        {matrixRow(updStructure.matrixQ, "Q", Q, editParameters, "input", "q",
                            errorField, handleQChange, handleParametersKeyUp, "output", "step")}
                        {matrixRow(updStructure.matrixR, "R", R, editParameters, "input", "r",
                            errorField, handleRChange, handleParametersKeyUp, "input", "step")}
                        {buttonRow(onEditParameters, updateTuningParameters, onCancelParameters, editParameters)}
                        {tableDevider()}

                        {/* Constraints section */}
                        {header("Constraints")}
                        {matrixRow(updStructure.inMovesConstraints, "Input moves constraints", inMovesConstraints,
                            editConstraints, "input", "imc", errorField, handleInMovesConstraints,
                            handleConstraintsKeyUp, "")}
                        {matrixRow(updStructure.inMovesConstraintsUsed, "Input moves constraints used", inMovesConstraintsUsed,
                            editConstraints, "input", "imcu", errorField, handleInMovesConstraintsUsed,
                            handleConstraintsKeyUp, "")}
                        {matrixRow(updStructure.inputConstraints, "Input constraints", inputConstraints,
                            editConstraints, "input", "ic", errorField, handleInputConstraints,
                            handleConstraintsKeyUp, "")}
                        {matrixRow(updStructure.inputConstraintsUsed, "Input constraints used", inputConstraintsUsed,
                            editConstraints, "input", "icu", errorField, handleInputConstraintsUsed,
                            handleConstraintsKeyUp, "")}
                        {matrixRow(updStructure.outputConstraints, "Output constraints", outputConstraints,
                            editConstraints, "input", "ic", errorField, handleOutputConstraints,
                            handleConstraintsKeyUp, "")}
                        {matrixRow(updStructure.outputConstraintsUsed, "Output constraints used", outputConstraintsUsed,
                            editConstraints, "input", "icu", errorField, handleOutputConstraintsUsed,
                            handleConstraintsKeyUp, "")}
                        {buttonRow(onEdistConstraints, updateConstraints, onCancelConstraints, editConstraints)}
                        {tableDevider()}

                        {/* Observer section */}
                        {header("Kalman observer")}
                        {matrixRow(updStructure.observer.matrixQk, "Qk", kalmanQk, editKalman, "input", "kalmanQk",
                            errorField, handleKalmanQk, handleKalmanKeyUp)}
                        {matrixRow(updStructure.observer.matrixRk, "Rk", kalmanRk, editKalman, "input", "kalmanRk",
                            errorField, handleKalmanRk, handleKalmanKeyUp)}
                        {matrixRow(updStructure.observer.matrixPk, "Pk", kalmanPk, editKalman, "input", "kalmanPk",
                            errorField, handleKalmanPk, handleKalmanKeyUp)}
                        {matrixRow(updStructure.observer.matrixKk, "Kk", kalmanKk, editKalman, "input", "kalmanKk",
                            errorField, handleKalmanKk, handleKalmanKeyUp)}
                        {buttonRow(onEdistKalman, updateKalman, onCancelKalman, editKalman)}
                        {tableDevider()}

                        {/* Data points section */}
                        {header("Data points")}
                        {dataPointsList("Inputs", inputsS, editDataPoints, errorField,
                            handleInputs, handleDataPointsKeyUp, tagList)}
                        {dataPointsList("Outputs", outputsS, editDataPoints, errorField,
                            handleOutputs, handleDataPointsKeyUp, tagList)}
                        {dataPointsList("Trajectory", trajectoryS, editDataPoints, errorField,
                            handleTrajectory, handleDataPointsKeyUp, tagList)}
                        {dataPointsList("Computed input values", optInputsS, editDataPoints, errorField,
                            handleOptInputs, handleDataPointsKeyUp, tagList)}
                        {dataPointsList("MPC in use", [mpcInUse], editDataPoints, errorField,
                            handleMpcInUse, handleDataPointsKeyUp, tagList.filter(value => value.dataType === DataType.bool))}
                        {buttonRow(onEditDataPoints, updateDataPoints, onCancelDataPoints, editDataPoints)}
                        {tableDevider()}

                        <tr style={{height: "30rem"}}></tr>
                        </tbody>
                    </table>
                    <div className="mt-3"></div>

                </div> :
                <Loader/>
            }
        </div>
    )
}

export default MpcComp;