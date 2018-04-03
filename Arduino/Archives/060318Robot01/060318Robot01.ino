#include <Streaming.h>
#include <SharpIR.h>
#include <DualVNH5019MotorShield.h>
#include "Settings.h"
#include "communication.h"
#include "RingBuffer.h"
#include <math.h>
RingBuffer usbBufferIn;

uint8_t last_sent = 0;

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
    //goFORWARD(12);
  //goFORWARDObst(10,0);

  //goFORWARD(1);

  //calibrateRIGHT();
  //goRIGHT(90);
  //calibrateFRONT();
  //goLEFT(90);
  //calibrateRIGHT();
}

void loop() {
  //stringCommands();
  commWithRPI();
}

void goFront(int noBlocks){
  while(mRev[0] < noBlocks && mRev[1] < noBlocks){
    md.setSpeeds(300, 300);
    Serial << "During: M1-mRev1:" << mRev[0] << "-mCounter:" << mCounter[0] << " M2-mRev2:" << mRev[1] << "-mCounter:" << mCounter[1] << endl;
  }
  Serial << "After: M1-mRev1:" << mRev[0] << "-mCounter:" << mCounter[0] << " M2-mRev2:" << mRev[1] << "-mCounter:" << mCounter[1] << endl;
  md.setBrakes(400, 400);
}

//------------Functions for robot movements------------//
void goFORWARD(int noBlock) {
  int kP = 100;
  int kI = 6;
  int kD = 15;
  int error = 0;
  int errorRate = 0;
  int adjustment = 0;
  int totalErrors = 0;
  int lastError = 0;
  int lastTicks[2] = {0, 0};
  int setSpd1 = 300;              //Right motor
  int setSpd2 = 306;              //Left motor
  long lastTime = millis();
  resetMCounters();
  md.setSpeeds(setSpd1, setSpd2);
  delay(50);

  while (mRev[0] < noBlock && mRev[1] < noBlock) {
    if (millis() - lastTime > 100) {
      lastTime = millis();
      Serial.println("----");                       //Note: setSpeeds(mRIGHT, mLEFT)
      error = (mCounter[1] - lastTicks[1]) - (mCounter[0] - lastTicks[0]);            //0 = right motor, 1 = left motor, lesser tick time mean faster
      lastTicks[0] = mCounter[0];
      lastTicks[1] = mCounter[1];
      errorRate = error - lastError;                                                  //Counting change in error (for Kd)
      lastError = error;                                                              //Reassign the previous error as current error
      totalErrors += 2;                                                           //Add up total number of errors (for Ki)
      if (abs(error) > 0) {                                                           //if error exists
        adjustment = kP * error - kI * totalErrors + kD * errorRate;
        adjustment /= 100;
        setSpd1 += adjustment;
        setSpd2 -= adjustment;
        md.setSpeeds(setSpd1, setSpd2);
        //          Serial << "Setting speeds to:" << "M1 Speed: " << setSpd1 << " M2 Speed: " << setSpd2 << endl;
      }
    }
  }
  md.setBrakes(400, 400);
  resetMCounters();
}

int angleToTicks(long angle){
  return 17654 * angle / 1000;
}

void goRIGHT(int angle) {
  int ticks = angleToTicks(angle) - 30;
  int kP = 150;
  int kI = 6;
  int kD = 15;
  int error = 0;
  int errorRate = 0;
  int adjustment = 0;
  int totalErrors = 0;
  int lastError = 0;
  int lastTicks[2] = {0, 0};
  int setSpd1 = -200;              //Right motor
  int setSpd2 = 206;              //Left motor
  long lastTime = millis();
  resetMCounters();
  md.setSpeeds(setSpd1, setSpd2);
  delay(10);
      // Right                  Left
  while (mCounter[0] < ticks && mCounter[1] < ticks) {
    if (millis() - lastTime > 50) {
      lastTime = millis();
      Serial.println("----");                       //Note: setSpeeds(mRIGHT, mLEFT)
      error = (mCounter[1] - lastTicks[1]) - (mCounter[0] - lastTicks[0]);            //0 = right motor, 1 = left motor, lesser tick time mean faster
      lastTicks[0] = mCounter[0];
      lastTicks[1] = mCounter[1];
      errorRate = error - lastError;                                                  //Counting change in error (for Kd)
      lastError = error;                                                              //Reassign the previous error as current error
      totalErrors += 2;                                                           //Add up total number of errors (for Ki)
      if (abs(error) > 0) {                                                           //if error exists
        adjustment = kP * error - kI * totalErrors + kD * errorRate;
        adjustment /= 100;
        setSpd1 += -adjustment;
        setSpd2 -= adjustment;

        md.setSpeeds(setSpd1, setSpd2);
                  Serial << "Setting speeds to:" << "M1 Speed: " << setSpd1 << " M2 Speed: " << setSpd2 << endl;
      }
    }
  }
  md.setBrakes(400, 400);
  resetMCounters();
}

