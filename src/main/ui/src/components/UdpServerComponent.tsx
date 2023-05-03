import {Constant} from "../constants/Constant";
import {useHttp} from "../hooks/http.hook";
import {useEffect, useState} from "react";
import {ProcessInterface} from "../models/ProcessInterface";
import {useNavigate, useParams} from "react-router-dom";
import {inputRow, textRow} from "../forms";
import Loader from "./Loader";
import {Tag} from "../models/Tag";
import UdpServer from "../models/UdpServer";
import {UdpServerRW} from "./UdpServerRW";
import {deepCopy} from "../utilities";

const UdpServerComponent = () => {

    const url = Constant.beurl;
    const {request} = useHttp();

    //>>>>>>>>>>>>>>>>>>>> Local states

    const [intr, setIntr] = useState<ProcessInterface>(new ProcessInterface())
    const [updIntr, setUpdIntr] = useState<ProcessInterface>(new ProcessInterface());
    const [edit, setEdit] = useState(false);
    const [loaded, setLoaded] = useState(false);
    const [error, setError] = useState('');
    const [errorField, setErrorField] = useState(new Array<string>());
    const [tagList, setTaglist] = useState([]);
    let {id} = useParams();
    const navigate = useNavigate();

    //>>>>>>>>>>>>>>>>>>>> Hooks

    useEffect(() => {
        getUdpServer();
        getTagList();
    }, [])

    //>>>>>>>>>>>>>>>>>>>> Modbus interface data loaded

    const getUdpServer = () => {
        const urlTale = 'intapi/interface';
        request(url + urlTale + '/' + id)
            .then(data => {
                setIntr(deepCopy(data));
                setUpdIntr(data);
                setLoaded(true);
            })
            .catch(() => {
                setError('Failed to load UDP Server interface');
                setLoaded(false);
            })
    }

    const setUdpServer = () => {
        const urlTale = 'intapi/intupdate';
        request(url + urlTale, "POST", JSON.stringify(updIntr))
            .then(_ => {
                getUdpServer();
                setEdit(false);
                // console.log(data);
            })
            .catch(() => setError('Failed to update modbus interface'))
        // }
    }

    const getTagList = () => {
        const urlTale = 'tagapi/tags';
        request(url + urlTale)
            .then(data => {
                setTaglist(data);
            })
            .catch(() => {
                setError('Could not fetch tag list.');
            })
    }

    const addTag = (type: string) => {
        // const urlTale = 'intapi/udpaddtag/' + id + '/' + type;
        // request(url + urlTale, "POST", JSON.stringify(updIntr))
        //     .then(_ => {
        //         getUdpServer();
        //         setEdit(false);
        //         // console.log(data);
        //     })
        //     .catch(() => setError('Failed to add tag to UDP Server interface'))
        const structure: UdpServer = updIntr.structure;
        type === "read" ? structure.read.push(new Tag()) : structure.write.push(new Tag());
        setUpdIntr({...updIntr, structure});
    }

    const onCancel = () => {
        setEdit(false);
        setUpdIntr({...updIntr, name: intr.name});
        handleUpdPort(intr.structure.port);
        setError('');
        setErrorField([]);
    }

    const onOK = () => {
        setEdit(false);
        setError('');
        setErrorField([]);
    }
    //>>>>>>>>>>>>>>>>>>>> Interface modifications
    const handleUpdName = (name: string) => {
        setUpdIntr({...updIntr, name});
    }

    const handleUpdPort = (port: number) => {
        const structure = {...updIntr.structure};
        structure.port = port;
        setUpdIntr({...updIntr, structure});
    }

    const handleKeyUp = (key: string) => {
        switch (key) {
            case "Enter":
                onOK()
                break;
            case "Escape":
                onCancel();
                break;
            default:
        }
    }

    const handleTag = (index: number, read: boolean, tag: Tag) => {
        const structure: UdpServer = {...updIntr.structure};
        read ? structure.read[index] = tag : structure.write[index] = tag;
        setUpdIntr({...updIntr, structure});
        // console.log(updIntr.structure.read[0]);
        // console.log(intr.structure.read[0]);
    }

    const deleteTag = (index: number, read: boolean) => {
        const structure: UdpServer = {...updIntr.structure};
        read ? structure.read.splice(index, 1) : structure.write.splice(index, 1);
    }

    /* Rendering */

    return (
        <div className="m-auto">
            <div className="mb-3 mt-3 btn btn-link -i-cursor" onClick={() => navigate('/ui/intlist/')}>
                {'<<BACK'}
            </div>
            {loaded ?
                <div className="paramsTable" style={{overflow: "visible"}}>
                    <table className="w-50">
                        <tbody>
                        {/* Columns width definition */}
                        <tr>
                            <th style={{width: "50%", minWidth: "8rem"}}></th>
                            <th style={{maxWidth: "50%", minWidth: "8rem"}}></th>
                        </tr>

                        {inputRow("Name", updIntr.name, edit, "input", "name",
                            errorField, handleUpdName, handleKeyUp, [], 0, null, false, false,
                            (intr.name === updIntr.name) ? {} : {backgroundColor: "#b5ffa0"})}
                        {textRow("Type", updIntr.type)}

                        {textRow("Status", updIntr.running ? "ON" : "OFF")}

                        {inputRow("Port", updIntr.structure.port?.toString(), edit, "input", "port",
                            errorField, handleUpdPort, handleKeyUp, [], 0, null, false, false,
                            (intr.structure.port === updIntr.structure.port) ? {} : {backgroundColor: "#b5ffa0"})}

                        </tbody>
                    </table>
                    <div className="mt-3"></div>
                    <div className="">
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
                            <button typeof="button" className="btn btn-success" onClick={() => onOK()}
                                    style={{width: "6rem"}}>
                                OK
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
                    <div className="">
                        <div className="mt-3">
                            <button typeof="button" className="btn btn-primary" disabled={intr.running}
                                    onClick={() => addTag("read")}
                                    style={{width: "10rem"}}>
                                Add read tag
                            </button>
                        </div>
                        <UdpServerRW
                            updIntr={updIntr}
                            intr={intr} read={true}
                            tagList={tagList}
                            handleTag={handleTag}
                            deleteTag={deleteTag}/>
                    </div>
                    <div className="">
                        <div className="mt-3">
                            <button typeof="button" className="btn btn-primary" disabled={intr.running}
                                    onClick={() => addTag("write")}
                                    style={{width: "10rem"}}>
                                Add write tag
                            </button>
                        </div>
                        <UdpServerRW
                            updIntr={updIntr}
                            intr={intr}
                            read={false}
                            tagList={tagList}
                            handleTag={handleTag}
                            deleteTag={deleteTag}/>
                        <button typeof="button" className="btn btn-success" onClick={() => setUdpServer()}
                                style={{width: "6rem"}} disabled={intr.running}>
                            Save
                        </button>
                    </div>
                </div> :
                <Loader/>
            }
        </div>
    )
}

export default UdpServerComponent;