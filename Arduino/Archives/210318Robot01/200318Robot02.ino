#include <Streaming.h>
#include <SharpIR.h>
#include <DualVNH5019MotorShield.h>
//#include <CustomMotorLib.h>
#include "communication.h"
#include "RingBuffer.h"
#include "Settings.h"

#ifdef DEBUG
#define D if(1)
#else
#define D if(0)                   //Change this: 1 = Debug mode, 0 = Disable debug prints
#endif

void setup() {
  Serial.begin(115200);
  RingBuffer_init(&usbBufferIn);
  D Serial.println("Robot: Hello World!");
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
  D Serial.println("Initializations Done");

  /**/
  //md.setSpeeds(350, 350);
  //delay(2000);
  //md.setBrakes(400, 400);
  /**/
}


void loop() {
  if(commands[0] != 0){
    stringCommands();
  }
  else{
    commWithRPI();
  }
}


void emergencyBrake(){
  irFrontReadings[1] = mfwdIrVal.distance();
  if(irFrontReadings[1] < 7){
    md.setBrakes(400, 400);
  }
}

//------------Functions for robot movements------------//
void goFORWARD(int distance) {
  long lastTime = micros();
  int setSpdR = 400;                //Original: 300
  int setSpdL = 400;                //Original: 300
  resetMCounters();
  lastError = 0;
  totalErrors = 0;
  lastTicks[0] = 0;
  lastTicks[1] = 0;
  int i = 50;
  while (i < 301) {
    if (micros() - lastTime > 50) {
      md.setSpeeds(i, i + 10);
      i++;
      lastTime = micros();
    }
  }

  lastTime = millis();
  delay(50);

  if (distance <= 1192) {
    while (mCounter[0] < distance && mCounter[1] < distance) {
      if (millis() - lastTime > 100) {
        PIDControl(&setSpdR, &setSpdL, 150, 7, 30, 0); //By block
        
        lastTime = millis();
        md.setSpeeds(setSpdR, setSpdL);
        //emergencyBrake();
      }
    }
  } else {
    while (mCounter[0] < distance - 445 && mCounter[1] < distance - 445) {
      if (millis() - lastTime > 100) {
        PIDControl(&setSpdR, &setSpdL, 100, 7, 15, 0); //Long distance
        lastTime = millis();
        md.setSpeeds(setSpdR, setSpdL);
      }
    }
    i = 0;
    lastTime = micros();
    while (mCounter[0] < distance && mCounter[1] < distance) {
      if (micros() - lastTime > 50) {
        md.setSpeeds(setSpdR - i, setSpdL - i);
        i++;
        if (i > 100)
          i = 100;
        lastTime = micros();
      }
    }
  }
  md.setBrakes(400, 400);
}

void goRIGHT(int angle) {
  int setSpdR = -300;              //Right motor
  int setSpdL = 306;              //Left motor
  long lastTime = millis();
  lastError = 0;
  totalErrors = 0;
  lastTicks[0] = 0;
  lastTicks[1] = 0;
  resetMCounters();

  md.setSpeeds(setSpdR, setSpdL);
  delay(50);
  while (mCounter[0] < angleToTicks(angle) - turnRightTicks - 200 && mCounter[1] < angleToTicks(angle) - turnRightTicks - 200) {
    if (millis() - lastTime > 100) {
      PIDControl(&setSpdR, &setSpdL, 150, 6, 15, 1);
      lastTime = millis();
      md.setSpeeds(setSpdR, setSpdL);
    }
  }
  int i = 0;
  lastTime = micros();
  while (mCounter[0] < angleToTicks(angle) - turnRightTicks && mCounter[1] < angleToTicks(angle) - turnRightTicks) {
    if (micros() - lastTime > 50) {
      md.setSpeeds(setSpdR + i, setSpdL - i);
      i++;
      if (i > 100)
        i = 100;
      lastTime = micros();
    }
  }
  md.setBrakes(400, 400);
}

