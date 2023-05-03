import inputSelect from "../forms/inputSelect";
import {useEffect, useState} from "react";
import {InterfaceType} from "../models/InterfaceType";
import ConfirmationWindow from "./ConfirmationWindow";
import {useHttp} from "../hooks/http.hook";
import {ProcessInterface} from "../models/ProcessInterface";
import objCompare from "../hooks/sorting.hook";
import * as yup from "yup";
import {useNavigate} from "react-router-dom";
import {Constant} from "../constants/Constant";

const InterfaceList = () => {

    /* Constants */
    const url = Constant.beurl;
    const {request} = useHttp();
    const defaultIntrfcType = 'Select interface type';

    /* Local states */
    const [addError, setAddError] = useState('');
    const [addErrorField, setAddErrorField] = useState(new Array<string>());
    const [addIntrfc, setAddIntrfc] = useState({name: '', type: defaultIntrfcType, running: false});


    /* Validation */
    const intrfcSchema = yup.object().shape({
        name: yup.string().required('Name required.'),
        type: yup.mixed().oneOf(Object.values(InterfaceType), 'Type required.'),

    });

    const validateIntrfc = (intrfc: ProcessInterface, setError: any, setErrorField: any) => {
        try {
            intrfcSchema.validateSync(intrfc, {abortEarly: false});
            setError('');
            setErrorField([]);
        } catch (err: any) {
            setError(err.inner.map((er: any) => er.message).join(' '));
            setErrorField(err.inner.map((er: any) => er.path));
        }
    }

    /* New interface data modification handling */
    const handleNewName = (name: string) => {
        setAddIntrfc({...addIntrfc, name})
    }
    const handleNewType = (type: InterfaceType) => {
        setAddIntrfc({...addIntrfc, type})
    }

    const addNewInterface = () => {
        validateIntrfc(addIntrfc as ProcessInterface, setAddError, setAddErrorField)
        if (intrfcSchema.isValidSync(addIntrfc)) {
            const urlTale = 'intapi/interfaceadd';
            request(url + urlTale, 'POST', JSON.stringify(addIntrfc))
                .then(() => clearInterfaceAddForm())
                .catch(() => setAddError('Failed to add interface'))
            // .finally(() => getInterfaceList())
        }
    }

    const clearInterfaceAddForm = () => {
        setAddIntrfc({name: '', type: '', running: false});
        setAddError('');
        setAddErrorField(new Array<string>());
    }


    const intrfcType = [defaultIntrfcType, ...Object.values(InterfaceType)];

    /* Rendering starts here */
    return (
        <div>
            <table className="m-auto">
                <tbody>
                <tr>
                    <td style={{width: "10rem"}}>
                        {inputSelect(addIntrfc.name, 'input', addErrorField,
                            'name', 'Name', handleNewName)}
                    </td>
                    <td style={{width: "15rem"}}>
                        {inputSelect(addIntrfc.type, 'select', addErrorField,
                            'type', '', handleNewType, intrfcType)}
                    </td>
                    <td style={{display: "flex", width: "10rem"}}>
                        <button className="btn btn-primary" onClick={addNewInterface}>Add</button>
                        <button className="btn btn-warning" onClick={clearInterfaceAddForm}>Clear</button>
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
            <Interfaces/>
        </div>

    )
}

