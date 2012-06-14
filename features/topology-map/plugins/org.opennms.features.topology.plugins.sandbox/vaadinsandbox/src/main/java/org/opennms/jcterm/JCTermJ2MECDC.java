/* -*-mode:java; c-basic-offset:2; -*- */
/* JCTermJ2MECDC
 * Copyright (C) 2004,2007 ymnk, JCraft,Inc.
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

import com.jcraft.jsch.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;

public class JCTermJ2MECDC  extends Panel implements KeyListener, ActionListener, Runnable, Term{
  static String COPYRIGHT=
"JCTerm 0.0.11\nCopyright (C) 2002,2007 ymnk<ymnk@jcraft.com>, JCraft,Inc.\n"+
"Official Homepage: http://www.jcraft.com/jcterm/\n"+
"This software is licensed under GNU LGPL.\n"+
"This software is using JSch(http://www.jcraft.com/jsch/) and\n"+
"BouncyCastle Crypto API(http://www.bouncycastle.org/).";

  private static int windowWidth = 640;
  private static int windowHeight = 480;
  private static int titleHeight = 13;
  private static int taskbarHeight = 19;
  private static int margin = 2;

  private static final int SHELL=0;
  private static final int SFTP=1;
  private static final int EXEC=2;

  private int mode=SHELL;

  private boolean reverse=false;

  private OutputStream out;
  private InputStream in;

  private Image img;
  private Image background;
  private Graphics cursor_graphics;
  private Graphics graphics;

  private java.awt.Color defaultbground=Color.black;
  private java.awt.Color defaultfground=Color.white;
  private java.awt.Color bground=Color.black;
  private java.awt.Color fground=Color.white;

  private java.awt.Component term_area=null;
  private java.awt.Font font;

  private int term_width=80;
  private int term_height=24;

  private int x=0;
  private int y=0;
  private int descent=0;

  private int char_width;
  private int char_height;

  private String xhost="127.0.0.1";    
  private int xport=0;    
  private boolean xforwarding=false;
  private String user=System.getProperty("user.name");
  private String host="127.0.0.1";
  private int port=22;

  private String command="";

  private String proxy_http_host=null;
  private int proxy_http_port=0;

  private String proxy_socks5_host=null;
  private int proxy_socks5_port=0;

  private Session session=null;
  private Proxy proxy=null;

  private boolean antialiasing=true;
  private int line_space=0;
//  private int line_space=-2;
  private int compression=0;

  private Splash splash=null;

  public JCTermJ2MECDC(){
    enableEvents(AWTEvent.KEY_EVENT_MASK);
  }

  void initGraphics(){
    font=new java.awt.Font("monospaced", Font.PLAIN, 16);
/**/
//    img=createImage(1, 1);
//    graphics=img.getGraphics();
    graphics=getGraphics();
    graphics.setFont(font);
    {
      FontMetrics fo = graphics.getFontMetrics(font);
      descent=fo.getDescent();
      char_width=(int)(fo.charWidth((char)'@'));
      char_height=(int)(fo.getHeight())+(line_space*2);   
//char_width=8;
//char_height=16;
    }