void goLEFT(int angle) {
  int setSpdR = 300;              //Right motor
  int setSpdL = -315;              //Left motor
  long lastTime = millis();
  lastError = 0;
  totalErrors = 0;
  lastTicks[0] = 0;
  lastTicks[1] = 0;
  resetMCounters();

  md.setSpeeds(setSpdR, setSpdL);
  delay(50);

  while (mCounter[0] < angleToTicks(angle) - turnLeftTicks - 200 && mCounter[1] < angleToTicks(angle) - turnLeftTicks - 200) {
    if (millis() - lastTime > 100) {
      PIDControl(&setSpdR, &setSpdL, 150, 6, 15, -1);
      lastTime = millis();
      md.setSpeeds(setSpdR, setSpdL);
    }
  }
  int i = 0;
  lastTime = micros();
  while (mCounter[0] < angleToTicks(angle) - turnLeftTicks && mCounter[1] < angleToTicks(angle) - turnLeftTicks) {
    if (micros() - lastTime > 50) {
      md.setSpeeds(setSpdR - i, setSpdL + i);
      i++;
      if (i > 100)
        i = 100;
      lastTime = micros();
    }
  }
  md.setBrakes(400, 400);
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
    adjustment = ((kP * error) - (kI * totalErrors) + (kD * errorRate)) / 100;
    if (dr == 1 || dr == -1) {
      *setSpdR += -adjustment * dr;
      *setSpdL -= adjustment * dr;
    }
    else {
      *setSpdR += adjustment;
      *setSpdL -= adjustment;
    }
  }
}