void goLEFT(int angle) {
  int ticks = angleToTicks(angle) - 30;
  int kP = 150;
  int kI = 6;
  int kD = 15;
  int error = 0;
  int errorRate = 0;
  int adjustment = 0;
  int totalErrors = 0;
  int lastError = 0;
  int lastTicks[2] = {0, 0};
  int setSpd1 = 200;              //Right motor
  int setSpd2 = -206;              //Left motor
  long lastTime = millis();
  resetMCounters();
  md.setSpeeds(setSpd1, setSpd2);
  delay(10);
      // Right                  Left
  while (mCounter[0] < ticks && mCounter[1] < ticks) {
    if (millis() - lastTime > 50) {
      lastTime = millis();
      Serial.println("----");                       //Note: setSpeeds(mRIGHT, mLEFT)
      error = (mCounter[1] - lastTicks[1]) - (mCounter[0] - lastTicks[0]);            //0 = right motor, 1 = left motor, lesser tick time mean faster
      lastTicks[0] = mCounter[0];
      lastTicks[1] = mCounter[1];
      errorRate = error - lastError;                                                  //Counting change in error (for Kd)
      lastError = error;                                                              //Reassign the previous error as current error
      totalErrors += 2;                                                           //Add up total number of errors (for Ki)
      if (abs(error) > 0) {                                                           //if error exists
        adjustment = kP * error - kI * totalErrors + kD * errorRate;
        adjustment /= 100;
        setSpd1 += adjustment;
        setSpd2 -= -adjustment;

        md.setSpeeds(setSpd1, setSpd2);
                  Serial << "Setting speeds to:" << "M1 Speed: " << setSpd1 << " M2 Speed: " << setSpd2 << endl;
      }
    }
  }
  md.setBrakes(400, 400);
  resetMCounters();
}

void PIDControl(int *setSpdR, int *setSpdL) {
  int kP = 3;
  int kI = 0;
  int kD = 3;
  int error;
  int errorRate;
  int adjustment;
  //int totalErrors;
  int lastError;
  int lastTicks[2];

  error = (mCounter[1] - lastTicks[1]) - (mCounter[0] - lastTicks[0]);            //0 = right motor, 1 = left motor, lesser tick time mean faster
  lastTicks[0] = mCounter[0];
  lastTicks[1] = mCounter[1];
  errorRate = error - lastError;
  lastError = error;
  //totalErrors += error;

  if (error > 5) {
    //adjustment = ((abs(error)*kP/10) + (errorRate*kD/10) + (totalErrors*kI/10))/2;
    adjustment = ((abs(error) * kP / 10) + (errorRate * kD / 10)) / 2;
    *setSpdR += adjustment;
    *setSpdL -= adjustment;

    //Serial << "RIGHT faster by: " << error << " m1Ticks: " << mCounter[0] << "m2Ticks: " << mCounter[1] << endl;
    //Serial << "Final adjustment: " << adjustment << endl;
  }

  else if (error < -5) {
    //adjustment = ((abs(error)*kP/10) + (errorRate*kD/10) + (totalErrors*kI/10))/2;
    adjustment = ((abs(error) * kP / 10) + (errorRate * kD / 10)) / 2;
    *setSpdR -= adjustment;
    *setSpdL += adjustment;

    //Serial << "LEFT faster by: " << error << " m1Ticks: " << mCounter[0] << "m2Ticks: " << mCounter[1] << endl;
    //Serial << "Final adjustment: " << adjustment << endl;
  }
}

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

