#include <SharpIR.h>
#include <DualVNH5019MotorShield.h>
#include <EEPROM.h>

//NOTES:
//1124 when speed = 400 / 562 when speed <= 50 Set to 1100 on air

//Pin Definitions. Instead of storing in int, saves RAM.
#define m1EncA 3 //Microcontroller pin 5, PORTD, PCINT2_vect, PCINT19
#define m1EncB 5 //Microcontroller pin 11, PORTD,PCINT2_vect, PCINT21
#define m2EncA 13 //Microcontroller pin 17, PORTB, PCINT0_vect, PCINT3
#define m2EncB 11 //Microcontroller pin 19, PORTB, PCINT0_vect, PCINT5
#define mfwdIr A0 //Middle forward IR
#define lfwdIr A1 //Left forward IR
#define rfwdIr A2 //Right forward IR
#define frgtIr A5 //Front right IR. The only long range IR.
#define flftIr A3 //Front left IR
#define brgtIr A4 //Back right IR

#define offset1 10
#define offset2 10
#define offset3 10
#define offset4
#define offset5
#define offset6

#define shrtmodel 1080
#define longmodel 20150

//Variables used in the ISRs for the Motor Encoders
uint8_t encA[2] = {m1EncA, m2EncA};
uint8_t encB[2] = {m1EncB, m2EncB};
volatile int mCounter[2] = {0, 0};
volatile uint8_t encState[2] = {0, 0};
volatile uint8_t encLastState[2] = {0, 0};
volatile uint8_t mRev[2] = {0, 0};
volatile long tickTime[2] = {0, 0};
volatile long unsigned prevTickTime[2] = {0, 0};
volatile uint8_t flag[2] = {0, 0};

//Instantiate IR and Motor objects from library
SharpIR ir1(lfwdIr, shrtmodel, 0.0353, 0.0934);
SharpIR ir2(mfwdIr, shrtmodel, 0.0358, 0.0878);
SharpIR ir3(rfwdIr, shrtmodel, 0.0405, 0.06);
SharpIR ir4(frgtIr, shrtmodel, 0.0405, 0.06);
SharpIR ir5(flftIr, longmodel, 0.0183, -0.0163);
SharpIR ir6(brgtIr, shrtmodel, 0.0405, 0.06);
DualVNH5019MotorShield md;

void setup() {
  Serial.begin(115200);
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

  //Initialize variables
  encLastState[0] = digitalRead(encA[0]);
  encLastState[1] = digitalRead(encA[1]);
  prevTickTime[0] = micros();
  prevTickTime[1] = micros();

  delay(1000);

  //motorRevTest(int sampleSize, int speedVal, int timeVal)
  //Serial.println("Sample Size: 10 Speed: 400 Time: 3000");
  //motorRevTest(10, 400, 3000, 1);
}

void DEBUG(int sel){
  switch(sel){
    case 0: 
            if(flag[0] == 1 || flag[1] == 1){ 
              Serial.println("Encoder ticks output");
              Serial.print("m1EncVal: ");
              Serial.print(mCounter[0]);
              Serial.print(" m1Rev: ");
              Serial.print(mRev[0]);
              Serial.print(" m2EncVal: ");
              Serial.print(mCounter[1]);
              Serial.print(" m2Rev: ");
              Serial.println(mRev[1]);
              if(flag[0] = 1) flag[0] = 0;
              if(flag[1] = 1) flag[1] = 0;
            }
            break;
            
    case 1: 
            if(flag[0] == 1 || flag[1] == 1){
              Serial.println("Encoder ticks speed");
              Serial.print("m1TickTime: ");
              Serial.print(tickTime[0]);
              Serial.print(" m2TickTime: ");
              Serial.print(tickTime[1]);
              if(abs(tickTime[0] - tickTime[1]) < 1000){
                Serial.print(" Speed diff: ");
                Serial.println(abs(tickTime[0] - tickTime[1]));
              }
                if(flag[0] = 1) flag[0] = 0;
                if(flag[1] = 1) flag[1] = 0;
            }
            break;
  }
}

void loop() {
  //DEBUG(0);

//if(seeFront() > 0.5){
  //moveFwd();
//}
}

