from ArduinoInterface import *
import queue
import _thread

class Main:

    def __init__(self):
        try: 
            #instantitate objects and start connection
            print("Instantiating objects")
            self.arduino = ArduinoInterface()
            self.arduino.start_arduino_connection()

        except:
            print("Instantiate object failed")

 
    def endArduino(self):
        self.arduino.end_arduino_connection()


    #get data from queue and write to arduino
    def writeArduino(self, q):
        while True:
            if not q.empty():
                data = q.get() 
                self.arduino.write_to_arduino(data)

    #get data from arduino and pass to queue
    def readArduino(self,q):
        while True: 
            data = self.arduino.read_from_arduino()
            q.out(data)
 
    def threadCreate(self):
        try: 
            print("Starting Queue and Threads")
            start.q = queue.Queue()
            _thread.start_new_thread(start.readArduino, (start.q,))
            _thread.start_new_thread(start.writeArduino, (start.q,))
        except Exception:
            print("Error: Unable to start thread/queue" )

        while 1:
            pass

start = Main()
start.threadCreate()
