/* -*-mode:java; c-basic-offset:2; -*- */
/* JCTerm
 * Copyright (C) 2002,2007 ymnk, JCraft,Inc.
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

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;

public class JCTermAWT  extends Panel implements KeyListener,  /*Runnable,*/ Term{

  static String COPYRIGHT=
"JCTerm 0.0.11\nCopyright (C) 2002,2007 ymnk<ymnk@jcraft.com>, JCraft,Inc.\n"+
"Official Homepage: http://www.jcraft.com/jcterm/\n"+
"This software is licensed under GNU LGPL.";

  private OutputStream out;
  private InputStream in;
  Emulator emulator=null;

  Connection connection=null;

  private Image img;
  //private Image background;
  private Graphics cursor_graphics;
  private Graphics graphics;
  private java.awt.Color defaultbground=Color.black;
  private java.awt.Color defaultfground=Color.white;
  private java.awt.Color bground=Color.black;
  private java.awt.Color fground=Color.white;
  private java.awt.Component term_area=null;
  private java.awt.Font font;

  private boolean bold=false;
  private boolean underline=false;
  private boolean reverse=false;

  private int term_width=80;
  private int term_height=24;

  private int x=0;
  private int y=0;
  private int descent=0;

  private int char_width;
  private int char_height;

  /*
  private String xhost="127.0.0.1";    
  private int xport=0;    
  private boolean xforwarding=false;
  private String user=System.getProperty("user.name");
  private String host="127.0.0.1";

  private String proxy_http_host=null;
  private int proxy_http_port=0;

  private String proxy_socks5_host=null;
  private int proxy_socks5_port=0;
  */

  private boolean antialiasing=true;
//  private int line_space=0;
  private int line_space=-2;
  private int compression=0;

  private Splash splash=null;

  private final Object[] colors={Color.black, Color.red, Color.green, 
				 Color.yellow,Color.blue,
				 Color.magenta, Color.cyan, Color.white};

  public JCTermAWT(){
    enableEvents(AWTEvent.KEY_EVENT_MASK);
    /*
    initGraphics();
    */
  }

  private void setFont(String fname){
    font=java.awt.Font.decode(fname);
    Image img=createImage(1, 1);
    Graphics graphics=img.getGraphics();
    graphics.setFont(font);
    {
      FontMetrics fo = graphics.getFontMetrics();
      descent=fo.getDescent();
//System.out.println(fo.getDescent());
//System.out.println(fo.getAscent());
//System.out.println(fo.getLeading());
//System.out.println(fo.getHeight());
//System.out.println(fo.getMaxAscent());
//System.out.println(fo.getMaxDescent());
//System.out.println(fo.getMaxDecent());
//System.out.println(fo.getMaxAdvance());
      char_width=(int)(fo.charWidth((char)'@'));
      char_height=(int)(fo.getHeight())+(line_space*2);   
//      descent+=line_space;
    }
    img.flush();
    graphics.dispose();
  }

  void initGraphics(){
    setFont("Monospaced-14");
    /*
    background=createImage(char_width, char_height);
    {
      Graphics foog=background.getGraphics();
      foog.setColor(getBackGround());
      foog.fillRect(0, 0, char_width, char_height);
      foog.dispose();
    }
    */

    img=createImage(getTermWidth(), getTermHeight());
    graphics=img.getGraphics();
    graphics.setFont(font);
    if(splash!=null) splash.draw(img, getTermWidth(), getTermHeight());
    else clear();
    cursor_graphics=(img.getGraphics());
    cursor_graphics.setColor(getForeGround());
    cursor_graphics.setXORMode(getBackGround());

    term_area=this;

    Panel panel=this;
    panel.setSize(getTermWidth(), getTermHeight());
    panel.setFocusable(true);
  }

  public void setFrame(java.awt.Component term_area){
    this.term_area=term_area;
  }

  private Thread thread=null;

