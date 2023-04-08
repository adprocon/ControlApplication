import inputSelect from "../forms/inputSelect";
import {useState} from "react";
import ModbusRW from "../models/ModbusRW";
import {useHttp} from "../hooks/http.hook";
import ModbusRwType from "../models/ModbusRwType";
import * as yup from "yup";
import ConfirmationWindow from "./ConfirmationWindow";
import {useNavigate} from "react-router-dom";
import {Constant} from "../constants/Constant";

type Props = {
    readWrites: ModbusRW[];
    nodeId: string | undefined;
    typeRead: boolean;
    title: string;
    refresh: () => void;
    running: boolean;
};

const ModbusRwComponent = (props: Props) => {

    const {readWrites, nodeId, typeRead, title, refresh, running} = props;

    const url = Constant.beurl;
    const {request} = useHttp();

    //>>>>>>>>>>>>>>>>>>>> Local states

    const [editRw, setEditRw] = useState(-1);
    const [rw, setRw] = useState<ModbusRW>(new ModbusRW());
    const [deletedRw, setDeletedRw] = useState<number|undefined>(-1);
    const [error, setError] = useState('');
    const [errorFields, setErrorFields] = useState(new Array<string>());
    const navigate = useNavigate();

    //>>>>>>>>>>>>>>>>>>>> Validation

    const modbusRwSchema = yup.object().shape({
        name: yup.string().required('Name required'),
        slaveID: yup.number().min(1).max(254).typeError('Incorrect Slave ID value. Must be integer [1...254].'),
        address: yup.number().required().integer().min(0).max(99999).typeError('Incorrect address value. Must be integer [0...99999].'),
        length: yup.number().required().integer().min(0).max(100).typeError('Incorrect length. Must be integer [0...100].')
    })

    const validateModbusRW = (rw: ModbusRW, setError: any, setErrorField: any) => {
        try {
            modbusRwSchema.validateSync(rw, {abortEarly: false});
            setError('');
            setErrorField([]);
        } catch (err: any) {
            console.log(err.inner);
            setError(err.inner.map((er: any) => er.message).join(' '));
            setErrorField(err.inner.map((er: any) => er.path));
        }
    }
    //>>>>>>>>>>>>>>>>>>>> RW modifications

    const addRW = () => {
        const urlTale = 'intapi/modbusrwadd/';
        const type = typeRead ? '/read' : '/write';
        request(url + urlTale + nodeId + type, "POST")
            .then(data => {
                setEditRw(-1);
                refresh();
                // console.log(data);
            })
            .catch(() => setError('Failed to delete Modbus RW'))
    }

    const updateRw = (index: number) => {
        const urlTale = 'intapi/modbusrwupdate/';
        validateModbusRW(rw, setError, setErrorFields);
        if (modbusRwSchema.isValidSync(rw)) {
            request(url + urlTale + nodeId + '/' + index, "POST", JSON.stringify(rw))
                .then(data => {
                    setEditRw(-1);
                    refresh();
                    // console.log(data);
                })
                .catch(() => setError('Failed to update Modbus RW'))
        }
    }

    const modifyRw = () => {

    }

    const deleteRw = () => {
        const urlTale = 'intapi/modbusrwdelete/';
        const type = typeRead ? '/read/' : '/write/';
        console.log(deletedRw)
        request(url + urlTale + nodeId + type + deletedRw, "DELETE")
            .then(data => {
                setEditRw(-1);
                refresh();
                // console.log(data);
            })
            .catch(() => setError('Failed to delete Modbus RW'))
            .finally(() => setDeletedRw(-1));
    }

    const onRwEdit = (rw: ModbusRW, index: number) => {
        setRw(rw);
        setEditRw(index);
    }

    const onCancel = () => {
        setError('');
        setErrorFields([]);
        setEditRw(-1);
    }

    const handleRwName = (name: string) => {
        setRw({...rw, name});
    }

    const handleSlaveId = (slaveID: number) => {
        setRw({...rw, slaveID});
    }

    const handleAddress = (address: number) => {
        setRw({...rw, address});
    }

    const handleLength = (length: number) => {
        setRw({...rw, length});
    }

    const handleRwType = (type: ModbusRwType) => {
        setRw({...rw, type});
    }


    //>>>>>>>>>>>>>>>>>>>> Rendering

    const RwRow = (mbRw: ModbusRW, index: number, editedRw: ModbusRW, read: boolean) => {
        return (
                <tr key={index}>
                    <td>
                        <div hidden={editRw === index}>{mbRw.name}</div>
                        <div hidden={editRw !== index}>
                            {inputSelect(editedRw.name, "input", errorFields, "name",
                                "Name", handleRwName)}
                        </div>
                    </td>
                    <td>
                        <div hidden={editRw === index}>{mbRw.slaveID}</div>
                        <div hidden={editRw !== index}>
                            {inputSelect(editedRw.slaveID, "input", errorFields, "slaveID",
                                "Slave ID", handleSlaveId)}
                        </div>
                    </td>
                    <td>
                        <div hidden={editRw === index}>{mbRw.address}</div>
                        <div hidden={editRw !== index}>
                            {inputSelect(editedRw.address, "input", errorFields, "address",
                                "Address", handleAddress)}
                        </div>
                    </td>
                    <td>
                        <div hidden={editRw === index}>{mbRw.length}</div>
                        <div hidden={editRw !== index}>
                            {inputSelect(editedRw.length, "input", errorFields, "length",
                                "Length", handleLength)}
                        </div>
                    </td>
                    <td>
                        <div hidden={editRw === index}>{mbRw.type}</div>
                        <div hidden={editRw !== index}>
                            {inputSelect(editedRw.type, "select", errorFields, "type",
                                "Type", handleRwType, [...Object.values(ModbusRwType)])}
                        </div>
                    </td>
                    <td style={{display: 'flex'}}>
                        <div hidden={index === editRw} style={{display: "flex"}}>
                            <button className="btn btn-outline-primary" type="button"
                                    onClick={() => onRwEdit(mbRw, index)} disabled={running}>
                                <i className="fa fa-pencil-square-o"></i>
                            </button>
                            <button className="btn btn-outline-warning" type="button"
                            onClick={() => navigate(`${typeRead ? 'read' : 'write'}/${index}`)}>
                                <i className="fa fa-cogs"></i>
                            </button>
                            <button className="btn btn-outline-danger" type="button" disabled={running}
                                    onClick={() => {
                                        console.log(index);
                                        setDeletedRw(index);
                                    }}>
                                <i className="fa fa-trash-o"></i>
                            </button>
                        </div>
                        <div hidden={index !== editRw} style={{display: "flex"}}>
                            <button type="button" className="btn btn-outline-warning" onClick={() => onCancel()}>
                                <i className="fa fa-times" aria-hidden="true"></i>
                            </button>
                            <button type="button" className="btn btn-outline-success" onClick={() => updateRw(index)}
                            disabled={running}>
                                <i className="fa fa-floppy-o" aria-hidden="true"></i>
                            </button>
                        </div>

                    </td>
                </tr>
                // {/*<tr hidden={index !== editRw}  key={index+1000}>*/}
                // {/*    <td>*/}
                // {/*        <div>*/}
                // {/*            {error !== '' ? (*/}
                // {/*                <div className="localtooltip" style={{verticalAlign: "text-top"}}>*/}
                // {/*                    <span className="tooltiptext">{error}</span>*/}
                // {/*                </div>*/}
                // {/*            ) : null}*/}
                // {/*        </div>*/}
                // {/*    </td>*/}
                // {/*</tr>*/}
        )
    }

    const ReadWrites = (readWrite: Array<ModbusRW>, read: boolean) => {
        return (
            <table>
                {/*<thead>*/}
                <tbody>
                <tr>
                    <th style={{width: "10rem"}}>
                        <div>Name</div>
                    </th>
                    <th style={{width: "6rem"}}>
                        <div>Slave ID</div>
                    </th>
                    <th style={{width: "6rem"}}>
                        <div>Address</div>
                    </th>
                    <th style={{width: "6rem"}}>
                        <div>Length</div>
                    </th>
                    <th style={{width: "10rem"}}>
                        <div>Type</div>
                    </th>
                    <th style={{width: "10rem"}}></th>
                </tr>
                {/*</thead>*/}
                {readWrite.map((item, index) => {
                    // console.log(readWrite);
                    return (RwRow(item, index, rw, read));
                })}
                </tbody>
            </table>
        )
    }

    return (
        <div className="mt-5">
            <div style={{display: "flex", height: "2rem"}} className="justify-content-start">
                <div className="fw-bold fs-5">{title}</div>
                <div className='ms-5'>
                    <button className="btn btn-sm btn-outline-primary" onClick={() => addRW()}
                    disabled={running}>Add</button>
                </div>
            </div>
            {ReadWrites(readWrites, true)}
            <ConfirmationWindow
                header={"Confirmation"}
                body={`Please, confirm ${typeRead ? 'read' : 'write'} instance delete.`}
                show={deletedRw !== -1}
                onCancel={() => setDeletedRw(-1)}
                onConfirm={() => deleteRw()}
            />
        </div>
    )
}

export default ModbusRwComponent;