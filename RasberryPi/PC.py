import socket
import ArduinoInterface


from queue import Queue
from threading import Thread


def PC_recv(to_arduino_queue, from_arduino_queue):
	host = ''
	port = 5000
	
	serversock = socket.socket()#create a new socket object
	serversock.bind((host, port))#bind socket
	

	serversock.listen(1)
	print ("Listening")
	clientsock, clientaddr = serversock.accept()
	print ("Connection from: " + str(clientaddr))
	received = ""
	message_end = False
	while True:
		while not message_end:
			data = clientsock.recv(1024)                 
			for i in range(len(data)):
				# if new line
				print(data[i])
				if(data[i] == 10):
					message_end = True
					break
				received += str(data[i])
			
		
		# send to Arduino
		to_arduino_queue.put(received)
		received = ""
		message_end = False
			
		# receives from Arduino
		if(not from_arduino_queue.empty):
			from_arduino_queue.get(block=False)
		# sends to Algo
		
		

	clientsock.close()
	
	"""while True:
		data = clientsock.recv(1024).decode('utf-8')
		if data == "-1":
			break
			
		print ("received: " + data)
		print ("sending: "+ data)
		
		#this part can be use to send data back
		clientsock.send(data.encode('utf-8'))#echo"""
		
def Arduino_Thread(to_arduino_queue, from_arduino_queue):
	arduino = ArduinoInterface.ArduinoInterface()
	arduino.start_arduino_connection()
	while True:
		to_send = to_arduino_queue.get()
		package = "~" + to_send
		package += "!"
		bytes_to_send = bytes(package, "ascii")
		arduino.write_to_arduino(bytes_to_send)
		arduino.read_from_arduino()
	
if __name__ == "__main__":
	to_arduino_queue = Queue()
	from_arduino_queue = Queue()
	#android_queue = Queue()
	t1 = Thread(target=PC_recv, args=(to_arduino_queue,from_arduino_queue))
	t2 = Thread(target=Arduino_Thread, args=(to_arduino_queue,from_arduino_queue))
	t1.start()
	t2.start()