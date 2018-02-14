from ArduinoInterface import *


class Main:
    def __init__(self):
        #arduino object
        self.arduino = ArduinoInterface()

    def connectArduino(self):
        self.arduino.start_arduino_connection()

    def endArduino(self):
        self.arduino.end_arduino_connection()

    def writeArduino(self):
        i = 0
        while i < 10:
            data = "testing 1,2,3"
            self.arduino.write_to_arduino(data.encode())
            i += 1


start = Main()
start.connectArduino()
start.arduino.read_from_arduino()
start.writeArduino()
start.endArduino()
