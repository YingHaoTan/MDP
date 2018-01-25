#include <SharpIR.h>

int irPin = A0;
SharpIR sharp(irPin, 1080);

void setup() {
  // put your setup code here, to run once:

  //pinMode(irPin, INPUT);
  Serial.begin(9600);
  
  
}

void loop() {
  // put your main code here, to run repeatedly:
  Serial.println(sharp.distance());
  delay(500);
}