void calibratePos() {
  calibrateRIGHT();
  calibrateFRONT();
}

void calibrateRIGHT() {
  int calibrateF = 1;
  while (calibrateF == 1) {
    scanRIGHT(&irRightReadings[0]);
    int turnTicks = 0;
    resetMCounters();

    if ((abs(irRightReadings[0] - irRightReadings[1]) > 0)) {
      //Serial.println("Calibrating Right");
      turnTicks = (irRightReadings[0] - irRightReadings[1]) * 20;
      if (turnTicks > 0) {
        //Serial << "Moving right abit of ticks: " << turnTicks << endl;
        while (mCounter[0] < abs(turnTicks) && mCounter[1] < abs(turnTicks)) {
          md.setSpeeds(-300, 300);
        }
      }
      else {
        //Serial << "Moving left abit of ticks: " << turnTicks << endl;
        while (mCounter[0] < abs(turnTicks) && mCounter[1] < abs(turnTicks)) {
          md.setSpeeds(300, -300);
        }
      }
      md.setBrakes(400, 400);
      delay(100);
    }
    else {
      //Serial.println("Right Calibration complete");
      calibrateF = 0;
    }
  }
}

//This part need to Optimize
void calibrateFRONT() {
  int calibrate = 1;
  while (calibrate == 1) {
    scanFORWARD(&irFrontReadings[0]);
    resetMCounters();
    // crude way to measure distance from front if all three read 0
    if (irFrontReadings[2] != 10 && irFrontReadings[1] != 10 ) {
      int turnTicks = 0;
      //Serial.println("Calibrating Front");
      turnTicks = (irFrontReadings[1] - 9) * 30;
      //Increase speed if further from block, reverse if less
      if (turnTicks > 0) {
        while (mCounter[0] < abs(turnTicks) && mCounter[1] < abs(turnTicks)) {
          md.setSpeeds(300, 300);
        }
      }
      else {
        while (mCounter[0] < abs(turnTicks) && mCounter[1] < abs(turnTicks)) {
          md.setSpeeds(-300, -300);
        }
      }
      md.setBrakes(400, 400);
      delay(100);
    }
    else {
      //Serial.println("Front Calibration complete");
      calibrate = 0;
    }
  }
}



//------------Functions for checklists------------//
void goFORWARDObst(int blocks, int diag) {
  for (int count = 0; count < blocks; count++) {
    scanFORWARD(&irFrontReadings[0]);
    
    if (irFrontReadings[1] > (0 + diag) && irFrontReadings[1] <= (10 + diag)) {
      if (diag) {
        //insert rotate 45 degree function below
        goLEFT(45);

        //insert move forward 
        goFORWARDCM(irFrontReadings[0]);
        goRIGHT(90);
        //move forward by some cm
        goFORWARDCM(irFrontReadings[0]);
        
        //turn 45 back
        goLEFT(45);
        
        count += 5;
      } else {
        Serial.println("In else");
        goLEFT(90);
        delay(500);
        goFORWARD(1);
        delay(500);
        goFORWARD(1);
        delay(500);
        goRIGHT(90);
        delay(500);
        goFORWARD(1);
        delay(500);
        goFORWARD(1);
        delay(500);
        goFORWARD(1);
        delay(500);
        goFORWARD(1);
        delay(500);
        goRIGHT(90);
        delay(500);
        goFORWARD(1);
        delay(500);
        goFORWARD(1);
        delay(500);
        goLEFT(90);
        count+=4;
      } 
    } else {
        goFORWARD(1);
    }
    delay(500);
  }
}

