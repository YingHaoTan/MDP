#include "DualVNH5019MotorShield.h"

// Constructors ////////////////////////////////////////////////////////////////

DualVNH5019MotorShield::DualVNH5019MotorShield()
{
  //Pin map
  _INA1 = 2;				//PORTD bit 2
  _INB1 = 4;				//PORTD bit 4
  _EN1DIAG1 = 6;
  _CS1 = A0; 
  _INA2 = 7;				//PORTD bit 7
  _INB2 = 8;				//PORTB bit 0
  _EN2DIAG2 = 12;
  _CS2 = A1;
}

DualVNH5019MotorShield::DualVNH5019MotorShield(unsigned char INA1, unsigned char INB1, unsigned char EN1DIAG1, unsigned char CS1, 
                                               unsigned char INA2, unsigned char INB2, unsigned char EN2DIAG2, unsigned char CS2)
{
  //Pin map
  //PWM1 and PWM2 cannot be remapped because the library assumes PWM is on timer1
  _INA1 = INA1;
  _INB1 = INB1;
  _EN1DIAG1 = EN1DIAG1;
  _CS1 = CS1;
  _INA2 = INA2;
  _INB2 = INB2;
  _EN2DIAG2 = EN2DIAG2;
  _CS2 = CS2;
}

// Public Methods //////////////////////////////////////////////////////////////
void DualVNH5019MotorShield::init()
{
// Define pinMode for the pins and set the frequency for timer1.

  pinMode(_INA1,OUTPUT);
  pinMode(_INB1,OUTPUT);
  pinMode(_PWM1,OUTPUT);
  pinMode(_EN1DIAG1,INPUT);
  pinMode(_CS1,INPUT);
  pinMode(_INA2,OUTPUT);
  pinMode(_INB2,OUTPUT);
  pinMode(_PWM2,OUTPUT);
  pinMode(_EN2DIAG2,INPUT);
  pinMode(_CS2,INPUT);
  #if defined(__AVR_ATmega168__)|| defined(__AVR_ATmega328P__) || defined(__AVR_ATmega32U4__)
  // Timer 1 configuration
  // prescaler: clockI/O / 1
  // outputs enabled
  // phase-correct PWM
  // top of 400
  //
  // PWM frequency calculation
  // 16MHz / 1 (prescaler) / 2 (phase-correct) / 400 (top) = 20kHz
  TCCR1A = 0b10100000;
  TCCR1B = 0b00010001;
  ICR1 = 400;
  #endif
}
// Set speed for motor 1, speed is a number betwenn -400 and 400
void DualVNH5019MotorShield::setM1Speed(int speed)
{
  unsigned char reverse = 0;
  
  if (speed < 0)
  {
    speed = -speed;  // Make speed a positive quantity
    reverse = 1;  // Preserve the direction
  }
  if (speed > 400)  // Max PWM dutycycle
    speed = 400;
  #if defined(__AVR_ATmega168__)|| defined(__AVR_ATmega328P__) || defined(__AVR_ATmega32U4__)
  OCR1A = speed;
  #else
  analogWrite(_PWM1,speed * 51 / 80); // default to using analogWrite, mapping 400 to 255
  #endif
  if (speed == 0)
  {
	PORTD = PORTD & 0b11101011;
    //digitalWrite(_INA1,LOW);   // Make the motor coast no
    //digitalWrite(_INB1,LOW);   // matter which direction it is spinning.
  }
  else if (reverse)
  {
	PORTD = (PORTD | 0b00010000) & 0b11111011;
    //digitalWrite(_INA1,LOW);
    //digitalWrite(_INB1,HIGH);
  }
  else
  {
	PORTD = (PORTD | 0b00000100) & 0b11101111;
    //digitalWrite(_INA1,HIGH);
    //digitalWrite(_INB1,LOW);
  }
}

// Set speed for motor 2, speed is a number betwenn -400 and 400
void DualVNH5019MotorShield::setM2Speed(int speed)
{
  unsigned char reverse = 0;
  
  if (speed < 0)
  {
    speed = -speed;  // make speed a positive quantity
    reverse = 1;  // preserve the direction
  }
  if (speed > 400)  // Max 
    speed = 400;
  #if defined(__AVR_ATmega168__)|| defined(__AVR_ATmega328P__) || defined(__AVR_ATmega32U4__)
  OCR1B = speed;
  #else
  analogWrite(_PWM1,speed * 51 / 80); // default to using analogWrite, mapping 400 to 255
  analogWrite(_PWM2,speed * 51 / 80); // default to using analogWrite, mapping 400 to 255
  #endif 
  if (speed == 0)
  {
	PORTD = PORTD & 0b01111111;
	PORTB = PORTD & 0b11111110;
    //digitalWrite(_INA2,LOW);   // Make the motor coast no
    //digitalWrite(_INB2,LOW);   // matter which direction it is spinning.
  }
  else if (reverse)
  {
	PORTD = PORTD & 0b01111111;
	PORTB = PORTB | 0b00000001;
    //digitalWrite(_INA2,LOW);
    //digitalWrite(_INB2,HIGH);
  }
  else
  {
	PORTD = PORTD | 0b10000000;
	PORTB = PORTD & 0b11111110;
    //digitalWrite(_INA2,HIGH);
    //digitalWrite(_INB2,LOW);
  }
}

