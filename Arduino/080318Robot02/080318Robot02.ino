#include <Streaming.h>
#include <SharpIR.h>
#include <DualVNH5019MotorShield.h>
#include "Settings.h"
#include "communication.h"
#include "RingBuffer.h"
RingBuffer usbBufferIn;

uint8_t last_sent = 0;
static int lastTicks[2] = {0, 0};
static int lastError;
static int totalErrors;

void setup() {
  Serial.begin(115200);
  RingBuffer_init(&usbBufferIn);
  //Serial.println("Robot: Hello World!");
  md.init();

  //Initialise Motor Encoder Pins, digitalWrite high to enable PullUp Resistors
  pinMode(m1EncA, INPUT);
  pinMode(m1EncB, INPUT);
  pinMode(m2EncA, INPUT);
  pinMode(m2EncB, INPUT);

  //Innitializes the Motor Encoders for Interrupts
  pciSetup(m1EncA);
  pciSetup(m1EncB);
  pciSetup(m2EncA);
  pciSetup(m2EncB);

  delay(2000);
  //Serial.println("Initializations Done");
}

void loop() {
//  stringCommands();
  commWithRPI();
//delay(2000);
}

//------------Functions for robot movements------------//
void goFORWARD(int noBlock) {
  int setBlocks = blockToTicks(noBlock);
  long lastTime = micros();
  int setSpdR = 300;
  int setSpdL = 306;
  resetMCounters();
  lastError = 0;
  totalErrors = 0;
  lastTicks[0] = 0;
  lastTicks[1] = 0;
  int i = 100;
  while(i < 301){
    if(micros() - lastTime > 50){
      md.setSpeeds(i, i+10);
      i++;
      lastTime = micros();
    }
  }

  lastTime = millis();
  delay(50);

  while (mCounter[0] < setBlocks && mCounter[1] < setBlocks) {
    if (millis() - lastTime > 100) {
      if(noBlock > 1)
        PIDControl(&setSpdR, &setSpdL, 100, 7, 15, 0); //Long distance      
      else {
        PIDControl(&setSpdR, &setSpdL, 140, 7, 15, 0); //By block     
      }
      lastTime = millis();
      md.setSpeeds(setSpdR, setSpdL);
    }
  }

  md.setBrakes(400, 400);
}

void goRIGHT(int angle) {
  int ticks = angleToTicks(angle) - 34;
  int setSpdR = -200;              //Right motor
  int setSpdL = 206;              //Left motor
  long lastTime = millis();
  lastError = 0;
  totalErrors = 0;
  lastTicks[0] = 0;
  lastTicks[1] = 0;
  resetMCounters();

  md.setSpeeds(setSpdR, setSpdL);
  delay(50);

  while (mCounter[0] < ticks && mCounter[1] < ticks) {
    if (millis() - lastTime > 100) {
      PIDControl(&setSpdR, &setSpdL, 150, 6, 15, 1);
      lastTime = millis();
      md.setSpeeds(setSpdR, setSpdL);
    }
  }

  md.setBrakes(400, 400);
}

void goLEFT(int angle) {
  int ticks = angleToTicks(angle) - 34;
  int setSpdR = 200;              //Right motor
  int setSpdL = -206;              //Left motor
  long lastTime = millis();
  lastError = 0;
  totalErrors = 0;
  lastTicks[0] = 0;
  lastTicks[1] = 0;
  resetMCounters();

  md.setSpeeds(setSpdR, setSpdL);
  delay(50);

  while (mCounter[0] < ticks && mCounter[1] < ticks) {
    if (millis() - lastTime > 100) {
      PIDControl(&setSpdR, &setSpdL, 150, 6, 15, -1);
      lastTime = millis();
      md.setSpeeds(setSpdR, setSpdL);
    }
  }

  md.setBrakes(400, 400);
//  resetMCounters();
}

