/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emu;
import java.awt.Graphics;
import java.awt.Color;

import javax.swing.JPanel;
import chip8.Chip8;

public class ChipPanel extends JPanel{
    private Chip8 chip;
     public ChipPanel(Chip8 chip){ //assigns the chip to the panel
         this.chip = chip;
     }
     public void paint(Graphics g){
         byte[] frame = chip.getFrame(); //gets the frame from the chip8
         for(int i=0; i < frame.length ;i++){ //cycles through every pixel
             if(frame[i] == 1){
                g.setColor(Color.GREEN);
             }
             else{
                g.setColor(Color.BLACK);
             }
            int x = i % 64; //0<=x<=64 resets every line
            int y = (int)Math.floor(i/64);
            g.fillRect(x * 10, y * 10, 10, 10);
        }
    }
}
    
    

