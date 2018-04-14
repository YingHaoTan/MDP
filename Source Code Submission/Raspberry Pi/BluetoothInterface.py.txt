# to implement without the library look up http://blog.kevindoran.co/bluetooth-programming-with-python-3/
from bluetooth import *
import os

# SERVER SIDE
class BluetoothInterface:

    def __init__(self):
        # after getting bluetooth address, now must implement to transfer data.
        # implement server sidsse
        os.system("sudo hciconfig hci0 piscan")
        self.rpi_sock = BluetoothSocket(RFCOMM)
        # assign hostmac address for bluetooth adapters if needed as may have multiple adapters
        #self.port = self.rpi_sock.getsockname()[1] 
        self.port = 4
        self.host = ''

    def connect(self):
        try:
            self.rpi_sock.bind((self.host, self.port))
            self.rpi_sock.listen(1)
            uuid = '00001101-0000-1000-8000-00805F9B34FB'
            advertise_service(self.rpi_sock, "MDP6-Server",
             service_id = uuid,
             service_classes = [ uuid, SERIAL_PORT_CLASS ],
             profiles = [ SERIAL_PORT_PROFILE ],
             ) 
            print("Waiting for connection on RFCOMM channel %d" % self.port) 
            
            self.android_sock, self.android_address = self.rpi_sock.accept()
            self.android_sock.setblocking(False)
            self.android_sock.settimeout(0.0)
            if (self.android_address[0] == '08:60:6E:AD:33:FC'):
                print("Accepted Connection from socket: " + str(self.android_sock) + " Address: " + str(self.android_address))
                return 1
            else:
                print("Connection to Nexus 7 failed")
                return 0
        except Exception as e:
            print("Bluetooth Connection Error:", e)

            return 0


    def disconnect(self):
        try:
            self.android_sock.close()
            self.rpi_sock.close()
            print("Bluetooth device have been successfully disconnected")
        except:
            print("Failed to disconnect")

    #read message from android socket,
    def read(self):
        try:
            data = self.android_sock.recv(1024)
            return data
        except:
            pass

    #write to android socket
    def write(self, msg):
        try:
            self.android_sock.sendall(msg)
        except:
            pass


if __name__ == "__main__":
    start = BluetoothInterface()
    start.connect()
        