//Direction(dr): -1 = left, 0 = straight, 1 = right
void PIDControl(int *setSpdR, int *setSpdL, int kP, int kI, int kD, int dr) {
  int adjustment;
  int error = (mCounter[1] - lastTicks[1]) - (mCounter[0] - lastTicks[0]);            //0 = right motor, 1 = left motor, lesser tick time mean faster
  int errorRate = error - lastError;  
  lastError = error;
  lastTicks[0] = mCounter[0];
  lastTicks[1] = mCounter[1];
  totalErrors += 2;                                                           //Add up total number of errors (for Ki)
  if (error != 0) {                                                           //if error exists
    adjustment = ((kP * error) - (kI * totalErrors) + (kD * errorRate))/100;
    if(dr == 1 || dr == -1){
      *setSpdR += -adjustment * dr;
      *setSpdL -= adjustment * dr;
    }
    else{
      *setSpdR += adjustment;
      *setSpdL -= adjustment;
    }
  }
}

//after turning, measure offset in ticks then left ticks - right ticks, / 2 then add to the ticks
void tickCorrection(int right, int left){
  //int leftDirection = invSignVal(left);
  //int rightDirection = invSignVal(right);
  if(left > 0){
    while(mCounter[0] < -right && mCounter[1] > -left){
      md.setSpeeds(100, -100);
    }
  }
  else if(right > 0){
    while(mCounter[0] > -right && mCounter[1] < -left){
      md.setSpeeds(-100, 100);
    }
  }
  md.setBrakes(400, 400);
  delay(300);
  resetMCounters();
}

int angleToTicks(long angle){
  return 17654 * angle / 1000;
}

int blockToTicks(int blocks){
  return (1183-98) * blocks;
}

//JM - Attempt 1 at making code cleaner
void calibrateRIGHT() {
  scanRIGHT(&irRightReadings[0]);
  int turnTicks = 0;
  while (irRightReadings[0] != irRightReadings[1]) {
    resetMCounters();
    
    turnTicks = (irRightReadings[0] - irRightReadings[1]) * 8;
    
    if(abs((irRightReadings[0] - irRightReadings[1]) == 1) && abs(turnTicks) > 2){
      turnTicks -= 1;
    }
//    Serial.println(turnTicks);
    if (turnTicks > 0) {
      while (mCounter[0] < abs(turnTicks) && mCounter[1] < abs(turnTicks)) {
        md.setSpeeds(-150, 150);
      }
    }
    else {
      while (mCounter[0] < abs(turnTicks) && mCounter[1] < abs(turnTicks)) {
        md.setSpeeds(150, -150);
      }
    }
    md.setBrakes(400, 400);
    delay(100);
    scanRIGHT(&irRightReadings[0]);
  }
}

//JM - Attempt 1 at making code cleaner
void calibrateFRONT() {
  scanFORWARD(&irFrontReadings[0]);
  int turnTicks = 0;
  while (irFrontReadings[2] != 9 && irFrontReadings[0] != 9) {
    resetMCounters();
    turnTicks = (irFrontReadings[0] - 9) * 20;
    if (turnTicks > 0) {
      while (mCounter[0] < abs(turnTicks) && mCounter[1] < abs(turnTicks)) {
        md.setSpeeds(200, 200);
      }
    }
    else {
      while (mCounter[0] < abs(turnTicks) && mCounter[1] < abs(turnTicks)) {
        md.setSpeeds(-200, -200);
      }
    }
    md.setBrakes(400, 400);
    delay(100);
    scanFORWARD(&irFrontReadings[0]);
  }
}


//------------Functions for IR Sensors------------//
void scanFORWARD(int *pData) {
  pData[0] = lfwdIrVal.distance(); //Left
  pData[1] = mfwdIrVal.distance(); // Middle
  pData[2] = rfwdIrVal.distance(); //Right
   //Serial << "FORWARD: <- Left: " << pData[0] << " () Mid: " << pData[1] << " -> Right: " << pData[2] << " \n" << endl;
}

void scanRIGHT(int *pData) {
  pData[0] = frgtIrVal.distance(); //Right Front
  pData[1] = brgtIrVal.distance(); //Right Back
   //Serial << "RIGHT: -> Right(Short): " << pData[0] << " -> Right(Long): " << pData[1] << " \n" << endl;
}

