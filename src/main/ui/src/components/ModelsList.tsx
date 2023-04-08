import {Constant} from "../constants/Constant";
import {useHttp} from "../hooks/http.hook";
import inputSelect from "../forms/inputSelect";
import ConfirmationWindow from "./ConfirmationWindow";
import {useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";
import {ProcessModel} from "../models/ProcessModel";
import * as yup from "yup";
import {ModelType} from "../models/ModelType";
import objCompare from "../hooks/sorting.hook";

const ModelsList = () => {
    const url = Constant.beurl;
    const {request} = useHttp();
    // const defaultModelType = 'Select model type';
    const defaultModelType = 'State-Space';

    //>>>>>>>>>>>>>>>>>>>> Local states

    const [modelsList, setModelsList] = useState(new Array<ProcessModel>())
    const [edit, setEdit] = useState(-1);
    const [rowError, setRowError] = useState('');
    const [errorField, setErrorField] = useState(new Array<string>());
    const [deleteModel, setDeleteModel] = useState({id: 0, name: 'init', type: ''});
    const [addError, setAddError] = useState('');
    const [addErrorField, setAddErrorField] = useState(new Array<string>());
    const [addModel, setAddModel] = useState({name: '', type: defaultModelType});
    const [sorting, setSorting] = useState('name');
    const [reverse, setReverse] = useState(false);
    const [deleteModelId, setDeleteModelId] = useState<number | undefined>(0);
    let navigate = useNavigate();

    //>>>>>>>>>>>>>>>>>>>> Hooks
    useEffect(() => {
        getModelList();
    }, [])

    //>>>>>>>>>>>>>>>>>>>> Models list loading

    const getModelList = () => {
        const urlTale = 'modelapi/models';
        request(url + urlTale)
            .then(data => {
                setModelsList(data)
                // console.log(data);
                setRowError('')
            })
            .catch(() => setRowError('Failed to load models list'))
    }

    //>>>>>>>>>>>>>>>>>>>> List sorting

    const handleSorting = (field: string) => {
        if (sorting === field) {
            setReverse(!reverse);
        } else {
            setSorting(field);
            setReverse(false);
        }
    }

    //>>>>>>>>>>>>>>>>>>>> Validation
    const modelSchema = yup.object().shape({
        name: yup.string().required('Name required.'),
        type: yup.mixed().oneOf(Object.values(ModelType), 'Type required.'),

    });

    const validateModel = (model: ProcessModel, setError: any, setErrorField: any) => {
        try {
            modelSchema.validateSync(model, {abortEarly: false});
            setError('');
            setErrorField([]);
        } catch (err: any) {
            setError(err.inner.map((er: any) => er.message).join(' '));
            setErrorField(err.inner.map((er: any) => er.path));
        }
    }

    //>>>>>>>>>>>>>>>>>>>> Interface modifications
    const handleNewName = (name: string) => {
        setAddModel({...addModel, name})
    }
    const handleNewType = (type: ModelType) => {
        setAddModel({...addModel, type})
    }

    const addNewModel = () => {
        validateModel(addModel as ProcessModel, setAddError, setAddErrorField)
        if (modelSchema.isValidSync(addModel)) {
            const urlTale = 'modelapi/modeladd';
            request(url + urlTale, 'POST', JSON.stringify(addModel))
                .then(() => clearModelAddForm())
                .catch(() => setAddError('Failed to add model'))
                .finally(() => getModelList())
        }
    }

    const clearModelAddForm = () => {
        setAddModel({name: '', type: 'State-Space'});
        setAddError('');
        setAddErrorField(new Array<string>());
    }

    const deleteMdl = () => {
        const urlTale = 'modelapi/modeldelete';
        request(url + urlTale + '/' + deleteModelId, 'DELETE')
            .then()
            .catch((error) => {
                console.log(error)
                setAddError('Failed to delete interface');
            })
            .finally(() => {
                setDeleteModelId(0)
                getModelList();
            })
    }

    const onEdit = (id: number, type: string) => {
        console.log("Edit pressed " + ModelType.STATE_SPACE.toString())
        if (type === ModelType.STATE_SPACE.toString()) {
            navigate(`./statespace/${id}`);
        }
    }

    const startSim = (id: number) => {
        const urlTale = 'modelapi/runsimulation/';
        request(url + urlTale + id, "POST")
            .then(() => {
                setEdit(-1);
            })
            .catch(() => setRowError('Failed to start simulation'))
            .finally(() => {
                getModelList();
            })
    }

    //>>>>>>>>>>>>>>>>>>>> Rendering

    const renderModelsList = (modelsList: ProcessModel[]) => {
        return [...modelsList]
            .sort((a, b) => objCompare(a, b, sorting, reverse))
            .map(({id, name, type, simulationRunning}, index) => {
                return (
                    <tr key={id} className="">
                        <td>
                            <div>{name}</div>
                        </td>
                        <td>
                            <div>{type}</div>
                        </td>

                        <td>
                            <div style={simulationRunning ? {color: "darkgreen", fontWeight: "bold"} : {color: "lightgray"}}>
                                {simulationRunning ? "ON" : "OFF"}
                            </div>
                        </td>

                        <td className="">
                            <div hidden={edit === index} style={{display: "flex"}} className="justify-content-end">
                                <div className=""></div>
                                <button className="btn btn-secondary me-3 btn-sm" style={{width:"4.5rem"}}
                                        onClick={() => startSim(id)}>
                                    {simulationRunning ? "Stop" : "Simulate"}
                                </button>
                                <button className="btn btn-primary btn-sm"
                                        onClick={() => onEdit(id, type)}>
                                    Modify
                                </button>
                                <button className="btn btn-warning btn-sm"
                                        onClick={() => setDeleteModelId(id)}>
                                    Delete
                                </button>

                                {rowError !== '' ? (
                                    <div className="localtooltip">
                                        <span className="tooltiptext">{rowError}</span>
                                    </div>
                                ) : null}
                            </div>
                        </td>

                    </tr>
                )
            })

    }

    // const mdlType = [defaultModelType, ...Object.values(ModelType)];
    const mdlType = [...Object.values(ModelType)];

    return (
        <div>
            <table className="m-auto">
                <tbody>
                <tr>
                    <td style={{width: "10rem"}}>
                        {inputSelect(addModel.name, 'input', addErrorField,
                            'name', 'Name', handleNewName)}
                    </td>
                    <td style={{width: "15rem"}}>
                        {inputSelect(addModel.type, 'select', addErrorField,
                            'type', 'Type', handleNewType, mdlType)}
                    </td>
                    <td style={{display: "flex", width: "10rem"}}>
                        <button className="btn btn-primary" onClick={addNewModel}>Add</button>
                        <button className="btn btn-warning" onClick={clearModelAddForm}>Clear</button>
                    </td>
                </tr>
                </tbody>
            </table>
            {addError !== '' ? (
                <div className="localtooltip">
                    <span className="tooltiptext">{addError}</span>
                </div>
            ) : null}

            <div className="mt-5"></div>

            <table className="table table-striped tablelist m-auto">
                <thead>
                <tr className="">
                    <th className="tablelistheading" style={{width: "12rem"}} onClick={() => handleSorting('name')}
                    >Name {sorting === 'name' ? (reverse ? <span>&uarr;</span> : <span>&darr;</span>) : ""}</th>
                    <th className="tablelistheading" style={{width: "8rem"}} onClick={() => handleSorting('type')}
                    >Type {sorting === 'type' ? (reverse ? <span>&uarr;</span> : <span>&darr;</span>) : ""}</th>
                    <th className="tablelistheading" style={{width: "8rem"}} onClick={() => handleSorting('simulationRunning')}
                    >Simulation {sorting === 'simulationRunning' ? (reverse ? <span>&uarr;</span> : <span>&darr;</span>) : ""}</th>
                    <th className="tablelistheading" style={{width: "10rem"}}></th>
                </tr>
                </thead>
                <tbody>
                {renderModelsList(modelsList)}
                </tbody>
            </table>

            <ConfirmationWindow
                header={"Confirmation"}
                body={"Please, confirm interface delete."}
                show={deleteModelId !== 0}
                onCancel={() => setDeleteModelId(0)}
                onConfirm={() => deleteMdl()}
            />
        </div>
    )

}

export default ModelsList;