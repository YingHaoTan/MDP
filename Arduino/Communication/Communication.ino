#include "communication.h"

#define BUFFER_SIZE                               32

// need to check if will overflow
uint8_t last_sent = 0;
uint8_t to_receive = 0;
uint8_t incomingBuffer[BUFFER_SIZE];
int bufferIndex = 0;

// timer
bool yetToReceiveAck = false;
unsigned long timer = millis();
unsigned long timeout = 250; // 250 milliseconds


void setup() {
  //The default is 8 data bits, no parity, one stop bit.
  Serial.begin(115200);
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
          Serial.println(instructMsg.id);
          Serial.println("ACK received:");
          Serial.println(instructMsg.ack);*/
        if (to_receive == instructMsg.id && last_sent == instructMsg.ack) {
          switch (instructMsg.action) {
            case TURN_LEFT:
              break;
            case TURN_RIGHT:
              break;
            case FORWARD:
              break;
          }

          to_receive += 1;
          last_sent += 1;
          yetToReceiveAck = false;

          sendStatusUpdate();
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

    }
  */
  if (millis() > timer + timeout && yetToReceiveAck) {
    sendStatusUpdate();
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


void sendStatusUpdate() {
  // Put sensor readings here
  StatusMessage statusPayload;
  statusPayload.id = last_sent;
  statusPayload.front1 = 10;
  statusPayload.front2 = 11;
  statusPayload.front3 = 12;
  statusPayload.right1 = 13;
  statusPayload.right2 = 14;
  statusPayload.left1 = 12;
  statusPayload.reached = 1;
  statusPayload.ack = to_receive;


  // Crafts message to send
  Message msg;
  msg.type = ARDUINO_STATUS;
  memcpy(&msg.payload, &statusPayload, sizeof(statusPayload));

  uint8_t tmpOutBuffer[64] = {0};
  tmpOutBuffer[0] = '~';
  memcpy(&tmpOutBuffer[1], &msg, sizeof(msg));
  tmpOutBuffer[sizeof(msg) + 1] = '!';

  // Need to test
  Serial.write((byte *)&tmpOutBuffer, sizeof(tmpOutBuffer));

  //start_timer()
  timer = millis();
  yetToReceiveAck = true;
}

