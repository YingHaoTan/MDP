#CLIENT SIDE
import bluetooth

android_addr = "08:60:6E:AD:33:FC"

port = 1

android_socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
android_socket.connect((android_addr, port))
s
while(True):
    android_socket.send("data...")
    
android_socket.close()
 
