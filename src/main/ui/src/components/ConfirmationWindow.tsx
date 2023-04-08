
const ConfirmationWindow = (props: any) => {

    const {header, body, show, onCancel, onConfirm} = props;

    if (!show) {
        return null;
    }

    return (
        <div className="modalwindow">
            <div className="modalcontent">
                <div className="modalheader">
                    <h5 className="modaltitle">{header}</h5>
                </div>
                <div className="modalbody">
                    {body}
                </div>
                <div className="modalfooter">
                    <button className="btn btn-danger me-3" onClick={onConfirm}>Yes</button>
                    <button className="btn btn-primary ms-3" onClick={onCancel}>No</button>
                </div>
            </div>
        </div>
    )
}

export default ConfirmationWindow;