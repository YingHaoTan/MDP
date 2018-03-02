#to implement without the library look up http://blog.kevindoran.co/bluetooth-programming-with-python-3/
import bluetooth

#SERVER SIDE	
	

def __init__(self,port,host):
	#after getting bluetooth address, now must implement to transfer data.
	#implement server sidsse
	self.rpi_socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
	#assign hostmac address for bluetooth adapters if needed as may have multiple adapters
	self.port = port
	self.host = host
	self.rpi_socket.bind((host,port))
	self.rpi_socket.listen(1)
	self.size = 1024
	
def connect():
    try:
        self.android_sock, self.android_address = rpi_socket.accept()
	if(self.android_address== '' ):
		print("Accepted Connection from: " + self.android_sock + self.android_address)
		return 1
	else:
	 	print("Connection is not the nexus 7") 
		continue
    except:
	print("Failed conncetion") 
        return 1

    except:
        print("Bluetooth Connection Error")
        self.android_sock.close()
        self.rpi_sock.close()
        return 0


def disconnect():
    try:
        self.android_sock.close()
        self.rpi_sock.close()
        print("Bluetooth device have been successfully disconnected")
    except:
        print("Failed to disconnect")

def receive_msg():
	data = self.android_sock.recv(self.size)
	return data


def send_msg(msg):
    	self.android_sock.send(msg)









	
