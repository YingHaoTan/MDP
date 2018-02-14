import socket

def PC_recv():
	host = ''
	port = 5000
	
	serversock = socket.socket()#create a new socket object
	serversock.bind((host, port))#bind socket
	
	serversock.listen(1)
	
	clientsock, clientaddr = serversock.accept()
	print ("Connection from: " + str(clientaddr))
	
	while True:
		data = clientsock.recv(1024).decode('utf-8')
		if data == "-1":
			break
			
		print ("received: " + data)
		print ("sending: "+ data)
		
		#this part can be use to send data back
		clientsock.send(data.encode('utf-8'))#echo
		
	clientsock.close()
	
if __name__ == "__main__":
	PC_recv()