void moveFwd(){
  while(mRev[0] < 1 || mRev[1] < 1){
     md.setSpeeds(300, 300);
     //mRev[0] = 0;
     //mRev[1] = 0;
  }
  //else{
    md.setBrakes(400, 400);
   // mRev[0] = 0;
    //mRev[1] = 0;
  //}
}

//Method calls for IR sensors
double seeFront(){
  //int dis1 = ir1.distance();
  double dis2 = ((ir2.distance() - offset2) / 10) + 0.5;
  //double dis2 = ir2.distance() - offset
  double actualDist = ir2.distance();
  //int dis3 = ir3.distance();
  
  //Serial.print("Mean distance of left fwd sensor: ");  // returns it to the serial monitor
  //Serial.println(dis1);

  Serial.print("Mean voltage of mid fwd sensor: ");  // returns it to the serial monitor
  Serial.print(actualDist);
  Serial.print(" in blocks:");
  Serial.println(dis2);

  //Serial.print("Mean distance of right fwd sensor: ");  // returns it to the serial monitor
  //Serial.println(dis3);

  return dis2;
}

void seeLeft(){
  int dis5=ir5.distance();
  Serial.print("Mean distance of front side sensor (long): ");  // returns it to the serial monitor
  Serial.println(dis5);
}

void seeRight(){
  int dis4=ir4.distance();
  int dis6=ir6.distance();

  Serial.print("Mean distance of front side sensor (short): ");  // returns it to the serial monitor
  Serial.println(dis4);
  
  Serial.print("Mean distance of back side sensor: ");  // returns it to the serial monitor
  Serial.println(dis6);
}

void motorRevTest(int sampleSize, int speedVal, int timeVal, uint8_t writeAccess){
  int loopCount = sampleSize;
  int sampleNum = 0;

  int eAddress = 0;
  
  while(loopCount > 0){
    md.setSpeeds(speedVal, speedVal);
    delay(timeVal);
    md.setBrakes(400, 400);
    Serial.print("Sample ");
    Serial.print(sampleNum++);
    Serial.print(": m1EncVal: ");
    Serial.print(mCounter[0]);
    Serial.print(" m1Rev: ");
    Serial.print(mRev[0]);
    Serial.print(" m2EncVal: ");
    Serial.println(mCounter[1]);
    Serial.print(" m2Rev: ");
    Serial.println(mRev[1]);
    if(writeAccess == 1){
      EEPROM.write(eAddress, mCounter[0] & 0xff);
      EEPROM.write(eAddress+1, mCounter[0] >> 8);
      eAddress = eAddress+2;
      EEPROM.write(eAddress, mCounter[1] & 0xff);
      EEPROM.write(eAddress+1, mCounter[1] >> 8);
      eAddress = eAddress+2;
    }
    
    loopCount--;
    delay(2000);
    md.setSpeeds(-speedVal, -speedVal);
    delay(timeVal);
    md.setBrakes(400, 400);
    mCounter[0] = 0;
    mCounter[1] = 0;
    delay(1000);
  }
}

void mEncoder(int motor, int setTick){
  //tickTime[motor] = micros() - prevTickTime[motor];
  //prevTickTime[motor] = micros();
  encState[motor] = digitalRead(encA[motor]);
  if(encState[motor] != encLastState[motor]){          //Was there a change in state?
    if(digitalRead(encB[motor]) != encState[motor]){   //If EncA state is different from EncB
      mCounter[motor]++;                               //Then it's going forward so ++ ticks
    }
    else{
      mCounter[motor]--;                               //Else it is going in reverse so -- ticks
    }
  }
    
  encLastState[motor] = encState[motor];

  if((mCounter[motor] % setTick) == 0){
    mRev[motor]++;
    //mCounter[motor] = 0;
  }
}

//ISR for Motor 1 Encoders
ISR(PCINT2_vect){
  flag[0] = 1;
  mEncoder(0, 1124);
}

//ISR for Motor 2 Encoders
ISR(PCINT0_vect){
  flag[1] = 1;
  mEncoder(1, 1124);
}

//Standard function to enable interrupts on any pins
void pciSetup(byte pin)
{
    *digitalPinToPCMSK(pin) |= bit (digitalPinToPCMSKbit(pin));  // enable pin
    PCIFR  |= bit (digitalPinToPCICRbit(pin)); // clear any outstanding interrupt
    PCICR  |= bit (digitalPinToPCICRbit(pin)); // enable interrupt for the group
}