const Interfaces = () => {

    const url = Constant.beurl;
    const {request} = useHttp();

    const [interfaceList, setInterfaceList] = useState(new Array<ProcessInterface>())
    const [edit, setEdit] = useState(-1);
    const [rowError, setRowError] = useState('');
    const [sorting, setSorting] = useState('name');
    const [reverse, setReverse] = useState(false);
    const [deleteIntId, setDeleteIntId] = useState<number | undefined>(0);
    let navigate = useNavigate();

    /* Hooks */
    useEffect(() => {
        getInterfaceList();
        const interval = setInterval(() => {
            getInterfaceList();
        }, 1000);
        return () => clearInterval(interval);
    }, [])


    /* Interface list loading */
    const getInterfaceList = () => {
        const urlTale = 'intapi/interfaces';
        request(url + urlTale)
            .then(data => {
                setInterfaceList(data);
                setRowError('');
                // console.log(data);
            })
            .catch(() => setRowError('Failed to load interfaces'))
    }

    /* Interface list sorting */
    const handleSorting = (field: string) => {
        if (sorting === field) {
            setReverse(!reverse);
        } else {
            setSorting(field);
            setReverse(false);
        }
    }


    const deleteInterface = () => {
        const urlTale = 'intapi/interfacedelete';
        request(url + urlTale + '/' + deleteIntId, 'DELETE')
            .then()
            .catch((error) => {
                console.log(error)
                setRowError('Failed to delete interface');
            })
            .finally(() => {
                setDeleteIntId(0)
                getInterfaceList();
            })
    }

    const switchInterface = (id: number) => {
        const urlTale = 'intapi/interfaceswitch';
        request(url + urlTale + '/' + id, 'POST')
            .then()
            .catch((error) => {
                console.log(error)
                setRowError('Failed to switch interface');
            })
            .finally(() => {
                getInterfaceList();
            })
    }

    const onEdit = (id: number, type: InterfaceType) => {
        console.log(type === InterfaceType.MODBUSSLAVE);
        switch (type) {
            case InterfaceType.MODBUSMASTER:
            case InterfaceType.MODBUSSLAVE:
            {
                console.log("test");
                navigate(`/ui/modbus/${id}`);
                break;
            }
            case InterfaceType.UDPSERVER:
            {
                navigate(`/ui/udpserver/${id}`);
                break;
            }
            default:
            {
                console.log("default");
                break;
            }
        }
    }


    const renderInterfaceList = (interfaceList: ProcessInterface[]) => {
        return [...interfaceList]
            .sort((a, b) => objCompare(a, b, sorting, reverse))
            .map(({id, name, type, running, status}, index) => {
                return (
                    <tr key={id} className="">
                        <td>
                            <div>{name}</div>
                        </td>
                        <td>
                            <div>{type}</div>
                        </td>
                        <td>
                            <div style={status === "Connected" ? {color: "green", fontWeight: "bold"}
                                : status === "Stopped" ? {color: "lightgray"} : {}}
                                 className="">
                                {/*{running ? "ON" : "OFF"}*/}
                                {status}
                            </div>
                        </td>

                        <td className="">
                            <div hidden={edit === index} style={{display: "flex"}} className="justify-content-end">
                                <button
                                    className={running ? "btn btn-warning btn-sm" : "btn btn-success btn-sm"}
                                    onClick={() => switchInterface(id)}>
                                    {running ? 'Stop' : 'Start'}
                                </button>
                                <div className="ps-3"></div>
                                <button className="btn btn-primary btn-sm"
                                        onClick={() => onEdit(id, type)}>
                                    Modify
                                </button>
                                <button className="btn btn-warning btn-sm"
                                        onClick={() => setDeleteIntId(id)}>
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

    return (
        <>
            <table className="table table-striped tablelist m-auto">
                <thead>
                <tr className="">
                    <th className="tablelistheading" style={{width: "12rem"}} onClick={() => handleSorting('name')}
                    >Name {sorting === 'name' ? (reverse ? <span>&uarr;</span> : <span>&darr;</span>) : ""}</th>
                    <th className="tablelistheading" style={{width: "10rem"}} onClick={() => handleSorting('type')}
                    >Type {sorting === 'type' ? (reverse ? <span>&uarr;</span> : <span>&darr;</span>) : ""}</th>
                    <th className="tablelistheading" style={{width: "8rem"}} onClick={() => handleSorting('running')}
                    >Status {sorting === 'running' ? (reverse ? <span>&uarr;</span> : <span>&darr;</span>) : ""}</th>
                    <th className="tablelistheading" style={{width: "10rem"}}></th>
                </tr>
                </thead>
                <tbody>
                {renderInterfaceList(interfaceList)}
                </tbody>
            </table>

            <ConfirmationWindow
                header={"Confirmation"}
                body={"Please, confirm interface delete."}
                show={deleteIntId !== 0}
                onCancel={() => setDeleteIntId(0)}
                onConfirm={() => deleteInterface()}
            />
        </>
    )


}

export default InterfaceList;