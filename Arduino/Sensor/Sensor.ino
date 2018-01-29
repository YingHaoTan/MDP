#include <SharpIR.h>

#define mfwdIr A0       // These three sensors represent the front portion of the arduino, A0 - A4 are all short range IRs, A5 is the only long range IR
#define lfwdIr A1
#define rfwdIr A2
#define brgtIr A3
#define frgtIr A4
#define flftIr A5 
#define shrtmodel 1080
#define longmodel 20150
// ir: the pin where your sensor is attached
// model: an int that determines your sensor:  1080 for GP2Y0A21Y
//                                            20150 for GP2Y0A02Y

SharpIR ir1(lfwdIr, shrtmodel);
SharpIR ir2(mfwdIr, shrtmodel);
SharpIR ir3(rfwdIr, shrtmodel);
SharpIR ir4(flftIr, longmodel);
SharpIR ir5(frgtIr, shrtmodel);
SharpIR ir6(brgtIr, shrtmodel);


void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
}

void loop() {
  delay(2000);   

  unsigned long pepe1=millis();  // takes the time before the loop on the library begins

  int dis1=ir1.distance();  // this returns the distance to the object you're measuring
  int dis2=ir2.distance();
  int dis3=ir3.distance();
  int dis4=ir4.distance();
  int dis5=ir5.distance();
  int dis6=ir6.distance();

  Serial.print("Mean distance of left fwd sensor: ");  // returns it to the serial monitor
  Serial.println(dis1);

  Serial.print("Mean distance of mid fwd sensor: ");  // returns it to the serial monitor
  Serial.println(dis2);

  Serial.print("Mean distance of right fwd sensor: ");  // returns it to the serial monitor
  Serial.println(dis3);

  Serial.print("Mean distance of back side sensor: ");  // returns it to the serial monitor
  Serial.println(dis6);

  Serial.print("Mean distance of front side sensor (short): ");  // returns it to the serial monitor
  Serial.println(dis5);

  Serial.print("Mean distance of front side sensor (long): ");  // returns it to the serial monitor
  Serial.println(dis4);
  
  unsigned long pepe2=millis()-pepe1;  // the following gives you the time taken to get the measurement
  Serial.print("Time taken (ms): ");
  Serial.println(pepe2);  

}
