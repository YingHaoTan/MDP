# 1 "c:\\Users\\Renzeydood\\Documents\\~NTU Stuffs\\2.2\\CE3004 - MDP Multidisiplinary Project\\MDP\\Arduino\\080318Robot02\\080318Robot02.ino"
# 1 "c:\\Users\\Renzeydood\\Documents\\~NTU Stuffs\\2.2\\CE3004 - MDP Multidisiplinary Project\\MDP\\Arduino\\080318Robot02\\080318Robot02.ino"
# 2 "c:\\Users\\Renzeydood\\Documents\\~NTU Stuffs\\2.2\\CE3004 - MDP Multidisiplinary Project\\MDP\\Arduino\\080318Robot02\\080318Robot02.ino" 2
# 3 "c:\\Users\\Renzeydood\\Documents\\~NTU Stuffs\\2.2\\CE3004 - MDP Multidisiplinary Project\\MDP\\Arduino\\080318Robot02\\080318Robot02.ino" 2
# 4 "c:\\Users\\Renzeydood\\Documents\\~NTU Stuffs\\2.2\\CE3004 - MDP Multidisiplinary Project\\MDP\\Arduino\\080318Robot02\\080318Robot02.ino" 2
# 5 "c:\\Users\\Renzeydood\\Documents\\~NTU Stuffs\\2.2\\CE3004 - MDP Multidisiplinary Project\\MDP\\Arduino\\080318Robot02\\080318Robot02.ino" 2
# 6 "c:\\Users\\Renzeydood\\Documents\\~NTU Stuffs\\2.2\\CE3004 - MDP Multidisiplinary Project\\MDP\\Arduino\\080318Robot02\\080318Robot02.ino" 2
# 7 "c:\\Users\\Renzeydood\\Documents\\~NTU Stuffs\\2.2\\CE3004 - MDP Multidisiplinary Project\\MDP\\Arduino\\080318Robot02\\080318Robot02.ino" 2
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
  pinMode(3 /*Microcontroller pin 5, PORTD, PCINT2_vect, PCINT19*/, 0x0);
  pinMode(5 /*Microcontroller pin 11, PORTD,PCINT2_vect, PCINT21*/, 0x0);
  pinMode(13 /*Microcontroller pin 17, PORTB, PCINT0_vect, PCINT3*/, 0x0);
  pinMode(11 /*Microcontroller pin 19, PORTB, PCINT0_vect, PCINT5*/, 0x0);

  //Innitializes the Motor Encoders for Interrupts
  pciSetup(3 /*Microcontroller pin 5, PORTD, PCINT2_vect, PCINT19*/);
  pciSetup(5 /*Microcontroller pin 11, PORTD,PCINT2_vect, PCINT21*/);
  pciSetup(13 /*Microcontroller pin 17, PORTB, PCINT0_vect, PCINT3*/);
  pciSetup(11 /*Microcontroller pin 19, PORTB, PCINT0_vect, PCINT5*/);

  delay(2000);
  //Serial.println("Initializations Done");
}

void loop() {
  stringCommands();
  //commWithRPI();
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
  int setSpdR = -200; //Right motor
  int setSpdL = 206; //Left motor
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
  int setSpdR = 200; //Right motor
  int setSpdL = -206; //Left motor
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
}

