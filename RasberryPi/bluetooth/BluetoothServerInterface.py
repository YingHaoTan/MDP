#to implement without the library look up http://blog.kevindoran.co/bluetooth-programming-with-python-3/
import bluetooth

#SERVER SIDE	
	

def __init__(self,port,host):
	#after getting bluetooth address, now must implement to transfer data.
	#implement server sidsse
	self.rpi_socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
	#assign hostmac address for bluetooth adapters if needed as may have multiple adapters
	self.size = 1024
	self.port = port
	self.host = host
	try: 
		self.rpi_socket.bind((host,port))
		self.rpi_socket.listen(1)
	except:
		print("error binding") 
	
def connect(self):
    try:
        self.android_sock, self.android_address = rpi_socket.accept()
	if(self.android_address== '08:60:6E:AD:33:FC' ):
		print("Accepted Connection from socket: " + self.android_sock + " Address: "+ self.android_address)
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

def receive_msg(self):
	try: 
		data = self.android_sock.recv(self.size)
		return data
	except:
		print("BT Read error") 


def send_msg(self,msg):
	try:
    		self.android_sock.send(msg)
	except:
		print("BT Send error") 








	
