import {useEffect, useState} from "react";
import {useHttp} from "../hooks/http.hook";
import {useNavigate, useParams} from "react-router-dom";
import inputSelect from "../forms/inputSelect";
import {ModbusRwItem} from "../models/ModbusRwItem";
import {Tag} from "../models/Tag";
import {Constant} from "../constants/Constant";
import * as yup from "yup";

const ModbusRwItemComponent = () => {

    const url = Constant.beurl;
    const {request} = useHttp();

    let {rwid, id, type} = useParams();

    //>>>>>>>>>>>>>>>> Local variables
    const [itemsList, setItemslist] = useState(new Array<ModbusRwItem>());
    const [editList, setEditlist] = useState(new Array<ModbusRwItem>());
    const [tagList, setTaglist] = useState([]);
    const [edit, setEdit] = useState(false);
    const [error, setError] = useState('');
    const [errorFields, setErrorFields] = useState(new Array<string>());
    const navigate = useNavigate();

    //>>>>>>>>>>>>>>>>>>>> Hooks

    useEffect(() => {
        getRwList();
        getTagList();
    }, [])

    useEffect(() => {
        // console.log(errorFields);
    }, [errorFields])

    useEffect(() => {
        // console.log(itemsList);
        setEditlist(copyArray(itemsList));
    }, [itemsList])

    //>>>>>>>>>>>>>>>>>>>> Validation

    const modbusRwItemSchema = yup.object().shape({
        gain: yup.number().typeError('Gain must be number.'),
        offset: yup.number().typeError('Offset must be number.'),
    })

    const validateModbusRWItem = (): Boolean => {
        let error: string = '';
        let errFields = new Array<string>();
        let pass = true;
        editList.forEach((item, index) => {
            try {
                modbusRwItemSchema.validateSync(item, {abortEarly: false});
            } catch (err: any) {
                pass = false;
                // console.log(err.inner);
                let newerror = err.inner.map((er: any) => er.message).join(' ');
                error = error.includes(newerror) ? error : newerror.includes(error) ? newerror : error + ' ' + newerror;
                errFields = [...errFields, ...(err.inner.map((er: any) => (er.path + index)))];
            }
        })
        setError(error);
        setErrorFields(errFields);
        return pass;
    }

    //>>>>>>>>>>>>>>>> List modifications
    const getRwList = () => {
        const urlTale = 'intapi/modbusrw';
        request(url + urlTale + '/' + id + '/' + type + '/' + rwid)
            .then(data => {
                setItemslist([...data]);
                setEditlist([...data]);
                // console.log(data);
            })
            .catch(() => {
                setError('Failed to load modbusrw list');
            })
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

    const updateRwList = () => {
        const urlTale = 'intapi/modbusrwupdate';
        if (validateModbusRWItem()) {
            setError('');
            setErrorFields([]);
            request(url + urlTale + '/' + id + '/' + type + '/' + rwid,
                'POST', JSON.stringify(editList))
                .then(data => {
                    getRwList();
                    getTagList();
                    setEdit(false);
                })
                .catch(() => {
                    setError('Could not update list.');
                })
        }
    }

    const handleTagName = (name: string, index: number) => {
        const list: ModbusRwItem[] = [...editList];
        list[index].tagName = name;
        setEditlist([...list]);
    }

    const handleGain = (gain: number, index: number) => {
        const list: ModbusRwItem[] = [...editList];
        list[index].gain = gain;
        setEditlist([...list]);
    }

    const handleOffset = (offset: number, index: number) => {
        const list: ModbusRwItem[] = [...editList];
        list[index].offset = offset;
        setEditlist([...list]);
    }

    const handleIeee754 = (ieee754: boolean, index: number) => {
        const list: ModbusRwItem[] = [...editList];
        // console.log(index);
        list[index].ieee754 = ieee754;
        setEditlist([...list]);
    }

    const onCancel = () => {
        // console.log('Test Cancel');
        setEdit(false);
        // setEditlist([...itemsList]);
        setEditlist(copyArray(itemsList));
    }

    const copyArray = (arr1: ModbusRwItem[]): ModbusRwItem[] => {
        const list: ModbusRwItem[] = [];
        arr1.forEach((item, index) => {
            list[index] = {...item};
        })
        return list;
    }

    //>>>>>>>>>>>>>>>>>>>> Rendering

    const RwRow = (rwItem: ModbusRwItem, index: number, editItem: ModbusRwItem) => {
        return (
            <tr key={index}>
                <td style={{height: "2.5rem"}}>
                    <div>{rwItem.modbusAddress}</div>
                </td>
                <td>
                    <div hidden={edit || rwItem.blocked}>{rwItem.tagName}</div>
                    <div hidden={!edit}>
                        {inputSelect(editItem.tagName, "select", errorFields, "tagName",
                            "", handleTagName,
                            ["Not used", ...tagList.map((tag: Tag) => tag.tagName)], index)}
                    </div>
                </td>
                <td>
                    <div hidden={edit || rwItem.ieee754 || rwItem.blocked}>{rwItem.gain}</div>
                    <div hidden={!edit}>
                        {inputSelect(editItem.gain, "input", errorFields, "gain" + index,
                            "Gain", handleGain, [], index, editItem.ieee754)}
                    </div>
                </td>
                <td>
                    <div hidden={edit || rwItem.ieee754 || rwItem.blocked}>{rwItem.offset}</div>
                    <div hidden={!edit}>
                        {inputSelect(editItem.offset, "input", errorFields, "offset" + index,
                            "Offset", handleOffset, [], index, editItem.ieee754)}
                    </div>
                </td>
                <td>
                    <div hidden={edit} className="ms-3">
                        {/*{rwItem.ieee754.toString()}*/}
                        {rwItem.ieee754 ?
                            <i className="fa fa-check-square-o" aria-hidden="true" style={{color: "blue"}}></i> :
                            <i className="fa fa-square-o" aria-hidden="true"></i>}

                    </div>
                    <div hidden={!edit}>
                        <div className="ms-3">
                            {inputSelect(editItem.ieee754, "input", errorFields, "ieee754",
                                "", handleIeee754, [], index)}
                        </div>
                    </div>
                </td>
                <td>
                    <div className="ms-4">
                        {/*{rwItem.blocked.toString()}*/}
                        {rwItem.blocked ?
                            <i className="fa fa-check-square-o" aria-hidden="true" style={{color: "red"}}></i> :
                            <i className="fa fa-square-o" aria-hidden="true" style={{color: "green"}}></i>}
                    </div>
                </td>
            </tr>
        )
    }

    const ReadWrites = () => {
        return (
            <table>
                <thead>
                <tr>
                    <th style={{width: "10rem"}}>
                        <div>Address</div>
                    </th>
                    <th style={{width: "10rem"}}>
                        <div>Tag</div>
                    </th>
                    <th style={{width: "6rem"}}>
                        <div>Gain</div>
                    </th>
                    <th style={{width: "6rem"}}>
                        <div>Offset</div>
                    </th>
                    <th style={{width: "6rem"}}>
                        <div>IEEE754</div>
                    </th>
                    <th style={{width: "10rem"}}>
                        <div>Blocked</div>
                    </th>
                    <th style={{width: "10rem"}}></th>
                </tr>
                </thead>
                <tbody>
                {itemsList.map((item, index) => {
                    return (RwRow(item, index, editList[index]));
                })}
                </tbody>
            </table>
        )
    }

    return (
        <>
            <div className="mb-3 mt-3 btn btn-link -i-cursor" onClick={() => navigate('/ui/modbus/' + id)}>
                {'<<BACK'}
            </div>
            <div style={{display: "flex", height: "2rem"}} className="justify-content-start">
                <div className="fw-bold fs-5">Modbus {type ? 'read' : 'write'} items list</div>
                <div className='ms-5' hidden={edit}>
                    <button className="btn btn-sm btn-outline-primary" onClick={() => setEdit(true)}>Edit</button>
                </div>
                <div className='ms-5' hidden={!edit}>
                    <button className="btn btn-sm btn-outline-primary" onClick={() => onCancel()}>Cancel</button>
                    <button className="btn btn-sm btn-outline-primary" onClick={() => updateRwList()}>Save</button>
                </div>
            </div>
            <div className="mt-3"></div>
            <div>
                {error !== '' ? (
                    <div className="localtooltip" style={{verticalAlign: "text-top"}}>
                        <span className="tooltiptext">{error}</span>
                    </div>
                ) : null}
            </div>
            {ReadWrites()}
        </>
    )
}

export default ModbusRwItemComponent;