  public void start(Connection connection){
    this.connection=connection;
    in=connection.getInputStream();
    out=connection.getOutputStream();
    emulator=new EmulatorVT100(this, in);
    emulator.reset();
    emulator.start();

    if(splash!=null) 
      splash.draw(img, getTermWidth(), getTermHeight());
    else 
      clear();
    redraw(0, 0, getTermWidth(), getTermHeight());
  }

  /*
  MyUserInfo ui=null;

  public void run(){
    while(thread!=null){
      try{
	try{
	}
	catch(Exception e){
          //System.out.println(e);
          break;
	}
        requestFocus();
	emulator=new EmulatorVT100(this, in);
	emulator.reset();
	emulator.start();
      }
      catch(Exception e){
	//e.printStackTrace();
      }
      break;
    }

    thread=null;

    if(splash!=null) 
      splash.draw(img, getTermWidth(), getTermHeight());
    else 
      clear();

    redraw(0, 0, getTermWidth(), getTermHeight());
  }
  */

  public void update(Graphics g){
    //System.out.println("update");
    if(img==null){
      initGraphics();
    }
    g.drawImage(img, 0, 0, term_area);
    //super.update(g);
  }
  public void paint(Graphics g){
    //System.out.println("paint");
    if(img==null){
      initGraphics();
    }
    g.drawImage(img, 0, 0, term_area);
    //super.paint(g);
  }

  public void processKeyEvent(KeyEvent e){
//System.out.println(e);
    int id=e.getID();
    if(id == KeyEvent.KEY_PRESSED) { keyPressed(e); }
    else if(id == KeyEvent.KEY_RELEASED) { /*keyReleased(e);*/ }
    else if(id == KeyEvent.KEY_TYPED) { keyTyped(e);/*keyTyped(e);*/ }
    e.consume(); // ??
  }

