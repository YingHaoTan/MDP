# interfaces the Arduino with the Raspberry Pi
import serial
import time

class ArduinoInterface(object):

    def __init__(self):
        #self.port = '/dev/ttyACM0'
        self.port='COM13'
	#self.port='/dev/tty.usbmodem1421'
        self.baudrate = 115200
        self.ser = None
        # self.parity= serial.PARITY_ODD
        # self.bytesize=serial.SEVENBITS

    # Start and validate connection to Arduino
    def start_arduino_connection(self):
        print("Initialising Connection to Arduino From Raspberry Pi...")
        try:
            self.ser = serial.Serial(self.port, self.baudrate)
            if (self.ser.isOpen()):
                print("Serial Port: " + self.ser.portstr + " is successfully opened")
        except serial.SerialException as e:
            print("Serial Port: {} failed to open. Error: {}".format(self.port, e))

    # Read message from Arduino
    def read_from_arduino(self):
        a=0
        while a < 10:
            try:
                inByte = self.ser.inWaiting()
                read_msg = self.ser.read(inByte).decode()
                if read_msg:
                    print("Message read from arduino: " + read_msg)
                    a +=1
		    return inByte
            except serial.SerialException:
                pass

    # Write message to Arduino
    def write_to_arduino(self, msg):
        print("Message to write to arduino: " + msg.decode() )
        try:
            time.sleep(.5)
            self.ser.write(msg)
            print("Message written successfully")
        except Exception as e:
            print("Error: " + e)

    def end_arduino_connection(self):
        self.ser.close()
        print("Arduino connection closed")

