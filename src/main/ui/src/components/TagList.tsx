import "./components.scss"

import {useHttp} from "../hooks/http.hook";
import {Tag} from "../models/Tag";
import {useEffect, useRef, useState} from "react";
import {DataType} from "../models/DataType";
import * as yup from "yup";
import ConfirmationWindow from "./ConfirmationWindow";
import inputSelect from "../forms/inputSelect";
import objCompare from "../hooks/sorting.hook";
import {Constant} from "../constants/Constant";
import {TagNumberFormat} from "../utilities";

const TagList = () => {

    const url = Constant.beurl;
    const {request} = useHttp();
    const errorBorder = {border: "2px solid rgba(255, 0, 0, 0.3)"};
    const defaultDataTypeValue = "Select data type";

// >>>>>>>>>>>>> Local states

    const [addError, setAddError] = useState('');
    const [addErrorField, setAddErrorField] = useState(new Array<string>());
    const [addTag, setAddTag] = useState({tagName: "", dataType: defaultDataTypeValue, value: ""});

    /* >>>>>>>>>>>>>>>>>>>>>>>> */
    /* Hooks */

    const addTagName = useRef<HTMLInputElement>(null);

    useEffect(() => {
        onDataTypeChange(addTag, handleNewValue)
    }, [addTag.dataType])

    const onDataTypeChange = (tag: any, handleValue: any) => {
        switch (tag.dataType) {
            case DataType.double:
                handleValue(Number(tag.value));
                break;
            case DataType.int:
                handleValue(Math.round(Number(tag.value)));
                break;
            case DataType.bool:
                handleValue(Number(tag.value) !== 0);
                break;
        }
    }

    /* >>>>>>>>>>>>>>>>>>>>>>>>>>>>> */
    /* Add new tag */
    function addNewTag() {
        const localTag = {id: '', ...addTag}
        if (addTag.value === '') {

        }
        validateTag(addTag as Tag, setAddError, setAddErrorField);

        if (tagSchema.isValidSync(addTag)) {
            const urlTale = 'tagapi/tagadd';
            request(url + urlTale, 'POST', JSON.stringify(localTag))
                .then(data => {
                    // dispatch(tagAdding());
                    clearTagAddForm();
                })
                .catch((error) => {
                    setAddError("This tag already exists.");
                })
                .finally(() => {
                    // getTagList();
                    // setEdit(-1);
                })
            if (addTagName.current !== null) {
                addTagName.current.focus();
            }
        }
    }

    const clearTagAddForm = () => {
        setAddTag({tagName: '', dataType: defaultDataTypeValue, value: ''});
        setAddError('');
        setAddErrorField(new Array<string>());
    }

    const handleNewTagName = (tagName: string) => {
        setAddTag({...addTag, tagName})
    }
    const handleNewDataType = (dataType: DataType) => {
        setAddTag({...addTag, dataType})
    }
    const handleNewValue = (value: any) => {
        setAddTag({...addTag, value})
    }

    const handleNewKeyUp = (key: string) => {
        if (key === "Enter") {
            addNewTag();
        }
    }


// >>>>>>>> Validation
    const tagSchema = yup.object().shape({
        tagName: yup.string().required('Name required.'),
        dataType: yup.mixed().oneOf(Object.values(DataType), 'Data type required.'),
        value: yup.mixed().when('dataType', {
            is: 'bool',
            then: yup.mixed().oneOf([true, false, 'true', 'false'], 'Value incorrect.'),
            otherwise: yup.mixed().test('type-check', 'Value incorrect.',
                function (value) {
                    return !isNaN(parseFloat(value));
                }
            )
        })
    });

    const validateTag = (tag: Tag, setError: any, setErrorField: any) => {
        console.log("Validation");
        try {
            tagSchema.validateSync(tag, {abortEarly: false});
            setError('');
            setErrorField([]);
        } catch (err: any) {
            // console.log(err.inner.map((el: any) => el.path));
            setError(err.inner.map((er: any) => er.message).join(' '));
            setErrorField(err.inner.map((el: any) => el.path));
        }
    }

    /* >>>>>>>>>>>>>>>>>>>>> */
    /* Rendering starts here */

    const dataTypeOptions = [defaultDataTypeValue, ...Object.values(DataType)];

    /* Render main component */
    return (
        /* Add new tag */
        <div className="">
            <table className="m-auto">
                <tbody>
                <tr>
                    <td style={{width: "12rem"}}>
                        {inputSelect(addTag.tagName, 'input', addErrorField,
                            'tagName', 'Name', handleNewTagName,
                            [],0, false, handleNewKeyUp, addTagName)}
                    </td>
                    <td style={{width: "10rem"}}>
                        {inputSelect(addTag.dataType, 'select', addErrorField,
                            'dataType', '', handleNewDataType, dataTypeOptions,
                            0, false)}
                    </td>
                    <td>
                        <div style={{width: "10rem"}} className="d-flex justify-content-center">
                        {inputSelect(addTag.value, 'input', addErrorField,
                            'value', 'Value', handleNewValue,
                            [], 0, false, handleNewKeyUp)}
                        </div>
                    </td>
                    <td style={{display: "flex"}}>
                        <button className="btn btn-primary" onClick={addNewTag}>Add</button>
                        <button className="btn btn-warning" onClick={clearTagAddForm}>Clear</button>
                    </td>
                </tr>
                </tbody>
            </table>
            {addError !== '' ? (
                <div className="localtooltip d-flex justify-content-center">
                    <span className="tooltiptext">{addError}</span>
                </div>
            ) : null}

            <div className="mt-5"></div>

            <ListOfTags></ListOfTags>
        </div>
    )
}

