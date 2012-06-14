/* -*-mode:java; c-basic-offset:2; -*- */
/* JCTerm
 * Copyright (C) 2007 ymnk, JCraft,Inc.
 *  
 * Written by: ymnk<ymnk@jcaft.com>
 *   
 *   
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
   
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.jcraft.jcterm;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class JCTermSWT extends Canvas implements PaintListener,
    ControlListener, DisposeListener, KeyListener, Term{

  private static boolean xor_not_supported=SWT.getPlatform().equals("carbon");

  private OutputStream out;
  private InputStream in;
  private Emulator emulator=null;

  private Connection connection=null;

  private Image img;
  private GC graphics;

  private Image backgroundImg;

  private Color black=new Color(null, 0, 0, 0);
  private Color red=new Color(null, 255, 0, 0);
  private Color green=new Color(null, 0, 255, 0);
  private Color blue=new Color(null, 0, 0, 255);
  private Color yellow=new Color(null, 255, 255, 0);
  private Color magenta=new Color(null, 255, 0, 255);
  private Color cyan=new Color(null, 0, 255, 255);
  private Color white=new Color(null, 255, 255, 255);
  private final Object[] colors= {black, red, green, yellow, blue, magenta,
      cyan, white};

  private Color defaultbground=black;
  private Color defaultfground=white;
  private Color bground=black;
  private Color fground=white;
  private Font font;

  private boolean bold=false;
  private boolean underline=false;
  private boolean reverse=false;

  private int column=80;
  private int row=24;

  private int x=0;
  private int y=0;
  private int descent=0;

  private int char_width;
  private int char_height;
  private int term_width;
  private int term_height;

  private int leading;

  private int font_size=16;

  //private boolean antialiasing=true;
  //private int line_space=0;
  private int line_space=-2;
  private int compression=0;

  private Splash splash=null;

  public JCTermSWT(Composite parent){
    super(parent, SWT.NO_BACKGROUND);
    addPaintListener(this);
    addControlListener(this);
    addDisposeListener(this);
    addKeyListener(this);
  }

  public void paintControl(PaintEvent e){

    Display display=Display.getDefault();
    if(display==null){
      return;
    }

    if(img==null){
      Rectangle bounds=getBounds();
      setSize(display, bounds.width, bounds.height);
    }

    synchronized(this){
      e.gc.drawImage(img, 0, 0);
    }
  }

  public void controlMoved(ControlEvent e){
  }

  public void controlResized(ControlEvent e){
    //System.out.println("contorlResized");

    Display display=Display.getDefault();
    if(display==null){
      return;
    }

    Rectangle bounds=getBounds();

    if(bounds.width==0||bounds.height==0)
      return;

    setSize(display, bounds.width, bounds.height);
  }

  public void setFont(int size){
    Display display=Display.getDefault();
    if(display==null){
      return;
    }

    if(font!=null){
      font.dispose();
    }

    font=new Font(display, "Terminal", size, SWT.NORMAL);
    //font = new Font(display,"Fixed Width",size,SWT.NORMAL);
    graphics.setFont(font);
    FontMetrics fm=graphics.getFontMetrics();
    char_width=fm.getAverageCharWidth();
    char_height=fm.getHeight();
    leading=fm.getLeading();
    term_width=char_width*column;
    term_height=char_height*row;
    draw_cursor();
    font_size=size;

  }

  private void setSize(Display display, int w, int h){
    Image oimg=img;
    synchronized(this){
      if(graphics!=null){
        graphics.dispose();
      }

      ;
      img=new Image(display, w, h);
      graphics=new GC(img);
      graphics.setBackground(getBackGround());
      graphics.setForeground(getForeGround());
      graphics.fillRectangle(0, 0, w, h);
      if(backgroundImg!=null){
        graphics.drawImage(backgroundImg, 0, 0);
      }
      /*
          if(xor_not_supported){
          if(graphics.getTextAntialias()!=SWT.OFF)
          	graphics.setTextAntialias(SWT.OFF);
          }
      */
      if(font==null){
        setFont(font_size);
      }
      else{
        graphics.setFont(font);
      }
    }
    column=w/getCharWidth();
    row=h/getCharHeight();

    term_width=char_width*column;
    term_height=char_height*row;

    if(emulator!=null)
      emulator.reset();

    clear_area(0, 0, w, h);

    if(oimg!=null){
      synchronized(this){
        Rectangle clip=graphics.getClipping();
        graphics.setClipping(0, 0, getTermWidth(), getTermHeight());
        graphics.drawImage(oimg, 0, 0);
        graphics.setClipping(clip);
      }
    }

    if(connection!=null){
      connection.requestResize(this);
    }

    if(oimg!=null){
      oimg.dispose();
    }

  }

  public void widgetDisposed(DisposeEvent e){
    for(int i=0; i<colors.length; i++){
      ((Color)colors[i]).dispose();
    }
    if(img!=null){
      img.dispose();
      img=null;
    }
    if(graphics!=null){
      graphics.dispose();
      graphics=null;
    }

    if(font!=null){
      font.dispose();
      font=null;
    }

    if(backgroundImg!=null){
      backgroundImg.dispose();
      backgroundImg=null;
    }
  }

  public void setBackgroundImage(Image img){
    if(backgroundImg!=null){
      backgroundImg.dispose();
      backgroundImg=null;
    }
    backgroundImg=img;
  }

  public void beep(){
    final Display display=Display.getDefault();
    if(display==null){
      return;
    }

    display.asyncExec(new Runnable(){
      public void run(){
        display.beep();
      }
    });
  }

  public void clear(){
    synchronized(this){
      graphics.fillRectangle(0, 0, getTermWidth(), getTermHeight());
      if(backgroundImg!=null){
        Rectangle clip=graphics.getClipping();
        graphics.setClipping(0, 0, getTermWidth(), getTermHeight());
        graphics.drawImage(backgroundImg, 0, 0);
        graphics.setClipping(clip);
      }
    }
  }

  public void clear_area(int x1, int y1, int x2, int y2){
    synchronized(this){
      if(backgroundImg==null||reverse){
        graphics.fillRectangle(x1, y1, x2-x1, y2-y1);
      }
      else{
        Rectangle clip=graphics.getClipping();
        graphics.setClipping(x1, y1, x2-x1, y2-y1);
        graphics.drawImage(backgroundImg, 0, 0);
        graphics.setClipping(clip);
      }
    }
  }

  public void drawBytes(byte[] buf, int s, int len, int x, int y){
    synchronized(this){
      graphics.drawString(new String(buf, s, len), x, y-char_height, true);
      if(bold)
        graphics.drawString(new String(buf, s, len), x+1, y-char_height, true);
    }
  }

  public void drawString(String str, int x, int y){
    synchronized(this){
      graphics.drawString(str, x, y-char_height);
      if(bold)
        graphics.drawString(str, x+1, y-char_height);
    }
  }

  public void draw_cursor(){
    //System.out.println("draw_cursor: "+graphics);
    if(graphics==null)
      return;

    synchronized(this){
      if(!xor_not_supported){
        graphics.setXORMode(true);
        graphics.setBackground(white);
        graphics.fillRectangle(x, y-char_height, char_width, char_height);
        graphics.setBackground(getBackGround());
        graphics.setXORMode(false);
      }
    }
    repaint(x, y-char_height, char_width, char_height);
  }

  public void redraw(int x, int y, int width, int height){
    repaint(x, y, width, height);
  }

  private class RepaintHandler implements Runnable{
    int x;
    int y;
    int width;
    int height;
    Canvas canvas;

    RepaintHandler(Canvas canvas){
      this.canvas=canvas;
    }

    public void run(){
      if(!canvas.isDisposed())
        canvas.redraw(x, y, width, height, false);
    }
  }

  private RepaintHandler rh=new RepaintHandler(this);

  private void repaint(final int x, final int y, final int width,
      final int height){
    Display display=Display.getDefault();
    if(display==null)
      return;

    rh.x=x;
    rh.y=y;
    rh.width=width;
    rh.height=height;
    display.syncExec(rh);
  }

  public void scroll_area(int x, int y, int w, int h, int dx, int dy){
    synchronized(this){
      graphics.copyArea(x, y, w, h, x+dx, y+dy);
    }
    repaint(x+dx, y+dy, w, h);
  }

  public void setCursor(int x, int y){
    this.x=x;
    this.y=y;
  }

  public void start(Connection connection){
    this.connection=connection;
    in=connection.getInputStream();
    out=connection.getOutputStream();
    emulator=new EmulatorVT100(this, in);
    emulator.reset();
    emulator.start();

    try{
      if(splash!=null)
        splash.draw(img, getTermWidth(), getTermHeight());
      else
        clear();

      redraw(0, 0, getTermWidth(), getTermHeight());
    }
    catch(Exception e){

    }
  }

  private byte[] buf=new byte[1];

  public void keyPressed(KeyEvent e){
    //System.out.println("keyPressed: "+emulator+" "+graphics+" "+img);
    if(emulator==null)
      return;

    int stateMask=e.stateMask;
    int keycode=e.keyCode;
    //System.out.println("keycode: "+keycode); 
    byte[] code=null;
    switch(keycode){
      case SWT.CONTROL:
      case SWT.SHIFT:
      case SWT.ALT:
        //case KeyEvent.VK_CAPS_LOCK:
        return;
      case SWT.CR:
        code=emulator.getCodeENTER();
        break;
      case SWT.ARROW_UP:
        code=emulator.getCodeUP();
        break;
      case SWT.ARROW_DOWN:
        code=emulator.getCodeDOWN();
        break;
      case SWT.ARROW_RIGHT:
        code=emulator.getCodeRIGHT();
        break;
      case SWT.ARROW_LEFT:
        code=emulator.getCodeLEFT();
        break;
      case SWT.F1:
        code=emulator.getCodeF1();
        break;
      case SWT.F2:
        code=emulator.getCodeF2();
        break;
      case SWT.F3:
        code=emulator.getCodeF3();
        break;
      case SWT.F4:
        code=emulator.getCodeF4();
        break;
      case SWT.F5:
        code=emulator.getCodeF5();
        break;
      case SWT.F6:
        code=emulator.getCodeF6();
        break;
      case SWT.F7:
        code=emulator.getCodeF7();
        break;
      case SWT.F8:
        code=emulator.getCodeF8();
        break;
      case SWT.F9:
        code=emulator.getCodeF9();
        break;
      case SWT.F10:
        code=emulator.getCodeF10();
        break;
      case SWT.TAB:
        code=emulator.getCodeTAB();
        break;
    }
    if(code!=null){
      try{
        out.write(code, 0, code.length);
        out.flush();
      }
      catch(Exception ee){
      }
      return;
    }
    /*
    SWT.SHIFT
    SWT.ALT
    SWT.ARROW_DOWN
    SWT.ARROW_LEFT
    SWT.ARROW_RIGHT
    SWT.ARROW_UP
    SWT.BS
    SWT.CONTROL / SWT.CTRL
    SWT.CTRL
    SWT.DEL
    SWT.ESC
    SWT.F1 - SWT.F12
    SWT.CR <- RETURN
    */

    char character=e.character;
    //System.out.println("character: " + character);
    buf[0]=(byte)(character);
    try{
      out.write(buf, 0, 1);
      out.flush();
    }
    catch(Exception ee){
    }
  }

  public void keyReleased(KeyEvent e){
  }

  public int getTermWidth(){
    return term_width;
  }

  public int getTermHeight(){
    return term_height;
  }

  public int getCharWidth(){
    return char_width;
  }

  public int getCharHeight(){
    return char_height;
  }

  public int getColumnCount(){
    return column;
  }

  public int getRowCount(){
    return row;
  }

  public void setSplash(Splash foo){
    this.splash=foo;
  }

  public void setLineSpace(int foo){
    this.line_space=foo;
  }

  public void setCompression(int compression){
    if(compression<0||9<compression)
      return;
    this.compression=compression;
  }

  private Color toColor(Object o){
    if(o instanceof Color){
      return (Color)o;
    }
    return defaultfground;
  }

  public void setDefaultForeGround(Object f){
    defaultfground=toColor(f);
  }

  public void setDefaultBackGround(Object f){
    defaultbground=toColor(f);
  }

  public void setForeGround(Object f){
    fground=toColor(f);
    synchronized(this){
      graphics.setForeground(getForeGround());
      graphics.setBackground(getBackGround());
    }
  }

  public void setBackGround(Object b){
    bground=toColor(b);
    synchronized(this){
      graphics.setForeground(getForeGround());
      graphics.setBackground(getBackGround());
    }
  }

  private Color getForeGround(){
    if(reverse)
      return bground;
    return fground;
  }

  private Color getBackGround(){
    if(reverse)
      return fground;
    return bground;
  }

  public Object getColor(int index){
    if(colors==null||index<0||colors.length<=index)
      return null;
    return colors[index];
  }

  public void setBold(){
    bold=true;
  }

  public void setUnderline(){
    underline=true;
  }

  public void setReverse(){
    reverse=true;
    synchronized(this){
      if(graphics!=null){
        graphics.setForeground(getForeGround());
        graphics.setBackground(getBackGround());
      }
    }
  }

  public void resetAllAttributes(){
    bold=false;
    underline=false;
    reverse=false;
    bground=defaultbground;
    fground=defaultfground;
    synchronized(this){
      if(graphics!=null){
        graphics.setForeground(getForeGround());
        graphics.setBackground(getBackGround());
      }
    }
  }
}
