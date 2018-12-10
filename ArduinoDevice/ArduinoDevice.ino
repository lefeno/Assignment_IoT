#include <SoftwareSerial.h>
#include <Servo.h>
#include <stdlib.h>
#define MAXPOT 2
enum Hum {state1, state2};
enum Hum a;
enum PotState {pot1, pot2};
enum PotState pot;
enum ServoState {SERVO_INIT, SERVO_ON, SERVO_OFF};
enum ServoState servo;
enum BLEState {SEND_SUCCESS , SEND_DATA, SEND_FAIL};
enum BLEState stateW = SEND_DATA ;
String control;
String control_12;
char control_0, control_1 , control_2;
int valueRead;
boolean isWatering = false ;
//char c;
//String c;
boolean beginToWater = false;
int intcontrol;
char rx_byte = 0; // tín hiệu test cho động cơ di chuyển tới vị trí 80 độ
int pos ; // Góc quay của động cơ
int T_on, T_off;
int temp = 0;
int v2 = 91 ;
int SENSOR_MH_A_PIN = A0;
int SENSOR_MH_A_PIN02 = A1;
//stateW = SEND_DATA ;
SoftwareSerial BLUETOOTH(6, 7); // (RX_pin,TX_pin)
//Servo myservo;


void setup() {
  Serial.begin(9600);
  BLUETOOTH.begin(9600);
  pinMode(4, OUTPUT); // chân D0 của sensor
  pinMode(5, OUTPUT); // chânN của relay

  pinMode(9, OUTPUT); // chân DC+ của relay
  pinMode(12, OUTPUT); //chân VCC của mạch sensor
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(4, HIGH);
  digitalWrite(12, HIGH); //chân VCC của mạch sensor
  digitalWrite(9, HIGH);  // chân DC+ của relay
  T_on = 1000; // servo có thời gian di chuyển trong 1s
  T_off = 3000; // bơm nước trong 4s
//  temp = T_on / 10;
  servo = SERVO_INIT;
  //TIMSK0 = 0;
  //digitalWrite(4, HIGH);
  //myservo.attach(8); // Tín hiệu điều khiển động cơ
  //pinMode(3, OUTPUT);
}


float readSensor(int pot) {
  float reading;
  switch (pot) {
    case pot1:
      reading = analogRead(SENSOR_MH_A_PIN);
      break;
    case pot2:
      reading = v2;
      v2 = (v2 + 5)%90;
      break;
    default:
      break;
  }
  float voltage;
  voltage = map(reading, 0, 1023, 0, 5000); // chuyển thang đo
  float hum = (voltage / 5000) * 100;  // % Độ ẩm đất
  if (hum > 90) {
    a = state1;
  } else {
    a = state2;
  }
  return (100 - hum);
}
void sendData(){
  for (int i = 0 ; i < MAXPOT; i++) {
//      Serial.print("Sensor");
//      Serial.print(i + 1);
//      Serial.print(":");
//      Serial.println(readSensor(i));
      valueRead = readSensor(i);

//      if((i == control_0 - 49 && valueRead >= intcontrol && beginToWater == true) || temp >= 40){
      if((i == control_0 - 49 && valueRead >= intcontrol && beginToWater == true) || temp >= 40){
        beginToWater = false;
        isWatering = false;
        digitalWrite(5,LOW);
        if(temp >= 40){
          stateW = SEND_FAIL;
        } else {
          stateW = SEND_SUCCESS;
        }
//        Serial.println("Đã tưới thành công");
        Serial.println("Stop watering");
        temp = 0;
      } else if(valueRead < intcontrol && beginToWater == true && isWatering == false && i == control_0 - 49){
        digitalWrite(5,HIGH);
        Serial.print("Bắt đầu tưới pot số ");
        Serial.println(control_0 - 48);
        isWatering = true;
      } else if (isWatering == true){
        ++temp;
      }
      
      String myStr;
      //Serial.print("Pot : ");
      //Serial.println(i+1);
      switch (a) {
        case state1:
          myStr = String(i + 1) + "0" + String((int)readSensor(i));
          break;
        case state2:
          myStr = String(i + 1) + String((int)readSensor(i));
          break;
        default:
          break;
      }
      myStr = "x" + myStr + "y";
//      Serial.println(myStr);
      byte buf[8];
      myStr.getBytes(buf, 8);
      //Serial.println(buf[3]);
      BLUETOOTH.write(buf, 8);

      delay(100);
  }
}
void sendSuccess(boolean fail){
  String abcStr;
  if(!fail){
    abcStr = (String) control_0 + "EE" ;  
  } else {
    abcStr = (String) control_0 + "FF" ;
  }
  
  abcStr = "x" + abcStr + "y";
  byte auf[8];
  abcStr.getBytes(auf, 8);
  BLUETOOTH.write(auf, 8);
  delay(500);
}
void bluetooth() {
  if (BLUETOOTH.available()) {
    control = BLUETOOTH.readString();
//    Serial.println(control);
    control_0 = control.charAt(0);
    beginToWater = true;
//    Serial.print("Tưới nước pot ");
//    Serial.println(control_0 - 48);
    control_12 = control.substring(1);
    intcontrol = control_12.toInt();
    temp = 0; // begin timer
//    Serial .print("Mức nước: ");
//    Serial.println(intcontrol);
  }
  else {
    switch (stateW){
      case SEND_DATA:
        sendData();
        break;
      case SEND_SUCCESS:
         sendSuccess(false);
         stateW = SEND_DATA;
        break;
      case SEND_FAIL:
         sendSuccess(true);
         stateW = SEND_DATA;
        break;
      default:
//        Serial.println("BLE state invalid");
        break;
    }
    
   }
}


void loop() {
  bluetooth();
  // action();

//  switch (servo) {
//    case SERVO_INIT:    // động cơ ở vị trí bắt đầu
//      Serial.println("vị trí ban đầu của servo nè");  //aaa
//      //       digitalWrite(7,LOW);
//      //       myservo.write(80);
//      if ( control_0 == 't') {
//        servo = SERVO_ON;
//        Serial.println("Servo di chuyển tới chổ cần tưới nè");
//        //digitalWrite(7,LOW);
//        // myservo.write(80);
//      } else {
//        servo = SERVO_INIT;
//      }
//      break;
//    case SERVO_OFF :
//      if (temp == 0) {
//        temp = T_on / 10;
//        servo = SERVO_INIT;
//        Serial.println("Bơm nước xong dzòi nè hihi xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
//      } else {
//        temp = temp - 1;
//        delay(10);
//      }
//      break;
//    case SERVO_ON : // bắt đầu bơm nước trong 4s
//      //Serial.println("có vào servo on ko vậy ?");
//      if (temp == 0) {
//        servo = SERVO_OFF;
//        temp = T_off / 10;
//        Serial.println("Bơm nước nè hihi");
//        //digitalWrite(7,HIGH);
//      } else {
//        temp = temp - 1  ;
//        delay(10);
//      }
//      break;
//    default:
//      break;
//  }

  //delay(10);
}
