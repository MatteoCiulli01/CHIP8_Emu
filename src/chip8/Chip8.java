/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chip8;

import java.awt.Toolkit;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;




public class Chip8 {
    public boolean isRunning = false;
    
   private char memory[]; //Main memory
   private char V[]; //registers
   private char stack[]; //stack
   
   private char I;  //Memory Pointer
   private char ip; //Instuction Pointer (Program Counter), starts at 0x200
   private char sp; //stack pointer
   
   private int delay_timer; //sleep until it reaches 0 (?)
   private int sound_timer; //beeps until reaches 0
   
   private byte[] keys; //input variable
   private byte[] frame; //stores the frame to be printed, need to upscale
   
   private boolean needsRedraw;
   
   private Random randomizer = new Random();
   
   //used to get accurate timings
   private long currentInstruction=0;
   private long lastInstruction;
   //used to tick the timers
   private long currentTick=0;
   private long lastTick=0;
   
   public void init(){
       memory = new char[4096];
       V = new char[16];
       stack = new char[16];
       
       I=0x0;
       ip=0x200;
       sp=0x0;
       
       delay_timer=0;
       sound_timer=0;
       
       keys=new byte[16];
       frame=new byte[64*32];
       
       needsRedraw = false;
       
       loadFont();
   }
   
