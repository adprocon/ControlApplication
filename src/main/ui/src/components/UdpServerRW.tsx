import {Tag} from "../models/Tag";
import {ProcessInterface} from "../models/ProcessInterface";
import UdpServer from "../models/UdpServer";
import {useEffect, useRef, useState} from "react";
import inputSelect from "../forms/inputSelect";
import ConfirmationWindow from "./ConfirmationWindow";

type Props = {
    updIntr: ProcessInterface;
    intr: ProcessInterface;
    read: boolean;
    tagList: Tag[];
    handleTag: any;
    deleteTag: any;
}
export const UdpServerRW = (props: Props) => {

    const ref = useRef<any>(null);
    const {updIntr, intr, read, tagList, handleTag, deleteTag} = props;
    const [editRow, setEditRow] = useState(-1);
    const [errorField, setErrorField] = useState(new Array<string>());
    const [deletedRWid, setDeletedRWid] = useState(-1);

    const unmodifiedList: Tag[] = read ? (intr.structure as UdpServer).read : (intr.structure as UdpServer).write;

    useEffect(() => {
        document.addEventListener('click', handleClickOutside);
        return () => {
            document.removeEventListener('click', handleClickOutside);
        };
    }, [])

    const handleClickOutside = (e: any) => {
        if ((ref.current && !ref.current.contains(e.target)) || e.target.id === "num" || e.target.id === "type") {
            setEditRow(-1);
        }
    }
    const handleTagName = (value: string, index: number) => {
        let tag = tagList.find(tag => tag.tagName === value);
        // console.log(unmodifiedList);
        handleTag(index, read, tag)
        setEditRow(-1);
    }

    const handleKeyAction = () => {

    }

    const deleteRW = () => {
        deleteTag(deletedRWid, read);
        setDeletedRWid(-1);
    }

    const rwRows = (list: Tag[]) => {
        return list.map(({tagName, dataType}, index) => {
            return (
                <tr key={index}
                    style={unmodifiedList.length > index ? {} : {backgroundColor: "#b5ffa0"}}
                >
                    <td style={{width: "3rem"}} id={"num"}>
                        {index + 1}
                    </td>
                    <td style={{width: "15rem"}}>
                        <div hidden={editRow === index}
                             onClick={() => setEditRow(index)} id={"name"}>
                            <div
                                className="d-inline-block w-auto"
                                style={(index < unmodifiedList.length && unmodifiedList[index].tagName === tagName) ? {} : {backgroundColor: "#b5ffa0"}}
                            >
                                {tagName}
                            </div>
                        </div>
                        <div hidden={editRow !== index}>
                            {inputSelect(tagName, "select", errorField, "name", "Name",
                                handleTagName, tagList.map((tag) => tag.tagName)
                                    .sort((a, b) => {
                                        return a > b ? 1 : (a < b ? -1 : 0)
                                    }),
                                index, false, handleKeyAction, undefined, true, false)}
                        </div>
                    </td>
                    <td style={{width: "10rem"}} id={"type"}>
                        {dataType}
                    </td>
                    <td>
                        <div style={{minWidth: "4.5rem"}}>
                            <button typeof="button" className="btn btn-outline-primary btn-sm me-1" disabled={updIntr.running}
                                    onClick={() => setEditRow(index)}
                                    style={{width: "2rem"}} id={"edit"}>
                                <i className="fa fa-pencil-square-o"></i>
                            </button>
                            <button typeof="button" className="btn btn-outline-danger btn-sm" disabled={updIntr.running}
                                    onClick={() => setDeletedRWid(index)}
                                    style={{width: "2rem"}} id={"edit"}>
                                <i className="fa fa-trash-o"></i>
                            </button>
                        </div>
                    </td>
                </tr>
            )
        })
    }

    return (
        <div>
            <table ref={ref} className="table table-striped d-inline-block">
                <tbody>
                {rwRows(read ? (updIntr.structure as UdpServer).read : (updIntr.structure as UdpServer).write)}
                </tbody>
            </table>
            {/* Tag delete confirmation window */}
            <ConfirmationWindow
                header={"Confirmation"}
                body={`Please, confirm ${read ? "read" : "write"} tag ${deletedRWid + 1} delete.`}
                show={deletedRWid !== -1}
                onCancel={() => setDeletedRWid(-1)}
                onConfirm={() => deleteRW()}
            />
        </div>
    )
}