#include "communication.h"
#include "RingBuffer.h"

#define BUFFER_SIZE                               256

// need to check if will overflow
RingBuffer usbBufferIn;


uint8_t last_sent = 0;
uint8_t incomingBuffer[BUFFER_SIZE];
int bufferIndex = 0;

// timer
bool yetToReceiveAck = false;
bool alreadyReceived = false;
bool stopFlag = false;
unsigned long timer = millis();
unsigned long timeout = 1000; // 250 milliseconds


void setup() {
  //The default is 8 data bits, no parity, one stop bit.
  Serial.begin(115200);
  pinMode(LED_BUILTIN, OUTPUT);
  RingBuffer_init(&usbBufferIn);
}

void loop() {
  if (Serial.available() > 0) {
    putIncomingUSBMessageToBuffer();
    int traversalIndex = 0;
    uint8_t tmpInBuffer = 0;
    
    if (usbBufferIn.count >= 6) {
      
      if (RingBuffer_get(&usbBufferIn, &tmpInBuffer, 0) == true && tmpInBuffer == '~') {
        uint8_t messageType = 0;
        if (RingBuffer_get(&usbBufferIn, &tmpInBuffer, 1) == true) {
          messageType = tmpInBuffer;
        }
        if (messageType == ARDUINO_INSTRUCTION) {
          if (5 < usbBufferIn.count) {
            if (RingBuffer_get(&usbBufferIn, &tmpInBuffer, 5) == true && tmpInBuffer == '!') {
              
              InstructionMessage instructMsg;
              
              RingBuffer_get(&usbBufferIn, &instructMsg.id , 2);
              RingBuffer_get(&usbBufferIn, &instructMsg.action, 3);
              RingBuffer_get(&usbBufferIn, &instructMsg.obstacleInFront, 4);
          
              if (last_sent == instructMsg.id && alreadyReceived == false) {
                
                alreadyReceived = true;
                yetToReceiveAck = false;
                switch (instructMsg.action) {
                  case TURN_LEFT:
                    //turnLeft();
                    //delay(1000);
                    sendStatusUpdate();
                    incrementID();
                    alreadyReceived = false;
                    break;
                  case TURN_RIGHT:
                    //turnRight();
                    //delay(1000);
                    sendStatusUpdate();
                    incrementID();
                    alreadyReceived = false;
                    break;
                  case FORWARD:
                    //moveForward();
                    //delay(1000);
                    sendStatusUpdate();
                    incrementID();
                    alreadyReceived = false;
                    break;
                  case SCAN:
                    sendStatusUpdate();
                    incrementID();
                    alreadyReceived = false;
                    break;
                  case START:
                    sendStatusUpdate();
                    incrementID();
                    alreadyReceived = false;
                    break;
                  case STOP:
                    //Serial.flush();
                    yetToReceiveAck = false;
                }
                RingBuffer_erase(&usbBufferIn, 6);
              }
              
            } else {
              RingBuffer_pop(&usbBufferIn);
            }
          }
        } else if(messageType == ARDUINO_STREAM){
            StreamMessage streamMsg;
            uint8_t payloadSize = 0;
            // may not matter
            RingBuffer_get(&usbBufferIn, &streamMsg.id, 2);
            RingBuffer_get(&usbBufferIn, &payloadSize, 3);

            uint8_t tmpPayload[payloadSize] = {0};

            //Serial.write(payloadSize);
            for(int i = 0; i < payloadSize; i++){
              RingBuffer_get(&usbBufferIn, &(tmpPayload[i]), 4+i);
            }
            memcpy(streamMsg.streamActions, &tmpPayload, payloadSize);

            // you have all your actions inside streamMsg.streamActions;   

            RingBuffer_erase(&usbBufferIn, 5 + payloadSize);
                       
          }
      } else {
        RingBuffer_pop(&usbBufferIn);
      }
    }
    /*else if (messageType == ARDUINO_STREAM) {
      //Stuck at this part
      uint8_t length1 = (uint8_t)incomingBuffer[traversalIndex + 3];
      if (length1 == 32) {
        digitalWrite(LED_BUILTIN, HIGH);
      }
      }
      else {
      traversalIndex++;
      }
      }
      else {
      RingBuffer_pop(&usbBufferIn);
      }
      }*/
  }
  /*

    if(completedMove()){
  	sendStatusUpdate();
  	last_sent++;
    alreadyReceived = false;

    }
  */
  if ((millis() > timer + timeout) && yetToReceiveAck) {
    resendStatusUpdate();
  }


}

void putIncomingUSBMessageToBuffer() {
  uint8_t tmpBuffer[1024] = {0}; //not allocated
  uint8_t length = 0;

  while (Serial.available()) {
    tmpBuffer[length] = Serial.read();
    length++;
  }
  if (length) {
    for (uint16_t i = 0; i < length; i++) {
      RingBuffer_push(&usbBufferIn, tmpBuffer[i]);
    }
  }


  /*uint8_t tmpBuffer[BUFFER_SIZE] = {0}; //not allocated
    uint8_t length = 0;

    while (Serial.available()) {
    tmpBuffer[length] = Serial.read();
    length++;
    }
    for (uint8_t i = 0; i < length; i++) {
    incomingBuffer[bufferIndex] = tmpBuffer[i];
    bufferIndex++;

    // If buffer is not enough, go back to the front
    if (bufferIndex == BUFFER_SIZE) {
      bufferIndex = 0;
    }
    }*/
}

void resendStatusUpdate() {
  decrementID();
  sendStatusUpdate();
  incrementID();
}


void sendStatusUpdate() {
  // Put sensor readings here
  StatusMessage statusPayload;
  statusPayload.id = last_sent;
  statusPayload.front1 = 0;
  statusPayload.front2 = 0;
  statusPayload.front3 = 0;
  statusPayload.right1 = 0;
  statusPayload.right2 = 0;
  statusPayload.left1 = 0;
  statusPayload.reached = 1;


  // Crafts message to send
  Message msg;
  msg.type = ARDUINO_UPDATE;
  memcpy(&msg.payload, &statusPayload, sizeof(statusPayload));

  uint8_t tmpOutBuffer[64] = {0};
  tmpOutBuffer[0] = '~';
  memcpy(&tmpOutBuffer[1], &msg, sizeof(msg));
  tmpOutBuffer[sizeof(msg) + 1] = '!';

  // Need to test
  Serial.write((byte *)&tmpOutBuffer, sizeof(tmpOutBuffer));
  //Serial.flush();

  //start_timer()
  timer = millis();
  yetToReceiveAck = true;
}

void incrementID() {
  last_sent = (last_sent + 1) % 126;
}

void decrementID() {
  if (last_sent == 0) {
    last_sent = 125;
  }
  else {
    last_sent = last_sent - 1;
  }
}