//    img.flush();
//    graphics.dispose();
/**/
//System.out.println("char_width: "+char_width+" "+char_height);

    background=createImage(char_width, char_height);
    {
      Graphics foog=background.getGraphics();
      foog.setColor(bground);
      foog.fillRect(0, 0, char_width, char_height);
      foog.dispose();
    }

    img=createImage(getTermWidth(), getTermHeight());
    graphics=img.getGraphics();
    graphics.setFont(font);

    if(splash!=null) splash.draw(img, getTermWidth(), getTermHeight());
    else clear();

    cursor_graphics=(img.getGraphics());
    cursor_graphics.setColor(fground);
    cursor_graphics.setXORMode(bground);

    term_area=this;

    Panel panel=this;
    panel.setSize(getTermWidth(), getTermHeight());
  }

  public void setFrame(java.awt.Component term_area){
    this.term_area=term_area;
  }

  private Thread thread=null;
  public void kick(){
    this.thread=new Thread(this);
    this.thread.start();
  }

  MyUserInfo ui=null;
  JSch jsch=null;
  private Emulator emulator=null;
  public void run(){
    if(jsch==null){ 
      jsch=new JSch(); 
      String home=System.getProperty("user.home")+File.separator;
      home=home+".ssh"+File.separator;
      try{
	jsch.setKnownHosts(home+"known_hosts");
	File file=new java.io.File(home, "id_dsa");
	if(file.exists())
	  jsch.addIdentity(file.getPath());
	file=new java.io.File(home, "id_rsa");
	if(file.exists())
	  jsch.addIdentity(file.getPath());
      }
      catch(Exception e){
      }
    }

    while(thread!=null){
      try{

	try{
  	  session=jsch.getSession(user, host, port);
	  session.setProxy(proxy);

	  ui=new MyUserInfo();
	  session.setUserInfo(ui);

	  java.util.Properties config=new java.util.Properties();
	  if(compression==0){
	    config.put("compression.s2c", "none");
	    config.put("compression.c2s", "none");
	  }
	  else{
	    config.put("compression.s2c", "zlib,none");
	    config.put("compression.c2s", "zlib,none");
	  }
	  session.setConfig(config);

	  session.connect();
	}
	catch(Exception e){
          //System.out.println(e);
          break;
	}

	Channel channel=null;

	if(mode==SHELL){
          channel=session.openChannel("shell");
	  if(xforwarding){
 	    session.setX11Host(xhost);
	    session.setX11Port(xport+6000);
 	    channel.setXForwarding(true);
	  }

	  out=channel.getOutputStream();
	  in=channel.getInputStream();

	  channel.connect();
	}
	else if(mode==SFTP){

	  out=new PipedOutputStream();
	  in=new PipedInputStream();
	  channel=session.openChannel("sftp");
	  channel.connect();

	  (new Sftp((ChannelSftp)channel, 
		    (InputStream)(new PipedInputStream((PipedOutputStream)out)),
		    new PipedOutputStream((PipedInputStream)in))).kick();
	}
	else if(mode==EXEC){
	  channel=session.openChannel("exec");
	  ((ChannelExec)channel).setCommand(command);
	  InputStream in=channel.getInputStream();
	  OutputStream out=channel.getOutputStream();
	  channel.connect();

	  byte[] buf=new byte[1024];
	  StringBuffer sb=new StringBuffer();
	  while(true){
	    int i=in.read(buf,0,buf.length);
	    if(i<=0)break;
	    sb.append(new String(buf, 0, i));
	  }
	  channel.disconnect();
	  MessageDialog msg=new MessageDialog(command, sb.toString());
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

    if(session!=null){
      session.disconnect();
      session=null;
    }

    if(splash!=null) splash.draw(img, getTermWidth(), getTermHeight());
    else clear();

    redraw(0, 0, getTermWidth(), getTermHeight());
  }

  public void paint(Graphics g){
    super.paint(g);
    if(img!=null){ 
      g.drawImage(img, 0, 0, term_area); 
    }
  }

  public void processKeyEvent(KeyEvent e){
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
    int mod=e.getModifiers();

    if((mod&InputEvent.CTRL_MASK)!=0){
      if('a'<=keychar && keychar<='z'){
	keychar=(char)((keychar-'a')+1);
      }
    }

    if((keychar&0xff00)==0){
      obuffer[0]=(byte)(keychar);
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
//    synchronized(graphics){
    graphics.setColor(bground);
    graphics.fillRect(0, 0, char_width*term_width, char_height*term_height);
    graphics.setColor(fground);

    getGraphics().drawImage(img, 0, 0, term_area);

//    }
  }
  public void setCursor(int x, int y){
    this.x=x;
    this.y=y;
  }
  public void draw_cursor(){
    cursor_graphics.fillRect(x, y-char_height, char_width, char_height);
//    term_area.repaint(x, y-char_height, char_width, char_height);

//  repaint(x, y-char_height, char_width, char_height);
//  getGraphics().drawImage(img, 0, 0, term_area);

    Graphics g=getGraphics();
    g.setClip(x, y-char_height, char_width, char_height);
//    synchronized(graphics){
    g.drawImage(img, 0, 0, term_area);
//    }
  }    
  public void redraw(int x, int y, int width, int height){
//    term_area.repaint(x, y, width, height);

//    repaint(x, y, width, height);
// getGraphics().drawImage(img, 0, 0, term_area);

    Graphics g=getGraphics();
    g.setClip(x, y, width, height);
//    synchronized(graphics){
    g.drawImage(img, 0, 0, term_area);
//    }
  }

  public void clear_area(int x1, int y1, int x2, int y2){
    /*
    for(int i=y1; i<y2; i+=char_height){
      for(int j=x1; j<x2; j+=char_width){
        graphics.drawImage(background, j, i, term_area); 
      }
    }
    */
//    synchronized(graphics){
    graphics.setColor(bground);
    graphics.fillRect(x1, y1, x2-x1, y2-y1);
    graphics.setColor(fground);

    /*
    Graphics g=getGraphics();
    g.setColor(bground);
    g.fillRect(x1, y1, x2-x1, y2-y1);
    g.setColor(fground);
    */

//    }
  }    
  public void scroll_area(int x, int y, int w, int h, int dx, int dy){
    getGraphics().copyArea(x, y, w, h, dx, dy);
//    synchronized(graphics){
    graphics.copyArea(x, y, w, h, dx, dy);
//    }
  }
  public void drawBytes(byte[] buf, int s, int len, int x, int y){
//System.out.println("drawBytes: s="+s+",x="+x+",y="+y+" "+new String(buf, s, len));
if(s>0){
  byte[] foo=new byte[len];
  System.arraycopy(buf, s, foo, 0, len);
  s=0;
  buf=foo;
}
if(len!=buf.length){
  byte[] foo=new byte[len];
  System.arraycopy(buf, 0, foo, 0, len);
  buf=foo;
}

//System.out.print  ("drawBytes: ");
//for(int i=s; i<s+len; i++){
//System.out.print(Integer.toHexString(buf[i]&0xff)+":");
//}
//System.out.println("");

    graphics.setColor(bground);
    clear_area(x, y, x+len*char_width, y+char_height);
    graphics.setColor(fground);

//synchronized(graphics){
    graphics.drawBytes(buf, s, len, x, y-(descent+line_space));
//}
//getGraphics().drawBytes(buf, s, len, x, y-(descent+line_space));
  }
  public void drawString(String str, int x, int y){
//System.out.println("drawString: "+str);
//    clear_area(x, y, x+str.length()*char_width, y+char_height);
//    graphics.setColor(fground);
//    synchronized(graphics){
    graphics.drawString(str, x, y-(descent+line_space));
//    }
  }
  public void beep(){
    Toolkit.getDefaultToolkit().beep();
  }
  public class MyUserInfo implements UserInfo{
    public boolean promptYesNo(String str){
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

  /** Ignores key released events. */
  public void keyReleased(KeyEvent event){}
//  public void keyPressed(KeyEvent event){}

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
  public void setSplash(Splash foo){this.splash=foo;}
  public void setXHost(String xhost){this.xhost=xhost;}
  public void setXPort(int xport){this.xport=xport;}
  public void setXForwarding(boolean foo){this.xforwarding=foo;}
  public void setLineSpace(int foo){this.line_space=foo;}
  public void setAntiAliasing(boolean foo){ }
  public void setCompression(int compression){
    if(compression<0 || 9<compression) return;
    this.compression=compression;
  }
  public int getCompression(){return compression;}
  public void setUserHost(String userhost){
    try{
      String _user=userhost.substring(0, userhost.indexOf('@'));
      String _host=userhost.substring(userhost.indexOf('@')+1);
      this.user=_user;
      this.host=_host;
    }
    catch(Exception e){
    }
  }
  public void openSession(){
    kick();
  }
  public void setPortForwardingL(int port1, String host, int port2){
    try{session.setPortForwardingL(port1, host, port2);}
    catch(JSchException e){
    }
  }
  public void setPortForwardingR(int port1, String host, int port2){
    try{ session.setPortForwardingR(port1, host, port2); }
    catch(JSchException e){
    }
  }

  Dialog dialog=null;
  TextField textf=null;
  Button ok=null;
  Label label=null;

  private Dialog getDialog(){
    if(dialog==null){
      dialog=new Dialog(new Frame(), "", true);
      ok = new Button("OK");  
      Button cancel = new Button("CANCEL");  
      textf=new TextField(20);
      dialog.setLayout(new FlowLayout()); 
      dialog.setLocation(100,50);
      ok.addActionListener(this);
      cancel.addActionListener(this);
      dialog.add(textf);
      dialog.add(ok);
      dialog.add(cancel);
      dialog.pack();
    }
//    textf.setEchoCharacter((char)0);
    textf.setEchoChar((char)0);
    return dialog;
  }

  public void actionPerformed(ActionEvent e) {
    String action = e.getActionCommand();
    if (action.equals("Open SHELL Session...") ||
	action.equals("Open SFTP Session...") ||
	action.equals("Open EXEC Session...")
	){
      if(thread==null){
        if(action.equals("Open SHELL Session...")){ mode=SHELL; }
        else if(action.equals("Open SFTP Session...")){ mode=SFTP; }
        else if(action.equals("Open EXEC Session...")){ mode=EXEC; }
	while(true){
	  InputDialog dialog=new InputDialog("Enter username@hostname",
					     "",
					     false);
	  if(mode==EXEC){
	    InputDialog _dialog=new InputDialog("Enter command", command, false);
	    command=_dialog.getText();
	  }

	  try{
	    port=22;
	    String _host=dialog.getText();
	    if(_host==null) return;
	    String _user=_host.substring(0, _host.indexOf('@'));
	    _host=_host.substring(_host.indexOf('@')+1);
	    if(_host==null || _host.length()==0){
	      return;
	    }
	    if(_host.indexOf(':')!=-1){
	      try{
		port=Integer.parseInt(_host.substring(_host.indexOf(':')+1));
	      }
	      catch(Exception eee){}
	      _host=_host.substring(0, _host.indexOf(':'));
	    }
	    user=_user;
	    host=_host;
	    openSession();
	    return;
	  }
	  catch(Exception ee){
	  }
	}
      }
    }
    else if (action.equals("HTTP...")){
      String foo=getProxyHttpHost();
      int bar=getProxyHttpPort();

      InputDialog dialog=new InputDialog("HTTP proxy server (hostname:port)",
					 ((foo!=null&&bar!=0)? foo+":"+bar : ""),
					 false);
      String proxy=dialog.getText();
      if(proxy==null) return;
      if(proxy.length()==0){
        setProxyHttp(null, 0);
        return;
      }
      try{
        foo=proxy.substring(0, proxy.indexOf(':'));
	bar=Integer.parseInt(proxy.substring(proxy.indexOf(':')+1));
	if(foo!=null){
          setProxyHttp(foo, bar);
	}
      }
      catch(Exception ee){
      }

    }
    else if (action.equals("SOCKS5...")){
      String foo=getProxySOCKS5Host();
      int bar=getProxySOCKS5Port();

      InputDialog dialog=new InputDialog("SOCKS5 server (hostname:1080)",
					 ((foo!=null&&bar!=0)? foo+":"+bar : ""),
					 false);
      String proxy=textf.getText();
      if(proxy==null) return;
      if(proxy.length()==0){
        setProxySOCKS5(null, 0);
	return;
      }
      try{
        foo=proxy.substring(0, proxy.indexOf(':'));
  	bar=Integer.parseInt(proxy.substring(proxy.indexOf(':')+1));
        if(foo!=null){
          setProxySOCKS5(foo, bar);
	}
      }
      catch(Exception ee){
      }
    }
      /*
    else if(action.equals("X11 Forwarding...")){
      String display=null;
      display=JOptionPane.showInputDialog(this,
					  "XDisplay name (hostname:0)", 
					  (xhost==null)? "": (xhost+":"+xport));
      try{
        if(display!=null){
          xhost=display.substring(0, display.indexOf(':'));
	  xport=Integer.parseInt(display.substring(display.indexOf(':')+1));
	  xforwarding=true;
	}
      }
      catch(Exception ee){
        xforwarding=false;
	xhost=null;
      }
    }
      */
    /*
    else if((action.equals("AntiAliasing"))){
      setAntiAliasing(!antialiasing);
    }
      */
    else if(action.equals("About...")){
      MessageDialog msg=new MessageDialog("About...", COPYRIGHT);
    }
    else if(action.equals("Compression...")){
      InputDialog dialog=new InputDialog("Compression level(0-9)",
					 new Integer(compression).toString(),
					 false);

      String foo=dialog.getText();
      try{
        if(foo!=null){
	  compression=Integer.parseInt(foo);
	}
      }
      catch(Exception ee){
      }
    }
    else if(action.equals("Line Space...")){
      InputDialog dialog=new InputDialog("Line Space",
					 new Integer(line_space).toString(),
					 false);

      String foo=dialog.getText();
      try{
        if(foo!=null){
	  setLineSpace(Integer.parseInt(foo));
	}
      }
      catch(Exception ee){
      }
    }
    else if((action.equals("Local Port...")) || 
	    (action.equals("Remote Port..."))){
      if(session==null){
	MessageDialog msg=new MessageDialog("Error...",
					    "Establish the connection before this setting.");
	return;
      }

      try{
        String title="";
	if(action.equals("Local Port...")){
          title+="Local port forwarding";
	}
	else{
          title+="remote port forwarding";
	}
        title+="(port:host:hostport)";

	InputDialog dialog=new InputDialog(title, "", false);

	String foo=dialog.getText();
	if(foo==null) return;
	int port1=Integer.parseInt(foo.substring(0, foo.indexOf(':')));
	foo=foo.substring(foo.indexOf(':')+1);
	String host=foo.substring(0, foo.indexOf(':'));
	int port2=Integer.parseInt(foo.substring(foo.indexOf(':')+1));

	if(action.equals("Local Port...")){
	  setPortForwardingL(port1, host, port2);
	}
	else{
	  setPortForwardingR(port1, host, port2);
	}
      }
      catch(Exception ee){
      }
    }
    else if (action.equals("Quit")){
      quit();
    }
    else if (action.equals("CANCEL")){
      dialog.setVisible(false);
    }
/*
    else if (action.equals("passwd")){
      ui.passwd=textf.getText();
      dialog.setVisible(false);
      return;
    }
    else if (action.equals("passphrase")){
      ui.passphrase=textf.getText();
      dialog.setVisible(false);
      return;
    }
    else if (action.equals("SOCKS5... return")){
      String proxy=textf.getText();
      if(proxy.length()==0){
        setProxySOCKS5(null, 0);
	dialog.setVisible(false);
	return;
      }
      try{
        String foo=proxy.substring(0, proxy.indexOf(':'));
  	int bar=Integer.parseInt(proxy.substring(proxy.indexOf(':')+1));
        if(foo!=null){
          setProxySOCKS5(foo, bar);
	  dialog.setVisible(false);
	}
      }
      catch(Exception ee){
      }
    }

    else if ((action.equals("Local Port... return")) || 
	    (action.equals("Remote Port... return"))){
      String foo=textf.getText();
      int port1=Integer.parseInt(foo.substring(0, foo.indexOf(':')));
      foo=foo.substring(foo.indexOf(':')+1);
      String host=foo.substring(0, foo.indexOf(':'));
      int port2=Integer.parseInt(foo.substring(foo.indexOf(':')+1));

      if(action.equals("Local Port...")){
	setPortForwardingL(port1, host, port2);
      }
      else{
	setPortForwardingR(port1, host, port2);
      }
      dialog.setVisible(false);
    }
    else if (action.equals("HTTP... return")){
      String proxy=textf.getText();
      if(proxy.length()==0){
        setProxyHttp(null, 0);
	dialog.setVisible(false);
        return;
      }
      try{
        String foo=proxy.substring(0, proxy.indexOf(':'));
	int bar=Integer.parseInt(proxy.substring(proxy.indexOf(':')+1));
	if(foo!=null){
          setProxyHttp(foo, bar);
	  dialog.setVisible(false);
	}
      }
      catch(Exception ee){
      }
    }
    else if (action.equals("Open SHELL Session... return") ||
	     action.equals("Open SFTP Session... return") ||
	     action.equals("Open EXEC Session... return")
	     ){
      try{
        port=22;
	String _host=textf.getText();
	String _user=_host.substring(0, _host.indexOf('@'));
	_host=_host.substring(_host.indexOf('@')+1);
	if(_host==null || _host.length()==0){
	  return;
	}
	if(_host.indexOf(':')!=-1){
	try{
	  port=Integer.parseInt(_host.substring(_host.indexOf(':')+1));
	}
	catch(Exception eee){}
          _host=_host.substring(0, _host.indexOf(':'));
        }
	user=_user;
	host=_host;

	dialog.setVisible(false);
	openSession();
      }
      catch(Exception ee){
      }
    }
    */
  }

  public MenuBar getMenuBar(){
    MenuBar mb=new MenuBar();
    Menu m;
    MenuItem mi;

    m=new Menu("File");
    mi=new MenuItem("Open SHELL Session...");
    mi.addActionListener(this);
//    mi.setActionCommand("Open SHELL Session...");
    m.add(mi);
    mi=new MenuItem("Open SFTP Session...");
    mi.addActionListener(this);
//    mi.setActionCommand("Open SFTP Session...");
    m.add(mi);
    mi=new MenuItem("Open EXEC Session...");
    mi.addActionListener(this);
//    mi.setActionCommand("Open EXEC Session...");
    m.add(mi);
    mi=new MenuItem("Quit");
    mi.addActionListener(this);
//    mi.setActionCommand("Quit");
    m.add(mi);
    mb.add(m);

    m=new Menu("Proxy");
    mi=new MenuItem("HTTP...");
    mi.addActionListener(this);
//    mi.setActionCommand("HTTP...");
    m.add(mi);
    mi=new MenuItem("SOCKS5...");
    mi.addActionListener(this);
//    mi.setActionCommand("SOCKS5...");
    m.add(mi);
    mb.add(m);

    m=new Menu("PortForwarding");
    mi=new MenuItem("Local Port...");
    mi.addActionListener(this);
//   mi.setActionCommand("Local Port...");
    m.add(mi);
    mi=new MenuItem("Remote Port...");
    mi.addActionListener(this);
//    mi.setActionCommand("Remote Port...");
    m.add(mi);
    /*
    mi=new MenuItem("X11 Forwarding...");
    mi.addActionListener(this);
    mi.setActionCommand("X11 Forwarding...");
    m.add(mi);
    */
    mb.add(m);

    /*
    m=new Menu("Etc");
    mi=new MenuItem("Compression...");
    mi.addActionListener(this);
    mi.setActionCommand("Compression...");
    m.add(mi);
//    mi=new MenuItem("Line Space...");
//    mi.addActionListener(this);
//    mi.setActionCommand("Line Space...");
//    m.add(mi);
    mb.add(m);
    */

    m=new Menu("Help");
    mi=new MenuItem("About...");
    mi.addActionListener(this);
    mi.setActionCommand("About...");
    m.add(mi);
    mb.add(m);

    return mb;
  }

  public static void main(String[] arg){


    try{
      Class c=Class.forName("org.bouncycastle.LICENSE");
      c=Class.forName("com.jcraft.jsch.bc.DH");
      c=Class.forName("com.jcraft.jsch.bc.BlowfishCBC");
      c=Class.forName("com.jcraft.jsch.bc.TripleDESCBC");
      c=Class.forName("com.jcraft.jsch.bc.SHA1");
      c=Class.forName("com.jcraft.jsch.bc.MD5");
      c=Class.forName("com.jcraft.jsch.bc.HMACMD5");
      c=Class.forName("com.jcraft.jsch.bc.HMACMD596");
      c=Class.forName("com.jcraft.jsch.bc.HMACSHA1");
      c=Class.forName("com.jcraft.jsch.bc.HMACSHA196");
      c=Class.forName("com.jcraft.jsch.bc.SignatureDSA");
      c=Class.forName("com.jcraft.jsch.bc.SignatureRSA");
      c=Class.forName("com.jcraft.jsch.bc.KeyPairGenDSA");
      c=Class.forName("com.jcraft.jsch.bc.KeyPairGenRSA");
      c=Class.forName("com.jcraft.jsch.bc.Random");
    }
    catch(Exception e){
    }

    JCTermJ2MECDC term=new JCTermJ2MECDC();
    Frame frame=new Frame("JCTermJ2MECDC");

    frame.addWindowListener(new WindowAdapter(){
      public void windowClosing(WindowEvent e){System.exit(0);}
    });

    MenuBar mb=term.getMenuBar();
    frame.setMenuBar(mb);

    /**/
    frame.setSize((windowWidth - margin * 2),
		  (windowHeight - titleHeight - taskbarHeight
		   - margin * 2));
    /**/
    //frame.setSize(term.getTermWidth(), term.getTermHeight());

    frame.add("Center", term);
    frame.pack();
    term.initGraphics();

    /*
    frame.setResizable(true);
    {
      Insets insets=frame.getInsets();
      int foo=term.getTermWidth();
      int bar=term.getTermHeight();
      foo+=(insets.left+insets.right);
      bar+=(insets.top+insets.bottom);
      frame.setSize(foo, bar);
    }
    */

    frame.setSize((windowWidth - margin * 2),
		  (windowHeight - titleHeight - taskbarHeight
		   - margin * 2));

    frame.setResizable(false);
    term.setVisible(true);
    frame.setVisible(true);
    term.setFrame(frame);
  }
  public void quit(){
    thread=null;
    if(session!=null){
      session.disconnect();
      session=null;
    }
  }

  class InputDialog implements ActionListener{
    String result=null;
    Dialog dialog=null;
    TextField textf=null;
    InputDialog(String title, String text, boolean passwd){
      super();
      dialog=new Dialog(new Frame(), title, true);
      Button ok = new Button("OK");  
      Button cancel = new Button("CANCEL");  
      textf=new TextField(20);
      textf.setText(text);
      //if(passwd){ textf.setEchoCharacter('*'); }
      if(passwd){ textf.setEchoChar('*'); }
      dialog.setLayout(new FlowLayout()); 
      dialog.setLocation(100,50);
      ok.addActionListener(this);
      cancel.addActionListener(this);
      textf.addActionListener(this);
      dialog.add(textf);
      dialog.add(ok);
      dialog.add(cancel);
      dialog.pack();
      dialog.setVisible(true);
      while(true){
	try{Thread.sleep(1000);}catch(Exception ee){}
	if(!dialog.isVisible()){
	  break;
	}
      }
    }
    public void actionPerformed(ActionEvent e) {
      String action=e.getActionCommand();
      if(action.equals("OK")){ result=textf.getText(); }
      else if(action.equals("CANCEL")){ }
      else{ result=textf.getText(); }
      dialog.setVisible(false);
      return;
    }
    String getText(){return result;}
  }

  class PromptDialog implements ActionListener{
    boolean result=false;
    Dialog dialog=null;
    PromptDialog(String title, String msg){
      super();
      dialog=new Dialog(new Frame(), title, true);
      Button ok = new Button("YES");  
      Button cancel = new Button("NO");  
      dialog.setLayout(new FlowLayout()); 
      dialog.setLocation(100,50);
      ok.addActionListener(this);
      cancel.addActionListener(this);
      TextArea text = new TextArea(msg);  
      dialog.add(text);
      text.setEditable(false);
      dialog.add(ok);
      dialog.add(cancel);
      dialog.pack();
      dialog.setVisible(true);
    }
    boolean isVisible(){ return dialog.isVisible(); }
    public void actionPerformed(ActionEvent e) {
      String action=e.getActionCommand();
      if(action.equals("YES")){
	result=true;
      }
      dialog.setVisible(false);
    }
  }
  class MessageDialog implements ActionListener{
    Dialog dialog=null;
    MessageDialog(String title, String msg){
      super();
      dialog=new Dialog(new Frame(), title, true);
      Button ok = new Button("OK");  
      dialog.setLayout(new FlowLayout()); 
      dialog.setLocation(100,50);
      ok.addActionListener(this);
      TextArea text = new TextArea(msg);  
      dialog.add(text);
      text.setEditable(false);
      dialog.add(ok);
      dialog.pack();
      dialog.setVisible(true);
    }
    boolean isVisible(){ return dialog.isVisible(); }
    public void actionPerformed(ActionEvent e) {
      String action=e.getActionCommand();
      dialog.setVisible(false);
    }
  }

  public void start(Connection connection){ }
  public void setDefaultForeGround(Object foreground){
  }
  public void setDefaultBackGround(Object background){
  }
  public void setForeGround(Object f){
    if(f instanceof String){
      fground=java.awt.Color.getColor((String)f);
    }
    if(f instanceof java.awt.Color){
      fground=(java.awt.Color)f;
    }
    graphics.setColor(getForeGround());
  } 
  public void setBackGround(Object b){
    if(b instanceof String){
      bground=java.awt.Color.getColor((String)b);
    }
    if(b instanceof java.awt.Color){
      bground=(java.awt.Color)b;
    }
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
  public void setBold(){ }
  public void setUnderline(){ }
  public void setReverse(){ 
    reverse=true;
    if(graphics!=null)
      graphics.setColor(getForeGround());
  }
  public void resetAllAttributes(){
    reverse=false;
    bground=defaultbground;
    fground=defaultfground;
    if(graphics!=null)
      graphics.setColor(getForeGround());
  }
  public Object getColor(int index){ return null; }
}
