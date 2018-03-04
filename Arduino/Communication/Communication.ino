#include "communication.h"

#define BUFFER_SIZE                               32

// need to check if will overflow
uint8_t last_sent = 0;
uint8_t incomingBuffer[BUFFER_SIZE];
int bufferIndex = 0;

// timer
bool yetToReceiveAck = false;
bool alreadyReceived = false;
unsigned long timer = millis();
unsigned long timeout = 1000; // 250 milliseconds


void setup() {
  //The default is 8 data bits, no parity, one stop bit.
  Serial.begin(115200);
  pinMode(LED_BUILTIN, OUTPUT);
}

void loop() {
  if (Serial.available() > 0) {
    putIncomingUSBMessageToBuffer();
    int traversalIndex = 0;
    /*Serial.println(bufferIndex);
      Serial.println("TraversalIndex:");
      Serial.println(traversalIndex);*/
    while (bufferIndex > traversalIndex + 4) {
      if (incomingBuffer[traversalIndex] == '~' && incomingBuffer[traversalIndex + 4] == '!') {
        
        InstructionMessage instructMsg;
        memcpy(&instructMsg, &incomingBuffer[traversalIndex + 1], 3);
        /*Serial.println("ID received:");
         Serial.println(instructMsg.id);*/
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
          }

          bufferIndex = 0;
          break;
        }
        else {
          // Received wrong instruction
          //Serial.println("RECEIVED WRONG INSTRUCTION");
        }
        traversalIndex += 5;
      }
      else {
        traversalIndex++;
      }
    }
  }
  /*

    if(completedMove()){
  	sendStatusUpdate();
  	last_sent++;
    alreadyReceived = false;

    }
  */
  if (millis() > timer + timeout && yetToReceiveAck) {
    resendStatusUpdate();
  }


}

void putIncomingUSBMessageToBuffer() {
  uint8_t tmpBuffer[BUFFER_SIZE] = {0}; //not allocated
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
  }
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
  Serial.flush();

  //start_timer()
  timer = millis();
  yetToReceiveAck = true;
}

void incrementID(){
  last_sent = (last_sent+1)%126;  
}

void decrementID(){
  if(last_sent == 0){
    last_sent = 125;  
  }  
  else{
    last_sent = last_sent - 1;  
  }
}

