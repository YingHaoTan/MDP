import ArduinoInterface
import socket

from queue import Queue
from threading import Thread


class Main(object):

	def __init__(self):
		pass

 
	#get data from queue and write to arduino, then get data from arduino and pass to queue
	def Arduino_Thread(self, to_arduino_queue, from_arduino_queue, port='COM13', baud_rate=115200):
		arduino = ArduinoInterface.ArduinoInterface(port = port, baud_rate = baud_rate)
		arduino.start_arduino_connection()
		while True:
			to_send_bytes = to_arduino_queue.get()
			to_send_bytes.insert(0, bytes("~", "ascii"))
			to_send_bytes.append(bytes("!", "ascii"))
			arduino.write_to_arduino(b''.join(to_send_bytes))
			
			temp = arduino.read_from_arduino()
			string_to_send_tcp = ""
			for i in range(len(temp)):
				string_to_send_tcp += temp[i].decode("ascii")			
			from_arduino_queue.put(string_to_send_tcp)
			
			
	def PC_Thread(self, to_arduino_queue, from_arduino_queue, to_bluetooth_queue, from_bluetooth_queue, host='', port=5000):
		
		serversock = socket.socket()	#create a new socket object
		serversock.bind((host, port))	#bind socket
		
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
					
				
			
			# sends to Arduino
			# ONLY RECEIVES THESE TWO THINGS FROM PC = ARDUINO_INSTRUCTION((byte)(0x02)), ANDROID_UPDATE((byte)0x05);
			if(received[0] == (2).to_bytes(1, byteorder='big')):
				to_arduino_queue.put(received[1:4])
				message_end = False
				received = []
				
			
			
			# sends to bluetooth
			'''if(received[0] == (5).to_bytes(1, byteorder='big')):
				to_bluetooth_queue.put()
			'''
			
			# receives from Arduino, doesn't block
			if(not from_arduino_queue.empty()):
				string_to_send_tcp = from_arduino_queue.get()
				# Ends with a ~
				string_to_send_tcp += "~"
				print("sending to PC")
				clientsock.send(string_to_send_tcp.encode("ascii"))
			
			# receives from Bluetooth, doesn't block
			if(not from_bluetooth_queue.empty()):
				pass
			

		clientsock.close()
		
	def Bluetooth_Thread(self, to_bluetooth_queue, from_bluetooth_queue):
		# put your bluetooth stuffs here
		pass
 
	def threads_create(self):
		try: 		
			to_arduino_queue = Queue()
			from_arduino_queue = Queue()
			
			to_bluetooth_queue = Queue()
			from_bluetooth_queue = Queue()
			
			
			t1 = Thread(target=self.PC_Thread, args=(to_arduino_queue,from_arduino_queue, to_bluetooth_queue, from_bluetooth_queue, '', 5000))
			t2 = Thread(target=self.Arduino_Thread, args=(to_arduino_queue,from_arduino_queue,'COM13', 115200))
			t1.start()
			t2.start()
	
		except Exception as e:
			print(str(e))



if __name__ == "__main__":
	start = Main()
	start.threads_create()