void goFORWARDCM(int lengthCM){
  int tptcm = 1183;
  int lenMove = 1183 * sqrt(2 * lengthCM *lengthCM) / 10;
  int kP = 100;
  int kI = 0;
  int kD = 8;
  int error = 0;
  int errorRate = 0;
  int adjustment = 0;
  int totalErrors = 0;
  int lastError = 0;
  int lastTicks[2] = {0, 0};
  int setSpd1 = 200;              //Right motor
  int setSpd2 = 202;              //Left motor
  long lastTime = millis();

  md.setSpeeds(setSpd1, setSpd2);
  delay(50);

  while (mCounter[0] < lenMove && mCounter[1] < lenMove) {
    if (millis() - lastTime > 100) {
      lastTime = millis();
      Serial.println("----");                       //Note: setSpeeds(mRIGHT, mLEFT)
      error = (mCounter[1] - lastTicks[1]) - (mCounter[0] - lastTicks[0]);            //0 = right motor, 1 = left motor, lesser tick time mean faster
      lastTicks[0] = mCounter[0];
      lastTicks[1] = mCounter[1];
      errorRate = error - lastError;                                                  //Counting change in error (for Kd)
      lastError = error;                                                              //Reassign the previous error as current error
      totalErrors += error;                                                           //Add up total number of errors (for Ki)
      if (abs(error) > 0) {                                                           //if error exists
        adjustment = kP * error + kI * totalErrors + kD * errorRate;
        adjustment /= 100;
        setSpd1 += adjustment;
        setSpd2 -= adjustment;
        md.setSpeeds(setSpd1, setSpd2);
      }
    }
  }
  
  md.setBrakes(400, 400);
  resetMCounters();
  
}



//------------Functions for IR Sensors------------//
void scanFORWARD(int *pData) {
  pData[0] = ir1.distance(); //Left
  pData[1] = ir2.distance(); // Middle
  pData[2] = ir3.distance(); //Right
  //Serial << "FORWARD: <- Left: " << pData[0] << " () Mid: " << pData[1] << " -> Right: " << pData[2] << endl;
}

void scanRIGHT(int *pData) {
  pData[0] = ir4.distance(); //Right Front
  pData[1] = ir6.distance(); //Right Back
  //Serial << "RIGHT: ->^ Right(Short): " << pData[0] << " ->v Right(Long): " << pData[1] << endl;
}

void scanLEFT() {
  irLeftReading = ir5.distance();
  //Serial << "LEFT: <-^ Left(Long): " << irLeftReading << endl;
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
  if(((mCounter[motor] % setTick) == 0 && (mCounter[motor]!=0))){
    mRev[motor]++;
  }
}

void resetMCounters() {
  mCounter[0] = 0;
  mCounter[1] = 0;
  mRev[0] = 0;
  mRev[1] = 0;
}

//ISR for Motor 1 Encoders
ISR(PCINT2_vect) {
  flag[0] = 1;
  mEncoder(0, 1183);
}

