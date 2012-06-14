/* -*-mode:java; c-basic-offset:2; -*- */
/* JCTermMIDP
 * Copyright (C) 2005,2007 ymnk, JCraft,Inc.
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
import java.io.*;

import javax.microedition.midlet.*;
import javax.microedition.io.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;
import java.io.*;
import java.util.Vector;

public class JCTermMIDP extends MIDlet implements Runnable, CommandListener{
  static String COPYRIGHT=
"JCTerm 0.0.11\nCopyright (C) 2002,2007 ymnk<ymnk@jcraft.com>, JCraft,Inc.\n"+
"Official Homepage: http://www.jcraft.com/jcterm/\n"+
"This software is licensed under GNU LGPL.\n"+
"This software is using JSch for J2ME(http://j2me.jsch.org/) and\n"+
"BouncyCastle Crypto API(http://www.bouncycastle.org/).";

  private static Display display;
  private Form f;
  private StringItem si;
  private TextField tf;

  private Command exitCommand=new Command("Exit", Command.EXIT, 0);
  private Command connectCommand=new Command("Connect", Command.ITEM, 1);
  private Command clearKeyCommand=new Command("Clear private key", Command.ITEM, 2);
  private Command cancelCommand=new Command("Cancel", Command.SCREEN, 0);
  private Command okCommand=new Command("Ok", Command.SCREEN, 1);
  private Command inputCommand=new Command("Input", Command.SCREEN, 1);
  private TextBox resultBox=new TextBox("Result", "", 2048, 0);
  private TextBox tb=new TextBox("Input", "", 64, 0);
  private ChoiceGroup choice=new ChoiceGroup("", Choice.MULTIPLE);

  private Displayable nextScreen=null;

  private String host=null;
  private String user=null;
  private int port=22;

  private boolean isPaused;

  private final String UH="";

  private OutputStream out;
  private InputStream in;

//private Image background;
//private Graphics cursor_graphics;
//private int compression=0;

  public JCTermMIDP(){
    display=Display.getDisplay(this);
    f=new Form("JCTerm");
    si=new StringItem("Status", "No connection");
    tf=new TextField("user@hostname", UH, 30, TextField.ANY);

    f.append(tf);
    f.append(choice);
    f.append("");
    f.append(COPYRIGHT);

    choice.append("Install private key", null);

    f.addCommand(exitCommand);
    f.addCommand(connectCommand);
    f.addCommand(clearKeyCommand);
    f.setCommandListener(this);
    display.setCurrent(f);

    tb.addCommand(okCommand);
    tb.setCommandListener(this);

    resultBox.addCommand(okCommand);
    resultBox.setCommandListener(this);

    initGraphics();

  }

  private Thread thread=null;
  public void start() {
    thread=new Thread(this);
    thread.start();
  }

  public void run() {
    try{
      if(host!=null && user!=null){
	connect();
      }
      if(session==null){
	si.setText("No connection");
      }
    } 
    catch (Exception e) {
      //e.printStackTrace();
    }
  }

  private void connect(){
    si.setText("Connecting to "+user+"@"+host+"...");
    nextScreen=display.getCurrent();

    try{
      Session session=null;
      try{ session=getSession(user, host); }
      catch(JSchException ee){
	//System.out.println(ee);
	si.setText("No connection");
	f.addCommand(connectCommand);
	f.addCommand(clearKeyCommand);
	display.setCurrent(f);
	return;
      } 
      si.setText("Connected to "+user+"@"+host);

      f.addCommand(connectCommand);
      f.addCommand(clearKeyCommand);
      display.setCurrent(f);

      if(choice.isSelected(0)){
	installPKey(session);
      }
      else{
	canvas.setTitle("JCTerm: "+user+"@"+host);
	display.setCurrent(canvas);
	canvas.initGraphics();

	channel=session.openChannel("shell");
	out=channel.getOutputStream();
	in=channel.getInputStream();
	channel.connect();

	((ChannelShell)channel).setPtySize(canvas.getColumnCount(),
					   canvas.getRowCount()-1,
					   canvas.getWidth(),
					   canvas.getHeight());

	emulator=new EmulatorVT100(canvas, in);
	emulator.reset();
	canvas.setOutputStream(out);
	emulator.start();
	channel.disconnect();
	f.addCommand(connectCommand);
	f.addCommand(clearKeyCommand);
	display.setCurrent(f);
      }
    }
    catch(Exception e){
      //System.err.println(e);
    }
  }

  private void clearKey(){
    try{ RecordStore.deleteRecordStore(".ssh"); }
    catch(RecordStoreException e){ }
  }
  private void installPKey(Session session) throws Exception {
    resultBox.setTitle("Install private key");
    resultBox.delete(0, resultBox.size());
    display.setCurrent(resultBox);

    String[] pkey={"id_dsa", "id_rsa"};
    for(int j=0; j<pkey.length; j++){

      Channel channel=session.openChannel("exec");
      ((ChannelExec)channel).setCommand("cat .ssh/"+pkey[j]);
      InputStream in=channel.getInputStream();
      OutputStream out=channel.getOutputStream();
      channel.connect();
      int c;
      int i=0;
      byte[] foo=new byte[1024];
      while(true){
	c=in.read(foo, i, foo.length-i);
	if(c==-1)break; 
	i+=c;
      }
      //System.out.println(pkey[j]+": i="+i);
      if(/*i>=0*/ 100<=i ){
	resultBox.insert(pkey[j]+"\n", resultBox.size());
	resultBox.insert(new String(foo, 0, i)+"\n", resultBox.size());
	try{
	  RecordStore store=RecordStore.openRecordStore(".ssh", true);
	  store.addRecord(foo, 0, i);
	  store.closeRecordStore();
	}
	catch(RecordStoreException e){
	  //System.out.println(e);
	}
      }
      else{
	resultBox.insert(pkey[j]+" is not found.\n", resultBox.size());
      }
      channel.disconnect();
    }
  }

  public boolean isPaused(){ return isPaused; }
  public void startApp(){ isPaused=false; }
  public void pauseApp(){ isPaused=true; }

  public void destroyApp(boolean unconditional) {
    stop();
  }
  public void stop() {
    try {
      if(session!=null){
	session.disconnect();
	session=null;
      }
    }
    catch (Exception e) {}
    thread=null;
  }

  public void commandAction(Command c, Displayable s) {
//System.out.println("key: "+c+" "+s);
    if(c==clearKeyCommand){
      clearKey();
      return;
    }
    if(c==okCommand && 
       display.getCurrent()==resultBox){
      if(nextScreen==f){
        f.addCommand(connectCommand);
	f.addCommand(clearKeyCommand);
      }
      display.setCurrent(nextScreen);
      return;
    }
    if(s==tb){
//     System.out.println(tb.getString());
      byte[] foo=tb.getString().getBytes();
      try{
	out.write(foo, 0, foo.length);
	out.flush();
      }
      catch(Exception e){ }
      display.setCurrent(nextScreen);
      return;
    }
    if(c==inputCommand){
      display.setCurrent(tb);
      nextScreen=s;
      return;
    }
    if(c==okCommand || c==cancelCommand){
      if(c==cancelCommand){
        f.addCommand(connectCommand);
	f.addCommand(clearKeyCommand);
      }
      display.setCurrent(f); 
    }

    if(c==connectCommand && !isPaused()) {
      if(thread!=null && thread.isAlive()){
	return;
      }
      String _host=tf.getString();
      int index=_host.indexOf('@');
      if(index<=0) return;
      String _user=_host.substring(0, _host.indexOf('@'));
      _host=_host.substring(_host.indexOf('@')+1);
      index=_host.indexOf(':');
      if(index>0){
	try{port=Integer.parseInt(_host.substring(_host.indexOf(':')+1));}
	catch(Exception e){}
	_host=_host.substring(0, _host.indexOf(':'));
      }
      if(_host==null || _host.length()==0 ||
	 _user==null || _user.length()==0){
	return;
      }
      host=_host;
      user=_user;
      thread=new Thread(this);
      thread.start();
      f.removeCommand(connectCommand);
      f.removeCommand(clearKeyCommand);
    }

    if((c==Alert.DISMISS_COMMAND) || (c==exitCommand)){
      stop();
      notifyDestroyed();
      destroyApp(true);
    }
  }

  private JSch jsch=null;
  private Session session=null;
  Channel channel=null;

  private MyCanvas canvas;

  private void initGraphics(){
    canvas=new MyCanvas();
    canvas.addCommand(exitCommand);
    canvas.addCommand(inputCommand);
    canvas.setCommandListener(this);

//    cursor_graphics=(img.getGraphics());
//    cursor_graphics.setColor(getForeGround());
//    cursor_graphics.setXORMode(getBackGround());
  }

  public void kick(){
    this.thread=new Thread(this);
    this.thread.start();
  }

  MyUserInfo ui=null;
  private Emulator emulator=null;

