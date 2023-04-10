# ControlApplication

### Description
The ControlApplication is an industrial process control application that utilizes the Model Predictive Control algorithm, based on a State-Space model. The application is written in Kotlin and features a web-based user interface built with React and TypeScript. Communication with PLC or DCS is achieved via Modbus/TCP protocol.

### Model Predictive Control
The application features a classical MPC algorithm with input moves, as well as input and output constraint management. Matrix operations are carried out with the [EJML] library, and the optimization problem is solved using the Interior-Point method.

### Process Interface
The industrial processes are controlled using PLC or DCS, most of which support the Modbus communication protocol. The [jlibmodbus] library is used to implement the protocol. Although not the most convenient communication protocol, Modbus is widely supported and simple to use, with a history dating back to 1979 (with its TCP version introduced in 1999).
To transfer single precision floating point values, the IEEE-754 standard can be utilized. When using this standard, a single number can be sent or received using two registers.

### State-Space model
The ControlApplication does not currently support State-Space model identification, so this must be done externally using separate identification software. Examples of software that can be used for this purpose include [Systems Identification Package for PYthon (SIPPY)] and [System Identification Toolbox for MATLAB]. These tools provide a range of advanced identification algorithms and modeling techniques to accurately identify and estimate the system's parameters, allowing for efficient and effective control.
ControlApplication provides the ability to simulate the process using a given mathematical model. This means that the initial setup of the Model Predictive Control algorithm can be performed offline without the need for interaction with the real process.

### Data points table
The exchange of data takes place through the data points table, which must be created before using it in process simulation or MPC configuration, or process interface configuration. The system currently supports three types of data: floating-point numbers, integer numbers, and boolean. Floating-point numbers are primarily used in control, while boolean values are used for returning watchdog ticks to PLC/DCS. However, integer numbers do not currently have any specific use.

### Project building
This project is built using the Gradle build system with Kotlin. To start, clone the repository using Git and open it in your preferred IDE, e.g. [IntelliJ]. Before proceeding, make sure to build the Gradle configuration first. The project requires Java version 11 (Eclipse Temurin version 11.0.18 was used during development). [SdkMan] is one of the most convenient ways to install it. Configure Spring Boot to use Java version 11, and set net.apcsimple.ControlApplication.main as the main class in the module net.apcsimple.controlapplication.ControlServer. You can use Gradle bootJar command to generate a single .jar file application. Application can be run with double-click.

### User interface React project
For the React user interface, the compiled version is already available in /src/main/resources/static, while the React project itself is located in the /src/main/ui folder. Run the "npm install" command in /ui folder before starting project in IDE. After building modified project it should be copied to /src/main/resources/static replacing existing files.

[jlibmodbus]: https://github.com/kochedykov/jlibmodbus
[EJML]: http://ejml.org/wiki/index.php?title=Main_Page
[Systems Identification Package for PYthon (SIPPY)]: https://github.com/CPCLAB-UNIPI/SIPPY
[System Identification Toolbox for MATLAB]: https://mathworks.com/products/sysid.html
[SdkMan]: https://sdkman.io
[IntelliJ]: https://www.jetbrains.com/idea/
