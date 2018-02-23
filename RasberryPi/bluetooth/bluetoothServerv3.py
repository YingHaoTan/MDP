import socket
import queue

hostMACAddress = '' # The MAC     address of a Bluetooth adapter on the server
port = 1 
size = 1024
rpi_sock=socket.socket(AF_BLUETOOTH,SOCK_STREAM,BTPROTO_RFCOMM)
rpi_sock.bind((hostMACAddress,port))
rpi_sock.listen(1)
send_window = queue.Queue()
recv_window = queue.Queue()

"""May consider passing in a specific address for rpi to connect to."""
def connect():
	try:
    	android_sock, android_address = s.accept()
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
		
		
def receive_msg_from_android():
	"""not sure whether to implement a except to note for interruption between comunication.
	Maybe have another function that deals with problems if not OK packet is received back?"""
    data = android_sock.recv(size)
    return (data)
        
            
def send_msg_to_android(msg):
	android_sock.send(msg)
	
if __name__ == "__main__":
	conn = connect()
	while conn != 1:
		connect()
		
	
	
		
            
	
"""Receiving acknowledgement from android and sending acknowledgemnet to them"""