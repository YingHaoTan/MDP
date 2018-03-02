# Arduino
MDP Group 6 arduino source code repository

Current Versions:

- Stable: 020318Robot01
    - Changes: 
        - Added all the communications related codes, to run the communication, the call the commWithRPI() function in the loop()
        - Created a seperate PID function PIDControl(), for readability and for easier changes
        - And other changes

- Stable: 230218Robot01Stable
    - Changes:
        - Fixed PID tuning the kP and kD constant to 3 and timed the PID function to activate at an interval of 500ms
        - Able to read strings of commands in an array (To use, comment "int action = readCommand()", and uncomment "int action[] = {}")
        - Added external library "Streaming" for better Serial print commands (No overheads!)
        - Added a calibrateRIGHT() function to calibrate the robot to be parallel from the wall on the right, and calibrateF is used to prevent unnecessarily calling calibrateRIGHT() multiple times


To Dos:
- Complete linearization for Left Side sensor and Front(Left) sensor
- Tune the acccuracy of the IR Sensors
- Optimizations
- Reimplement PID