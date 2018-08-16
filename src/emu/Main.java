/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emu;

import chip8.Chip8;



public class Main extends Thread {
    
    static Chip8 chip8;
    private EmuFrame frame;
    public static int tickRate=540;
    
    public Main(){
        chip8 = new Chip8();
        chip8.init();
        chip8.loadDefault();
        //chip8.loadROM("C:\\Users\\Nonno\\Desktop\\PUZZLE");
        frame= new EmuFrame(chip8);
        chip8.isRunning=true;
        //run();
    }
    
    public void run(){
        while(true){
            if(chip8.isRunning==true){
                chip8.setKeyBuffer(frame.getKeyBuffer());
                chip8.run();
                if(chip8.needsRedraw()){
                    frame.repaint();
                    chip8.removeDrawFlag();
                }
                try{
                    long millis=(long) Math.floor(1000/tickRate);
                    int nanos=(int) ((Math.round(1000000000D/tickRate)) - (millis*1000000)); //not 100% accurate, doesn't count the time it took to interpret the instruction
                    //System.out.println("Sleeping for "+millis+" millis and "+nanos+" nanos");
                Thread.sleep(millis, nanos); //apparently this is not ideal
                }catch (InterruptedException e){/*unthrown*/}
            }
            else{
                System.out.print(""); //If not present the interpreter won't resume for some reason, needs fix
            }
        }
    }
    
    
    public static void main(String[] args){
        Main main = new Main();
        main.start();
    }
}
