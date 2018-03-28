
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
#define lfwdIrOS -4 //Left forward IR
#define rfwdIrOS -4 //Right forward IR
#define frgtIrOS -4 //Front right IR
#define flftIrOS -2 //Front left IR.  The only long range IR.
#define brgtIrOS -4 //Back right IR

#define shrtmodel 1080
#define longmodel 20150

//---------------------Definitions for Motors---------------------//
#define turnLeftTicks  3
#define turnRightTicks -5
#define forwardOffsetTicks 155

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

int RPIExpDelay = 20;
int RPIFPDelay =  100;

int mvmtCounter[] = {0, 0, 0}; //[0]forward, [1]left, [2]right

//---------------------Functional Check---------------------//
int commandsDelay = 300;
int burstMovBlocks =8;
int commands[] = 
                {0};                        //Set to commWithRPI mode
                //{5};                      // IR Sensors, full scan
                //{1,1,1,1,1,1,7,1,1,1,1,1,1,7,1,1,1,1,1,1,7,1,1,1,1,1,1};              //Forward movement, block by block
                //{2,2,2,2,2,2,2,2,1,1,1};  //Left turns
                //{3,3,3,3,3,3,3,3,1,1,1};  //Right turns
                //{7,7,7,7,1,1,1};              //Left turn 180
                //{5,4,5};                //Calibrate side
                //{6,0};              //Calibrate corner
                //{5,9,5};             //Calibrate with any blocks
                //{3,1,3,1,3,1,3,1,1,1,1};  //Loop movement (When algorithm gets stuck)
                //{7,8,0};                      //Forward burst movement
                //{6,2,1,1,1,3,1,2,1,3,1,2,1,3, 1,2,1,3,1,2,1,3,1,2,1};
                //{1,1,1,1,1,1,3,1,3,1,1,1,1,1,1,2,1,2,1,1,1,1,1,1,3,1,3,1,1,1,1,1,1}; //Snake movement
                //{1,3,1,2,1,3,1,2,1,3,1,2,1,3,1,2,1,3,1,2}; //Stairs
                //{4,1,1,1,1,1,1,1,1,1,1};
                //{6,2,1,1,1,1,3,1,2,1,3,1,2,1,3,1,2,1,3,1,1,1,1,3,1,1,1,1,3,1,2,1,3,1,2,1,3,1,2,1,2,1,1,1,7};
                // {1,1,1,1,1,1,1,1,1,0};
//---------------------Instantiate IR and Motor objects from library---------------------//
SharpIR mfwdIrVal(mfwdIrPin, shrtmodel, 0.0360, 0.060);
SharpIR lfwdIrVal(lfwdIrPin, shrtmodel, 0.0340, 0.090);
SharpIR rfwdIrVal(rfwdIrPin, shrtmodel, 0.0353, 0.080);
SharpIR frgtIrVal(frgtIrPin, shrtmodel, 0.0363, 0.086);
SharpIR flftIrVal(flftIrPin, longmodel, 0.01265, 0.16454);
SharpIR brgtIrVal(brgtIrPin, shrtmodel, 0.03651, 0.065);

// SharpIR mfwdIrVal(mfwdIrPin, shrtmodel, 0.0365, 0.060);
// SharpIR lfwdIrVal(lfwdIrPin, shrtmodel, 0.0350, 0.090);
// SharpIR rfwdIrVal(rfwdIrPin, shrtmodel, 0.0355, 0.080);
// SharpIR frgtIrVal(frgtIrPin, shrtmodel, 0.0363, 0.086);
// SharpIR flftIrVal(flftIrPin, longmodel, 0.01265, 0.16454);
// SharpIR brgtIrVal(brgtIrPin, shrtmodel, 0.03651, 0.065);


DualVNH5019MotorShield md;
RingBuffer usbBufferIn;