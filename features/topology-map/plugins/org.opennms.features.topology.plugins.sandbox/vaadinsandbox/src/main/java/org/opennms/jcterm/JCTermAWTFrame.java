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

import com.jcraft.jsch.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.image.*;

public class JCTermAWTFrame  extends java.awt.Frame implements ActionListener, Runnable{

  private static final int SHELL=0;
  private static final int SFTP=1;
  private static final int EXEC=2;

  private int mode=SHELL;
  int port=22;

  private String xhost="127.0.0.1";    
  private int xport=0;    
  private boolean xforwarding=false;
  private String user=System.getProperty("user.name");
  private String host="127.0.0.1";

  private String proxy_http_host=null;
  private int proxy_http_port=0;

  private String proxy_socks5_host=null;
  private int proxy_socks5_port=0;

  private JSchSession jschsession=null;
  private Proxy proxy=null;

  private int line_space=-2;
  private int compression=0;

  private Splash splash=null;

  private JCTermAWT term=null;

  private Connection connection=null;

  public JCTermAWTFrame(String name){
    super(name);

    enableEvents(AWTEvent.KEY_EVENT_MASK);
    addWindowListener(new WindowAdapter(){
	public void windowClosing(WindowEvent e){System.exit(0);}
      });

    MenuBar mb=getMenuBar();
    setMenuBar(mb);

    term=new JCTermAWT();

    setSize(term.getTermWidth(), term.getTermHeight());
    add("Center", term);

    pack();
    term.setVisible(true);
    setVisible(true);

    term.initGraphics();

    setResizable(true);
    {
      Insets insets=getInsets();
      int foo=term.getTermWidth();
      int bar=term.getTermHeight();
      foo+=(insets.left+insets.right);
      bar+=(insets.top+insets.bottom);
      setSize(foo, bar);
    }
    setResizable(false);
  }

  private Thread thread=null;

  public void kick(){
    this.thread=new Thread(this);
    this.thread.start();
  }

