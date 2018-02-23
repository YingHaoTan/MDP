#include <Streaming.h>
#include <SharpIR.h>
#include <DualVNH5019MotorShield.h>
#include "Constants.h"

boolean calibrateF = 1;

void setup() {
	Serial.begin(19200);
  Serial.println("Robot: Hello World!");
  md.init();
  
  //Initialise Motor Encoder Pins, digitalWrite high to enable PullUp Resistors
  pinMode(m1EncA, INPUT);
  digitalWrite(m1EncA, HIGH);
  pinMode(m1EncB, INPUT);
  digitalWrite(m1EncB, HIGH);
  pinMode(m2EncA, INPUT);
  digitalWrite(m2EncA, HIGH);
  pinMode(m2EncB, INPUT);
  digitalWrite(m2EncB, HIGH);

  //Innitializes the Motor Encoders for Interrupts
  pciSetup(m1EncA);
  pciSetup(m1EncB);
  pciSetup(m2EncA);
  pciSetup(m2EncB);

  delay(2000);

  //goFORWARD(5);
  //md.setSpeeds(300, 300);
  //delay(2000);
  //md.setBrakes(400, 400);

  //goACCELERATE();
}

void loop() {
	int action = readCommands();
  //md.setSpeeds(300, 300);
  //DEBUG(1);
  //int action = 0;
  switch (action){
    case 1: 
            Serial.println("Moving forward");
            goFORWARD(5);
            calibrateF = 1;
            break;

    case 2:
            Serial.println("Moving left");
            goLEFT();
            calibrateF = 1;
            break;

    case 3:
            Serial.println("Moving right");
            goRIGHT();
            calibrateF = 1;
            break;

    case 4:
            Serial.println("Calibrating");
            while(calibrateF == 1){
              scanRIGHT(&irRightReadings[0]);
              calibrateRIGHT();
            }
            break;

    case 5:
            Serial.println("Doing Full Scan");
            scanFORWARD(&irFrontReadings[0]);
            scanLEFT();
            scanRIGHT(&irRightReadings[0]);
            break;
  }
}

int readCommands(){
	int action;

	while(Serial.available() == 0){}
  action = Serial.parseInt();
  Serial.println(action);

	return action;
}

void sendStatus(){
}

void goACCELERATE(){
  long lastMillis = millis();
  for(int x = 100; x < 380; x=x+10){
      Serial << "Speed Val: " << x << endl;
      md.setSpeeds(x, x);
  }
  delay(3000);
  md.setBrakes(400, 400);
}

void goFORWARD(int noBlock) {
  long kP = 20;
  long kI = 5;
  long kD = 11;
  long error = 0;
  long errorRate = 0;
  long adjustment = 0;
  static long totalErrors;
  static long lastError;
  static int lastTicks[2];
  int setSpd1 = 300;
  int setSpd2 = 310;
  md.setSpeeds(setSpd1, setSpd2);

  while (mRev[0] < noBlock && mRev[1] < noBlock){
    Serial.println("----");                       //Note: setSpeeds(mRIGHT, mLEFT)
    error = (mCounter[1] - lastTicks[1]) - (mCounter[0] - lastTicks[0]);            //0 = right motor, 1 = left motor, lesser tick time mean faster
    lastTicks[0] = mCounter[0];
    lastTicks[1] = mCounter[1];
    errorRate = error - lastError;
    lastError = error;
    totalErrors += error;

    if (error > 5) {                             //Right Motor faster then left motor
      Serial << "Motor 1 faster than motor 2 by: " << error << " m1Ticks: " << mCounter[0] << "m2Ticks: " << mCounter[1] << endl;
      adjustment = ((abs(error)*kP/10) + (errorRate*kD/10) + (totalErrors*kI/10))/2;
      Serial << "Current adjustment: " << adjustment << endl;
      setSpd1 += adjustment;
      setSpd2 -= adjustment;
    }
    
    else if(error < -5) {
      Serial << "Motor 2 faster than motor 1 by: " << error << " m1Ticks: " << mCounter[0] << "m2Ticks: " << mCounter[1] << endl;    
      adjustment = ((abs(error)*kP/10) + (errorRate*kD/10) + (totalErrors*kI/10))/2;
      Serial << "adjustment setting: " << adjustment << endl;
      setSpd1 -= adjustment;
      setSpd2 += adjustment;
    }

    else{
      Serial << "No change, error is:" << error << endl;
    }

    md.setSpeeds(setSpd1,setSpd2);
    Serial << "Setting speeds to:" << "M1 Speed: " << setSpd1 << " M2 Speed: " << setSpd2 << endl;
    delay(70);
    Serial.println("----");
  }

  resetMCounters();
  md.setBrakes(400, 400);
  
}

