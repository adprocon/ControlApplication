import React from 'react';
import './App.css';
import TagList from "./components/TagList";
import {NavBar} from "./components/NavBar";
import {BrowserRouter, Navigate, Route, Routes} from "react-router-dom";
import InterfaceList from "./components/InterfaceList";
import Modbus from "./components/Modbus";
import ModbusRwItemComponent from "./components/ModbusRwItemComponent";
import ModelsList from "./components/ModelsList";
import StateSpaceModel from "./components/StateSpaceModel";
import ControllersList from "./components/ControllersList";
import MpcComp from "./components/MpcComp";
import StateSpaceSimulation from "./components/StateSpaceSimulation";
import MpcDiagComp from "./components/MpcDiagComp";
import UdpServerComponent from "./components/UdpServerComponent";

const App = () => {

    return (
        // <Provider store={store}>
        <BrowserRouter>
            <div className="container" style={{maxWidth: "80%"}}>
                <NavBar/>
                <div className="mt-3"></div>
                <Routes>
                    <Route path='/ui/taglist' element={<TagList/>}/>
                    <Route path='/ui/intlist' element={<InterfaceList/>}/>
                    <Route path='/ui/modbus/:id' element={<Modbus/>}></Route>
                    <Route path='/ui/udpserver/:id' element={<UdpServerComponent/>}></Route>
                    <Route path='/ui/modbus/:id/:type/:rwid' element={<ModbusRwItemComponent/>}></Route>
                    <Route path='/ui/models' element={<ModelsList/>}/>
                    <Route path='/ui/models/statespace/:id' element={<StateSpaceModel/>}/>
                    <Route path='/ui/models/statespacesimulation/:id' element={<StateSpaceSimulation/>}/>
                    <Route path='/ui/controllers' element={<ControllersList/>}/>
                    <Route path='/ui/controllers/mpc/:id' element={<MpcComp/>}/>
                    <Route path='/ui/controller/diagnostics/:id' element={<MpcDiagComp/>}/>
                    <Route path="*" element={<Navigate to="/ui/taglist" replace/>}/>
                </Routes>
            </div>
        </BrowserRouter>
        // </Provider>
    );
}

export default App;
