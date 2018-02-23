import socket
import queue
#import thread
#import time

hostMACAddress = '08:60:6E:AD:33:FC' # The MAC     address of a Bluetooth adapter on the server
port = 1 
size = 1024
rpi_sock=socket.socket(AF_BLUETOOTH,SOCK_STREAM,BTPROTO_RFCOMM)
rpi_sock.bind((hostMACAddress,port))
rpi_sock.listen(1)
send_window = queue.Queue()
recv_window = queue.Queue()
#send_window_to_algo = queue.Queue(). Not sure if this should be implemented by multithreading or here
#recv_window_from_algo = queue.Queue()

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
		
"""		
def receive_msg():
	#not sure whether to implement a except to note for interruption between comunication.
	#Maybe have another function that deals with problems if not OK packet is received back?
    data = android_sock.recv(size)
    
    return (data)
        
"""

def receive_msg():
	data = android_sock.recv(size)
	while data != q && !(recv_window.full()):
		recv_window.put(data)
		data = android_sock.recv(size)
"""        
def send_msg(msg):
	android_sock.send(msg)
"""
def send_msg(msg):
#not sure if condition is required. Because by default, block should be true
	while !(send_window.empty()):
		send_window.get()
		android_sock.send(msg)
		
if __name__ == "__main__":
	conn = connect()
	
	while conn != 1:
		connect()
		conn = connect()
		
	response = receive_msg()
		
	##should have two threads each running send and recv functions,
	##So what? Externally, we should have 2 functions to manage android and algo communication
	##Internally, we should have 2 threads to manage send and recv?
		
		
	
	
		
            
	
"""Receiving acknowledgement from android and sending acknowledgemnet to them"""