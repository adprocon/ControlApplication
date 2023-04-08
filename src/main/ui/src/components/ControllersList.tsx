import {Constant} from "../constants/Constant";
import {useHttp} from "../hooks/http.hook";
import {useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";
import inputSelect from "../forms/inputSelect";
import ConfirmationWindow from "./ConfirmationWindow";
import {ProcessController} from "../models/ProcessController";
import objCompare from "../hooks/sorting.hook";
import {ModelType} from "../models/ModelType";
import * as yup from "yup";
import {ControllerType} from "../models/ControllerType";

const ControllersList = () => {

    const url = Constant.beurl;
    const {request} = useHttp();
    // const defaultControllerType = 'Select controller type';
    const defaultControllerType = 'Model Predictive Controller';

    //>>>>>>>>>>>>>>>>>>>> Local states

    const [controllersList, setControllersList] = useState(new Array<ProcessController>())
    const [edit, setEdit] = useState(-1);
    const [rowError, setRowError] = useState('');
    const [errorField, setErrorField] = useState(new Array<string>());
    const [addError, setAddError] = useState('');
    const [addErrorField, setAddErrorField] = useState(new Array<string>());
    const [addController, setAddController] = useState({name: '', type: defaultControllerType, running: false});
    const [sorting, setSorting] = useState('name');
    const [reverse, setReverse] = useState(false);
    const [deleteConId, setDeleteConId] = useState<number | undefined>(0);
    let navigate = useNavigate();

    //>>>>>>>>>>>>>>>>>>>> Hooks
    useEffect(() => {
        getControllersList();
        // const interval = setInterval(() => {
        //     getControllersList();
        // },1000);
        // return () => clearInterval(interval);
    }, [])

    //>>>>>>>>>>>>>>>>>>>> Controllers list loading

    const getControllersList = () => {
        const urlTale = 'conapi/controllers';
        request(url + urlTale)
            .then(data => {
                setControllersList(data)
                setRowError('')
            })
            .catch(() => setRowError('Failed to load controllers'))
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
    const controllerSchema = yup.object().shape({
        name: yup.string().required('Name required.'),
        type: yup.mixed().oneOf(Object.values(ControllerType), 'Type required.'),

    });

    const validateModel = (controller: ProcessController, setError: any, setErrorField: any) => {
        try {
            controllerSchema.validateSync(controller, {abortEarly: false});
            setError('');
            setErrorField([]);
        } catch (err: any) {
            setError(err.inner.map((er: any) => er.message).join(' '));
            setErrorField(err.inner.map((er: any) => er.path));
        }
    }

    //>>>>>>>>>>>>>>>>>>>> Controllers modifications
    const handleNewName = (name: string) => {
        setAddController({...addController, name})
    }
    const handleNewType = (type: ModelType) => {
        setAddController({...addController, type})
    }

    const addNewController = () => {
        validateModel(addController as ProcessController, setAddError, setAddErrorField)
        if (controllerSchema.isValidSync(addController)) {
            const urlTale = 'conapi/controlleradd';
            request(url + urlTale, 'POST', JSON.stringify(addController))
                .then(() => clearConAddForm())
                .catch(() => setAddError('Failed to add controller'))
                .finally(() => getControllersList())
        }
    }

    const clearConAddForm = () => {
        setAddController({name: '', type: defaultControllerType, running: false});
        setAddError('');
        setAddErrorField(new Array<string>());
    }

    const deleteController = () => {
        const urlTale = 'conapi/controllerdelete';
        request(url + urlTale + '/' + deleteConId, 'DELETE')
            .then()
            .catch((error) => {
                console.log(error)
                setAddError('Failed to delete interface');
            })
            .finally(() => {
                setDeleteConId(0)
                getControllersList();
            })
    }

    const switchController = (id: number) => {
        const urlTale = 'conapi/controllerswitch';
        request(url + urlTale + '/' + id, 'POST')
            .then()
            .catch((error) => {
                console.log(error)
                setAddError('Failed to switch controller');
            })
            .finally(() => {
                getControllersList();
            })
    }

    const onEdit = (id: number, type: string) => {
        // console.log("Edit pressed " + ControllerType.MODEL_PREDICTIVE_CONTROLLER.toString())
        if (type === ControllerType.MODEL_PREDICTIVE_CONTROLLER.toString()) {
            navigate(`./mpc/${id}`);
        }
    }

    //>>>>>>>>>>>>>>>>>>>> Rendering

    const renderControllerList = (controllersList: ProcessController[]) => {
        return [...controllersList]
            .sort((a, b) => objCompare(a, b, sorting, reverse))
            .map(({id, name, type, running}, index) => {
                return (
                    <tr key={id} className="">
                        <td>
                            <div>{name}</div>
                        </td>
                        <td>
                            <div>{type}</div>
                        </td>
                       <td>
                            <div style={running ? {color: "darkgreen", fontWeight: "bold"} : {color: "lightgray"}}>
                                {running ? "Running" : "Stopped"}
                            </div>
                        </td>

                        <td className="">
                            <div hidden={edit === index} style={{display: "flex"}} className="justify-content-end">
                                <button className={running ? "btn btn-warning btn-sm" : "btn btn-success btn-sm"}
                                        onClick={() => switchController(id)}>
                                    {running ? 'Stop' : 'Start'}
                                </button>
                                <div className="ps-3"></div>
                                <button className="btn btn-primary btn-sm"
                                        onClick={() => onEdit(id, type)}>
                                    Modify
                                </button>
                                <button className="btn btn-warning btn-sm"
                                        onClick={() => setDeleteConId(id)}>
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

    // const mdlType = [defaultControllerType, ...Object.values(ControllerType)];
    const mdlType = [...Object.values(ControllerType)];

    return (
        <div>
            <table className="m-auto">
                <tbody>
                <tr>
                    <td style={{width: "10rem"}}>
                        {inputSelect(addController.name, 'input', addErrorField,
                            'name', 'Name', handleNewName)}
                    </td>
                    <td style={{width: "15rem"}}>
                        {inputSelect(addController.type, 'select', addErrorField,
                            'type', '', handleNewType, mdlType)}
                    </td>
                     <td style={{display: "flex", width: "10rem"}}>
                        <button className="btn btn-primary" onClick={addNewController}>Add</button>
                        <button className="btn btn-warning" onClick={clearConAddForm}>Clear</button>
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
                    <th className="tablelistheading" style={{width: "18rem"}} onClick={() => handleSorting('type')}
                    >Type {sorting === 'type' ? (reverse ? <span>&uarr;</span> : <span>&darr;</span>) : ""}</th>
                    <th className="tablelistheading" style={{width: "8rem"}} onClick={() => handleSorting('running')}
                    >Status {sorting === 'running' ? (reverse ? <span>&uarr;</span> : <span>&darr;</span>) : ""}</th>
                    <th className="tablelistheading" style={{width: "10rem"}}></th>
                </tr>
                </thead>
                <tbody>
                {renderControllerList(controllersList)}
                </tbody>
            </table>

            <ConfirmationWindow
                header={"Confirmation"}
                body={"Please, confirm controller delete."}
                show={deleteConId !== 0}
                onCancel={() => setDeleteConId(0)}
                onConfirm={() => deleteController()}
            />
        </div>
    )
}

export default ControllersList;