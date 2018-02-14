import socket

def PC_send():
	host = ''
	port = 5000
	
	clientsock = socket.socket()
	clientsock.connect((host,port))
	
	data = input("Command for robot: ")
	while data != "-1":
		clientsock.send(data.encode('utf-8'))
		feedback = clientsock.recv(1024).decode('utf-8')
		print("Received: "+ feedback)
		data = input("Command for robot: ")
		
	clientsock.close()

if __name__ == "__main__":	
	PC_send()
	
	