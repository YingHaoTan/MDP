#to implement without the library look up http://blog.kevindoran.co/bluetooth-programming-with-python-3/
#WARNING!!! CODE IS NOT TESTED YET
import bluetooth

target_name = "Nexus 7"
target_addr = "08:60:6E:AD:33:FC"
count = 0

#function responsible for locating bluetooth devices finding its address
def searchBlue():

    global target_name
    global target_addr
    global count

    #search for device at least 3 times
    while(count<3):
        #logger.logger.debug('scanning for device...')
        nearby_device = bluetooth.discover_devices()


        #logger.logger.debug('searching for target_addr...')
        for blueaddr in nearby_device:
            if target_name == bluetooth.lookup_name(blueaddr) and target_addr == blueaddr:
                print ("located bluetooth device ", target_name, " : ", target_addr)

            else:
                #logger.logger.debug('search again...')
                count+=1
                searchBlue()

    return

#calling of function	
searchBlue()

if count == 3:
    print ("failed to locate device nearby")
		

#SERVER SIDE	
		
#after getting bluetooth address, now must implement to transfer data.
#implement server sidsse
RPI_socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
#assign hostmac address for bluetooth adapters if needed as may have multiple adapters
port = 1
rpi_socket.bind(("",port))
rpi_socket.listen(1)

android_socket, android_addr = rpi_socket.accept()
print("Accepted connection from ", android_addr)

#must have a condition to break. signal can be send through data to
#break connection?
#http://blog.kevindoran.co/bluetooth-programming-with-python-3/ for more info
while(True):
    data = rpi_socket.recv(1024)#recv max of 1024 char
    print ("received %s" % data)
    android_socket.send(data)
    # have to check to see if this if statement break in between when robot is idle
    if not data: 
    	break


android_socket.close()
rpi_socket.close()








	
