/* -*-mode:java; c-basic-offset:2; -*- */
/* JCTermSwingFrame
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
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.util.Vector;

public class JCTermSwingFrame extends JFrame
                              implements Frame, ActionListener, Runnable{
  static String COPYRIGHT="JCTerm 0.0.11\nCopyright (C) 2002,2012 ymnk<ymnk@jcraft.com>, JCraft,Inc.\n"
      +"Official Homepage: http://www.jcraft.com/jcterm/\n"
      +"This software is licensed under GNU LGPL.";

  private static int counter=1;
  static void resetCounter(){
    counter=1;
  }

  private int mode=SHELL;

  private String xhost="127.0.0.1";
  private int xport=0;
  private boolean xforwarding=false;
  private String user="";
  private String host="127.0.0.1";

  private String proxy_http_host=null;
  private int proxy_http_port=0;

  private String proxy_socks5_host=null;
  private int proxy_socks5_port=0;

  private JSchSession jschsession=null;
  private Proxy proxy=null;

  private int compression=0;

  private Splash splash=null;

  private JCTermSwing term=null;

  private Connection connection=null;

  private Channel channel=null;

  private boolean close_on_exit = true;

  private Frame frame = this;

  private String configName = "default";

  public boolean getCloseOnExit(){
    return close_on_exit;
  }

  public void setCloseOnExit(boolean close_on_exit) {
    this.close_on_exit = close_on_exit;
  }

  public JCTermSwingFrame(){
  }

  public JCTermSwingFrame(String name){
    this(name, "default");
  }

  public JCTermSwingFrame(String name, String configName){
    super(name);
    this.configName = configName;

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    enableEvents(AWTEvent.KEY_EVENT_MASK);
    addWindowListener(new WindowAdapter(){
      public void windowClosing(WindowEvent e){
        System.exit(0);
      }
    });

    JMenuBar mb=getJMenuBar();
    setJMenuBar(mb);

    term=new JCTermSwing();
    getContentPane().add("Center", term);
    pack();
    term.setVisible(true);

    ComponentAdapter l = new ComponentAdapter(){
      public void componentResized(ComponentEvent e){
        Component c=e.getComponent();
        Container cp = ((JFrame)c).getContentPane();
        int cw=c.getWidth();
        int ch=c.getHeight();
        int cwm=c.getWidth()-cp.getWidth();
        int chm=c.getHeight()-cp.getHeight();
        cw-=cwm;
        ch-=chm;
        JCTermSwingFrame.this.term.setSize(cw, ch);
      }
    };
    addComponentListener(l);

    applyConfig(configName);

    openSession();
  }

  private Thread thread=null;

  public void kick(){
    this.thread=new Thread(this);
    this.thread.start();
  }

  public void run(){
    String destination = null;
    while(thread!=null){
      try{
        int port=22;
        try{
          String[] destinations = JCTermSwing.getCR().load(configName).destinations;
          String _host = promptDestination(term, destinations);
          destination = _host;
          if(_host==null){
            break;
          }
          String _user=_host.substring(0, _host.indexOf('@'));
          _host=_host.substring(_host.indexOf('@')+1);
          if(_host==null||_host.length()==0){
            continue;
          }
          if(_host.indexOf(':')!=-1){
            try{
              port=Integer.parseInt(_host.substring(_host.indexOf(':')+1));
            }
            catch(Exception eee){
            }
            _host=_host.substring(0, _host.indexOf(':'));
          }
          user=_user;
          host=_host;
        }
        catch(Exception ee){
          continue;
        }

        try{
          UserInfo ui=new MyUserInfo();

          jschsession=JSchSession.getSession(user, null, host, port, ui, proxy);
          setCompression(compression);

          Configuration conf = JCTermSwing.getCR().load(configName);
          conf.addDestination(destination);
          JCTermSwing.getCR().save(conf);
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

          (new Sftp((ChannelSftp)channel, (InputStream)(new PipedInputStream(
              (PipedOutputStream)out)), new PipedOutputStream(
              (PipedInputStream)in))).kick();
        }

        final OutputStream fout=out;
        final InputStream fin=in;
        final Channel fchannel=channel;

        connection=new Connection(){
          public InputStream getInputStream(){
            return fin;
          }

          public OutputStream getOutputStream(){
            return fout;
          }

          public void requestResize(Term term){
            if(fchannel instanceof ChannelShell){
              int c=term.getColumnCount();
              int r=term.getRowCount();
              ((ChannelShell)fchannel).setPtySize(c, r, c*term.getCharWidth(),
                  r*term.getCharHeight());
            }
          }

          public void close(){
            fchannel.disconnect();
          }
        };
        frame.setTitle("["+(counter++)+"] "+user+"@"+host+(port!=22 ? (":"+new Integer(port).toString()) : ""));
        term.requestFocus();
        term.start(connection);
      }
      catch(Exception e){
        //e.printStackTrace();
      }
      break;
    }
    frame.setTitle("JCTerm");
    thread=null;

    dispose_connection();

    if(getCloseOnExit()){
      frame.setVisible(false);
      frame.dispose();
    }
    else{ 
      term.clear();
      term.redraw(0, 0, term.getWidth(), term.getHeight());
    }
  }

  void dispose_connection(){
    synchronized(this){
      if(channel!=null){
        channel.disconnect();
        channel=null;
      }
    }
  }

  public class MyUserInfo implements UserInfo, UIKeyboardInteractive{
    public boolean promptYesNo(String str){
      Object[] options= {"yes", "no"};
      int foo=JOptionPane.showOptionDialog(JCTermSwingFrame.this.term, str,
          "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
          null, options, options[0]);
      return foo==0;
    }

    String passwd=null;
    String passphrase=null;
    JTextField pword=new JPasswordField(20);

    public String getPassword(){
      return passwd;
    }

    public String getPassphrase(){
      return passphrase;
    }

    public boolean promptPassword(String message){
      Object[] ob= {pword};
      JPanel panel=new JPanel();
      panel.add(pword);
      pword.requestFocusInWindow();      
      JOptionPane pane = new JOptionPane(panel,
                                         JOptionPane.QUESTION_MESSAGE,
                                         JOptionPane.OK_CANCEL_OPTION){
        public void selectInitialValue() {
        }
      };

      JDialog dialog = pane.createDialog(JCTermSwingFrame.this.term, 
                                         message);
      dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      dialog.setVisible(true);
      Object o = pane.getValue();

      if(o != null && ((Integer)o).intValue()==JOptionPane.OK_OPTION){
        passwd=pword.getText();
        return true;
      }
      else{
        return false;
      }
    }

    public boolean promptPassphrase(String message){
      return true;
    }

    public void showMessage(String message){
      JOptionPane.showMessageDialog(null, message);
    }

    final GridBagConstraints gbc=new GridBagConstraints(0, 0, 1, 1, 1, 1,
        GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0,
            0, 0), 0, 0);
    private Container panel;

    public String[] promptKeyboardInteractive(String destination, String name,
        String instruction, String[] prompt, boolean[] echo){
      panel=new JPanel();
      panel.setLayout(new GridBagLayout());

      gbc.weightx=1.0;
      gbc.gridwidth=GridBagConstraints.REMAINDER;
      gbc.gridx=0;
      panel.add(new JLabel(instruction), gbc);
      gbc.gridy++;

      gbc.gridwidth=GridBagConstraints.RELATIVE;

      JTextField[] texts=new JTextField[prompt.length];
      for(int i=0; i<prompt.length; i++){
        gbc.fill=GridBagConstraints.NONE;
        gbc.gridx=0;
        gbc.weightx=1;
        panel.add(new JLabel(prompt[i]), gbc);

        gbc.gridx=1;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        gbc.weighty=1;
        if(echo[i]){
          texts[i]=new JTextField(20);
        }
        else{
          texts[i]=new JPasswordField(20);
          texts[i].requestFocusInWindow();
        }
        panel.add(texts[i], gbc);
        gbc.gridy++;
      }
      for(int i=prompt.length-1; i>0; i--){
        texts[i].requestFocusInWindow();
      }
      JOptionPane pane = new JOptionPane(panel,
                                         JOptionPane.QUESTION_MESSAGE,
                                         JOptionPane.OK_CANCEL_OPTION){
        public void selectInitialValue() {
        }
      };
      JDialog dialog = pane.createDialog(JCTermSwingFrame.this.term, 
                                         destination+": "+name);
      dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      dialog.setVisible(true);
      Object o = pane.getValue();
      if(o != null && ((Integer)o).intValue()==JOptionPane.OK_OPTION){
        String[] response=new String[prompt.length];
        for(int i=0; i<prompt.length; i++){
          response[i]=texts[i].getText();
        }
        return response;
      }
      else{
        return null; // cancel
      }
    }
  }

  public void setProxyHttp(String host, int port){
    proxy_http_host=host;
    proxy_http_port=port;
    if(proxy_http_host!=null&&proxy_http_port!=0){
      proxy=new ProxyHTTP(proxy_http_host, proxy_http_port);
    }
    else{
      proxy=null;
    }
  }

  public String getProxyHttpHost(){
    return proxy_http_host;
  }

  public int getProxyHttpPort(){
    return proxy_http_port;
  }

  public void setProxySOCKS5(String host, int port){
    proxy_socks5_host=host;
    proxy_socks5_port=port;
    if(proxy_socks5_host!=null&&proxy_socks5_port!=0){
      proxy=new ProxySOCKS5(proxy_socks5_host, proxy_socks5_port);
    }
    else{
      proxy=null;
    }
  }

  public String getProxySOCKS5Host(){
    return proxy_socks5_host;
  }

  public int getProxySOCKS5Port(){
    return proxy_socks5_port;
  }

  public void setXHost(String xhost){
    this.xhost=xhost;
  }

  public void setXPort(int xport){
    this.xport=xport;
  }

  public void setXForwarding(boolean foo){
    this.xforwarding=foo;
  }

  public void setCompression(int compression){
    if(compression<0||9<compression)
      return;
    this.compression=compression;
    if(jschsession!=null){
      if(compression==0){
        jschsession.getSession().setConfig("compression.s2c", "none");
        jschsession.getSession().setConfig("compression.c2s", "none");
	jschsession.getSession().setConfig("compression_level", "0");
      }
     else{
       jschsession.getSession().setConfig("compression.s2c", "zlib@openssh.com,zlib,none");
       jschsession.getSession().setConfig("compression.c2s", "zlib@openssh.com,zlib,none");
       jschsession.getSession().setConfig("compression_level", 
                                          new Integer(compression).toString());
     }
     try{
       jschsession.getSession().rekey();
     }
     catch(Exception e){
       System.out.println(e);
     }
    }
  }

  public void setFontSize(int size){
    Configuration conf = JCTermSwing.getCR().load(configName);
    conf.font_size = size;
    JCTermSwing.getCR().save(conf);
    _setFontSize(size);
  }

  private void _setFontSize(int size){
    int mwidth = frame.getWidth()-term.getTermWidth();
    int mheight = frame.getHeight()-term.getTermHeight();
    term.setFont("Monospaced-"+size);
    frame.setSize(mwidth+term.getTermWidth(), mheight+term.getTermHeight());
    term.clear();
    term.redraw(0, 0, term.getWidth(), term.getHeight());
  }

  public int getCompression(){
    return this.compression;
  }

  public void setLineSpace(int foo){
    term.setLineSpace(foo);
  }

  public void setSplash(Splash foo){
    this.splash=foo;
  }

  public boolean getAntiAliasing(){
    return term.getAntiAliasing();
  }

  public void setAntiAliasing(boolean foo){
    term.setAntiAliasing(foo);
  }

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
    try{
      jschsession.getSession().setPortForwardingL(port1, host, port2);
    }
    catch(JSchException e){
    }
  }

  public void setPortForwardingR(int port1, String host, int port2){
    if(jschsession==null)
      return;
    try{
      jschsession.getSession().setPortForwardingR(port1, host, port2);
    }
    catch(JSchException e){
    }
  }

  public void actionPerformed(ActionEvent e){
    String action=e.getActionCommand();

    int _mode = SHELL;
    if(action.equals("Open SHELL Session...")){
      _mode=SHELL;
    }
    else if(action.equals("Open SFTP Session...")){
      _mode=SFTP;
    }

    if(action.equals("Open SHELL Session...")
        ||action.equals("Open SFTP Session...")){
      if(thread==null){
        mode=_mode;
        openSession();
      }
      else {
        frame.openFrame(_mode, configName);
      }
    }
    else if(action.equals("HTTP...")){
      String foo=getProxyHttpHost();
      int bar=getProxyHttpPort();
      String proxy=JOptionPane.showInputDialog(this,
          "HTTP proxy server (hostname:port)", ((foo!=null&&bar!=0) ? foo+":"
              +bar : ""));
      if(proxy==null)
        return;
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
    else if(action.equals("SOCKS5...")){
      String foo=getProxySOCKS5Host();
      int bar=getProxySOCKS5Port();
      String proxy=JOptionPane.showInputDialog(this,
          "SOCKS5 server (hostname:1080)", ((foo!=null&&bar!=0) ? foo+":"+bar
              : ""));
      if(proxy==null)
        return;
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
    else if(action.equals("X11 Forwarding...")){
      String display=JOptionPane.showInputDialog(this,
          "XDisplay name (hostname:0)", (xhost==null) ? "" : (xhost+":"+xport));
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
    else if((action.equals("AntiAliasing"))){
      setAntiAliasing(!getAntiAliasing());
    }
    else if(action.equals("Compression...")){
      String foo=JOptionPane
          .showInputDialog(
              this,
              "Compression level(0-9)\n0 means no compression.\n1 means fast.\n9 means slow, but best.",
              new Integer(compression).toString());
      try{
        if(foo!=null){
          compression=Integer.parseInt(foo);
          setCompression(compression);
        }
      }
      catch(Exception ee){
      }
    }
    else if(action.equals("About...")){
      JOptionPane.showMessageDialog(this, COPYRIGHT);
      return;
    }
    else if((action.equals("Local Port..."))||(action.equals("Remote Port..."))){
      if(jschsession==null){
        JOptionPane.showMessageDialog(this,
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

        String foo=JOptionPane.showInputDialog(this, title, "");
        if(foo==null)
          return;
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
    else if(action.equals("Quit")){
      quit();
    }
  }

  public JMenuBar getJMenuBar(){
    JMenuBar mb=new JMenuBar();
    JMenu m;
    JMenuItem mi;

    m=new JMenu("File");
    mi=new JMenuItem("Open SHELL Session...");
    mi.addActionListener(this);
    mi.setActionCommand("Open SHELL Session...");
    m.add(mi);
    mi=new JMenuItem("Open SFTP Session...");
    mi.addActionListener(this);
    mi.setActionCommand("Open SFTP Session...");
    m.add(mi);
    mi=new JMenuItem("Quit");
    mi.addActionListener(this);
    mi.setActionCommand("Quit");
    m.add(mi);
    mb.add(m);

    m=new JMenu("Proxy");
    mi=new JMenuItem("HTTP...");
    mi.addActionListener(this);
    mi.setActionCommand("HTTP...");
    m.add(mi);
    mi=new JMenuItem("SOCKS5...");
    mi.addActionListener(this);
    mi.setActionCommand("SOCKS5...");
    m.add(mi);
    mb.add(m);

    m=new JMenu("PortForwarding");
    mi=new JMenuItem("Local Port...");
    mi.addActionListener(this);
    mi.setActionCommand("Local Port...");
    m.add(mi);
    mi=new JMenuItem("Remote Port...");
    mi.addActionListener(this);
    mi.setActionCommand("Remote Port...");
    m.add(mi);
    mi=new JMenuItem("X11 Forwarding...");
    mi.addActionListener(this);
    mi.setActionCommand("X11 Forwarding...");
    m.add(mi);
    mb.add(m);

    m=new JMenu("Etc");

    mi=new JMenuItem("AntiAliasing");
    mi.addActionListener(this);
    mi.setActionCommand("AntiAliasing");
    m.add(mi);

    mi=new JMenuItem("Compression...");
    mi.addActionListener(this);
    mi.setActionCommand("Compression...");
    m.add(mi);

    JMenu mcolor=new JMenu("Color");
    final ActionListener mcolor_action = new ActionListener(){
      public void actionPerformed(ActionEvent e){
        setFgBg(e.getActionCommand());
      }
    };
    mcolor.addMenuListener(new MenuListener(){
      public void menuSelected(MenuEvent me){
        JMenu jm = (JMenu)me.getSource();
        String[] fg_bg = JCTermSwing.getCR().load(configName).fg_bg;
        for(int i=0; i < fg_bg.length; i++){
          String[] tmp = fg_bg[i].split(":");
          JMenuItem mi = new JMenuItem("ABC");
          mi.setForeground(JCTermSwing.toColor(tmp[0]));
          mi.setBackground(JCTermSwing.toColor(tmp[1]));
          mi.setActionCommand(fg_bg[i]);
          mi.addActionListener(mcolor_action);
          jm.add(mi);
        }
      }
      public void menuDeselected(MenuEvent me){
        JMenu jm = (JMenu)me.getSource();
        jm.removeAll();
      }
      public void menuCanceled(MenuEvent arg){ }
    });
    m.add(mcolor);

    JMenu mfsize=new JMenu("Font size");
    final ActionListener mfsize_action = new ActionListener(){
      public void actionPerformed(ActionEvent e){
        String _font_size=e.getActionCommand();
        try {
          setFontSize(Integer.parseInt(_font_size));
        }
        catch(NumberFormatException nfe){
        }
      }
    };
    mfsize.addMenuListener(new MenuListener(){
      public void menuSelected(MenuEvent me){
        JMenuItem mi;
        JMenu jm = (JMenu)me.getSource();
        int font_size = JCTermSwing.getCR().load(configName).font_size;
        mi = new JMenuItem("Smaller ("+(font_size-1)+")");;
        mi.setActionCommand(""+(font_size-1));
        mi.addActionListener(mfsize_action);
        jm.add(mi);
        mi = new JMenuItem("Larger ("+(font_size+1)+")");
        mi.setActionCommand(""+(font_size+1));
        mi.addActionListener(mfsize_action);
        jm.add(mi);
      }
      public void menuDeselected(MenuEvent me){
        JMenu jm = (JMenu)me.getSource();
        jm.removeAll();
      }
      public void menuCanceled(MenuEvent arg){ }
    });
    m.add(mfsize);

    mb.add(m);

    m=new JMenu("Help");
    mi=new JMenuItem("About...");
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
    /*
    if(jschsession!=null){
      jschsession.dispose();
      jschsession=null;
    }
    */
  }

  public void setTerm(JCTermSwing term){
    this.term=term;
  }

  public Term getTerm(){
    return term;
  }

  public void openFrame(int _mode, String configName){
    JCTermSwingFrame c = new JCTermSwingFrame("JCTerm", configName);
    c.mode=_mode;
    c.setXForwarding(true);
    c.setXPort(xport);
    c.setXHost(xhost);
    c.setLocationRelativeTo(null);
    c.setVisible(true);
    c.setResizable(true);
  }

  void setFgBg(String fg_bg){
    Configuration conf = JCTermSwing.getCR().load(configName);
    conf.addFgBg(fg_bg);
    JCTermSwing.getCR().save(conf);
    _setFgBg(fg_bg);
  }

  private void _setFgBg(String fg_bg){
    String[] tmp = fg_bg.split(":");
    Color fg = JCTermSwing.toColor(tmp[0]);
    Color bg = JCTermSwing.toColor(tmp[1]);
    term.setForeGround(fg);
    term.setDefaultForeGround(fg);
    term.setBackGround(bg);
    term.setDefaultBackGround(bg);
    term.resetCursorGraphics();
    term.clear();
    term.redraw(0, 0, term.getWidth(), term.getHeight());
  }

  private String promptDestination(JComponent term, String[] destinations){
    JComboBox jb = new JComboBox();
    jb.setEditable(true);

    for(int i=0; i<destinations.length; i++){
      jb.addItem(destinations[i]);
    }

    JPanel panel=new JPanel();
    jb.requestFocusInWindow();      
    JOptionPane pane = new JOptionPane(jb,
                                       JOptionPane.QUESTION_MESSAGE,
                                       JOptionPane.OK_CANCEL_OPTION){
      public void selectInitialValue() {
      }
    };

    JDialog dialog = pane.createDialog(JCTermSwingFrame.this.term, 
                                       "Enter username@hostname");
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.setVisible(true);
    Object o = pane.getValue();

    String d = null;
    if(o != null && ((Integer)o).intValue()==JOptionPane.OK_OPTION){
      d=(String)jb.getSelectedItem();
    }
    if(d == null || d.length()==0)
      return null;
    else
      return d;
  }

  void setFrame(Frame frame){
    this.frame=frame;
  }

  void applyConfig(String configName){
    this.configName = configName;
    Configuration conf = JCTermSwing.getCR().load(configName);
    _setFontSize(conf.font_size);
    _setFgBg(conf.fg_bg[0]);
  }

  public static void main(String[] arg){
    JCTermSwing.setCR(new ConfigurationRepositoryFS());

    String s = System.getProperty("jcterm.config.use_ssh_agent");
    if(s != null && s.equals("true"))
      JSchSession.useSSHAgent(true);

    final JCTermSwingFrame frame=new JCTermSwingFrame("JCTerm");
    frame.setCloseOnExit(false);
    frame.setVisible(true);
    frame.setResizable(true);
  }
}
