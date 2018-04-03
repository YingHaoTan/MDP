
//---------------------Pin Definitions. Instead of storing in int, saves RAM.---------------------//
#define m1EncA 3 //Microcontroller pin 5, PORTD, PCINT2_vect, PCINT19
#define m1EncB 5 //Microcontroller pin 11, PORTD,PCINT2_vect, PCINT21
#define m2EncA 13 //Microcontroller pin 17, PORTB, PCINT0_vect, PCINT3
#define m2EncB 11 //Microcontroller pin 19, PORTB, PCINT0_vect, PCINT5

#define mfwdIr A0 //Middle forward IR
#define lfwdIr A1 //Left forward IR
#define rfwdIr A2 //Right forward IR
#define frgtIr A5 //Front right IR
#define flftIr A3 //Front left IR. The only long range IR.
#define brgtIr A4 //Back right IR

//---------------------Definitions for IR Sensor---------------------//
#define offset1 0 //Middle forward IR
#define offset2 0 //Left forward IR
#define offset3 0 //Right forward IR
#define offset4 0 //Front right IR. The only long range IR.
#define offset5 3 //Front left IR
#define offset6 0 //Back right IR

#define shrtmodel 1080
#define longmodel 20150

//---------------------Definitions for Motors---------------------//
#define turnLeftTicks 1650 //820
#define turnRightTicks 1550 //790,

//---------------------Global Variables---------------------//
//Variables used in the ISRs for the Motor Encoders
uint8_t encA[2] = {m1EncA, m2EncA};
uint8_t encB[2] = {m1EncB, m2EncB};
volatile int mCounter[2] = {0, 0};
volatile uint8_t mRev[2] = {0, 0};
volatile long tickTime[2] = {0, 0};
volatile long unsigned prevTickTime[2] = {0, 0};
volatile uint8_t flag[2] = {0, 0};

double irFrontReadings[3] = {0, 0, 0};
double irRightReadings[2] = {0, 0};
double irLeftReading = 0;

//---------------------Instantiate IR and Motor objects from library---------------------//
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

SharpIR ir1(lfwdIr, shrtmodel, 0.0345, 0.115);
SharpIR ir2(mfwdIr, shrtmodel, 0.0375, 0.09123);
SharpIR ir3(rfwdIr, shrtmodel, 0.035, 0.110);
SharpIR ir4(frgtIr, shrtmodel, 0.03912, 0.06806);
SharpIR ir5(flftIr, longmodel, 0.01265, 0.16454);
SharpIR ir6(brgtIr, shrtmodel, 0.03651, 0.076500);
DualVNH5019MotorShield md;
