#include <SharpIR.h>

#define mfwdIr A0       // These three sensors represent the front portion of the arduino, A0 - A4 are all short range IRs, A5 is the only long range IR
#define lfwdIr A1
#define rfwdIr A2
#define frgtIr A5
#define flftIr A3
#define brgtIr A3 

#define shrtmodel 1080
#define longmodel 20150
// ir: the pin where your sensor is attached
// model: an int that determines your sensor:  1080 for GP2Y0A21Y
//                                            20150 for GP2Y0A02Y

SharpIR ir1(lfwdIr, shrtmodel, 0.0353, 0.0934);
SharpIR ir2(mfwdIr, shrtmodel, 0.0358, 0.0878);
SharpIR ir3(rfwdIr, shrtmodel, 0.0405, 0.06);
SharpIR ir4(frgtIr, shrtmodel, 0.0405, 0.06);
SharpIR ir5(flftIr, longmodel, 0.0183, -0.0163);
SharpIR ir6(brgtIr, shrtmodel, 0.0405, 0.06);


void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
}

void loop() {
  delay(100);   

  unsigned long pepe1=millis();  // takes the time before the loop on the library begins
  seeFront();
//  int dis1=ir1.distance();  // this returns the distance to the object you're measuring
//  int dis2V=analogRead(A0);
//  int dis2 = ir2.distance();
//  int dis3=ir3.distance();
//  int dis4=ir4.distance();
//  int dis5=ir5.distance();
//  int dis6=ir6.distance();

//  Serial.print("Mean distance of left fwd sensor: ");  // returns it to the serial monitor
//  Serial.println(dis1);
//
//  Serial.print("Mean voltage of mid fwd sensor: ");  // returns it to the serial monitor
////  Serial.println(dis2V);
//  Serial.println(dis2);
//
//  Serial.print("Mean distance of right fwd sensor: ");  // returns it to the serial monitor
//  Serial.println(dis3);
//
//  Serial.print("Mean distance of back side sensor: ");  // returns it to the serial monitor
//  Serial.println(dis6);
////
//  Serial.print("Mean distance of front side sensor (long): ");  // returns it to the serial monitor
//  Serial.println(dis5);
//
//  Serial.print("Mean distance of front side sensor (short): ");  // returns it to the serial monitor
//  Serial.println(dis4);
  
  unsigned long pepe2=millis()-pepe1;  // the following gives you the time taken to get the measurement
  Serial.print("Time taken (ms): ");
  Serial.println(pepe2);  
}

void seeFront(){
  int dis1=ir1.distance();
  int dis2 = ir2.distance();
  int dis3=ir3.distance();
  
  Serial.print("Mean distance of left fwd sensor: ");  // returns it to the serial monitor
  Serial.println(dis1);

  Serial.print("Mean voltage of mid fwd sensor: ");  // returns it to the serial monitor
  Serial.println(dis2);

  Serial.print("Mean distance of right fwd sensor: ");  // returns it to the serial monitor
  Serial.println(dis3);
}

void seeLeft(){
  int dis5=ir5.distance();
  Serial.print("Mean distance of front side sensor (long): ");  // returns it to the serial monitor
  Serial.println(dis5);
}

void seeRight(){
  int dis4=ir4.distance();
  int dis6=ir6.distance();

  Serial.print("Mean distance of front side sensor (short): ");  // returns it to the serial monitor
  Serial.println(dis4);
  
  Serial.print("Mean distance of back side sensor: ");  // returns it to the serial monitor
  Serial.println(dis6);

  
}