// Set speed for motor 1 and 2
void DualVNH5019MotorShield::setSpeeds(int m1Speed, int m2Speed)
{
  
  setM1Speed(m1Speed);
  setM2Speed(m2Speed);
  


  /*
  unsigned char m1Reverse = 0;
  unsigned char m2Reverse = 0;
  
  if (m1Speed < 0)
  {
    m1Speed = -m1Speed;  // make speed a positive quantity
    m1Reverse = 1;  // preserve the direction
  }
  if (m1Speed > 400)  // Max 
    m1Speed = 400;

  if (m2Speed < 0)
  {
    m2Speed = -m2Speed;  // make speed a positive quantity
    m2Reverse = 1;  // preserve the direction
  }
  if (m2Speed > 400)  // Max 
    m2Speed = 400;
  
  #if defined(__AVR_ATmega168__)|| defined(__AVR_ATmega328P__) || defined(__AVR_ATmega32U4__)
  OCR1B = m2Speed;
  OCR1A = m1Speed;
  #else
  analogWrite(_PWM2,speed * 51 / 80); // default to using analogWrite, mapping 400 to 255
  #endif 

  if (m1Speed == 0 && m2Speed == 0)
  {
    PORTD = PORTD & 0b11101011;
    //digitalWrite(_INA1,LOW);   // Make the motor coast no
    //digitalWrite(_INB1,LOW);   // matter which direction it is spinning.
	PORTD = PORTD & 0b01111111;
	PORTB = PORTD & 0b11111110;
    //digitalWrite(_INA2,LOW);   // Make the motor coast no
    //digitalWrite(_INB2,LOW);   // matter which direction it is spinning.
  }

  else if(m1Speed == 0 && m2Speed == m2Reverse){
    PORTD = PORTD & 0b11101011;
    //digitalWrite(_INA1,LOW);   // Make the motor coast no
    //digitalWrite(_INB1,LOW);   // matter which direction it is spinning.
    PORTD = PORTD & 0b01111111;
	PORTB = PORTB | 0b00000001;
    //digitalWrite(_INA2,LOW);
    //digitalWrite(_INB2,HIGH);
  }

  else if(m1Speed == 0 && m2Speed > 0){
    PORTD = PORTD & 0b11101011;
    //digitalWrite(_INA1,LOW);   // Make the motor coast no
    //digitalWrite(_INB1,LOW);   // matter which direction it is spinning.
    PORTD = PORTD | 0b10000000;
	PORTB = PORTD & 0b11111110;
    //digitalWrite(_INA2,HIGH);
    //digitalWrite(_INB2,LOW);
  }

  else if(m1Speed > 0 && m2Speed == 0){
   PORTD = (PORTD | 0b00000100) & 0b11101111;
    //digitalWrite(_INA1,HIGH);
    //digitalWrite(_INB1,LOW);
   PORTD = PORTD & 0b11101011;
    //digitalWrite(_INA1,LOW);   // Make the motor coast no
    //digitalWrite(_INB1,LOW);   // matter which direction it is spinning.
  }

  else if(m1Speed > 0 && m2Speed == m2Reverse){
   PORTD = (PORTD | 0b00000100) & 0b11101111;
    //digitalWrite(_INA1,HIGH);
    //digitalWrite(_INB1,LOW);
   PORTD = PORTD & 0b01111111;
   PORTB = PORTB | 0b00000001;
    //digitalWrite(_INA2,LOW);
    //digitalWrite(_INB2,HIGH);
  }

  else if(m1Speed > 0 && m2Speed > 0){
   PORTD = (PORTD | 0b00000100) & 0b11101111;
    //digitalWrite(_INA1,HIGH);
    //digitalWrite(_INB1,LOW);
   PORTD = PORTD | 0b10000000;
   PORTB = PORTD & 0b11111110;
    //digitalWrite(_INA2,HIGH);
    //digitalWrite(_INB2,LOW);
  }

  else if(m1Speed == m1Reverse && m2Speed == 0){
	PORTD = (PORTD | 0b00010000) & 0b11111011;
    //digitalWrite(_INA1,LOW);
    //digitalWrite(_INB1,HIGH);
	PORTD = PORTD & 0b11101011;
    //digitalWrite(_INA1,LOW);   // Make the motor coast no
    //digitalWrite(_INB1,LOW);   // matter which direction it is spinning.
  }

  else if(m1Speed == m1Reverse && m2Speed == m2Reverse){
   PORTD = (PORTD | 0b00010000) & 0b11111011;
    //digitalWrite(_INA1,LOW);
    //digitalWrite(_INB1,HIGH);
   PORTD = PORTD & 0b01111111;
   PORTB = PORTB | 0b00000001;
    //digitalWrite(_INA2,LOW);
    //digitalWrite(_INB2,HIGH);
  }

  else if(m1Speed == m1Reverse && m2Speed > 0){
   PORTD = (PORTD | 0b00010000) & 0b11111011;
    //digitalWrite(_INA1,LOW);
    //digitalWrite(_INB1,HIGH);
   PORTD = PORTD | 0b10000000;
   PORTB = PORTD & 0b11111110;
    //digitalWrite(_INA2,HIGH);
    //digitalWrite(_INB2,LOW);
  }
  */
}

