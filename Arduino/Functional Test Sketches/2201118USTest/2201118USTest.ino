void setup() {
  Serial.begin(9600);                  // Sets the baud rate to 9600
  Serial.println("Init the sensor");
  delay(200); //Give sensor some time to start up --Added By crystal  from Singapo, Thanks Crystal.
}

void loop()
{
  Serial.println(String("Distance: ") + getUSDist());
  delay(100);
}

int getUSDist(){
  int USValue = 0;
  uint8_t DMcmd[4] = {0x22, 0x00, 0x00, 0x22}; //distance measure command
  for(int i=0;i<4;i++)
  {
    Serial.write(DMcmd[i]);
  }

  delay(40); //delay for 75 ms
  unsigned long timer = millis();
  while(millis() - timer < 30)
  {
    if(Serial.available()>0)
    {
      int header=Serial.read(); //0x22
      int highbyte=Serial.read();
      int lowbyte=Serial.read();
      int sum=Serial.read();//sum

      if(header == 0x22){
        if(highbyte==255)
        {
          USValue=65525;  //if highbyte =255 , the reading is invalid.
        }
        else
        {
          USValue = highbyte*255+lowbyte;
        }
        return USValue;
      }
      else{
        while(Serial.available())  byte bufferClear = Serial.read();
        break;
      }
    }
  }
  delay(20);
  return USValue;
}

