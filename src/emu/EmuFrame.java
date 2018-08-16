/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emu;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import chip8.Chip8;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class EmuFrame extends JFrame implements KeyListener{
   
    private ChipPanel panel;
    private int[] keyBuffer;
    private int[] keyIDs;
    private JMenuBar menuBar;
    private JMenu mfile,moptions,mhelp, mspeed;
    private JMenuItem iloadrom,iexit;
    private JMenuItem i1000, i500, i250;
    final JFileChooser fc = new JFileChooser();
    
    
    
    
    public EmuFrame(Chip8 c){
        
        menuBar = new JMenuBar();
        mfile=new JMenu("File");
        iloadrom=new JMenuItem(new AbstractAction("Load ROM...") {
            public void actionPerformed(ActionEvent ae) {
                Main.chip8.isRunning=false;
                if(fc.showOpenDialog(panel)==0){
                    Main.chip8.init();
                    Main.chip8.loadROM(fc.getSelectedFile().getPath());
                    Main.chip8.isRunning=true;
                }
                else{
                    Main.chip8.isRunning=true;
                }
            }});
        iexit=new JMenuItem(new AbstractAction("Exit") {
            public void actionPerformed(ActionEvent ae) {
                System.exit(0);
            }});
        mfile.add(iloadrom);
        mfile.add(iexit);
        menuBar.add(mfile);
        
        
        
        setPreferredSize(new Dimension(640,320));
        pack(); //create the frame
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(false);
        setJMenuBar(menuBar);
        setPreferredSize(new Dimension(640 + getInsets().left + getInsets().right ,320 + getInsets().top + 23 + getInsets().bottom)); //add borders to the total dimension of the frame
        panel=new ChipPanel(c); //create the canvas
        setLayout(new BorderLayout()); //set the layout manager so you can use EmuPanel
        add(panel, BorderLayout.CENTER); //aline the center of panel with the center of the frame and adds it
        setDefaultCloseOperation(EXIT_ON_CLOSE); //close
        setTitle("CHIP-8 Emulator (Running @"+Main.tickRate+" Hz)");
        pack(); //reset the frame
        setVisible(true); //show the frame
        addKeyListener(this);
        
        
        keyBuffer = new int[16];
        keyIDs = new int[1024];
        
        setIDs();
        
    }
    public void setIDs(){
        for(int i=0;i<1024;i++){
            keyIDs[i]=-1;
        }
        keyIDs['X'] = 0x0;
        keyIDs['1'] = 0x1;
        keyIDs['2'] = 0x2;
        keyIDs['3'] = 0x3;
        keyIDs['Q'] = 0x4;
        keyIDs['W'] = 0x5;
        keyIDs['E'] = 0x6;
        keyIDs['A'] = 0x7;
        keyIDs['S'] = 0x8;
        keyIDs['D'] = 0x9;
        keyIDs['Z'] = 0xA;
        keyIDs['C'] = 0xB;
        keyIDs['4'] = 0xC;
        keyIDs['R'] = 0xD;
        keyIDs['F'] = 0xE;
        keyIDs['V'] = 0xF;
    }
    
    @Override
    public void keyReleased(KeyEvent e){
        if(keyIDs[e.getKeyCode()] != -1){
            keyBuffer[keyIDs[e.getKeyCode()]] = 0;
        }
    }
    
    @Override
    public void keyPressed(KeyEvent e){
        if(keyIDs[e.getKeyCode()] != -1){
            keyBuffer[keyIDs[e.getKeyCode()]] = 1;
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e){}
    
    public int[] getKeyBuffer() {
        return keyBuffer;
    }
}