//ISR for Motor 2 Encoders
ISR(PCINT0_vect) {
  flag[1] = 1;
  mEncoder(1, 1183);
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
                  delay(1000);
                  calCounter++;
                  sendStatusUpdate();
                  incrementID();
                  alreadyReceived = false;
                  break;
                case TURN_RIGHT:
                  goRIGHT(90);
                  delay(1000);
                  calCounter++;
                  sendStatusUpdate();
                  incrementID();
                  alreadyReceived = false;
                  break;
                case FORWARD:
                  goFORWARD(1);
                  delay(1000);
                  calCounter++;
                  sendStatusUpdate();
                  incrementID();
                  alreadyReceived = false;
                  break;
                case CAL_CORNER:
                  calibrateRIGHT();
                  goRIGHT(90);
                  calibrateFRONT();
                  goLEFT(90);
                  calibrateRIGHT();
                  delay(1000);
                  calCounter = 0;
                  sendStatusUpdate();
                  incrementID();
                  alreadyReceived = false;
                  break;

                case CAL_SIDE:
                  scanRIGHT(&irRightReadings[0]);
                  if(calCounter == 6 || (irRightReadings[0]!=irRightReadings[1])){
                    calibrateRIGHT();
                    calCounter = 0;
                  }
                  delay(1000);
                  sendStatusUpdate();
                  incrementID();
                  break;

                case SCAN:
                  sendStatusUpdate(); 
                  incrementID();
                  alreadyReceived = false;
                  break;

                case START:
                  calibrateRIGHT();
                  sendStatusUpdate();
                  incrementID();
                  alreadyReceived = false;
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

void toBlocks(){
  scanFORWARD(&irFrontReadings[0]);
  scanLEFT();
  scanRIGHT(&irRightReadings[0]);

  // Put sensor readings here
  StatusMessage statusPayload;
  statusPayload.id = last_sent;
  statusPayload.front1 = minVal((irFrontReadings[0] - offset1) / 10);
  statusPayload.front2 = minVal((irFrontReadings[1] - offset2) / 10);
  statusPayload.front3 = minVal((irFrontReadings[2] - offset3) / 10);
  statusPayload.right1 = minVal((irRightReadings[0] - offset4) / 10);
  statusPayload.right2 = minVal((irRightReadings[1] - offset6) / 10);
  statusPayload.left1 = minVal((irLeftReading - offset5) / 10);
  statusPayload.reached = 1;

  Message msg;
  msg.type = ARDUINO_UPDATE;
  memcpy(&msg.payload, &statusPayload, 8);

  uint8_t tmpOutBuffer[64] = {0};
  tmpOutBuffer[0] = '~';
  memcpy(&tmpOutBuffer[1], &msg, 9);
  tmpOutBuffer[10] = '!';

  /*
  Serial << "----------" << endl;
  Serial << "frontLEFT: " << tmpOutBuffer[3] << endl;
  Serial << "frontMID: " << tmpOutBuffer[4] << endl;
  Serial << "frontRIGHT: " << tmpOutBuffer[5] << endl;
  Serial << "rightFRONT: " << tmpOutBuffer[6] << endl;
  Serial << "rightBACK: " << tmpOutBuffer[7] << endl;
  Serial << "left: " << tmpOutBuffer[8] << endl;
  Serial << "----------" << endl;
  */

  Serial.print((int)tmpOutBuffer[3]);
  Serial.print((int)tmpOutBuffer[4]);
  Serial.println((int)tmpOutBuffer[5]);
 // Serial.write((uint8_t *)&tmpOutBuffer, sizeof(tmpOutBuffer));
  Serial.flush();

}

void stringCommands() {
  int commands[] = {5};
  static int x;
  switch (commands[x]){
    case 1: 
            Serial.println("Moving forward");
            goFORWARD(1);
            break;

    case 2:
            Serial.println("Moving left");
            goLEFT(90);
            break;

    case 3:
            Serial.println("Moving right");
            goRIGHT(90);
            break;

    case 4:
            Serial.println("Calibrating");
            scanRIGHT(&irRightReadings[0]);
            calibrateRIGHT();
            break;

    case 5:
            Serial.println("Doing Full Scan");
            //scanFORWARD(&irFrontReadings[0]);
            //scanLEFT();
            //scanRIGHT(&irRightReadings[0]);
            toBlocks();
            break;
            
    case 6:
            Serial.println("Accelerated movement");
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

uint8_t minVal(uint8_t val) {
  uint8_t newVal = val;
  if (val > 10) {
    newVal = 20;
  }
  return newVal;
}

void sendStatusUpdate() {
  scanFORWARD(&irFrontReadings[0]);
  scanLEFT();
  scanRIGHT(&irRightReadings[0]);

  // Put sensor readings here
  StatusMessage statusPayload;
  statusPayload.id = last_sent;
  statusPayload.front1 = minVal((irFrontReadings[0] - offset1) / 10);
  statusPayload.front2 = minVal((irFrontReadings[1] - offset2) / 10);
  statusPayload.front3 = minVal((irFrontReadings[2] - offset3) / 10);
  statusPayload.right1 = minVal((irRightReadings[0] - offset4) / 10);
  statusPayload.right2 = minVal((irRightReadings[1] - offset6) / 10);
  statusPayload.left1 = minVal((irLeftReading - offset5) / 10);
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