void scanLEFT() {
  irLeftReading = flftIrVal.distance();
   //Serial << "LEFT: <- Left(Long): " << irLeftReading << " \n" << endl;
}

void toBlocks(){
  scanFORWARD(&irFrontReadings[0]);
  scanLEFT();
  scanRIGHT(&irRightReadings[0]);

  // Put sensor readings here
  StatusMessage statusPayload;
  statusPayload.id = last_sent;
  statusPayload.front1 = shortIrVal((irFrontReadings[0] - lfwdIrOS) / 10);
  statusPayload.front2 = shortIrVal((irFrontReadings[1] - mfwdIrOS) / 10);
  statusPayload.front3 = shortIrVal((irFrontReadings[2] - rfwdIrOS) / 10);
  statusPayload.right1 = shortIrVal((irRightReadings[0] - frgtIrOS) / 10);
  statusPayload.right2 = shortIrVal((irRightReadings[1] - brgtIrOS) / 10);
  statusPayload.left1 = longIrVal((irLeftReading - flftIrOS) / 10);
  statusPayload.reached = 1;

  Message msg;
  msg.type = ARDUINO_UPDATE;
  memcpy(&msg.payload, &statusPayload, 8);

  uint8_t tmpOutBuffer[64] = {0};
  tmpOutBuffer[0] = '~';
  memcpy(&tmpOutBuffer[1], &msg, 9);
  tmpOutBuffer[10] = '!';


  // Serial << "----------" << endl;
  // Serial << "frontLEFT: " << tmpOutBuffer[3] << endl;
  // Serial << "frontMID: " << tmpOutBuffer[4] << endl;
  // Serial << "frontRIGHT: " << tmpOutBuffer[5] << endl;
  // Serial << "rightFRONT: " << tmpOutBuffer[6] << endl;
  // Serial << "rightBACK: " << tmpOutBuffer[7] << endl;
  // Serial << "left: " << tmpOutBuffer[8] << endl;
  // Serial << "----------" << endl;
  

 // Serial.write((uint8_t *)&tmpOutBuffer, sizeof(tmpOutBuffer));
  //Serial.flush();

}



//------------Functions for Motors------------//
void mEncoder(int motor, int setTick){
  //encState[motor] = digitalRead(encA[motor]);
  mCounter[motor]++;
  /*
  int direction = 0;
  if(encState[motor] != encLastState[motor]){          //Was there a change in state?
    if(digitalRead(encB[motor]) != encState[motor]){    //If EncA state is different from EncB
      direction = 1;                              //Then it's going forward so ++ ticks
    }
    else{
      direction = -1;                               //Else it is going in reverse so -- ticks
    }
  }
  */
    
  //encLastState[motor] = encState[motor];
  //Serial << "Inside mEncoder() for Motor: " << motor << " Ticks: " << mCounter[motor] << " " << mRev[motor] << " Direction " << direction << endl;
  //if(((mCounter[motor] % setTick) == 0 && (mCounter[motor]!=0))){
    //mRev[motor]++;
  //}
}

void resetMCounters() {
  mCounter[0] = 0;
  mCounter[1] = 0;
  //mRev[0] = 0;
  //mRev[1] = 0;
}

//ISR for Motor 1 Encoders
ISR(PCINT2_vect) {
  //flag[0] = 1;
  mEncoder(0, 2248);
}

//ISR for Motor 2 Encoders
ISR(PCINT0_vect) {
  //flag[1] = 1;
  mEncoder(1, 2248);
}

//Standard function to enable interrupts on any pins
void pciSetup(byte pin) {
  *digitalPinToPCMSK(pin) |= bit (digitalPinToPCMSKbit(pin));  // enable pin
  PCIFR  |= bit (digitalPinToPCICRbit(pin)); // clear any outstanding interrupt
  PCICR  |= bit (digitalPinToPCICRbit(pin)); // enable interrupt for the group
}



