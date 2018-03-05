# to implement without the library look up http://blog.kevindoran.co/bluetooth-programming-with-python-3/
import bluetooth


# SERVER SIDE
class BluetoothInterface:

    def __init__(self):
        # after getting bluetooth address, now must implement to transfer data.
        # implement server sidsse
        self.rpi_socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
        # assign hostmac address for bluetooth adapters if needed as may have multiple adapters
        self.port = self.rpi_socket.getsockname()[1] 
        self.host = ''



    def connect(self):
        try:
            self.rpi_socket.bind((self.host, self.port))
            self.rpi_socket.listen(1)
            uuid = '9f2c1227-5cc1-4685-b56a-2b7717cd8041'
            advertise_service( server_sock, "MDP-Server",
             service_id = uuid,
             service_classes = [ uuid, SERIAL_PORT_CLASS ],
             profiles = [ SERIAL_PORT_PROFILE ],
            # protocols = [ OBEX_UUID ]
             ) 
            print("Waiting for connection on RFCOMM channel %d" % self.port) 
            
            self.android_sock, self.android_address = self.rpi_socket.accept()
            if (self.android_address == '08:60:6E:AD:33:FC'):
                print("Accepted Connection from socket: " + self.android_sock + " Address: " + self.android_address)
                return 1
            else:
                print("Connection to Nexus 7 failed")
                return 0
        except:
            print("Bluetooth Connection Error")
            self.android_sock.close()
            self.rpi_sock.close()
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
            print("BT Read error")

    #write to android socket
    def write(self, msg):
        try:
            self.android_sock.send(msg)
        except:
            print("BT Send error")
                    
