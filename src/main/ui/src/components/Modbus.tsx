import {useHttp} from "../hooks/http.hook";
import {useEffect, useState} from "react";
import {useNavigate, useParams} from "react-router-dom";
import inputSelect from "../forms/inputSelect";
import ModbusNode from "../models/ModbusNode";
import Loader from "./Loader";
import ModbusRwComponent from "./ModbusRwComponent";
import * as yup from "yup";
import {Constant} from "../constants/Constant";
import {inputRow, textRow} from "../forms";
import {ProcessInterface} from "../models/ProcessInterface";

const Modbus = () => {

    const url = Constant.beurl;
    const {request} = useHttp();

    //>>>>>>>>>>>>>>>>>>>> Local states

    // const [node, setNode] = useState<ModbusNode>(new ModbusNode())
    // const [updNode, setUpdNode] = useState<ModbusNode>(new ModbusNode());
    const [intr, setIntr] = useState<ProcessInterface>(new ProcessInterface())
    const [updIntr, setUpdIntr] = useState<ProcessInterface>(new ProcessInterface());
    const [edit, setEdit] = useState(false);
    const [loaded, setLoaded] = useState(false);

    const [error, setError] = useState('');
    const [errorField, setErrorField] = useState(new Array<string>());

    let {id} = useParams();
    const navigate = useNavigate();

    //>>>>>>>>>>>>>>>>>>>> Hooks

    useEffect(() => {
        getModbus();
    }, [])

    // useEffect(() => {
    //     setNode(intr.structure);
    //     setUpdNode(intr.structure);
    //     console.log(intr);
    //     console.log("Test");
    // }, [intr])

    //>>>>>>>>>>>>>>>>>>>> Validation

    const modbusSchema = yup.object().shape({
        name: yup.string().required('Name required'),
        // ipAddress: yup.string().matches(/^((25[0-5]|2[0-4]\d|1\d\d|[1-9]\d|\d)(\.(?!$)|$)){4}$/gm, 'Incorrect IP address'),
        // port: yup.number().required().integer().min(1).max(65535).typeError('Incorrect port value.'),
        // readWriteCycle: yup.number().required().integer().min(1).typeError('Incorrect cycle value.')
    })

    const validateModbus = (intr: ProcessInterface, setError: any, setErrorField: any) => {
        try {
            modbusSchema.validateSync(intr, {abortEarly: false});
            setError('');
            setErrorField([]);
        } catch (err: any) {
            console.log(err.inner);
            setError(err.inner.map((er: any) => er.message).join(' '));
            setErrorField(err.inner.map((er: any) => er.path));
        }
    }

    //>>>>>>>>>>>>>>>>>>>> Modbus interface data loaded

    const getModbus = () => {
        const urlTale = 'intapi/interface';
        request(url + urlTale + '/' + id)
            .then(data => {
                setIntr(data);
                setUpdIntr(data);
                setLoaded(true);
                // console.log(data);
            })
            .catch(() => {
                setError('Failed to load modbus interface');
                setLoaded(false);
            })
    }

    const setModbus = () => {
        const urlTale = 'intapi/intupdate';
        if (modbusSchema.isValidSync(updIntr)) {
            request(url + urlTale, "POST", JSON.stringify(updIntr))
                .then(data => {
                    getModbus();
                    setEdit(false);
                })
                .catch(() => setError('Failed to update modbus interface'))
        }
    }

    const onCancel = () => {
        setEdit(false);
        // setUpdNode(node);
        setError('');
        setErrorField([]);
    }

    //>>>>>>>>>>>>>>>>>>>> Interface modifications
    const handleUpdName = (name: string) => {
        setUpdIntr({...updIntr, name});
    }

    const handleUpdIpAddress = (ipAddress: string) => {
        const structure = updIntr.structure;
        structure.ipAddress = ipAddress;
        setUpdIntr({...updIntr, structure});
    }

    const handleUpdPort = (port: number) => {
        const structure = updIntr.structure;
        structure.port = port;
        setUpdIntr({...updIntr, structure});
    }

    const handleUpdCycle = (cycle: number) => {
        const structure = updIntr.structure;
        structure.readWriteCycle = cycle;
        setUpdIntr({...updIntr, structure});
    }

    const handleUpdSwitchBytes = (swBytes: boolean) => {
        // console.log(swBytes);
        const structure = updIntr.structure;
        structure.switchBytes = swBytes;
        setUpdIntr({...updIntr, structure});
    }

    const handleUpdSwitchRegisters = (swRegisters: boolean) => {
        const structure = updIntr.structure;
        structure.switchRegisters = swRegisters;
        setUpdIntr({...updIntr, structure});
    }

    const handleKeyUp = (key: string) => {
        switch (key) {
            case "Enter":
                setModbus();
                break;
            case "Escape":
                onCancel();
                break;
            default:
        }
    }


    //>>>>>>>>>>>>>>>>>>>> Rendering

    return (
        <div className="m-auto">
            <div className="mb-3 mt-3 btn btn-link -i-cursor" onClick={() => navigate('/ui/intlist/')}>
                {'<<BACK'}
            </div>
            {loaded ?
                <div style={{overflow: "visible", width: "100%"}}>
                    <table className="paramsTable">
                        <tbody>
                        {/* Columns width definition */}
                        <tr>
                            <th style={{width: "30%", minWidth: "12rem"}}></th>
                            <th style={{maxWidth: "50%", minWidth: "20rem"}}></th>
                        </tr>

                        {inputRow("Name", updIntr.name, edit, "input", "name",
                            errorField, handleUpdName, handleKeyUp)}

                        {textRow("Type", updIntr.type)}

                        {textRow("Status", updIntr.running ? "ON" : "OFF")}

                        {inputRow("IP address", updIntr.structure.ipAddress, edit, "input", "ipAddress",
                            errorField, handleUpdIpAddress, handleKeyUp)}

                        {inputRow("Port", updIntr.structure.port?.toString(), edit, "input", "port",
                            errorField, handleUpdPort, handleKeyUp)}

                        {inputRow("Read cycle", updIntr.structure.readWriteCycle?.toString(), edit, "input",
                            "readWriteCycle", errorField, handleUpdCycle, handleKeyUp)}

                        {inputRow("Switch bytes", updIntr.structure.switchBytes, edit, "input",
                            "cycle", errorField, handleUpdSwitchBytes)}

                        {inputRow("Switch registers", updIntr.structure.switchRegisters, edit, "input",
                            "cycle", errorField, handleUpdSwitchRegisters, handleKeyUp)}

                        </tbody>
                    </table>
                    <div className="mt-3"></div>
                    <div className="paramsTable">
                        <div hidden={edit}>
                            <button typeof="button" className="btn btn-primary" disabled={intr.running}
                                    onClick={() => setEdit(true)}
                                    style={{width: "6rem"}}>
                                Edit
                            </button>
                        </div>
                        <div hidden={!edit}>
                            <button typeof="button" className="btn btn-primary" onClick={() => onCancel()}
                                    style={{width: "6rem"}}>
                                Cancel
                            </button>
                            <button typeof="button" className="btn btn-success" onClick={() => setModbus()}
                                    style={{width: "6rem"}}>
                                Save
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

                    <div style={{marginLeft: "10%", marginRight: "10%", width: "80%"}}>
                        <ModbusRwComponent readWrites={updIntr.structure.dataReads} nodeId={id} typeRead={true}
                                           title="Data reads" refresh={getModbus} running={intr.running}/>
                        <ModbusRwComponent readWrites={updIntr.structure.dataWrites} nodeId={id} typeRead={false}
                                           title="Data writes" refresh={getModbus} running={intr.running}/>
                    </div>
                </div> :
                <Loader/>
            }
        </div>
    )
}

export default Modbus;