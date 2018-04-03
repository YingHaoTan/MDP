#ifndef CustomMotorLib_h
#define CustomMotorLib_h

#include <Arduino.h>

class CustomMotorLib
{
  public:  
    // CONSTRUCTORS
    CustomMotorLib(); // Default pin selection.
    CustomMotorLib(unsigned char INA1, unsigned char INB1, unsigned char EN1DIAG1, unsigned char CS1, 
                           unsigned char INA2, unsigned char INB2, unsigned char EN2DIAG2, unsigned char CS2); // User-defined pin selection. 
    
    // PUBLIC METHODS
    void init(); // Initialize TIMER 1, set the PWM to 20kHZ. 
    void setM1Speed(int speed); // Set speed for M1.
    void setM2Speed(int speed); // Set speed for M2.
    void setSpeeds(int m1Speed, int m2Speed); // Set speed for both M1 and M2.
    void setM1Brake(int brake); // Brake M1. 
    void setM2Brake(int brake); // Brake M2.
    void setBrakes(int m1Brake, int m2Brake); // Brake both M1 and M2.
    
  private:
    unsigned char _INA1;
    unsigned char _INB1;
    static const unsigned char _PWM1 = 9;
    unsigned char _EN1DIAG1;
    unsigned char _CS1;
    unsigned char _INA2;
    unsigned char _INB2;
    static const unsigned char _PWM2 = 10;
    unsigned char _EN2DIAG2;
    unsigned char _CS2;
    
};

#endif