void goRIGHT(){

  int leftError = 0;
  int rightError = 0;
  
  Serial.println("Turning Right");
  while(mCounter[0] > -turnRightTicks && mCounter[1] < turnRightTicks){
    md.setSpeeds(-350, 350);
  }
  md.setBrakes(400, 400);
  delay(300);
  mCounter[0] = 0;
  mCounter[1] = 0;
  mRev[0] = 0;
  mRev[1] = 0;
}

void goLEFT(){
  Serial.println("Turning Left");
  while(mCounter[0] < turnLeftTicks && mCounter[1] > -turnLeftTicks){
    md.setSpeeds(350, -350);
  }
  md.setBrakes(400, 400);
  delay(300);
  mCounter[0] = 0;
  mCounter[1] = 0;
  mRev[0] = 0;
  mRev[1] = 0;
}

void scanFORWARD(double pData[]){
  pData[0] = ir1.distance() - offset2;
  //pData[1] = ((ir2.distance() - offset1) / 10) + 0.5;
  pData[1] = ir2.distance() - offset1;
  pData[2] = ir3.distance() - offset3;
  
  Serial << "Mean distance of left fwd sensor: " << pData[0] << endl;  // returns it to the serial monitor
  Serial << "Mean voltage of mid fwd sensor: " << pData[1] << endl;  // returns it to the serial monitor
  Serial << "Mean distance of right fwd sensor: " << pData[2] << endl;  // returns it to the serial monitor
}

void scanLEFT(){
  irLeftReading = ir5.distance() - offset5;
  Serial << "Mean distance of front side sensor (long): " << irLeftReading << endl;  // returns it to the serial monitor
}

void scanRIGHT(double *pData){
  pData[0] = ir4.distance() - offset4;
  pData[1] = ir6.distance() - offset6;

  Serial << "Mean distance of front side sensor (short): " << pData[0] << endl; // returns it to the serial monitor
  Serial << "Mean distance of back side sensor: " << pData[1] << endl;  // returns it to the serial monitor
}

void calibrateRIGHT(){

  int turnTicks = 0;
  resetMCounters();

  if(irRightReadings[0] == irRightReadings[1]){
      Serial.println("Calibrating complete");
      calibrateF = 0;
  }

  if((abs(irRightReadings[0] - irRightReadings[1]) > 0) && calibrateF == 1){
    Serial.println("Calibrating");
    turnTicks = (irRightReadings[0] - irRightReadings[1])*30;
    if(turnTicks > 0){
      Serial << "Moving right abit of ticks: " << turnTicks << endl;
      while(mCounter[0] < abs(turnTicks) && mCounter[1] < abs(turnTicks)){
        md.setSpeeds(-300, 300);
      }
      md.setBrakes(400, 400);
    }
    if(turnTicks < 0){
      Serial << "Moving left abit of ticks: " << turnTicks << endl;
      while(mCounter[0] < abs(turnTicks) && mCounter[1] < abs(turnTicks)){
        md.setSpeeds(300, -300);
      }
      md.setBrakes(400, 400);
    }
  }
}

void resetMCounters(){
  mCounter[0] = 0;
  mCounter[1] = 0;
  mRev[0] = 0;
  mRev[1] = 0;
}

void mEncoder(int motor, int setTick){
  if(mCounter[motor] % 4 == 0){
    tickTime[motor] = micros() - prevTickTime[motor];
    prevTickTime[motor] = micros();
  }
  mCounter[motor]++;                               //Then it's going forward so ++ ticks
  if((mCounter[motor] % setTick) == 0){
    mRev[motor]++;
    //mCounter[motor] = 0;
  }
}

void DEBUG(int sel){
  switch(sel){
    case 0: 
            if(flag[0] == 1 || flag[1] == 1){ 
              Serial.println("Encoder ticks output");
              Serial << "m1EncVal: " << mCounter[0] << " m1Rev: " << mRev[0] << " m2EncVal: " << mCounter[1] << " m2Rev: " << mRev[1] << endl;
            }
            break;
            
    case 1: 
            if(flag[0] == 1 || flag[1] == 1){
              Serial.println("Encoder ticks speed");
              Serial << "m1TickTime: " << tickTime[0] << " m2TickTime: " << tickTime[1] << " Speed diff: " << tickTime[0] - tickTime[1] << endl;
            }
            break;
            
     case 2:

            break;
     
  }

  if(flag[0] = 1) flag[0] = 0;
  if(flag[1] = 1) flag[1] = 0;
}

//ISR for Motor 1 Encoders
ISR(PCINT2_vect){
  flag[0] = 1;
  mEncoder(0, 850);
}

//ISR for Motor 2 Encoders
ISR(PCINT0_vect){
  flag[1] = 1;
  mEncoder(1, 850);
}

//Standard function to enable interrupts on any pins
void pciSetup(byte pin){
    *digitalPinToPCMSK(pin) |= bit (digitalPinToPCMSKbit(pin));  // enable pin
    PCIFR  |= bit (digitalPinToPCICRbit(pin)); // clear any outstanding interrupt
    PCICR  |= bit (digitalPinToPCICRbit(pin)); // enable interrupt for the group
}