//Direction(dr): -1 = left, 0 = straight, 1 = right
void PIDControl(int *setSpdR, int *setSpdL, int kP, int kI, int kD, int dr) {
  int adjustment;
  int error = (mCounter[1] - lastTicks[1]) - (mCounter[0] - lastTicks[0]); //0 = right motor, 1 = left motor, lesser tick time mean faster
  int errorRate = error - lastError;
  lastError = error;
  lastTicks[0] = mCounter[0];
  lastTicks[1] = mCounter[1];
  totalErrors += 2; //Add up total number of errors (for Ki)
  if (error != 0) { //if error exists
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

void calibrateRIGHT() {
  scanRIGHT(&irRightReadings[0]);
  int turnTicks = 0;
  while (irRightReadings[0] != irRightReadings[1]) {
    resetMCounters();

    turnTicks = (irRightReadings[0] - irRightReadings[1]) * 8;

    if((((irRightReadings[0] - irRightReadings[1]) == 1)>0?((irRightReadings[0] - irRightReadings[1]) == 1):-((irRightReadings[0] - irRightReadings[1]) == 1)) && ((turnTicks)>0?(turnTicks):-(turnTicks)) > 2){
      turnTicks -= 1;
    }
    if (turnTicks > 0) {
      while (mCounter[0] < ((turnTicks)>0?(turnTicks):-(turnTicks)) && mCounter[1] < ((turnTicks)>0?(turnTicks):-(turnTicks))) {
        md.setSpeeds(-150, 150);
      }
    }
    else {
      while (mCounter[0] < ((turnTicks)>0?(turnTicks):-(turnTicks)) && mCounter[1] < ((turnTicks)>0?(turnTicks):-(turnTicks))) {
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
  while (irFrontReadings[2] != 9 && irFrontReadings[0] != 9) {
    resetMCounters();
    turnTicks = (irFrontReadings[0] - 9) * 20;
    if (turnTicks > 0) {
      while (mCounter[0] < ((turnTicks)>0?(turnTicks):-(turnTicks)) && mCounter[1] < ((turnTicks)>0?(turnTicks):-(turnTicks))) {
        md.setSpeeds(200, 200);
      }
    }
    else {
      while (mCounter[0] < ((turnTicks)>0?(turnTicks):-(turnTicks)) && mCounter[1] < ((turnTicks)>0?(turnTicks):-(turnTicks))) {
        md.setSpeeds(-200, -200);
      }
    }
    md.setBrakes(400, 400);
    delay(100);
    scanFORWARD(&irFrontReadings[0]);
  }
}

//------------Functions for Checklists------------//

void checkDistance(){
  scanLEFT();
  Serial << "Block is : " << irLeftReading/10 * 10 << " away" << endl;
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
  statusPayload.front1 = shortIrVal((irFrontReadings[0] - -3 /*Left forward IR*/) / 10);
  statusPayload.front2 = shortIrVal((irFrontReadings[1] - -5 /*Middle forward IR*/) / 10);
  statusPayload.front3 = shortIrVal((irFrontReadings[2] - -3 /*Right forward IR*/) / 10);
  statusPayload.right1 = shortIrVal((irRightReadings[0] - -2 /*Front right IR. The only long range IR.*/) / 10);
  statusPayload.right2 = shortIrVal((irRightReadings[1] - -2 /*Back right IR*/) / 10);
  statusPayload.left1 = longIrVal((irLeftReading - 1 /*Front left IR*/) / 10);
  statusPayload.reached = 1;

  Message msg;
  msg.type = 0x01;
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
# 313 "c:\\Users\\Renzeydood\\Documents\\~NTU Stuffs\\2.2\\CE3004 - MDP Multidisiplinary Project\\MDP\\Arduino\\080318Robot02\\080318Robot02.ino"
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
extern "C" void __vector_5 /* Pin Change Interrupt Request 1 */ (void) __attribute__ ((signal,used, externally_visible)) ; void __vector_5 /* Pin Change Interrupt Request 1 */ (void) {
  //flag[0] = 1;
  mEncoder(0, 2248);
}

//ISR for Motor 2 Encoders
extern "C" void __vector_3 /* Pin Change Interrupt Request 0 */ (void) __attribute__ ((signal,used, externally_visible)) ; void __vector_3 /* Pin Change Interrupt Request 0 */ (void) {
  //flag[1] = 1;
  mEncoder(1, 2248);
}

//Standard function to enable interrupts on any pins
void pciSetup(byte pin) {
  *(((pin) <= 7) ? (&(*(volatile uint8_t *)(0x6D))) : (((pin) <= 13) ? (&(*(volatile uint8_t *)(0x6B))) : (((pin) <= 21) ? (&(*(volatile uint8_t *)(0x6C))) : ((uint8_t *)0)))) |= (1UL << ((((pin) <= 7) ? (pin) : (((pin) <= 13) ? ((pin) - 8) : ((pin) - 14))))); // enable pin
  (*(volatile uint8_t *)((0x1B) + 0x20)) |= (1UL << ((((pin) <= 7) ? 2 : (((pin) <= 13) ? 0 : 1)))); // clear any outstanding interrupt
  (*(volatile uint8_t *)(0x68)) |= (1UL << ((((pin) <= 7) ? 2 : (((pin) <= 13) ? 0 : 1)))); // enable interrupt for the group
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

        if (messageType == 0x02) {
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
                case 0x03:
                  goLEFT(90);
                  delay(150);
                  calCounter++;
                  sendStatusUpdate();
                  incrementID();
                  alreadyReceived = false;
                  break;
                case 0x04:
                  goRIGHT(90);
                  delay(150);
                  calCounter++;
                  sendStatusUpdate();
                  incrementID();
                  alreadyReceived = false;
                  break;
                case 0x05:
                  goFORWARD(1);
                  delay(150);
                  calCounter++;
                  sendStatusUpdate();
                  incrementID();
                  alreadyReceived = false;
                  break;
                case 0x08:
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

                case 0x09:
                  if(calCounter >= 4 || ((irRightReadings[0]!=irRightReadings[1]) && ((irRightReadings[0] - irRightReadings[1] <7)>0?(irRightReadings[0] - irRightReadings[1] <7):-(irRightReadings[0] - irRightReadings[1] <7)))){
                    calibrateRIGHT();
                    calCounter = 0;
                  }

                  if (irRightReadings[0] <= 7 || irRightReadings[0] >= 11 ){
                    goRIGHT(90);
                    calibrateFRONT();
                    goLEFT(90);
                  }
                  delay(150);
                  sendStatusUpdate();
                  incrementID();
                  break;

                case 0x02:
                  sendStatusUpdate();
                  incrementID();
                  alreadyReceived = false;
                  delay(150);
                  break;

                case 0x01:
                  calibrateRIGHT();
                  sendStatusUpdate();
                  incrementID();
                  alreadyReceived = false;
                  delay(150);
                  break;
                case 0x07:
                  yetToReceiveAck = false;
                  break;
              }
              RingBuffer_erase( & usbBufferIn, 6);
              //}

            } else {
              RingBuffer_pop( & usbBufferIn);
            }
          }
        } else if (messageType == 0x03) {
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

int commands[] = {1,1,1,1,1,1,0};

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
            if(calCounter >= 4 || ((irRightReadings[0]!=irRightReadings[1]) && ((irRightReadings[0] - irRightReadings[1] <7)>0?(irRightReadings[0] - irRightReadings[1] <7):-(irRightReadings[0] - irRightReadings[1] <7)))){
              calibrateRIGHT();
              calCounter = 0;
            }

            if (irRightReadings[0] <= 7 || irRightReadings[0] >= 11 ){
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

            if (irFrontReadings[0] - -3 /*Left forward IR*/ > threshold)
              Serial << 0 << " blocks away \n" << endl;
            else
              Serial << "Left Forward IR: " << shortIrVal((irFrontReadings[0] - -3 /*Left forward IR*/) / 10) << " blocks away " << endl;

            if (irFrontReadings[1] - -5 /*Middle forward IR*/ > threshold)
              Serial << 0 << " blocks away \n" << endl;
            else
              Serial << "Mid Forward IR: " << shortIrVal((irFrontReadings[1] - -5 /*Middle forward IR*/) / 10) << " blocks away " << endl;
            if (irFrontReadings[2] - -3 /*Right forward IR*/ > threshold)
              Serial << 0 << " blocks away \n" << endl;
            else
            Serial << "Right Forward IR: " << shortIrVal((irFrontReadings[2] - -3 /*Right forward IR*/) / 10) << " blocks away \n" << endl;
            Serial << "Front Right IR: " << shortIrVal((irRightReadings[0] - -2 /*Front right IR. The only long range IR.*/) / 10) << " blocks away " << endl;
            Serial << "Back Right IR: " << shortIrVal((irRightReadings[1] - -2 /*Back right IR*/) / 10) << " blocks away \n" << endl;
            Serial << "Left Long IR: " << longIrVal((irLeftReading - 1 /*Front left IR*/) / 10) << " blocks away " << endl;
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
 if (irFrontReadings[0] - -3 /*Left forward IR*/ > threshold)
    statusPayload.front1 = 0;
  else
    statusPayload.front1 = shortIrVal((irFrontReadings[0] - -3 /*Left forward IR*/) / 10);

  if (irFrontReadings[1] - -5 /*Middle forward IR*/ > threshold)
    statusPayload.front2 = 0;
  else
    statusPayload.front2 = shortIrVal((irFrontReadings[1] - -5 /*Middle forward IR*/) / 10);
  if (irFrontReadings[2] - -3 /*Right forward IR*/ > threshold)
    statusPayload.front3 = 0;
  else
    statusPayload.front3 = shortIrVal((irFrontReadings[2] - -3 /*Right forward IR*/) / 10);
  statusPayload.right1 = shortIrVal((irRightReadings[0] - -2 /*Front right IR. The only long range IR.*/) / 10);
  statusPayload.right2 = shortIrVal((irRightReadings[1] - -2 /*Back right IR*/) / 10);
  statusPayload.left1 = longIrVal((irLeftReading - 1 /*Front left IR*/) / 10);
  statusPayload.reached = 1;



  //Serial << irFrontReadings[1] << " " << irFrontReadings[0] << " " << irFrontReadings[2] << " " << irRightReadings[0] << " " << irRightReadings[1] << " " << irLeftReading << endl;
  //Serial << statusPayload.front1 << " " << statusPayload.front2 << " " << statusPayload.front3 << " " << statusPayload.right1 << " " << statusPayload.right2 << " " << statusPayload.left1 << endl;


  // Crafts message to send
  Message msg;
  msg.type = 0x01;
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
# 1 "c:\\Users\\Renzeydood\\Documents\\~NTU Stuffs\\2.2\\CE3004 - MDP Multidisiplinary Project\\MDP\\Arduino\\080318Robot02\\RingBuffer.ino"
void RingBuffer_init(RingBuffer *_this)
{
    /*****

      The following clears:

        -> buf

        -> head

        -> tail

        -> count

      and sets head = tail

    ***/
# 11 "c:\\Users\\Renzeydood\\Documents\\~NTU Stuffs\\2.2\\CE3004 - MDP Multidisiplinary Project\\MDP\\Arduino\\080318Robot02\\RingBuffer.ino"
    memset (_this, 0, sizeof (*_this));
}

unsigned int RingBuffer_modulo_inc(const unsigned int value, const unsigned int modulus)
{
    unsigned int my_value = value + 1;
    if (my_value >= modulus)
    {
      my_value = 0;
    }
    return (my_value);
}

unsigned int RingBuffer_modulo_dec(const unsigned int value, const unsigned int modulus)
{
    unsigned int my_value = (0==value) ? (modulus - 1) : (value - 1);
    return (my_value);
}

uint8_t RingBuffer_empty(RingBuffer *_this)
{
    return (0==_this->count);
}

void RingBuffer_flush(RingBuffer *_this, uint8_t clearBuffer)
{
  _this->count = 0;
  _this->head = 0;
  _this->tail = 0;
  if (clearBuffer)
  {
    memset (_this->buf, 0, sizeof (_this->buf));
  }
}

bool RingBuffer_full(RingBuffer *_this)
{
    return (_this->count>=512);
}

uint8_t RingBuffer_pop(RingBuffer *_this)
{
    uint8_t c;
    if (_this->count>0)
    {
      c = _this->buf[_this->tail];
      _this->buf[_this->tail] = 0xfe;
      _this->tail = RingBuffer_modulo_inc (_this->tail, 512);
      --_this->count;
      return c;
    }
    return 0;
}

bool RingBuffer_get(RingBuffer *_this, uint8_t *buffer, uint16_t index)
{
    if (_this->count>0 && buffer){
       *buffer = _this->buf[(_this->tail+index) % 512];
       return true;
    }
    else{
      *buffer = 0;
      return false;
    }
}

void RingBuffer_push(RingBuffer *_this, uint8_t value)
{
    if (_this->count < 512)
    {
      _this->buf[_this->head] = value;
      _this->head = RingBuffer_modulo_inc (_this->head, 512);
      ++_this->count;
    }
    else
    {
      _this->buf[_this->head] = value;
      _this->head = RingBuffer_modulo_inc (_this->head, 512);
      _this->tail = RingBuffer_modulo_inc (_this->tail, 512);
    }
}

bool RingBuffer_erase(RingBuffer *_this, uint16_t range)
{
    if (range <= _this->count)
    {
        for(uint16_t i = 0; i < range; i++)
            RingBuffer_pop(_this);

//        _this->tail = (_this->tail + range) % RINGBUFFER_SIZE;
//        _this->count -= range;
      return true;
    }
    else
      return false;
}

bool RingBuffer_find(RingBuffer *_this, uint8_t value)
{
    if (_this)
    {
        for (uint16_t i = 0; i < _this->count; i++)
        {
            if (_this->buf[(_this->tail + i) % 512] == value)
                return true;
        }
    }
    return false;
}