   public void run() {
       //fetch
       lastInstruction=currentInstruction;
       currentInstruction=System.nanoTime();
       char opcode = (char)((memory[ip] << 8) | memory[ip+1]);
       System.out.println(Integer.toHexString(opcode) + ": ");
       //decode & execute
        switch(opcode & 0xF000){
        case 0x0000:
            switch(opcode & 0x0FFF){
                case 0x00E0: //0x00E0 Clears the screen.
                    for(int i=0;i<frame.length;i++){
                        frame[i]=0;
                    }
                    ip+=2;
                    break;
                case 0x00EE: //0x00EE Returns from a subroutine.
                    ip=stack[--sp];//set the ip to go to the prev routine
                    ip+=2; //next instruction
                    break;
                default:
                    System.exit(0);
            }
            break;
        case 0x1000: //0x1NNN Jumps to address NNN
            ip=(char)(opcode&0x0FFF);
            break;
        case 0x2000:    //0x2NNN calls subroutine at NNN
            stack[sp++]=ip; //save the current instruction in the stack;
            ip=(char)(opcode & 0x0FFF); //sets the ip to run the subroutine
            break;
        case 0x3000:    //0x3XNN Skips the next instruction if VX equals NN.
            if(V[(opcode & 0x0F00)>>8] == (char)(opcode&0x00FF)){ //sets the value
                ip+=4;
            }else{
                ip+=2;
            }
            break;
        case 0x4000: //0x04XNN Skips the next instruction if VX doesn't equal NN
            if(V[(opcode&0x0F00)>>8]!= (char)(opcode&0x00FF)){
                ip+=4;
            }else{
                ip+=2;
            }
            break;
        case 0x5000: //0x5XY0 Skips the next instruction if VX equals VY.
            if(V[(opcode&0x0F00)>>8]== V[(opcode&0x00F0)>>4]){
                ip+=4;
            }else{
                ip+=2;
            }
            break;
        case 0x6000: //0x6XNN Sets VX to NN
            V[(opcode & 0x0F00)>>8] = (char)(opcode&0x00FF); //sets the value
            ip+=2;
            break;
        case 0x7000:{ //0x7XNN Adds NN to VX.
                int X=(opcode & 0x0F00)>>8;//find X
                int NN=opcode&0x00FF;
                V[X]=(char)((V[X]+NN)&0xFF);
                ip+=2;
                break;
            }
        case 0x8000:
            int regx= (opcode&0x0F00)>>8;
            int regy= (opcode & 0x00F0)>>4;
            switch(opcode&0x000F){
                case 0x0000: //0x8XY0 Sets VX to the value of VY.
                    V[regx]=V[regy];
                    ip+=2;
                    break;
                case 0x0001: //0x8XY1 Sets VX to VX or VY
                    V[regx]=(char)((V[regx]|V[regy]) & 0xFF);;
                    ip+=2;
                    break;
                case 0x0002: //0x8XY2 Sets VX to VX and VY
                    V[regx]=(char)((V[regx]&V[regy]) & 0xFF);;
                    ip+=2;
                    break;
                case 0x0003: //0x8XY3 Sets VX to VX xor VY
                    V[regx]=(char)((V[regx]^V[regy]) & 0xFF);;
                    ip+=2;
                    break;
                case 0x0004: //0x8XY4 Adds VY to VX. VF is set to 1 when there's a carry, and to 0 when there isn't.
                    if(V[regy]> 0xFF - V[regx]){
                       V[0xF]=1; 
                    }else{
                        V[0xF]=0;
                    }
                    V[regx]=(char)((V[regx]+V[regy]) & 0xFF); //sums and clears the excess
                    ip+=2;
                    break;
                case 0x0005: //0x8XY5 VY is subtracted from VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
                    if(V[regx]>V[regy]){
                        V[0xF]=1; 
                    }else{
                        V[0xF]=0;
                    }
                    V[regx]=(char)((V[regx]-V[regy]) & 0xFF); //subtract and clears the sign
                    ip+=2;
                    break;
                case 0x0006: //0x8X06 Stores the least significant bit of VX in VF and then shifts VX to the right by 1.
                    V[0xF]=(char)(V[(opcode & 0x0F00)>>8]&0x01);
                    V[(opcode & 0x0F00)>>8]>>=1;
                    ip+=2;
                    break;
                case 0x0007: //0x8XY7 Sets VX to VY minus VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
                    if(V[regy]>V[regx]){
                        V[0xF]=1; 
                    }else{
                        V[0xF]=0;
                    }
                    V[regx]=(char)((V[regy]-V[regx]) & 0xFF); //subtract and clears the sign
                    ip+=2;
                    break;
                case 0x000E: //0x8X0E Stores the most significant bit of VX in VF and then shifts VX to the left by 1.
                    V[0xF]=(char)(V[(opcode & 0x0F00)>>8]&0x80);
                    V[(opcode & 0x0F00)>>8]<<=1;
                    ip+=2;
                    break;   
            }
            break;
        case 0x9000: //0x9XY0 Skips the next instruction if VX doesn't equal VY
            if(V[(opcode&0x0F00)>>8]!=V[(opcode&0x00F0)>>4]){
                ip+=4;
            }else{
                ip+=2;
            }
            break;
        case 0xA000: //0xANNN sets I to the address NNN.
            I=(char)(opcode & 0x0FFF);
            ip+=2;
            break;
        case 0xB000: //0xBNNN Jumps to the address NNN plus V0
            ip=(char)((opcode&0x0FFF)+(int)(V[0]&0xFF));
            break;
        case 0xC000: //0xCXNN Sets VX to the result of a bitwise AND operation on a random number (Typically: 0 to 255) and NN.
            int rand=randomizer.nextInt(256);   //gets a random number
            V[(opcode & 0x0F00)>>8] = (char) (rand & (opcode & 0x0FF));
            ip+=2;
            break;
        case 0xD000: //0xDXYN Draws a sprite at (X, Y), size(8, N).If pixel is flipped, VF=1
            int x = V[(opcode & 0x0F00)>>8];
            int y = V[(opcode & 0x00F0)>>4];
            int N = opcode & 0x000F;
            V[0xf]=0;
            for(int _y=0; _y<N;_y++){ //Scans N lines of the screen
                int line=(memory[(int)I+_y]); //gets the first line of the sprite
                for (int _x=0;_x<8;_x++){ //for each column
                    int dot = line & (0x80 >> _x); //calculates the dot
                    if(dot!=0){
                        int absY=(y+_y)%32;
                        int absX=(x+_x)%64;
                        int index = absY * 64 + absX;//calculates coords
                        if(frame[index]==1)
                            V[0xF]=1;
                        frame[index]^=1;//flips the dot
                    }
                }
            }
            needsRedraw = true;
            ip += 2;//next instuction
            //System.out.println("Drawing at V[" + ((opcode & 0x0F00) >> 8) + "] = " + x + ", V[" + ((opcode & 0x00F0) >> 4) + "] = " + y);
            break;
        case 0xE000:
            int key=V[(opcode & 0x0F00)>>8];
            switch(opcode & 0x00FF){
                case 0x009E: //0xEX9E Skips the next instruction if the key stored in VX is pressed
                    if(keys[key]==1)
                        ip+=2;
                    break;
                case 0x00A1: //0xEXA1 Skips the next instruction if the key stored in VX isn't pressed
                    if(keys[key]!=1)
                        ip+=2;
                    break;
            }
            ip+=2; //next instruction
            break;
        case 0xF000:
            switch(opcode & 0x00FF){
                case 0x0007: //0x0X07 Sets VX to the value of the delay timer.
                    V[(opcode & 0x0F00)>>8]=(char)(delay_timer&0xFF);
                    ip+=2;
                    break;
                case 0x000A: //0x0X0A A key press is awaited, and then stored in VX.
                    for(int i=0;i<keys.length;i++){
                        if((keys[i]&0x0F) ==1){
                            V[(opcode&0x0F00)>>8]=(char)i;
                            ip+=2;
                            break;
                        }
                    }
                    break;
                case 0x0015: //0x0X15 Sets the delay timer to VX
                    delay_timer=(char) (V[(opcode & 0x0F00)>>8] &0xFF);
                    ip+=2;
                    break;
                case 0x0018: //0x0X18 Sets the sound timer to VX
                    sound_timer=V[(opcode&0x0F00)>>8];
                    ip+=2;
                    break;
                case 0x001E: //0x0X1E Adds VX to I.
                    //System.out.println("Adding ["+ (int)V[(opcode&0x0F00)>>8]+"] to [0x" + Integer.toHexString(I)+"]");
                    I+=V[(opcode&0x0F00)>>8];
                    ip+=2;
                    break;
                case 0x0029: //0x0X29 Sets I to the location of the sprite for the character in VX.
                    int charreg = (opcode & 0x0F00)>>8;
                    I=(char)(0x50+(V[charreg]*5)&0xFF);
                    ip+=2;
                    break;
                case 0x0033: //0x0X33 Store a binary-coded decimal value VX in I, I + 1 and I + 2
                    int num=V[(opcode&0x0F00)>>8];
                    int hundreds = (num - (num % 100)) / 100;
                    num -= hundreds * 100;
                    int tens = (num - (num % 10))/ 10;
                    num -= tens * 10;

                    memory[I+2]=(char)num;
                    memory[I+1]=(char)tens;
                    memory[I]=(char)hundreds;
                    ip+=2;
                    break;
                case 0x0055: //0x0X55 Stores V0 to VX (including VX) in memory starting at address I.
                    for(int i=0; i<=(opcode&0x0F00)>>8;i++){
                        memory[I+i]=V[i];
                    }
                    ip+=2;
                    break;
                case 0x0065: //0x0X65 Fills V0 to VX (including VX) with values from memory starting at address I.
                    int reg = (opcode&0x0F00)>>8; //gets the reg
                    for(int i=0;i<=reg; i++){
                        V[i]=memory[I+i];   //fils each reg
                    }
                    ip+=2; //next instuction
                    break;
                default:
                    break;
            }
            break;
        default:
            System.err.println("Unsupported Opcode");
            System.exit(0);  
       }
       tickTimers();
       
   }
   