  byte[] obuffer=new byte[3];
  public void keyPressed(KeyEvent e){
    int keycode=e.getKeyCode();
    byte[] code=null;
    switch(keycode){
    case KeyEvent.VK_CONTROL:
    case KeyEvent.VK_SHIFT:
    case KeyEvent.VK_ALT:
    case KeyEvent.VK_CAPS_LOCK:
      return;
    case KeyEvent.VK_ENTER:
      code=emulator.getCodeENTER();
      break;
    case KeyEvent.VK_UP:
      code=emulator.getCodeUP();
      break;
    case KeyEvent.VK_DOWN:
      code=emulator.getCodeDOWN();
      break;
    case KeyEvent.VK_RIGHT:
      code=emulator.getCodeRIGHT();
      break;
    case KeyEvent.VK_LEFT:
      code=emulator.getCodeLEFT();
      break;
    case KeyEvent.VK_F1:
      code=emulator.getCodeF1();
      break;
    case KeyEvent.VK_F2:
      code=emulator.getCodeF2();
      break;
    case KeyEvent.VK_F3:
      code=emulator.getCodeF3();
      break;
    case KeyEvent.VK_F4:
      code=emulator.getCodeF4();
      break;
    case KeyEvent.VK_F5:
      code=emulator.getCodeF5();
      break;
    case KeyEvent.VK_F6:
      code=emulator.getCodeF6();
      break;
    case KeyEvent.VK_F7:
      code=emulator.getCodeF7();
      break;
    case KeyEvent.VK_F8:
      code=emulator.getCodeF8();
      break;
    case KeyEvent.VK_F9:
      code=emulator.getCodeF9();
      break;
    case KeyEvent.VK_F10:
      code=emulator.getCodeF10();
      break;
    case KeyEvent.VK_TAB:
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

    char keychar=e.getKeyChar();
    if((keychar&0xff00)==0){
      obuffer[0]=(byte)(e.getKeyChar());
      try{
        out.write(obuffer, 0, 1);
        out.flush();
      }
      catch(Exception ee){
      }
    }
  }

  public void keyTyped(KeyEvent e){
    char keychar=e.getKeyChar();
    if((keychar&0xff00)!=0){
      char[] foo=new char[1];
      foo[0]=keychar;
      try{
        byte[] goo=new String(foo).getBytes("EUC-JP");
        out.write(goo, 0, goo.length);
        out.flush();
      }
      catch(Exception eee){ }
    }
  }

  public int getTermWidth(){ return char_width*term_width; }
  public int getTermHeight(){ return char_height*term_height; }
  public int getCharWidth(){ return char_width; }
  public int getCharHeight(){ return char_height; }
  public int getColumnCount(){ return term_width; }
  public int getRowCount(){ return term_height; }

  public void clear(){
    graphics.setColor(getBackGround());
    graphics.fillRect(0, 0, char_width*term_width, char_height*term_height);
    graphics.setColor(getForeGround());
  }
  public void setCursor(int x, int y){
    this.x=x;
    this.y=y;
  }
  public void draw_cursor(){
    cursor_graphics.fillRect(x, y-char_height, char_width, char_height);
//    term_area.repaint(x, y-char_height, char_width, char_height);
    repaint(x, y-char_height, char_width, char_height);
  }    
  public void redraw(int x, int y, int width, int height){
//    term_area.repaint(x, y, width, height);
    repaint(x, y, width, height);
  }
  public void clear_area(int x1, int y1, int x2, int y2){
    /*
    for(int i=y1; i<y2; i+=char_height){
      for(int j=x1; j<x2; j+=char_width){
        graphics.drawImage(background, j, i, term_area); 
      }
    }
    */
    graphics.setColor(getBackGround());
    graphics.fillRect(x1, y1, x2-x1, y2-y1);
    graphics.setColor(getForeGround());
  }    
  public void scroll_area(int x, int y, int w, int h, int dx, int dy){
    graphics.copyArea(x, y, w, h, dx, dy);
  }
  public void drawBytes(byte[] buf, int s, int len, int x, int y){
//System.out.println("drawBytes: "+new String(buf, s, len)+" "+graphics);
//    clear_area(x, y, x+len*char_width, y+char_height);
//    graphics.setColor(getForeGround());
    graphics.drawBytes(buf, s, len, x, y-(descent+line_space));
    if(bold)
      graphics.drawBytes(buf, s, len, x+1, y-(descent+line_space));
    if(underline){
      // TODO
    }
  }
  public void drawString(String str, int x, int y){
//System.out.println("drawString: "+str);
//    clear_area(x, y, x+str.length()*char_width, y+char_height);
//    graphics.setColor(getForeGround());
    graphics.drawString(str, x, y-(descent+line_space));
    if(bold)
      graphics.drawString(str, x+1, y-(descent+line_space));
    if(underline){
      // TODO
    }
  }
  public void beep(){
    Toolkit.getDefaultToolkit().beep();
  }
  /*
  public class MyUserInfo implements UserInfo{
    public boolean promptYesNo(String str){
//      System.out.println(str);
      PromptDialog dialog=new PromptDialog("Warning",str);
      while(true){
	try{Thread.sleep(1000);}catch(Exception e){}
	if(!dialog.isVisible()){
	  break;
	}
      }
      return dialog.result;
    }

    String passwd=null;
    String passphrase=null;

    public String getPassword(){  return passwd; }
    public String getPassphrase(){ return passphrase; }

    public boolean promptPassword(String message){
      InputDialog dialog=new InputDialog(message, "", true);
      passwd=dialog.getText();
      return passwd!=null;
    }
    public boolean promptPassphrase(String message){
      InputDialog dialog=new InputDialog(message, "", true);
      passphrase=dialog.getText();
      return passphrase!=null;
    }
    public void showMessage(String message){
      MessageDialog msg=new MessageDialog("", message);
    }
  }
  */

  /** Ignores key released events. */
  public void keyReleased(KeyEvent event){}
//  public void keyPressed(KeyEvent event){}

/*
  public void setProxyHttp(String host, int port){
    proxy_http_host=host;
    proxy_http_port=port;
    if(proxy_http_host!=null && proxy_http_port!=0){
      proxy=new ProxyHTTP(proxy_http_host, proxy_http_port);
    }
    else{
      proxy=null;
    }
  }
  public String getProxyHttpHost(){return proxy_http_host;}
  public int  getProxyHttpPort(){return proxy_http_port;}

  public void setProxySOCKS5(String host, int port){
    proxy_socks5_host=host;
    proxy_socks5_port=port;
    if(proxy_socks5_host!=null && proxy_socks5_port!=0){
      proxy=new ProxySOCKS5(proxy_socks5_host, proxy_socks5_port);
    }
    else{
      proxy=null;
    }
  }
  public String getProxySOCKS5Host(){return proxy_socks5_host;}
  public int  getProxySOCKS5Port(){return proxy_socks5_port;}
  public void setXHost(String xhost){this.xhost=xhost;}
  public void setXPort(int xport){this.xport=xport;}
  public void setXForwarding(boolean foo){this.xforwarding=foo;}
*/
  public void setSplash(Splash foo){this.splash=foo;}
  public void setLineSpace(int foo){this.line_space=foo;}
  /*
  public void setAntiAliasing(boolean foo){
  }
  */
  public void setCompression(int compression){
    if(compression<0 || 9<compression) return;
    this.compression=compression;
  }
  public int getCompression(){return compression;}

  /*
  public static void main(String[] arg){
    JCTermAWT term=new JCTermAWT();
    Frame frame=new Frame("JCTermAWT");

    frame.addWindowListener(new WindowAdapter(){
      public void windowClosing(WindowEvent e){System.exit(0);}
    });

    MenuBar mb=term.getMenuBar();
    frame.setMenuBar(mb);

    frame.setSize(term.getTermWidth(), term.getTermHeight());
    frame.add("Center", term);

    frame.pack();
    term.setVisible(true);
    frame.setVisible(true);

    term.initGraphics();

    frame.setResizable(true);
    {
      Insets insets=frame.getInsets();
      int foo=term.getTermWidth();
      int bar=term.getTermHeight();
      foo+=(insets.left+insets.right);
      bar+=(insets.top+insets.bottom);
      frame.setSize(foo, bar);
    }
    frame.setResizable(false);

    term.setFrame(frame);
  }
  public void quit(){
    thread=null;
    if(session!=null){
      session.disconnect();
      session=null;
    }
  }
  */

  private java.awt.Color toColor(Object o){
    if(o instanceof String){
      return java.awt.Color.getColor((String)o);
    }
    if(o instanceof java.awt.Color){
      return (java.awt.Color)o;
    }
    return Color.white;
  }
  public void setDefaultForeGround(Object f){
    defaultfground=toColor(f);
  }
  public void setDefaultBackGround(Object f){
    defaultbground=toColor(f);
  }
  public void setForeGround(Object f){
    fground=toColor(f);
    graphics.setColor(getForeGround());
  } 
  public void setBackGround(Object b){
    bground=toColor(b);
  } 
  private java.awt.Color getForeGround(){ 
    if(reverse)
      return bground; 
    return fground; 
  }
  private java.awt.Color getBackGround(){ 
    if(reverse)
      return fground; 
    return bground; 
  }

  public Object getColor(int index){
    if(colors==null || index<0 || colors.length<=index)
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
    if(graphics!=null)
      graphics.setColor((java.awt.Color)getForeGround());
  }
  public void resetAllAttributes(){
    bold=false;
    underline=false;
    reverse=false;
    bground=defaultbground;
    fground=defaultfground;
    if(graphics!=null)
      graphics.setColor((java.awt.Color)getForeGround());
  }
}