  public void run(){
    while(thread!=null){
      try{

	try{
	  UserInfo ui=new MyUserInfo();
  	  jschsession=JSchSession.getSession(user, null, host, port, ui, proxy);
	  java.util.Properties config=new java.util.Properties();
	  if(compression==0){
	    config.put("compression.s2c", "none");
	    config.put("compression.c2s", "none");
	  }
	  else{
	    config.put("compression.s2c", "zlib,none");
	    config.put("compression.c2s", "zlib,none");
	  }
	  jschsession.getSession().setConfig(config);
	  jschsession.getSession().rekey();
	}
	catch(Exception e){
          //System.out.println(e);
          break;
	}

	Channel channel=null;
	OutputStream out=null;
	InputStream in=null;

	if(mode==SHELL){
          channel=jschsession.getSession().openChannel("shell");
	  if(xforwarding){
 	    jschsession.getSession().setX11Host(xhost);
	    jschsession.getSession().setX11Port(xport+6000);
 	    channel.setXForwarding(true);
	  }

	  out=channel.getOutputStream();
	  in=channel.getInputStream();
	  channel.connect();
	}
	else if(mode==SFTP){

	  out=new PipedOutputStream();
	  in=new PipedInputStream();

	  channel=jschsession.getSession().openChannel("sftp");

	  channel.connect();

	  (new Sftp((ChannelSftp)channel, 
		    (InputStream)(new PipedInputStream((PipedOutputStream)out)),
		    new PipedOutputStream((PipedInputStream)in))).kick();
	}

	final OutputStream fout=out;
	final InputStream fin=in;
	final Channel fchannel=channel;

	connection=new Connection(){
	    public InputStream getInputStream(){return fin;}
	    public OutputStream getOutputStream(){return fout;}
	    public void requestResize(Term term){
	      if(fchannel instanceof ChannelShell){
		int c=term.getColumnCount();
		int r=term.getRowCount();
		((ChannelShell)fchannel).setPtySize(c, r,
						    c*term.getCharWidth(),
						    r*term.getCharHeight());
	      }
	    }
	    public void close(){
	      fchannel.disconnect();
	    }
	  };

	setTitle(user+"@"+host+(port!=22 ? new Integer(port).toString() : ""));
        term.requestFocus();
	term.start(connection);
      }
      catch(Exception e){
	//e.printStackTrace();
      }
      break;
    }

    setTitle("JCTerm");
    thread=null;
  }

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
    if(jschsession==null)
      return;
    try{ jschsession.getSession().setPortForwardingL(port1, host, port2);}
    catch(JSchException e){
    }
  }
  public void setPortForwardingR(int port1, String host, int port2){
    if(jschsession==null)
      return;
    try{ jschsession.getSession().setPortForwardingR(port1, host, port2); }
    catch(JSchException e){
    }
  }

  Dialog dialog=null;
  TextField textf=null;
  Button ok=null;
  Label label=null;

  private Dialog getDialog(){
    if(dialog==null){
      dialog=new Dialog(new java.awt.Frame(), "", true);
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
    textf.setEchoCharacter((char)0);
    return dialog;
  }

  public void actionPerformed(ActionEvent e) {
    String action = e.getActionCommand();
    if (action.equals("Open SHELL Session...") ||
	action.equals("Open SFTP Session...")
	){
      if(thread==null){
        if(action.equals("Open SHELL Session...")){ mode=SHELL; }
        else if(action.equals("Open SFTP Session...")){ mode=SFTP; }
	while(true){
	  InputDialog dialog=new InputDialog("Enter username@hostname",
					     "",
					     false);
	  try{
	    String _host=dialog.getText();
	    if(_host==null) return;
	    String _user=_host.substring(0, _host.indexOf('@'));
	    _host=_host.substring(_host.indexOf('@')+1);
	    if(_host==null || _host.length()==0){
	      return;
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
      MessageDialog msg=new MessageDialog("About...", JCTermAWT.COPYRIGHT);
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
      if(jschsession==null){
//        JOptionPane.showMessageDialog(this,
//				      "Establish the connection before this setting.");
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
	     action.equals("Open SFTP Session... return")
	     ){
      try{
	String _host=textf.getText();
	String _user=_host.substring(0, _host.indexOf('@'));
	_host=_host.substring(_host.indexOf('@')+1);
	if(_host==null || _host.length()==0){
	  return;
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
    mb.add(m);
    */

    m=new Menu("Etc");
//    mi=new MenuItem("AntiAliasing");
//    mi.addActionListener(this);
//    mi.setActionCommand("AntiAliasing");
//    m.add(mi);
    mi=new MenuItem("Compression...");
    mi.addActionListener(this);
    mi.setActionCommand("Compression...");
    m.add(mi);
//    mi=new MenuItem("Line Space...");
//    mi.addActionListener(this);
//    mi.setActionCommand("Line Space...");
//    m.add(mi);
    mb.add(m);

    m=new Menu("Help");
    mi=new MenuItem("About...");
    mi.addActionListener(this);
    mi.setActionCommand("About...");
    m.add(mi);
    mb.add(m);

    return mb;
  }

  public void quit(){
    thread=null;
    if(connection!=null){
      connection.close();
      connection=null;
    }
  }

  public void setTerm(JCTermAWT term){
    this.term=term;
  }

  public Term getTerm(){
    return term;
  }

  class InputDialog implements ActionListener{
    String result=null;
    Dialog dialog=null;
    TextField textf=null;
    InputDialog(String title, String text, boolean passwd){
      super();
      dialog=new Dialog(new java.awt.Frame(), title, true);
      Button ok = new Button("OK");  
      Button cancel = new Button("CANCEL");  
      textf=new TextField(20);
      textf.setText(text);
      if(passwd){ textf.setEchoCharacter('*'); }
      dialog.setLayout(new FlowLayout()); 
      dialog.setLocation(100,50);
      ok.addActionListener(this);
      cancel.addActionListener(this);
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
      else if(action.equals("CANCEL")){
      }
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
      dialog=new Dialog(new java.awt.Frame(), title, true);
      Button ok = new Button("YES");  
      Button cancel = new Button("NO");  
      dialog.setLayout(new FlowLayout()); 
      dialog.setLocation(100,50);
      ok.addActionListener(this);
      cancel.addActionListener(this);
      TextArea text = new TextArea(msg);  
      dialog.add(text);
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
      dialog=new Dialog(new java.awt.Frame(), title, true);
      Button ok = new Button("OK");  
      dialog.setLayout(new FlowLayout()); 
      dialog.setLocation(100,50);
      ok.addActionListener(this);
      TextArea text = new TextArea(msg);  
      dialog.add(text);
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

  public static void main(String[] arg){
    final JCTermAWTFrame frame=new JCTermAWTFrame("JCTerm");
    frame.setVisible(true);
    frame.setResizable(true);
  }
}