void calibrateRIGHT() {
  scanRIGHT(&irRightReadings[0]);
  int turnTicks = 0;
  while (irRightReadings[0] != irRightReadings[1] && abs(irRightReadings[0] - irRightReadings[1] < 7)) {
    resetMCounters();

    turnTicks = (irRightReadings[0] - irRightReadings[1]) * 8;

    if (abs((irRightReadings[0] - irRightReadings[1]) == 1) && abs(turnTicks) > 2) {
      turnTicks -= 1;
    }
    if (turnTicks > 0) {
      while (mCounter[0] < abs(turnTicks) *5 && mCounter[1] < abs(turnTicks) *5) {
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

void calibrateFRONT() {
  scanFORWARD(&irFrontReadings[0]);
  int turnTicks = 0;
  while (irFrontReadings[2] != 10 && irFrontReadings[0] != 10) {
    resetMCounters();
    turnTicks = (irFrontReadings[0] - 10) * 20;
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

void calibrateCORNER() {
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
}

void calibrateRIGHTV2() {
  scanRIGHT(&irRightReadings[0]);
  int turnTicks = 0;
  Serial << "start" <<endl ;
  while (irRightReadings[0] % 10 != irRightReadings[1] % 10 && abs(irRightReadings[0] - irRightReadings[1]) < 20) {
    resetMCounters();
    int Fdist = irRightReadings[0];
    int Bdist = irRightReadings[1];
    if(Fdist / 10 == Bdist / 10) {
      if(Fdist < Bdist){
        Bdist = Bdist - (1 + Fdist % 10);
        Fdist = Fdist - (1 + Fdist % 10);
      }
      else {
        Fdist = Fdist - (1 + Bdist % 10);
        Bdist = Bdist - (1 + Bdist % 10);
      }
    }
    else if (abs(Fdist / 10 - Bdist / 10) >1){
      int cnt = 1 + 10 * (abs(Fdist / 10 - Bdist / 10) -1);
      if(Fdist < Bdist){
        Bdist = Bdist + (cnt - Fdist % 10);
        Fdist = Fdist + (cnt - Fdist % 10);
      }
      else {
        Fdist = Fdist + (cnt - Bdist % 10);
        Bdist = Bdist + (cnt - Bdist % 10);
      }
    }
    turnTicks = (Fdist % 10 - Bdist % 10) * 8;
    if (abs((Fdist - Bdist) % 10) == 1 && abs(turnTicks) > 2) {
      turnTicks -= 1;
    }
    if (turnTicks > 0) {
      while (mCounter[0] < abs(turnTicks) *5 && mCounter[1] < abs(turnTicks) *5) {
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

void calibrateFRONTV2() {
  scanFORWARD(&irFrontReadings[0]);
  int turnTicks = 0;
  while (irFrontReadings[2] != 10 && irFrontReadings[0] != 10) {
    resetMCounters();
    turnTicks = (irFrontReadings[0] - 10) * 20;
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

void fwdCorrection(){
  //if(mvmtCounter[0] % 2 == 0){
    md.setM1Speed(-394);
    delay(6);
    md.setBrakes(400,400);
    resetMCounters();
  //}
}


int angleToTicks(long angle) {
  if (angle == 90)
    return 16800 * angle / 1000;
  else
     return 17280 * angle / 1000;
}

int blockToTicks(int blocks) {
  if (blocks == 1)
    return (1183 - forwardOffsetTicks) * blocks;
  else
    return 1192 * blocks;
}



//------------Functions for IR Sensors------------//
void scanFORWARD(int *pData) {
  pData[0] = lfwdIrVal.distance(); //Left
  delay(2);
  pData[1] = mfwdIrVal.distance(); // Middle
  delay(2);
  pData[2] = rfwdIrVal.distance(); //Right
  delay(2);
  D Serial << "FORWARD: <- Left: " << pData[0] << " () Mid: " << pData[1] << " -> Right: " << pData[2] << " \n" << endl;
}

void scanRIGHT(int *pData) {
  pData[0] = frgtIrVal.distance(); //Right Front
  delay(2);
  pData[1] = brgtIrVal.distance(); //Right Back
  delay(2);
  D Serial << "RIGHT: -> Right(Short): " << pData[0] << " -> Right(Long): " << pData[1] << " \n" << endl;
}

void scanLEFT() {
  irLeftReading = flftIrVal.distance();
  delay(2);
  D Serial << "LEFT: <- Left(Long): " << irLeftReading << " \n" << endl;
}



//------------Functions for Motors------------//
void resetMCounters() {
  mCounter[0] = 0;
  mCounter[1] = 0;
}

//ISR for Motor 1 (Right) Encoders
ISR(PCINT2_vect) {
  mCounter[0]++;
}

//ISR for Motor 2 (Left) Encoders
ISR(PCINT0_vect) {
  mCounter[1]++;
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

              alreadyReceived = true;
              yetToReceiveAck = false;
              switch (instructMsg.action) {
                case TURN_LEFT:
                  goLEFT(90);
                  delay(RPIExpDelay);
                  calCounter++;
                  sendStatusUpdate();
                  incrementID();
                  alreadyReceived = false;
                  break;
                case TURN_RIGHT:
                  goRIGHT(90);
                  delay(RPIExpDelay);
                  calCounter++;
                  sendStatusUpdate();
                  incrementID();
                  alreadyReceived = false;
                  break;
                case FORWARD:
                  goFORWARD(blockToTicks(1));
                  delay(RPIExpDelay);
                  mvmtCounter[0]++;
                  fwdCorrection();
                  calCounter++;
                  sendStatusUpdate();
                  incrementID();
                  alreadyReceived = false;
                  break;
                case CAL_CORNER:
                  calibrateCORNER();
                  delay(RPIExpDelay);
                  calCounter = 0;
                  sendStatusUpdate();
                  incrementID();
                  alreadyReceived = false;
                  break;

                case CAL_SIDE:
                  if (calCounter >= 4 || ((irRightReadings[0] != irRightReadings[1]) && abs(irRightReadings[0] - irRightReadings[1] < 7))) {
                    calibrateRIGHT();
                    if ((irRightReadings[0] <= 9 || irRightReadings[0] >= 11) && abs(irRightReadings[0] - irRightReadings[1] < 7)) {
                      goRIGHT(90);
                      calibrateFRONT();
                      goLEFT(90);
                      calibrateRIGHT();
                    }
                  }
                  calCounter = 0;
                  delay(RPIExpDelay);
                  sendStatusUpdate();
                  incrementID();
                  alreadyReceived = false;
                  break;

                case SCAN:
                  delay(RPIExpDelay);
                  sendStatusUpdate();
                  incrementID();
                  alreadyReceived = false;
                  
                  break;

                case START:
                  //                  calibrateRIGHT();
                  delay(RPIExpDelay);
                  sendStatusUpdate();
                  incrementID();
                  alreadyReceived = false;
                  break;

                case STOP:
                  yetToReceiveAck = false;
                  break;

                case TURN_ABOUT:
                  goLEFT(180);
                  delay(RPIExpDelay);
                  sendStatusUpdate();
                  incrementID();
                  alreadyReceived = false;
                  break;

                case CAL_ANY:
                  if(abs(irRightReadings[0] - irRightReadings[1]) < 20 &&abs(irRightReadings[0] - irRightReadings[1]) > 5)
                    calibrateRIGHTV2();
                  else
                    calibrateRIGHT();
                  delay(RPIExpDelay);
                  sendStatusUpdate();
                  incrementID();
                  alreadyReceived = false;
                  break;
				case UNEEDED_TURN_LEFT:
					// if(not within range){ turn_left(); }
					// else { doNotAdjust = true; }
					
					sendStatusUpdate();
					incrementID();
					alreadyReceived = false;
					break;
				case UNEEDED_TURN_RIGHT:
					// if(not within range){ turn_right() }
					// else { doNotAdjust = true; }
					
					sendStatusUpdate();
					incrementID();
					alreadyReceived = false;
					break;
				case UNEEDED_ABOUT_TURN:
					// if(not within range){ about_turn() }
					// else { doNotAdjust = true; }
					
					sendStatusUpdate();
					incrementID();
					alreadyReceived = false;
					break;
				case UNEEDED_CAL_CORNER:
					// if(not within range && doNotAdjust == false){ cal_corner() }
					
					doNotAdjust = false;
					sendStatusUpdate();
					incrementID();
					alreadyReceived = false;
					break;
				
              }
              RingBuffer_erase( & usbBufferIn, 6);

            } else {
              RingBuffer_pop( & usbBufferIn);
            }
          }
        } else if (messageType == ARDUINO_STREAM) {
          StreamMessage streamMsg;
          uint8_t payloadSize = 0;
          // may not matter
          RingBuffer_get(&usbBufferIn, &streamMsg.id, 2);
          RingBuffer_get(&usbBufferIn, &payloadSize, 3);

          uint8_t tmpInBuffer;
          if (4 + payloadSize <= usbBufferIn.count) {
            if (RingBuffer_get(&usbBufferIn, &tmpInBuffer, 4 + payloadSize) == true && tmpInBuffer == '!') {
              uint8_t tmpPayload[payloadSize] = {0};
              for (int i = 0; i < payloadSize; i++) {
                RingBuffer_get(&usbBufferIn, &(tmpPayload[i]), 4 + i);
              }
              memcpy(streamMsg.streamActions, &tmpPayload, payloadSize);

              for (int i = 0; i < payloadSize; i++) {
                int forwardCount = 0;
                uint8_t action = streamMsg.streamActions[i];


                switch (action) {
                  case FORWARD:
                    forwardCount = 1;
                    while (true) {
                      if ((i + 1) < payloadSize && streamMsg.streamActions[i + 1] == FORWARD) {
                        forwardCount++;
                        i++;
                      }
                      else {
                        break;
                      }
					  if(i+1 == payloadSize){
						// minus half
					  }
                    }
                    goFORWARD(blockToTicks(forwardCount));
                    delay(RPIFPDelay);
                    break;

                  case TURN_RIGHT:
                    goRIGHT(90);
                    delay(RPIFPDelay);
                    break;

                  case TURN_LEFT:
                    goLEFT(90);
                    delay(RPIFPDelay);
                    break;
                  
                  case TURN_ABOUT:
                    goLEFT(180);
                    delay(RPIFPDelay);
                  break;
                }
              }
			        sendStatusUpdate();
              incrementID();
              alreadyReceived = false;
              RingBuffer_erase(&usbBufferIn, payloadSize + 5);
            }
          }
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
  static int x;
  switch (commands[x]) {
    case 1:
      Serial.println("Moving forward");
      goFORWARD(blockToTicks(1));
      mvmtCounter[0]++;
      fwdCorrection();
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
      if (calCounter >= 4 || ((irRightReadings[0] != irRightReadings[1]) && abs(irRightReadings[0] - irRightReadings[1] < 7))) {
        calibrateRIGHT();
        if ((irRightReadings[0] <= 9 || irRightReadings[0] >= 11) && abs(irRightReadings[0] - irRightReadings[1] < 7)) {
          goRIGHT(90);
          calibrateFRONT();
          goLEFT(90);
          calibrateRIGHT();
        }
      }
      calCounter = 0;
      break;

    case 5:
      Serial.println("Doing Full Scan");
      scanFORWARD(&irFrontReadings[0]);
      scanLEFT();
      scanRIGHT(&irRightReadings[0]);
      Serial << "Left Forward IR: " << shortIrVal(irFrontReadings[0], 4, 34, lfwdIrOS) << " blocks away, actual: " << irFrontReadings[0] << endl;
      Serial << "Mid Forward IR: " << shortIrVal(irFrontReadings[1], 4, 35, mfwdIrOS) << " blocks away, actual: " << irFrontReadings[1] << endl;
      Serial << "Right Forward IR: " << shortIrVal(irFrontReadings[2], 4, 34, rfwdIrOS) << " blocks away, actual: " << irFrontReadings[2] << endl;
      Serial << "Front Right IR: " << shortIrVal(irRightReadings[0], 4, 34, frgtIrOS) << " blocks away, actual: " << irRightReadings[0] << endl;
      Serial << "Back Right IR: " << shortIrVal(irRightReadings[1], 4, 34, brgtIrOS) << " blocks away, actual: " << irRightReadings[1] << endl;
      Serial << "Left Long IR: " << longIrVal(irLeftReading, 7, 65, flftIrOS) << " blocks away, actual: " << irLeftReading << endl;
      break;

    case 6:
      Serial.println("Calibrate At Corner");
      calibrateCORNER();
      calCounter = 0;
      break;

    case 7:
      Serial.println("About Turn");
      goLEFT(180);
      calCounter++;
      break;
      
    case 8:
      Serial.println("Forward burst");
      goFORWARD(blockToTicks(burstMovBlocks));
      calCounter++;
      break;

    case 9:
      Serial.println("Calibrate At Any blocks");
      scanRIGHT(&irRightReadings[0]);
      if(abs(irRightReadings[0] - irRightReadings[1]) < 20 &&abs(irRightReadings[0] - irRightReadings[1]) > 5){
                    Serial << "V2" << endl;
                    calibrateRIGHTV2();
                  }else{
                    Serial << "Normal" << endl;
                    calibrateRIGHT();
                  }
      break;
  }
  delay(commandsDelay);

  if (x <= sizeof(commands) / sizeof(int)) {
    x++;
  }
}

uint8_t shortIrVal(int val, int blockThreshold, int cmThreshold, int offset) {
  
  int newVal = (val - offset )/ 10;
  if (val < 10){
    return 1;
  }
  if (newVal >= blockThreshold || val >= cmThreshold) {
    newVal = 0;
  }
  return newVal;
}

uint8_t longIrVal(int val, int blockThreshold, int cmThreshold, int offset) {
  uint8_t newVal = val / 10;
  if(val <= 20){
    newVal = 1;
  }

  else if (newVal >= blockThreshold || val >= cmThreshold) {
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

  delay(200);

  scanFORWARD(&irFrontReadings[0]);
  scanLEFT();
  scanRIGHT(&irRightReadings[0]);

  // Put sensor readings here
  StatusMessage statusPayload;
  statusPayload.id = last_sent;
  statusPayload.front1 = shortIrVal(irFrontReadings[0], 4, 34, lfwdIrOS);
  statusPayload.front2 = shortIrVal(irFrontReadings[1], 4, 35, mfwdIrOS);
  statusPayload.front3 = shortIrVal(irFrontReadings[2], 4, 34, rfwdIrOS);
  statusPayload.right1 = shortIrVal(irRightReadings[0], 4, 34, frgtIrOS);
  statusPayload.right2 = shortIrVal(irRightReadings[1], 4, 34, brgtIrOS);
  statusPayload.left1 = longIrVal(irLeftReading , 7, 65,flftIrOS);
  statusPayload.reached = 1;

  // Crafts message to send
  Message msg;
  msg.type = ARDUINO_UPDATE;
  memcpy(&msg.payload, &statusPayload, 8);

  uint8_t tmpOutBuffer[64] = {0};
  tmpOutBuffer[0] = '~';
  memcpy(&tmpOutBuffer[1], &msg, 9);
  tmpOutBuffer[10] = '!';

  Serial << '~' << (int)tmpOutBuffer[1] << (int)tmpOutBuffer[2] << (int)tmpOutBuffer[3] << (int)tmpOutBuffer[4] << (int)tmpOutBuffer[5] << (int)tmpOutBuffer[6] << (int)tmpOutBuffer[7] << (int)tmpOutBuffer[8] << (int)tmpOutBuffer[9] << '!' << endl;
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