//  public void setLineSpace(int foo){this.line_space=foo;}
//  public void setCompression(int compression){
//    if(compression<0 || 9<compression) return;
//    this.compression=compression;
//  }
//  public int getCompression(){return compression;}
//  public void setUserHost(String userhost){
//    try{
//      String _user=userhost.substring(0, userhost.indexOf('@'));
//      String _host=userhost.substring(userhost.indexOf('@')+1);
//      this.user=_user;
//      this.host=_host;
//    }
//    catch(Exception e){
//    }
//  }

  public void openSession(){
    kick();
  }

  public class MyUserInfo implements UserInfo, CommandListener{
    Alert prompt=new Alert("Prompt", "", null, AlertType.WARNING);
    Form input=new Form("");
    TextField pf=new TextField("", "", 20, TextField.PASSWORD);
    boolean result=false;
    String passwd="";
    MyUserInfo(){
      prompt.setTimeout(Alert.FOREVER);
      prompt.setCommandListener(this);
      input.append(pf);
      input.addCommand(cancelCommand);
      input.addCommand(okCommand);
      input.setCommandListener(this);
    }
    public String getPassword(){ return passwd; }
    public boolean promptYesNo(String str){ 
      prompt.setString(str);
      prompt.addCommand(okCommand);
      prompt.addCommand(cancelCommand);
      Displayable current=display.getCurrent();
      display.setCurrent(prompt, current); 
      while(true){
        try{Thread.sleep(500);}
        catch(Exception e){}
        if(prompt!=display.getCurrent()){
          break;
        }
      }
      prompt.removeCommand(okCommand);
      prompt.removeCommand(cancelCommand);
      return result;
    }

    String passphrase="";
    public String getPassphrase(){ return passphrase; }
    public boolean promptPassphrase(String message){ 
      input.setTitle(message);
      pf.setLabel("Passphrase");
      Displayable current=display.getCurrent();
      display.setCurrent(input);
      while(true){
        try{Thread.sleep(500);}
        catch(Exception e){}
        if(input!=display.getCurrent()){
          break;
        }
      }
      return result;
    }
    public boolean promptPassword(String message){ 
      input.setTitle(message);
      pf.setLabel("Password");
      Displayable current=display.getCurrent();
      display.setCurrent(input);
      while(true){
        try{Thread.sleep(500);}
        catch(Exception e){}
        if(input!=display.getCurrent()){
          break;
        }
      }
      return result;
    }
    public void showMessage(String message){
      prompt.setString(message);
      Displayable current=display.getCurrent();
      display.setCurrent(prompt, current);
      while(true){
        try{Thread.sleep(500);}
        catch(Exception e){}
        if(prompt!=display.getCurrent()){
          break;
        }
      }
    }
    public void commandAction(Command c, Displayable s) {
      if(input==display.getCurrent()){
	if(c==okCommand){ 
          String label=pf.getLabel();
          if(label.equals("Passphrase")){
	    passphrase=pf.getString();
          }
          else{
	    passwd=pf.getString();
          }
	}
      }
      if(c==okCommand){ result=true; }
      if(c==cancelCommand){ result=false; }
      display.setCurrent(f);
    }
  }

  private String last_uh="";
  private Session getSession(String user, String host) throws JSchException{

    if(jsch==null){
      jsch=new JSch();

      try{
	RecordStore store=RecordStore.openRecordStore(".ssh", false);
        int num=store.getNumRecords();
	for(int i=1; i<num+1; i++){
	  byte foo[]=store.getRecord(i);
	  //System.out.println("foo.length: "+foo.length);
	  jsch.addIdentity(new IdentityMem(new String(foo), "", jsch));
	  num--;
	}
	store.closeRecordStore();
      }
      catch(RecordStoreException e){
	//System.out.println(e);
      }

      //ByteArrayInputStream bis=
      //  new ByteArrayInputStream(known_hosts.getBytes());
      //jsch.setKnownHosts(bis);
    }

    String foo=user+"@"+host;
    if(last_uh.equals(foo) && session!=null){
      return session;
    }
    if(session!=null){
      try{session.disconnect();}catch(Exception e){}
      session=null;
    }
    Session _session=jsch.getSession(user, host, 22);
    _session.setSocketFactory(new MySocketFactory());
    _session.setUserInfo(new MyUserInfo());
    _session.connect();
    last_uh=foo;
    session=_session;
    return session;
  }

  class MySocketFactory implements SocketFactory{
    public Object createSocket(String host, int port) throws IOException{
      return Connector.open("socket://"+host+":"+port);
    }
    public InputStream getInputStream(Object socket) throws IOException{
      SocketConnection foo=(SocketConnection)socket;
      return foo.openInputStream();
    }
    public OutputStream getOutputStream(Object socket) throws IOException{
      SocketConnection foo=(SocketConnection)socket;
      return foo.openOutputStream();
    }
  }

  class MyCanvas extends Canvas implements Term{
    private int descent=0;
    private int line_space=0;
    private int windowWidth = 0;
    private int windowHeight = 0;
    private Font font;
    private int char_width;
    private int char_height;
    private int term_width;
    private int term_height;
    private Image img;
    private Graphics graphics;
    private int defaultbground=0x000000;
    private int defaultfground=0xffffff;
    private int bground=0x000000;
    private int fground=0xffffff;

    private int x=0;
    private int y=0;

    MyCanvas(){
      initGraphics();
    }

    void initGraphics(){
      windowWidth=getWidth();
      windowHeight=getHeight();
      font=Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_SMALL);
//    font=Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
      char_height=(int)font.getHeight();
      char_width=(int)font.stringWidth("X");
      term_height=(int)(windowHeight/char_height);
      term_width=(int)(windowWidth/char_width);
//System.out.println(term_width+" "+term_height);
      descent=char_height-font.getBaselinePosition();
//System.out.println("height: "+char_height);
//System.out.println("descent: "+descent);
      img=Image.createImage(windowWidth, windowHeight);
      graphics=img.getGraphics();
      graphics.setFont(font);
      clear();
    }

    public void setCursor(int x, int y){
      this.x=x;
      this.y=y;
    }

    public int getTermWidth(){ return char_width*term_width; }
    public int getTermHeight(){ return char_height*term_height; }
    public int getCharWidth(){ return char_width; }
    public int getCharHeight(){ return char_height; }
    public int getColumnCount(){ return term_width; }
    public int getRowCount(){ return term_height; }

    public void clear(){
      graphics.setColor(getBackGround());
      graphics.fillRect(0, 0, windowWidth, windowHeight);
      graphics.setColor(getForeGround());
    }
    public void draw_cursor(){
    /*
    cursor_graphics.fillRect(x, y-char_height, char_width, char_height);
    Graphics g=getGraphics();
    g.setClip(x, y-char_height, char_width, char_height);
    g.drawImage(img, 0, 0, term_area);
    */
    }    
    public void redraw(int x, int y, int width, int height){
//System.out.println("redraw: x="+x+",y="+y+",width="+width+",height="+height);
//      repaint();
      repaint(x, y, width, height);
    }

    public void clear_area(int x1, int y1, int x2, int y2){
//System.out.println("clear_area: x1="+x1+",y1="+y1+",x2="+x2+",y2="+y2);
      graphics.setColor(getBackGround());
      graphics.fillRect(x1, y1, x2-x1, y2-y1);
      graphics.setColor(getForeGround());
    }    
    public void scroll_area(int x, int y, int w, int h, int dx, int dy){
//System.out.println("scroll_area: x="+x+",y="+y+",w="+w+",h="+h+",dx="+dx+",dy="+dy);
      graphics.copyArea(x, y, w, h, x+dx, y+dy, 0);
    }

