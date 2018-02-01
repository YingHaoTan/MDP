#include <DualVNH5019MotorShield.h>

int m1EncA = 3; //Microcontroller pin 5, PORTD, PCINT2_vect, PCINT19
int m1EncB = 5; //Microcontroller pin 11, PORTD,PCINT2_vect, PCINT21
int m2EncA = 11; //Microcontroller pin 17, PORTB, PCINT0_vect, PCINT3
int m2EncB = 13; //Microcontroller pin 19, PORTB, PCINT0_vect, PCINT5

//Variables used in the ISRs for the Motor Encoders
volatile int m1Flag;
volatile int m1Counter;
volatile int a1State;
volatile int a1LastState;
volatile int m1Rev;
volatile int m2Flag;
volatile int m2Counter;
volatile int a2State;
volatile int a2LastState;
volatile int m2Rev;

//Variables used in the ISRs for the IR Sensors

DualVNH5019MotorShield md;

void setup() {
  Serial.begin(115200);
  Serial.println("Motor Encoder Test");
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

  m1Counter = 0;
  m1Flag = 0;
  m1Rev = 0;
  m2Counter = 0;
  m2Flag = 0;
  m2Rev = 0;
  
  //Initialise a reference pont for Motor EncoderA
  a1LastState = digitalRead(m1EncA);
  a2LastState = digitalRead(m2EncA);
  
}

void loop() {
    if(m1Flag == 1 || m2Flag == 1){
      Serial.print("M1EncVal: ");
      Serial.print(m1Counter);
      Serial.print(" M2EncVal: ");
      Serial.println(m2Counter);
      m1Flag=0;
      m2Flag=0;
    }
    
   if(m1Rev < 1){
      md.setM1Speed(400);
    }
    else{
      md.setM1Speed(0);
    }
    
   if(m2Rev < 1){
      md.setM2Speed(-400);
    }
    else{
      md.setM2Speed(0);
    }
}

//ISR for Motor 1 Encoders
ISR(PCINT2_vect){
  //if(digitalRead(m1EncA) == HIGH){
    m1Flag = 1;                             //Flag to alert interrupt is triggered
    
    a1State = digitalRead(m1EncA);
    
    if(a1State != a1LastState){             //Was there a change in state?
      if(digitalRead(m1EncB) != a1State){   //If EncA state is different from EncB
        m1Counter--;                        //Then it's going forward so ++ ticks
      }
      else{
        m1Counter++;                        //Else it is going in reverse so -- ticks
      }
    }
    
    a1LastState = a1State;

    if(m1Counter == 562){ //1124 when speed = 400 / 562 when speed <= 50
      m1Rev++;
      m1Counter = 0;
    }
  //}
}

//ISR for Motor 2 Encoders
ISR(PCINT0_vect){
  //if(digitalRead(m2EncA) == HIGH){
    m2Flag = 1;                           //Flag to alert interrupt is triggered
    
    a2State = digitalRead(m2EncA);
    if(a2State != a2LastState){             //Was there a change in state?
      if(digitalRead(m2EncB) != a2State){   //If EncA state is different from EncB
        m2Counter++;                        //Then it's going forward so ++ ticks
      }
      else{
        m2Counter--;                        //Else it is going in reverse so -- tics
      }
    }
    
    a2LastState = a2State;

    if(m2Counter == 562){ //1124 when speed = 400 / 562 when speed <= 50
      m2Rev++;
      m2Counter = 0;
    }
  //}
}

//Standard function to enable interrupts on any pins
void pciSetup(byte pin)
{
    *digitalPinToPCMSK(pin) |= bit (digitalPinToPCMSKbit(pin));  // enable pin
    PCIFR  |= bit (digitalPinToPCICRbit(pin)); // clear any outstanding interrupt
    PCICR  |= bit (digitalPinToPCICRbit(pin)); // enable interrupt for the group
}
