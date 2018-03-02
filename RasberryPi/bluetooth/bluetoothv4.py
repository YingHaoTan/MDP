#to implement without the library look up http://blog.kevindoran.co/bluetooth-programming-with-python-3/
import bluetooth

target_name = "Nexus 7"
target_addr = "08:60:6E:AD:33:FC"


#SERVER SIDE	
		
#after getting bluetooth address, now must implement to transfer data.
#implement server sidsse
rpi_socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
#assign hostmac address for bluetooth adapters if needed as may have multiple adapters
port = 1
rpi_socket.bind(("",port))
rpi_socket.listen(1)


def connect():
    try:
        android_sock, android_address = rpi_socket.accept()
        print("Accepted Connection from: " + client + address)
        return 1

    except:
        print("Bluetooth Connection Error")
        android_sock.close()
        rpi_sock.close()
        return 0


def disconnect():
    try:
        android_sock.close()
        rpi_sock.close()
        print("Bluetooth device have been successfully disconnected")
    
    except:
        print("Failed to disconnect")

def receive_msg():
    data = android_sock.recv(size)

def send_msg(msg):
    android_sock.send(msg)









	