//------------Functions for communications------------//
void commWithRPI() {
  static int calCounter = 0;
  if (Serial.available() > 0) {
    putIncomingUSBMessageToBuffer();
    int traversalIndex = 0;
    uint8_t tmpInBuffer = 0;

    if (usbBufferIn.count >= 6) {

      if (RingBuffer_get( & usbBufferIn, & tmpInBuffer, 0) == true && tmpInBuffer == '~') {
        uint8_t messageType = 0;
        if (RingBuffer_get( & usbBufferIn, & tmpInBuffer, 1) == true) {
          messageType = tmpInBuffer;
        }

        if (messageType == ARDUINO_INSTRUCTION) {
          if (5 < usbBufferIn.count) {
            if (RingBuffer_get( & usbBufferIn, & tmpInBuffer, 5) == true && tmpInBuffer == '!') {

              InstructionMessage instructMsg;

              RingBuffer_get( & usbBufferIn, & instructMsg.id, 2);
              RingBuffer_get( & usbBufferIn, & instructMsg.action, 3);
              RingBuffer_get( & usbBufferIn, & instructMsg.obstacleInFront, 4);

              //if (last_sent == instructMsg.id && alreadyReceived == false) {

              alreadyReceived = true;
              yetToReceiveAck = false;
              switch (instructMsg.action) {
                case TURN_LEFT:
                  goLEFT(90);
                  delay(150);
                  calCounter++;
                  sendStatusUpdate();
                  incrementID();
                  alreadyReceived = false;
                  break;
                case TURN_RIGHT:
                  goRIGHT(90);
                  delay(150);
                  calCounter++;
                  sendStatusUpdate();
                  incrementID();
                  alreadyReceived = false;
                  break;
                case FORWARD:
                  goFORWARD(1);
                  delay(150);
                  calCounter++;
                  sendStatusUpdate();
                  incrementID();
                  alreadyReceived = false;
                  break;
                case CAL_CORNER:
                  calibrateRIGHT();
                  delay(100);
                  calibrateFRONT();
                  delay(100);
//                  if(irRightReading[0] != 9 || ir Right Reading[1] != 10){
                    goRIGHT(90);
                    delay(100);
                    calibrateFRONT();
                    delay(100);
                    goLEFT(90);
                    delay(100);
                    calibrateRIGHT();
//                  }
                  delay(150);
                  calCounter = 0;
                  sendStatusUpdate();
                  incrementID();
                  alreadyReceived = false;
                  break;

                case CAL_SIDE:
//                  scanRIGHT(&irRightReadings[0]);
                  if(calCounter >= 4 || ((irRightReadings[0]!=irRightReadings[1]) && abs(irRightReadings[0] - irRightReadings[1] <7))){
                    calibrateRIGHT();
                    calCounter = 0;
                  }
                  
                  if (irRightReadings[0] <= 7 || irRightReadings[0] >= 11  ){
                    goRIGHT(90);
                    calibrateFRONT();
                    goLEFT(90);
                  }
                  delay(150);
                  sendStatusUpdate();
                  incrementID();
                  break;

                case SCAN:
                  sendStatusUpdate(); 
                  incrementID();
                  alreadyReceived = false;
                  delay(150);
                  break;

                case START:
                  calibrateRIGHT();
                  sendStatusUpdate();
                  incrementID();
                  alreadyReceived = false;
                  delay(150);
                  break;
                case STOP:
                  yetToReceiveAck = false;
                  break;
              }
              RingBuffer_erase( & usbBufferIn, 6);
              //}

            } else {
              RingBuffer_pop( & usbBufferIn);
            }
          }
        } else if (messageType == ARDUINO_STREAM) {
          StreamMessage streamMsg;
          uint8_t payloadSize = 0;
          // may not matter
          RingBuffer_get( & usbBufferIn, & streamMsg.id, 2);
          RingBuffer_get( & usbBufferIn, & payloadSize, 3);

          uint8_t tmpPayload[payloadSize] = {
            0
          };

          //Serial.write(payloadSize);
          for (int i = 0; i < payloadSize; i++) {
            RingBuffer_get( & usbBufferIn, & (tmpPayload[i]), 4 + i);
          }
          memcpy(streamMsg.streamActions, & tmpPayload, payloadSize);

          // you have all your actions inside streamMsg.streamActions;

          RingBuffer_erase( & usbBufferIn, 5 + payloadSize);
        }
      } else {
        RingBuffer_pop( & usbBufferIn);
      }
    }
  }

  if (millis() > timer + timeout && yetToReceiveAck) {
    resendStatusUpdate();
  }
}

