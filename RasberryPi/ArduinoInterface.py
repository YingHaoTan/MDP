# interfaces the Arduino with the Raspberry Pi
import serial
import time


class ArduinoInterface:

    def __init__(self, port, baud_rate):
        # self.port = '/dev/ttyACM0'
        self.port = port
        # self.port='/dev/tty.usbmodem1421'
        self.baudrate = baud_rate
        self.ser = None

    # self.parity= serial.PARITY_ODD
    # self.bytesize=serial.SEVENBITS

    # Start and validate connection to Arduino
    def connect(self):
        print("Initialising Connection to Arduino From Raspberry Pi...")
        try:
            self.ser = serial.Serial(self.port, self.baudrate)
            if (self.ser.isOpen()):
                self.ser.setDTR(False)
                time.sleep(1)
                self.ser.flush()
                self.ser.setDTR(True)
                print("Serial Port: " + self.ser.portstr + " is successfully opened")

        except serial.SerialException as e:
            print("Serial Port: {} failed to open. Error: {}".format(self.port, e))

    # Read message from Arduino
    def read(self):
        time.sleep(.001)
        # while True:
        if (self.ser.inWaiting()):
            inByte = self.ser.read()
            if (inByte == bytes('~', 'ascii')):
                toReturn = []
                counter = 0
                while True:
                    nextByte = self.ser.read()
                    counter += 1
                    if (nextByte == bytes('!', 'ascii') and counter == 10):
                        return toReturn
                    else:
                        toReturn.append(nextByte)
        return None

    # Write message to Arduino
    def write(self, msg):
        # print("Message to write to arduino: " + msg.decode() )
        try:
            time.sleep(.001)
            self.ser.write(msg)
            self.ser.flush()
            print("Message written successfully")
        except Exception as e:
            print("Error: " + e)

    def end_arduino_connection(self):
        self.ser.close()
        print("Arduino connection closed")