// Brake motor 1, brake is a number between 0 and 400
void DualVNH5019MotorShield::setM1Brake(int brake)
{
  // normalize brake
  if (brake < 0)
  {
    brake = -brake;
  }
  if (brake > 400)  // Max brake
    brake = 400;
  PORTD = PORTD & 0b11101011;
  //digitalWrite(_INA1, LOW);
  //digitalWrite(_INB1, LOW);
  #if defined(__AVR_ATmega168__)|| defined(__AVR_ATmega328P__) || defined(__AVR_ATmega32U4__)
  OCR1A = brake;
  #else
  analogWrite(_PWM1,brake * 51 / 80); // default to using analogWrite, mapping 400 to 255
  #endif
}

// Brake motor 2, brake is a number between 0 and 400
void DualVNH5019MotorShield::setM2Brake(int brake)
{
  // normalize brake
  if (brake < 0)
  {
    brake = -brake;
  }
  if (brake > 400)  // Max brake
    brake = 400;
  PORTD = PORTD & 0b01111111;
  PORTB = PORTD & 0b11111110;
  //digitalWrite(_INA2, LOW);
  //digitalWrite(_INB2, LOW);
  #if defined(__AVR_ATmega168__)|| defined(__AVR_ATmega328P__) || defined(__AVR_ATmega32U4__)
  OCR1B = brake;
  #else
  analogWrite(_PWM2,brake * 51 / 80); // default to using analogWrite, mapping 400 to 255
  #endif
}

// Brake motor 1 and 2, brake is a number between 0 and 400
void DualVNH5019MotorShield::setBrakes(int m1Brake, int m2Brake)
{
  //setM1Brake(m1Brake);
  //setM2Brake(m2Brake);

  // normalize brake
  if (m1Brake < 0)
    m1Brake = -m1Brake;

  if (m1Brake > 400)  // Max brake
    m1Brake = 400;

  if (m2Brake < 0)
    m2Brake = -m2Brake;

  if(m2Brake > 400)
    m2Brake = 400;

  PORTD = PORTD & 0b01111111;
  PORTB = PORTD & 0b11111110;
  PORTD = PORTD & 0b11101011;

  #if defined(__AVR_ATmega168__)|| defined(__AVR_ATmega328P__) || defined(__AVR_ATmega32U4__)
  OCR1B = m2Brake;
  OCR1A = m1Brake;
  #else
  analogWrite(_PWM1,brake * 51 / 80); // default to using analogWrite, mapping 400 to 255
  analogWrite(_PWM2,brake * 51 / 80); // default to using analogWrite, mapping 400 to 255
  #endif
}

// Return motor 1 current value in milliamps.
unsigned int DualVNH5019MotorShield::getM1CurrentMilliamps()
{
  // 5V / 1024 ADC counts / 144 mV per A = 34 mA per count
  return analogRead(_CS1) * 34;
}

// Return motor 2 current value in milliamps.
unsigned int DualVNH5019MotorShield::getM2CurrentMilliamps()
{
  // 5V / 1024 ADC counts / 144 mV per A = 34 mA per count
  return analogRead(_CS2) * 34;
}

// Return error status for motor 1 
unsigned char DualVNH5019MotorShield::getM1Fault()
{
  return !digitalRead(_EN1DIAG1);
}

// Return error status for motor 2 
unsigned char DualVNH5019MotorShield::getM2Fault()
{
  return !digitalRead(_EN2DIAG2);
}