void stringCommands() {
  static int calCounter = 0;
  //int commands[] = {2, 1, 3, 1, 0};
  //int commands[] = {1, 2, 3, 2, 3, 1, 2, 3, 2, 3, 1, 0};
  //int commands[] = {1, 4, 1, 4, 1, 4, 1, 4, 1, 4, 1, 4, 1, 4, 1, 4, 1, 4, 1, 0};
  //int commands[] = {1, 1, 1, 1, 6, 2, 1, 4, 1, 2, 1, 4, 1, 1, 1, 3, 1, 1, 1, 1, 0};
  //int commands[] = {6,2,1,1,1,0};
//  int commands[] = {1,1,1,1,2,1,1,1,1,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,2,1,1,2,1,1,1,1,2,1,2,1,1,1,3,1,1,1,1,3,1,1,1,2,0};
//  int commands[] = {1,4,1,4,1,4,1,4,1,4,1,4,1,4,1,4,0};
int commands[] = {5};
  
  int threshold = 35;
  static int x;
  switch (commands[x]){
    case 1: 
            Serial.println("Moving forward");
            goFORWARD(1);
            calCounter++;
            break;

    case 2:
            Serial.println("Moving left");
            goLEFT(90);
            calCounter++;
            break;

    case 3:
            Serial.println("Moving right");
            goRIGHT(90);
            calCounter++;
            break;

    case 4:
            Serial.println("Calibrate Right");
            scanRIGHT(&irRightReadings[0]);
            if(calCounter >= 4 || ((irRightReadings[0]!=irRightReadings[1]) && abs(irRightReadings[0] - irRightReadings[1] <7))){
              calibrateRIGHT();
              calCounter = 0;
            }
            
            if (irRightReadings[0] <= 7 || irRightReadings[0] >= 11  ){
              goRIGHT(90);
              calibrateFRONT();
              goLEFT(90);
            }
            break;

    case 5:
            Serial.println("Doing Full Scan");
            
            scanFORWARD(&irFrontReadings[0]);
            scanLEFT();
            scanRIGHT(&irRightReadings[0]);
            
            if (irFrontReadings[0] - lfwdIrOS > threshold)
              Serial << 0 << " blocks away \n" << endl;
            else
              Serial << "Left Forward IR: " << shortIrVal((irFrontReadings[0] - lfwdIrOS) / 10) << " blocks away " << endl;
            
            if (irFrontReadings[1] - mfwdIrOS > threshold)
              Serial << 0 << " blocks away \n" << endl;
            else
              Serial << "Mid Forward IR: " << shortIrVal((irFrontReadings[1] - mfwdIrOS) / 10) << " blocks away " << endl;
            if (irFrontReadings[2] - rfwdIrOS > threshold)
              Serial << 0 << " blocks away \n" << endl;
            else
            Serial << "Right Forward IR: " << shortIrVal((irFrontReadings[2] - rfwdIrOS) / 10) << " blocks away \n" << endl;
            Serial << "Front Right IR: " << shortIrVal((irRightReadings[0] - frgtIrOS) / 10) << " blocks away " << endl;
            Serial << "Back Right IR: " << shortIrVal((irRightReadings[1] - brgtIrOS) / 10) << " blocks away \n" << endl;
            Serial << "Left Long IR: " << longIrVal((irLeftReading - flftIrOS) / 10) << " blocks away " << endl;
            break;
            
    case 6:
            Serial.println("Calibrate At Corner");
            calibrateRIGHT();
            delay(100);
            calibrateFRONT();
            delay(100);
            goRIGHT(90);
            delay(100);
            calibrateFRONT();
            delay(100);
            goLEFT(90);
            delay(100);
            calibrateRIGHT();
            calCounter = 0;
            break;
            
    case 7:
            Serial.println("Going backwards");
            md.setSpeeds(-300, -300);
            delay(1500);
            md.setBrakes(400, 400);
            resetMCounters();
            break;
  }
  delay(200);
  
  if(x <= sizeof(commands)/sizeof(int)){
    x++;
  }
}

