
//---------------------Pin Definitions. Instead of storing in int, saves RAM.---------------------//
#define m1EncA 3 //Microcontroller pin 5, PORTD, PCINT2_vect, PCINT19
#define m1EncB 5 //Microcontroller pin 11, PORTD,PCINT2_vect, PCINT21
#define m2EncA 13 //Microcontroller pin 17, PORTB, PCINT0_vect, PCINT3
#define m2EncB 11 //Microcontroller pin 19, PORTB, PCINT0_vect, PCINT5

#define mfwdIrPin A0 //Middle forward IR
#define lfwdIrPin A1 //Left forward IR
#define rfwdIrPin A2 //Right forward IR
#define frgtIrPin A5 //Front right IR. The only long range IR.
#define flftIrPin A3 //Front left IR
#define brgtIrPin A4 //Back right IR

//---------------------Definitions for IR Sensor---------------------//
#define mfwdIrOS -5 //Middle forward IR
#define lfwdIrOS -3 //Left forward IR
#define rfwdIrOS -3 //Right forward IR
#define frgtIrOS -2 //Front right IR. The only long range IR.
#define flftIrOS 2 //Front left IR
#define brgtIrOS -2 //Back right IR

#define shrtmodel 1080
#define longmodel 20150

//---------------------Definitions for Motors---------------------//
#define turnLeftTicks 1650 //820
#define turnRightTicks 1640 //790,

//---------------------Global Variables---------------------//
//Variables used in the ISRs for the Motor Encoders
//uint8_t encA[2] = {m1EncA, m2EncA};
//uint8_t encB[2] = {m1EncB, m2EncB};
volatile int mCounter[2] = {0, 0}; //[0]right, [1]left
//volatile int encState[2] = {0, 0};
//volatile int encLastState[2] = {0, 0};
//volatile uint8_t mRev[2] = {0, 0};
//volatile uint8_t flag[2] = {0, 0};

int irFrontReadings[3] = {0, 0, 0}; //[0]left, [1]middle, [2]right
int irRightReadings[2] = {0, 0}; //[0]front, [1]back
int irLeftReading = 0;

//---------------------Instantiate IR and Motor objects from library---------------------//
SharpIR mfwdIrVal(mfwdIrPin, shrtmodel, 0.0375, 0.09123);
SharpIR lfwdIrVal(lfwdIrPin, shrtmodel, 0.0345, 0.115);
SharpIR rfwdIrVal(rfwdIrPin, shrtmodel, 0.0340, 0.120);
SharpIR frgtIrVal(frgtIrPin, shrtmodel, 0.03912, 0.06806);
SharpIR flftIrVal(flftIrPin, longmodel, 0.01265, 0.16454);
SharpIR brgtIrVal(brgtIrPin, shrtmodel, 0.03651, 0.076500);

//SharpIR rfwdIrVal(rfwdIrPin, shrtmodel, 0.035, 0.110);



//SharpIR ir1(lfwdIr, shrtmodel, 0.0353, 0.0934);
//SharpIR ir2(mfwdIr, shrtmodel, 0.035407, 0.133212);
//SharpIR ir3(rfwdIr, shrtmodel, 0.032403, 0.153431);
//
//SharpIR ir1(lfwdIr, shrtmodel, 0.03489, 0.12015);
//SharpIR ir2(mfwdIr, shrtmodel, 0.03599, 0.11123);
//SharpIR ir3(rfwdIr, shrtmodel, 0.03499, 0.12947);
//SharpIR ir4(frgtIr, shrtmodel, 0.036148, 0.112737);
//SharpIR ir5(flftIr, longmodel, 0.0360, -0.0163);
//SharpIR ir6(brgtIr, shrtmodel, 0.031142, 0.187284);

//SharpIR ir1(lfwdIr, shrtmodel, 0.035, 0.14);
//SharpIR ir2(mfwdIr, shrtmodel, 0.0361, 0.11123);
//SharpIR ir3(rfwdIr, shrtmodel, 0.03550, 0.14);
//SharpIR ir4(frgtIr, shrtmodel, 0.04918, 0.112737);
//SharpIR ir5(flftIr, longmodel, 0.0258, 0.3484);
//SharpIR ir6(brgtIr, shrtmodel, 0.04642, 0.36793);

DualVNH5019MotorShield md;
