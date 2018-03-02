import bluetooth

android_addr = "08:60:6E:AD:33:FC"

port = 1

android_socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
android_socket.connect((android_addr, port))

def connect():

def disconnect():

def receive_msg():
    data = android_sock.recv(size)

def send_msg(msg):
    android_sock.send(msg)

