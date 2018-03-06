import ArduinoInterface
import BluetoothInterface
import socket

from queue import Queue
from threading import Thread
import datetime
import time

class Main(object):

	def __init__(self):
		print('Opening bluetooth connection')
		self.android = BluetoothInterface.BluetoothInterface()
		self.android.connect()
		pass

 
	#get data from queue and write to arduino, then get data from arduino and pass to queue
	def Arduino_Thread(self, to_arduino_queue, from_arduino_queue, port='COM13', baud_rate=115200):
		arduino = ArduinoInterface.ArduinoInterface(port = port, baud_rate = baud_rate)
		arduino.start_arduino_connection()
		while True:
			if(not to_arduino_queue.empty()):
				to_send_bytes = to_arduino_queue.get()
				to_send_bytes.insert(0, bytes("~", "ascii"))
				to_send_bytes.append(bytes("!", "ascii"))
				arduino.write_to_arduino(b''.join(to_send_bytes))
			
			temp = arduino.read_from_arduino()
			if temp is not None:
				string_to_send_tcp = ""
				print("Receives from Arduino:")
				print(temp)
				for i in range(len(temp)):
					string_to_send_tcp += temp[i].decode("ascii")			
				from_arduino_queue.put(string_to_send_tcp)
			
			time.sleep(0.001)
			

	def PC_Thread(self, to_arduino_queue, from_arduino_queue, to_pc_queue, host='', port=5000):
		serversock = socket.socket()	#create a new socket object
		serversock.bind((host, port))	#bind socket
		serversock.setblocking(False)
		serversock.settimeout(0.0)
		serversock.listen(1)
		print ("Listening")
		#clientsock, clientaddr = serversock.accept()
		#print ("Connection from: " + str(clientaddr))
		received = []
		message_end = False


		ARDUINO_INSTRUCTION = (2).to_bytes(1, byteorder='big')
		ARDUINO_STREAM = (3).to_bytes(1, byteorder='big')
		
		while True:
			try:
				clientsock, clientaddr = serversock.accept()
				clientsock.setblocking(False)
				clientsock.settimeout(0.0)
				print ("Connection from: " + str(clientaddr))
				break
			except:
				continue
				
		
		while True:
			while not message_end:
				try: 
					data = clientsock.recv(1024)           				
					for i in range(len(data)):
						# if new line
						if(data[i] == 126):
							message_end = True
							print("Received from TCP: " + str(datetime.datetime.now()))
							break
						received.append(data[i].to_bytes(1, byteorder='big'))
				except:
					break
				
			
			# sends to Arduino
			# ONLY RECEIVES THESE TWO THINGS FROM PC = ARDUINO_INSTRUCTION((byte)(0x02)), ARDUINO_STREAM((byte)(0x03)), ANDROID_UPDATE((byte)0x05);
			if message_end and len(received) > 0:
				if(received[0] == ARDUINO_INSTRUCTION):
					print("Sends to Arduino: " + str(datetime.datetime.now()))
					print(received)
					to_arduino_queue.put(received)
					
				elif (received[0] == ARDUINO_STREAM):
					print('Received Arduino stream :' + str(len(received)))
					print(received)
					to_arduino_queue.put(received)

				message_end = False
				received = []

			
			
			# sends to bluetooth
			'''if(received[0] == (5).to_bytes(1, byteorder='big')):
				to_android_queue.put()
			'''
			
			# receives from Arduino, doesn't block
			if(not from_arduino_queue.empty()):
				string_to_send_tcp = from_arduino_queue.get()
				print("Received from Arduino: " + str(datetime.datetime.now()))
				# Ends with a ~
				string_to_send_tcp += "~"
				print("sending to PC")
				clientsock.sendall(string_to_send_tcp.encode("ascii"))


			# receives from Bluetooth, doesn't block
			if(not to_pc_queue.empty()):
				print("sending to PC from Android")
				from_android_str = to_pc_queue.get()
				clientsock.sendall(from_android_str.encode("ascii"))
			

			time.sleep(0.001)
			#send to algo
			
			#receive from algo

		clientsock.close()
		
	def read_android(self, to_pc_queue):
		
		##Bluetooth work between android -> rpi -> algo/pc
		##FOWARD
		##receive data from client which will be taken from to_bluetooth_queue and then data will be passed into
		##to_pc_queue
		##BACKWARD
		##apparently no need to care about backward transmission for now.
		##After creating a connection at the other end of bluetoothComm is between rpi - android
		##Not sure where to use the send method according to what I am thinking I think only the client will use the
		##send function. Server will only be using the send function if there is backward communication going on.
		## | android - rpi -> bluetooth | pc/algo - rpi -> wifi | arduino - rpi -> USB
		## Communciation between each thread is through queue. 
		
		ANDROID_INSTRUCTION = (5).to_bytes(1, byteorder='big')
		
		while(True):
			data = self.android.read()
			
			to_send = ANDROID_INSTRUCTION + data
			to_send = to_send.decode("ascii")+'~'
			to_pc_queue.put(to_send)
			print(to_send)


	def write_android(self, to_android_queue):
		pass

	def threads_create(self):
		try: 		
			to_arduino_queue = Queue()
			from_arduino_queue = Queue()
			
			to_android_queue = Queue()
			from_android_queue = Queue()

			to_pc_queue = Queue()
			
			#to_algo_queue = Queue()
			#from_algo_queue = Queue()
			
			##think theres problem with the queue passed in. I think it should also pcqueue.
			##t1 need all the queue since it is at the center of the communication.
			t1 = Thread(target=self.PC_Thread, args=(to_arduino_queue,from_arduino_queue, to_pc_queue, '', 5000))

			serial_ports = ArduinoInterface.list_ports()
			for port in serial_ports:
				print(port)
				if 'ACM' in port or 'COM' in port:
					to_connect = port
			

			#t2 = Thread(target=self.Arduino_Thread, args=(to_arduino_queue,from_arduino_queue,to_connect, 115200))
			#t2 = Thread(target=self.Arduino_Thread, args=(to_arduino_queue,from_arduino_queue,'/dev/ttyACM0', 115200))
			
			


			read_android_thread = Thread(target=self.read_android, args=([to_pc_queue]))
			write_android_thread = Thread(target=self.write_android, args=([to_android_queue]))

			t1.start()
			#t2.start()
			read_android_thread.start()
			write_android_thread.start()
			
			#t3 = Thread(target = self.Bluetooth_Thread, args = (to_android_queue, from_android_queue, '', 1))

	
			

			#t3.start()
	
		except Exception as e:
			print(str(e))



if __name__ == "__main__":
	start = Main()
	start.threads_create()