//uint8_t minVal(uint8_t val) {
//  uint8_t newVal = val;
//  if (val >= 10) {
//    newVal = 9;
//  }
//  return newVal;
//}

uint8_t shortIrVal(uint8_t val) {
  uint8_t newVal = val;
  if (val >= 4) {
    newVal = 0;
  }
  return newVal;
}

uint8_t longIrVal(uint8_t val) {
  uint8_t newVal = val;
  if (val >= 7) {
    newVal = 0;
  }
  return newVal;
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
}

void resendStatusUpdate() {
  decrementID();
  sendStatusUpdate();
  incrementID();
}

void sendStatusUpdate() {
  int threshold = 35 ;
  scanFORWARD(&irFrontReadings[0]);
  scanLEFT();
  scanRIGHT(&irRightReadings[0]);
  
  // Put sensor readings here
  StatusMessage statusPayload;
  statusPayload.id = last_sent;
//  statusPayload.front1 = shortIrVal((irFrontReadings[0] - lfwdIrOS) / 10);
//  statusPayload.front2 = shortIrVal((irFrontReadings[1] - mfwdIrOS) / 10);
//  statusPayload.front3 = shortIrVal((irFrontReadings[2] - rfwdIrOS) / 10);
 if (irFrontReadings[0] - lfwdIrOS > threshold)
    statusPayload.front1 =  0;
  else
    statusPayload.front1 =  shortIrVal((irFrontReadings[0] - lfwdIrOS) / 10);
  
  if (irFrontReadings[1] - mfwdIrOS > threshold)
    statusPayload.front2 =  0;
  else
    statusPayload.front2 = shortIrVal((irFrontReadings[1] - mfwdIrOS) / 10);
  if (irFrontReadings[2] - rfwdIrOS > threshold)
    statusPayload.front3 = 0;
  else
    statusPayload.front3 =  shortIrVal((irFrontReadings[2] - rfwdIrOS) / 10);
  statusPayload.right1 = shortIrVal((irRightReadings[0] - frgtIrOS) / 10);
  statusPayload.right2 = shortIrVal((irRightReadings[1] - brgtIrOS) / 10);
  statusPayload.left1 = longIrVal((irLeftReading - flftIrOS) / 10);
  statusPayload.reached = 1;

 
            
  //Serial << irFrontReadings[1] << " " << irFrontReadings[0] << " " << irFrontReadings[2] << " " << irRightReadings[0] << " " << irRightReadings[1] << " " << irLeftReading << endl;
  //Serial << statusPayload.front1 << " " << statusPayload.front2 << " " << statusPayload.front3 << " " << statusPayload.right1 << " " << statusPayload.right2 << " " << statusPayload.left1 << endl;


  // Crafts message to send
  Message msg;
  msg.type = ARDUINO_UPDATE;
  memcpy(&msg.payload, &statusPayload, 8);

  uint8_t tmpOutBuffer[64] = {0};
  tmpOutBuffer[0] = '~';
  memcpy(&tmpOutBuffer[1], &msg, 9);
  tmpOutBuffer[10] = '!';

  Serial<<'~'<<(int)tmpOutBuffer[1]<<(int)tmpOutBuffer[2]<<(int)tmpOutBuffer[3]<<(int)tmpOutBuffer[4]<<(int)tmpOutBuffer[5]<<(int)tmpOutBuffer[6]<<(int)tmpOutBuffer[7]<<(int)tmpOutBuffer[8]<<(int)tmpOutBuffer[9]<<'!'<<endl;
  Serial.flush();

  //start_timer()
  timer = millis();
  yetToReceiveAck = true;
}

void incrementID() {
  last_sent = (last_sent + 1) % 10;
}

void decrementID() {
  if (last_sent == 0) {
    last_sent = 9;
  }
  else {
    last_sent = last_sent - 1;
  }
}