const ListOfTags = () => {

    const url = Constant.beurl;
    const {request} = useHttp();

    const errorBorder = {border: "2px solid rgba(255, 0, 0, 0.3)"};

// >>>>>>>>>>>>> Local states

    const [tagList, setTaglist] = useState([]);
    const [edit, setEdit] = useState(-1);
    const [error, setError] = useState('');
    const [errorField, setErrorField] = useState(new Array<string>());
    const [tag, setTag] = useState({tagName: "init", dataType: DataType.double, value: 0});
    const [sorting, setSorting] = useState("tagName");
    const [reverse, setReverse] = useState(false);
    const [deleteTagId, setDeleteTagId] = useState(0);
    const [deleteTagName, setDeleteTagName] = useState("");

// >>>>>>>> Sorting

    const handleSorting = (field: string) => {
        if (sorting === field) {
            setReverse(!reverse);
        } else {
            setSorting(field);
            setReverse(false);
        }
    }

    /* >>>>>>>>>>>>>>>>>>>>>>>> */
    /* Hooks */

    const addTagName = useRef<HTMLInputElement>(null);
    // const confirmWindow = useRef<HTMLDivElement>(null);

    useEffect(() => {
        getTagList();
        const interval = setInterval(() => {
            if (edit < 0) {
                getTagList();
            }
            // console.log('Edit = ' + edit);
            // console.log(navigator.languages);
        }, 1000);
        return () => clearInterval(interval);
    }, [edit])

    useEffect(() => {
        validateTag(tag as Tag, setError, setErrorField);
    }, [tag])

    useEffect(() => {
        onDataTypeChange(tag, handleValue)
    }, [tag.dataType])

    const onDataTypeChange = (tag: any, handleValue: any) => {
        switch (tag.dataType) {
            case DataType.double:
                handleValue(Number(tag.value));
                break;
            case DataType.int:
                handleValue(Math.round(Number(tag.value)));
                break;
            case DataType.bool:
                handleValue(Number(tag.value) !== 0);
                break;
        }
    }

// >>>>>>>>>>>>>> Actions

    function getTagList() {
        const urlTale = 'tagapi/tags';
        request(url + urlTale)
            .then(data => {
                setTaglist(data);
            })
            .catch(() => {
                setError('Could not fetch tag list.');
            })
    }

    /* >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> */
    /* Delete tag */
    function deleteTag() {
        const urlTale = `tagapi/tagdelete/${deleteTagId}`;
        request(url + urlTale, 'DELETE')
            .then(data => {
            })
            .catch((error) => {
                setError('Could not delete tag.')
            })
            .finally(() => {
                getTagList();
                setEdit(-1);
                setDeleteTagId(0);
            })
    }

    /* >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> */
    /* Update tag */
    const updateTag = (id: number) => {
        let localTag = {id: id, ...tag}
        const urlTale = 'tagapi/tagupdate';
        request(url + urlTale, 'POST', JSON.stringify(localTag))
            .then(data => {
            })
            .catch((error) => {
                setError('Could not update tag.')
            })
            .finally(() => {
                getTagList();
                setEdit(-1);
            })
    }

    const handleTagName = (tagName: string) => {
        setTag({...tag, tagName});
    }
    const handleDataType = (dataType: DataType) => {
        setTag({...tag, dataType});
    }
    const handleValue = (value: any) => {
        setTag({...tag, value});
    }

    const handleKeyUp = (id: number) => (key: string) => {
        if (key === "Enter") {
            updateTag(id);
        }
    }


// >>>>>>>>>> Tag buttons
    const onCancel = () => {
        setEdit(-1);
        setError('');
        setErrorField([]);
    }

    const onEdit = (index: number, id: number) => {
        let tag = tagList.find((element: Tag) => element.id === id);
        setTag(typeof(tag) != 'undefined' ? tag : new Tag());
        setEdit(index);
    }

// >>>>>>>> Validation
    const tagSchema = yup.object().shape({
        tagName: yup.string().required('Name required.'),
        dataType: yup.mixed().oneOf(Object.values(DataType), 'Data type required.'),
        value: yup.mixed().when('dataType', {
            is: 'bool',
            then: yup.mixed().oneOf([true, false, 'true', 'false'], 'Value incorrect.'),
            otherwise: yup.mixed().test('type-check', 'Value incorrect.',
                function (value) {
                    return !isNaN(parseFloat(value));
                }
            )
        })
    });

    const validateTag = (tag: Tag, setError: any, setErrorField: any) => {
        try {
            tagSchema.validateSync(tag, {abortEarly: false});
            setError('');
            setErrorField([]);
        } catch (err: any) {
            setError(err.inner.map((er: any) => er.message).join(' '));
            setErrorField(err.inner.map((el: any) => el.path));
        }
    }

    /* >>>>>>>>>>>>>>>>>>>>> */
    /* Rendering starts here */

    const renderTagList = (list: Tag[]) => {
        return [...list]
            .sort((a, b) => objCompare(a, b, sorting, reverse))
            .map(({id, tagName, dataType, value}, index) => {
                return (
                    <tr key={id} className="">
                        {tagListCell(tagName, tag.tagName, 'input',
                            'tagName', index, handleTagName, handleKeyUp(id))}
                        {tagListCell(dataType, tag.dataType, 'select',
                            'dataType', index, handleDataType, handleKeyUp(id))}
                        {tagListCell(TagNumberFormat(value, dataType), tag.value, 'input',
                            'value', index, handleValue, handleKeyUp(id))}

                        <td>
                            <div hidden={edit === index} style={{display: "flex"}}  className="justify-content-end">
                                <button className="btn btn-primary btn-sm" onClick={() => onEdit(index, id)}>Edit
                                </button>
                                <button className="btn btn-warning btn-sm" onClick={() => {
                                    setDeleteTagId(id);
                                    setDeleteTagName(tagName);
                                }}>Delete</button>
                            </div>
                            <div hidden={edit !== index} style={{display: "flex"}}  className="justify-content-end">
                                <button className="btn btn-success btn-sm"
                                        onClick={() => updateTag(id)}
                                        disabled={error !== ''}>
                                    Save
                                </button>
                                <button className="btn btn-warning btn-sm ms-1" onClick={onCancel}>Cancel</button>
                                {error !== '' ? (
                                    <div className="localtooltip">
                                        <span className="tooltiptext">{error}</span>
                                    </div>
                                ) : null}
                            </div>
                        </td>
                    </tr>
                )
            })
    }

    const tagListCell = (
        fieldValue: string, fieldVariable: any, inputOrSelect: string,
        fieldError: string, index: number, handleAction: any, handleKeyAction: any
    ) => {
        return (
            <td className="tablelistcell">
                <div hidden={edit === index} style={{textAlign: "start"}}>{fieldValue}</div>
                <div hidden={edit !== index} style={{textAlign: "start"}}>
                    {inputSelect(fieldVariable, inputOrSelect, errorField, fieldError, '', handleAction,
                        [...Object.values(DataType)], 0, false, handleKeyAction)}
                </div>
            </td>
        )
    }

    // const dataTypeOptions = [defaultDataTypeValue, ...Object.values(DataType)];

    /* Render main component */
    return (
        <div className="tablelist">
            <table className="table table-striped">
                <thead>
                <tr className="">
                    <th className="tablelistheading" style={{width: "12rem"}} onClick={() => handleSorting('tagName')}
                    >Name {sorting === 'tagName' ? (reverse ? <span>&uarr;</span> : <span>&darr;</span>) : ""}</th>
                    <th className="tablelistheading" style={{width: "8rem"}} onClick={() => handleSorting('dataType')}
                    >Type {sorting === 'dataType' ? (reverse ? <span>&uarr;</span> : <span>&darr;</span>) : ""}</th>
                    <th className="tablelistheading" style={{width: "10rem"}} onClick={() => handleSorting('value')}
                    >Value {sorting === 'value' ? (reverse ? <span>&uarr;</span> : <span>&darr;</span>) : ""}</th>
                    <th className="tablelistheading" style={{width: "10rem"}}></th>
                </tr>
                </thead>
                <tbody className="">
                {renderTagList(tagList)}
                </tbody>
            </table>

            {/* Tag delete confirmation window */}
            <ConfirmationWindow
                header={"Confirmation"}
                body={`Please, confirm tag ${deleteTagName} delete.`}
                show={deleteTagId !== 0}
                onCancel={() => setDeleteTagId(0)}
                onConfirm={() => deleteTag()}
            />
        </div>
    )
}

export default TagList;