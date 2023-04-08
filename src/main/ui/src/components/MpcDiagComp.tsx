import {Constant} from "../constants/Constant";
import {useHttp} from "../hooks/http.hook";
import {useEffect, useState} from "react";
import {MpcDiag} from "../models/MpcDiag";
import {useParams} from "react-router-dom";
import {matrixRender} from "../forms";

const MpcDiagComp = () => {

    const url = Constant.beurl;
    const {request} = useHttp();

    /* Local states */
    const [mpcDiag, setMpcDiag] = useState<MpcDiag>(new MpcDiag());
    const [loaded, setLoaded] = useState(false);
    const [error, setError] = useState('');

    let {id} = useParams();

    useEffect(() => {
        getMpcDiag();
        const interval = setInterval(() => {
            getMpcDiag();
        }, 1000);
        return () => clearInterval(interval);
    }, [])

    useEffect(() => {
        // console.log(mpcDiag.freeResponseOut)
    }, [mpcDiag])


    /* Get diagnostics data */
    const getMpcDiag = () => {
        const urlTale = 'conapi/condiag';
        request(url + urlTale + '/' + id)
            .then(data => {
                console.log(data);
                setMpcDiag(data);
                setLoaded(true);
            })
            .catch(() => {
                setError('Failed to load MPC diagnostics');
                // setLoaded(false);
            })
    }

    const diagData = () => {
        return (
            <>
                <div style={{display: "flex"}}>
                    <div className="mr">
                        Free response
                        {matrixRender(mpcDiag.freeResponseOut[0], "", "out ", 120)}
                    </div>
                    <div className="mr">
                        Prediction
                        {matrixRender(mpcDiag.prediction[0], "", "out ", 120)}
                    </div>
                    <div className="mr">
                        Optimal inputs
                        {matrixRender(mpcDiag.optimalInputsHc[0], "", "in ", 120)}
                    </div>
                </div>
                <div style={{display: "flex"}}>
                    <div className="mr">
                        Free response error
                        {matrixRender(mpcDiag.freeResponseError[0], "", "out ", 120)}
                    </div>
                    <div className="mr">
                        Trajectory
                        {matrixRender(mpcDiag.trajectory[0], "", "out ", 120)}
                    </div>
                    <div className="mr">
                        Optimal inputs
                        {matrixRender(mpcDiag.optimalInputsHc[0], "", "in ", 120)}
                    </div>
                </div>
                <div style={{display: "flex", marginTop: "20px"}}>
                    <div className="mr">
                        Inputs
                        {matrixRender(mpcDiag.inputs[0], "", undefined, 120)}
                    </div>
                    <div className="mr">
                        States
                        {matrixRender(mpcDiag.states[0], "", undefined, 120)}
                    </div>
                    <div className="mr">
                        Restored states
                        {matrixRender(mpcDiag.restoredStates[0], "", undefined, 120)}
                    </div>
                    <div className="mr">
                        Computed Outputs
                        {matrixRender(mpcDiag.computedOutputs[0], "", undefined, 120)}
                    </div>
                    <div className="mr">
                        Outputs
                        {matrixRender(mpcDiag.outputs[0], "", undefined, 120)}
                    </div>
                </div>
                <div style={{display: "flex", marginTop: "20px"}}>
                    <div className="mr">
                        Optimal moves
                        {matrixRender(mpcDiag.optimalMovesHc[0], "", "in ", 120)}
                    </div>
                    <div className="mr">
                        Min constraints
                        {matrixRender(mpcDiag.dUMin[0], "", "in ", 120)}
                    </div>
                    <div className="mr">
                        Max constraints
                        {matrixRender(mpcDiag.dUMax[0], "", "in ", 120)}
                    </div>
                </div>
                <div style={{display: "flex", marginTop: "20px"}}>
                   <div className="mr">
                        U Min constraints
                        {matrixRender(mpcDiag.dUMinU[0], "", "in ", 120)}
                    </div>
                    <div className="mr">
                        U Max constraints
                        {matrixRender(mpcDiag.dUMaxU[0], "", "in ", 120)}
                    </div>
                    <div className="mr">
                        Z Min constraints
                        {matrixRender(mpcDiag.dUMinZ[0], "", "in ", 120)}
                    </div>
                    <div className="mr">
                        Z Max constraints
                        {matrixRender(mpcDiag.dUMaxZ[0], "", "in ", 120)}
                    </div>
                </div>
            </>
        )
    }

    return (
        <div>
            {loaded ? diagData() :
            <div>
                Control is not running
            </div>}
        </div>
    )
}

export default MpcDiagComp;