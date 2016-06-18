#include <TimerOne.h>
#include <SoftwareSerial.h>


// analog inputs
const int THROTTLE_PIN = 0;
const int LAMBDA_PIN = 1;

// digital input for ignition (must be ping 2 or 3 on Duemilanove, only these can be attached to interrupts)
const int IGNITION_PIN = 2;
const int PULSES_PER_REV = 1;
const int IGNITION_INTERRUPT = 0;

// HC-05 is connected to ping 4 and 5
SoftwareSerial btSerial(4, 5);


volatile unsigned long rpm = 0;
volatile unsigned long lastPulseTime = 0;

void setup() {
  // configure serials
  Serial.begin(9600);
  btSerial.begin(9600);

  // Timer1 is used to read inputs and send data periodically
  Timer1.initialize(100000); // every 100 ms
  Timer1.attachInterrupt(readAndSendData);

  // configure digital input and interrupt
  pinMode(IGNITION_PIN, INPUT);
  attachInterrupt(digitalPinToInterrupt(IGNITION_PIN), &handleIgnitionInterrupt, RISING);

  

}

/**
 * Called when the IGNITION_PIN rises
 */
void handleIgnitionInterrupt() {
  unsigned long now = micros();
  unsigned long interval = now - lastPulseTime;
  if (interval > 3000) { // max 20000 min^⁻1
     rpm = 60000000UL/(interval * PULSES_PER_REV);
     lastPulseTime = now;
  } 
}

/**
 * Reads the analog inputs and sends the values to the serial(s)
 */
void readAndSendData() {

  // read analog inputs
  int throttle = analogRead(THROTTLE_PIN);
  int lambda = analogRead(LAMBDA_PIN);

  unsigned long now = micros();
  unsigned long interval = now - lastPulseTime;
  // if lastPulseTime is greate than 120000 us (~ 500 RPM), the set rpm to 0
  if(interval < 0 || interval > 120000UL) {
    rpm = 0; 
  }
  
  // send to BT
  btSerial.print(throttle);
  btSerial.print(",");
  btSerial.print(lambda);
  btSerial.print(",");
  btSerial.print(rpm);
  btSerial.println();

  
  // debug to USB serial
  Serial.print(throttle);
  Serial.print(",");
  Serial.print(lambda);
  Serial.print(",");
  Serial.print(rpm);
  Serial.println();
 
}


void loop() {
  
}
