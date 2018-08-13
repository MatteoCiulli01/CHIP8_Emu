/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emu;

import chip8.Chip8;



public class Main extends Thread {
    
    private Chip8 chip8;
    private EmuFrame frame;
    
    public Main(){
        System.out.println("forse non carica la rom");
        chip8 = new Chip8();
        chip8.init();
        chip8.loadDefault();
        //chip8.loadROM("res\\pong2.c8");
        frame= new EmuFrame(chip8);
        //run();
    }
    
    public void run(){
        while(true){
            chip8.setKeyBuffer(frame.getKeyBuffer());
            chip8.run();
            if(chip8.needsRedraw()){
                frame.repaint();
                chip8.removeDrawFlag();
            }
            try{
            Thread.sleep(8 , 333333);
            }catch (InterruptedException e){
                //unthrown
            }
        }
    }
    
    
    public static void main(String[] args){
        Main main = new Main();
        main.start();
    }
}
