import Select from "react-select";

const inputSelect = (fieldVariable: any,
                     inputOrSelect: string,
                     errorField: Array<string>,
                     fieldError: string,
                     placeholder: string,
                     handleAction: any,
                     optionsArray?: Array<string>,
                     index?: number,
                     dsbl: boolean = false,
                     handleKeyAction?: any,
                     reff?: any,
                     isSearchable: boolean = false,
                     isClearable: boolean = false
) => {
    const errorBorder = {border: "2px solid rgba(255, 0, 0, 0.3)"};
    return (
        <>
            {inputOrSelect === 'input' ?
                typeof (fieldVariable) === "boolean" ?
                    <input
                        className="form-check-input"
                        type="checkbox"
                        checked={fieldVariable}
                        onChange={(e) => handleAction(e.target.checked, index)}
                        disabled={dsbl}
                        onKeyUp={(e) => {
                            handleKeyAction(e.key);
                        }}

                    /> :
                    <input
                        ref={reff}
                        className="form-control"
                        style={errorField.includes(fieldError) ? errorBorder : {display: "flex"}}
                        placeholder={placeholder}
                        type="text"
                        value={fieldVariable}
                        onChange={(e) => handleAction(e.target.value, index)}
                        disabled={dsbl}
                        onKeyUp={(e) => {
                            e.preventDefault();
                            if (handleKeyAction) {
                                handleKeyAction(e.key);
                            }
                        }}
                    /> : inputOrSelect === 'select' ?
                <Select
                    ref={reff}
                    className="basic-single"
                    classNamePrefix="select"
                    isClearable={isClearable}
                    isSearchable={isSearchable}
                    placeholder={placeholder}
                    // name="color"
                    // defaultValue={fieldVariable}
                    value={{value: fieldVariable, label: fieldVariable}}
                    // autoFocus={true}
                    onChange={(newValue: any) => handleAction(newValue ? newValue.value : "", index)}
                    // onKeyDown={(e) => {
                    //     e.preventDefault();
                    //     console.log(e.key);
                    //     if (handleKeyAction) {
                    //         handleKeyAction(e.key);
                    //     }
                    // }}
                    options={dataOptions(optionsArray)}
                /> : null
            }
        </>
    )
}


const dataOptions = (optionsArray?: Array<string>) => {
    let arr: Array<string>;
    if (optionsArray && optionsArray.length > 0) {
        arr = [...optionsArray]
    } else {
        return []
    }
    return arr.map((el) => {
        return (
            { value : el, label: el, color: "#000000", isFixed: true}
        )
    })
}

export default inputSelect;