import socket
import queue


android_addr = "08:60:6E:AD:33:FC"
port = 3
android_sock = socket.socket(socket.AF_BLUETOOTH, socket.SOCK_STREAM, socket.BTPROTO_RFCOMM)
send_window = queue.Queue()
ack_window = queue.Queue()
check_window = queue.Queue()# after data is send. It is placed inside this
#window to check. Only when acknowledgement is received, then only will
#they be removed from the window
#should we have a queue for user input. Should be android

def connect():
	try:
		android_sock.connect((android_addr,port))
		print('Connection successful')
		return 1
		
	except:
	    print("Bluetooth Connection Error") 
    	android_sock.close()
    	return 0
    	
    	
def disconnect():
	try:
		android_sock.close()
		print("Bluetooth device have been successfully disconnected")
		
	except:
		print("Failed to disconnect")
		
##send message to server	
def send_msg(msg):
	try:
		android_sock.send(msg)
		#check_window.put(msg)
		return 1
	except:
		print('Failed to send '+ msg)
		return 0

###read_ack() work by pulling data from check_window and checking it against
###the ack received back.
"""		
def read_ack():
	try:
		data = android_sock.recv(size)
		#inner loop to try the queue for check_window
			try:
				if !(check_window.empty()):
					check_data = check_window.get()
				else:
					while
		print('Message have been received properly by the server')
		return 1
	except:
		return 0
	

	data = android_sock.recv(size)
	while !(recv_window.full()):
		recv_window.put(data)
		data = android_sock.recv(size)
"""

def recv_msg(msg):
	try:
		android_sock.recv(size)
		return 1
	except:
		return 0
		
if __name__ == "__main__":
	conn = connect()
	
	while conn != 1:
		connect()
		conn = connect()