   private void tickTimers(){
       currentTick=System.nanoTime();
       if(currentTick-lastTick >= 16666667){ //ticks every 60 hz
           //System.err.println("Ticked");
           lastTick=currentTick;
            if(sound_timer > 0){
                if(Audio.isRunning==false)
                    Audio.playSound("/res/beep.wav");
                sound_timer--;
            }
            else{
                if(Audio.isRunning==true)
                Audio.stopSound();
            }

            if(delay_timer > 0)
                delay_timer--;
       }
   }
   
   public byte[] getFrame(){
        return frame;
    }

    public boolean needsRedraw() {
        return needsRedraw;
    }

    public void removeDrawFlag() {
        needsRedraw = false;
    }
    
    public void loadDefault(){
        InputStream input = Chip8.class.getResourceAsStream("/res/pong2.c8");
        try{
            DataInputStream rom = new DataInputStream(input);
            int offset=0;
            while(rom.available() > 0){
                memory[0x200+offset] = (char)(rom.readByte() & 0xFF);
                offset++;
        }
            }catch(IOException e){
            System.exit(0);
        }
    }
    
    public void loadROM(String file) {
        DataInputStream rom = null;
        try{
            rom = new DataInputStream(new FileInputStream(new File(file)));
            int offset=0;
            while(rom.available() > 0){
                memory[0x200+offset] = (char)(rom.readByte() & 0xFF);
                offset++;
        }
        }catch(IOException e){
            //TODO
            System.exit(0);
        }finally{
            if(rom != null){
                try{rom.close();}catch(IOException ex){}
            }
        }
    }
    
    public void loadFont(){
        for(int i=0;i<Chip8Resources.fontset.length;i++){
            memory[0x50+i]=(char) (Chip8Resources.fontset[i]&0xFF);
        }
    }

    public void setKeyBuffer(int[] keyBuffer) {
        for(int i = 0; i < keys.length; i++) {
            keys[i] = (byte)keyBuffer[i];
        }
    }
}
