#include <DualVNH5019MotorShield.h>

int m1EncA = 3;
int m1EncB = 5;

DualVNH5019MotorShield md;

void setup() {
  // put your setup code here, to run once
  Serial.begin(115200);
  Serial.println("Dual VNH5019 Motor Shield");
  md.init();
  pinMode(m1EncA, INPUT);
  pinMode(m1EncB, INPUT);
}

void loop() {
  // put your main code here, to run repeatedly:
  int val1 = 0;
  int val2 = 0;
  
  for (int i = 0; i <= 400; i++){
    md.setM1Speed(i);
    stopIfFault();
    val1 = digitalRead(m1EncA);
    val2 = digitalRead(m1EncB);
    if (i%200 == 100){
      Serial.print("M1 current: ");
      Serial.println(md.getM1CurrentMilliamps());
      Serial.println(val1);
      Serial.println(val2);
    }
    delay(2);
  }
  
  for (int i = 400; i >= -400; i--){
    md.setM1Speed(i);
    stopIfFault();
    val1 = digitalRead(m1EncA);
    val2 = digitalRead(m1EncB);
    if (i%200 == 100){
      Serial.print("M1 current: ");
      Serial.println(md.getM1CurrentMilliamps());
      Serial.println(val1);
      Serial.println(val2);
    }
    delay(2);
  }
  
  for (int i = -400; i <= 0; i++){
    md.setM1Speed(i);
    stopIfFault();
    val1 = digitalRead(m1EncA);
    val2 = digitalRead(m1EncB);
    if (i%200 == 100){
      Serial.print("M1 current: ");
      Serial.println(md.getM1CurrentMilliamps());
      Serial.println(val1);
      Serial.println(val2);
    }
    delay(2);
  }
}

void stopIfFault() {
  if (md.getM1Fault()){
    Serial.println("M1 fault");
    while(1);
  }
  if (md.getM2Fault()){
    Serial.println("M2 fault");
    while(1);}
}