//    private int anchor=Graphics.BOTTOM|Graphics.LEFT;
    private int anchor=Graphics.BASELINE|Graphics.LEFT;
    public void drawBytes(byte[] buf, int s, int len, int x, int y){
      graphics.setColor(getBackGround());
      clear_area(x, y, x+len*char_width, y+char_height);
      graphics.setColor(getForeGround());
      while(len>0){
	graphics.drawChar((char)buf[s], x, y-(descent+line_space), anchor);
	len--;
	s++;
	x+=char_width;
      }
      repaint();
    }
    public void drawString(String str, int x, int y){
//System.out.println("drawString: "+str);
      clear_area(x, y, x+str.length()*char_width, y+char_height);
      graphics.setColor(getForeGround());
      graphics.drawString(str, x, y-(descent+line_space), anchor);
    }
    public void beep(){
//    Toolkit.getDefaultToolkit().beep();
    }

    public void paint(Graphics g){
//    System.out.println("g: "+g);
      if(img!=null){ g.drawImage(img, 0, 0, 0);  }
    }

    private OutputStream out=null;
    public void setOutputStream(OutputStream out){
      this.out=out;
    }

    public void keyPressed(int keyCode){
//      System.out.println("keyCode: "+keyCode);
      byte[] input=null;
      keyCode=getGameAction(keyCode);
//      System.out.println("keyCode: "+keyCode);
      switch(keyCode){
      case FIRE:  // SELECT
	input=emulator.getCodeENTER();
	break;
      case UP:
	input=emulator.getCodeUP();
	break;
      case DOWN:
	input=emulator.getCodeDOWN();
	break;
      case RIGHT:
	input=new byte[1];
	input[0]=3;                         // ^C
	break;
      case LEFT:
	input=new byte[1];     
	input[0]=8;                         // back space, ^H
	break;
      default:
      }
      if(input!=null){
	try{
	  out.write(input, 0, input.length);
	  out.flush();
	}
	catch(Exception e){
	}
      }
    }
  }

  public void start(Connection connection){ }
  public void setDefaultForeGround(Object f){
    if(f instanceof Integer){
      defaultfground=((Integer)f).intValue();
    }
  }
  public void setDefaultBackGround(Object b){
    if(b instanceof Integer){
      defaultbground=((Integer)b).intValue();
    }
  }
  public void setForeGround(Object f){
    if(f instanceof Integer){
      fground=((Integer)f).intValue();
      graphics.setColor(fground);
    }
  } 
  public void setBackGround(Object b){
    if(b instanceof Integer){
      bground=((Integer)b).intValue();
    }
  } 
  private int getForeGround(){ 
    if(reverse)
      return bground; 
    return fground; 
  }
  private int getBackGround(){ 
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
