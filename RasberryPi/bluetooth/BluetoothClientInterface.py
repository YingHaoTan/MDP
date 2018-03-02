import bluetooth


class BluetoothClientInterface(object):
    
    def __init__(self, port, host):
        self.android_addr = "08:60:6E:AD:33:FC"
        self.port = 1 
        self.android_socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)

    def connect():
        try:
                self.android_socket.connect((self.android_addr, self.port))
        except: 
                print("Bluetooth Connection Error")
                android_sock.close()
                return 0
                
    def disconnect():
        try:
            self.android_socket.close()
            return 1
        except:
            print("Fail to disconnect")
            return 0

    def receive_msg():
        data = self.android_socket.recv(size)
        return data

    def send_msg(msg):
        self.android_socket.send(msg)

