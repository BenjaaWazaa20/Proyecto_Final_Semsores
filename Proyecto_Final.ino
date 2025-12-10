#include <SPI.h>
#include <MFRC522.h>
#include <Servo.h>
#include <Keypad.h>

#define SERVO_PIN 9
#define IR_PIN 7

#define RST_PIN 8
#define SS_PIN 10
MFRC522 rfid(SS_PIN, RST_PIN);

Servo servo;

const byte ROWS = 4; const byte COLS = 4;
char keys[ROWS][COLS] = {
  {'1','2','3','A'},
  {'4','5','6','B'},
  {'7','8','9','C'},
  {'*','0','#','D'}
};
byte rowPins[ROWS] = {2,3,4,5};
byte colPins[COLS] = {A0,A1,A2,A3};
Keypad keypad = Keypad( makeKeymap(keys), rowPins, colPins, ROWS, COLS );

bool paquetebuzon = false;
unsigned long cerrarautom = 0;
const unsigned long tabierto = 4000; 

byte ownerUID[4] = {0xE7, 0x86, 0xAF, 0x3F};
byte repartidorUID[4] = {0x89, 0x43, 0x6E, 0xA2}; 

String pin = "1234";
String pinact = "";
int intentosmalos = 0;

void setup(){
  Serial.begin(9600);
  SPI.begin();
  rfid.PCD_Init();
  servo.attach(SERVO_PIN);
  pinMode(IR_PIN, INPUT);
  cerrarBuzon();
  Serial.println("Buzon iniciado");
}

void loop(){
  if (Serial.available() > 0) {
    String comando = Serial.readStringUntil('\n');
    comando.trim(); 

    if (comando.equals("CERRAR_MANUAL")) {
      cerrarBuzon();
      Serial.println("cerrarBuzon"); 
    }
  }

  if(digitalRead(IR_PIN) == HIGH){
    if(!paquetebuzon){
      paquetebuzon = true;
      Serial.println("Paquete detectado dentro del buzon");
      cerrarBuzon();
    }
  } else {
    paquetebuzon = false;
  }

  if ( rfid.PICC_IsNewCardPresent() && rfid.PICC_ReadCardSerial() ){
    byte *uid = rfid.uid.uidByte;
    byte uidSize = rfid.uid.size;
    Serial.print("ID detectado: ");
    for (byte i = 0; i < uidSize; i++){
      Serial.print(uid[i], HEX);
      Serial.print(" ");
    }
    Serial.println();
    cerrarBuzon();

    if(compid(uid, ownerUID)){
      Serial.println("Dueño reconocido: abriendo buzon");
      servo.write(90); 
      rfid.PICC_HaltA();
    } else if(compid(uid, repartidorUID)){
      Serial.println("Repartidor reconocido: abriendo para depositar la entrega");
      abrirBuzon(); 
      delay(2000);
      rfid.PICC_HaltA();
      cerrarBuzon();
    } else {
      Serial.println("ID desconocicdo");
      rfid.PICC_HaltA();
      cerrarBuzon();
    }
  }

  char key = keypad.getKey();
  if(key){
    if(key == '#'){
      if(pinact == pin){
        Serial.println("PIN correcto: abriendo buzon");
        abrirBuzon();
        pinact = "";
        intentosmalos = 0;
      } else {
        Serial.println("PIN incorrecto");
        pinact = "";
        intentosmalos++;
      }
    } else if(key == '*'){
      pinact = "";
    } else {
      pinact += key;
      Serial.print("PIN actual: "); Serial.println(pinact);
    }
  }

  if(millis() > cerrarautom && cerrarautom != 0){
    cerrarBuzon();
    cerrarautom = 0;
  }
}

void abrirBuzon(){
  servo.write(90); 
  cerrarautom = millis() + tabierto;
}

void cerrarBuzon(){
  servo.write(0); 
}

bool compid(byte *a, byte *b){
  for(int i=0;i<4;i++) if(a[i] != b[i]) return false;
  return true;
}