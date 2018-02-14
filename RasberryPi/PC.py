import socket
import ArduinoInterface
from functools import reduce

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
	received = []
	message_end = False
	while True:
		while not message_end:
			data = clientsock.recv(1024)                 
			for i in range(len(data)):
				# if new line
				if(data[i] == 126):
					message_end = True
					break
				received.append(data[i].to_bytes(1, byteorder='big'))
				
			
		
		# send to Arduino
		
		# ONLY RECEIVES THESE TWO THINGS FROM PC = ARDUINO_INSTRUCTION((byte)(0x02)), ANDROID_UPDATE((byte)0x05);
		#to_send_arduino = "";
		if(received[0] == (2).to_bytes(1, byteorder='big')):
			#to_send_arduino += chr(int(received[1]))
			#to_send_arduino += chr(int(received[2]))
			to_arduino_queue.put(received[1:3])#to_send_arduino)
			
		received = []
		message_end = False

		# receives from Arduino
		if(not from_arduino_queue.empty()):
			string_to_send_tcp = from_arduino_queue.get()
			# Ends with a ~
			string_to_send_tcp += "~"
			print("sending to PC")
			clientsock.send(string_to_send_tcp.encode("ascii"))
		
		

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
		to_send_bytes = to_arduino_queue.get()
		to_send_bytes.insert(0, bytes("~", "ascii"))
		#package = "~" + to_send
		#package += "!"
		to_send_bytes.append(bytes("!", "ascii"))
		
		#bytes_to_send_arduino = bytes(package, "ascii")
		arduino.write_to_arduino(b''.join(to_send_bytes))
		
		
		temp = arduino.read_from_arduino()
		string_to_send_tcp = ""
		for i in range(len(temp)):
			string_to_send_tcp += temp[i].decode("ascii")			
		
		from_arduino_queue.put(string_to_send_tcp)
		
		
		
	
	
if __name__ == "__main__":
	to_arduino_queue = Queue()
	from_arduino_queue = Queue()
	t1 = Thread(target=PC_recv, args=(to_arduino_queue,from_arduino_queue))
	t2 = Thread(target=Arduino_Thread, args=(to_arduino_queue,from_arduino_queue))
	t1.start()
	t2.start()