
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
#define rfwdIrOS -7 //Right forward IR
#define frgtIrOS -2 //Front right IR. The only long range IR.
#define flftIrOS -2 //Front left IR
#define brgtIrOS -2 //Back right IR

#define shrtmodel 1080
#define longmodel 20150

//---------------------Definitions for Motors---------------------//
#define turnLeftTicks  0
#define turnRightTicks 0
#define forwardOffsetTicks 85

//---------------------Global Variables---------------------//
volatile int mCounter[2] = {0, 0}; //[0]right, [1]left

int irFrontReadings[3] = {0, 0, 0}; //[0]left, [1]middle, [2]right
int irRightReadings[2] = {0, 0}; //[0]front, [1]back
int irLeftReading = 0;

uint8_t last_sent = 0;           //Variable for communication

//Variables for PID to work
int lastTicks[2] = {0, 0};
int lastError;
int totalErrors;

int RPIExpDelay = 100;
int RPIFPDelay = 100;

//---------------------Functional Check---------------------//
int commandsDelay = 500;
int burstMovBlocks = 10;
int commands[] = 
                {0};                        //Set to commWithRPI mode
                //{5};                      // IR Sensors, full scan
                //{1,1,1,1,1};              //Forward movement, block by block
                //{2,2,2,2,2,2,2,2,1,1,1};  //Left turns
                //{3,3,3,3,3,3,3,3,1,1,1};  //Right turns
                //{7,7,1,1,1};              //Left turn 180
                //{4,1,1,1};                //Calibrate side
                //{6,2,1,1,1};              //Calibrate corner
                //{3,1,3,1,3,1,3,1,1,1,1};  //Loop movement (When algorithm gets stuck)
                //{8};                      //Forward burst movement

//---------------------Instantiate IR and Motor objects from library---------------------//
SharpIR mfwdIrVal(mfwdIrPin, shrtmodel, 0.0375, 0.09123);
SharpIR lfwdIrVal(lfwdIrPin, shrtmodel, 0.0345, 0.115);
SharpIR rfwdIrVal(rfwdIrPin, shrtmodel, 0.032, 0.130);
SharpIR frgtIrVal(frgtIrPin, shrtmodel, 0.03912, 0.06806);
SharpIR flftIrVal(flftIrPin, longmodel, 0.01265, 0.16454);
SharpIR brgtIrVal(brgtIrPin, shrtmodel, 0.03651, 0.076500);

//SharpIR rfwdIrVal(rfwdIrPin, shrtmodel, 0.034, 0.130);

//SharpIR rfwdIrVal(rfwdIrPin, shrtmodel, 0.034, 0.120);
//SharpIR rfwdIrVal(rfwdIrPin, shrtmodel, 0.035, 0.110);
//SharpIR ir1(lfwdIr, shrtmodel, 0.0353, 0.0934);
//SharpIR ir2(mfwdIr, shrtmodel, 0.035407, 0.133212);
//SharpIR ir3(rfwdIr, shrtmodel, 0.032403, 0.153431);


DualVNH5019MotorShield md;
RingBuffer usbBufferIn;