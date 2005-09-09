/*
 * Created on 31-ago-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.opennms.netmgt.inventory;
import java.io.*;
import java.util.*;


import org.opennms.netmgt.config.inventory.plugin.ssh.*;
import org.exolab.castor.xml.Unmarshaller;
import com.sshtools.j2ssh.SshClient; 
import com.sshtools.j2ssh.session.*;
import com.sshtools.j2ssh.connection.*;
import org.exolab.castor.xml.*;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.transport.IgnoreHostKeyVerification;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

public class SshBroker{
  // A buffered reader so we can request information from the user
  private static BufferedReader reader =
		new BufferedReader(new InputStreamReader(System.in));
  private Vector commandVec= new Vector();
  private String session=null; 
  private String user=null; 
  private String password=null; 
  private final int DEFAULT_PORT=22;
  private final int DEFAULT_TIMEOUT=1; 
  private int timeout;
  private String ipAddress;
 
  public SshBroker(String ipAddress, Map parameters, String configFile)
	  throws  IOException, ValidationException, MarshalException {
	  
	  Category log = ThreadCategory.getInstance(getClass());	
	  this.ipAddress = ipAddress;	
	  InputStream cfgIn = new FileInputStream(configFile);
	  SshConfig sshConfig = (SshConfig) Unmarshaller.unmarshal(SshConfig.class,	new InputStreamReader(cfgIn));
	  Enumeration sessionEnum = sshConfig.enumerateSession();
		
	  String strPort = (String)parameters.get("port");
	  int port=DEFAULT_PORT;
	  if(strPort!=null){
		  port = Integer.parseInt(strPort);
	  }
	  String strTimout = (String)parameters.get("timeout");
	  timeout = DEFAULT_TIMEOUT;
	  if(strTimout!=null){
		  timeout = Integer.parseInt(strTimout);
	  }
	  session = (String) parameters.get("session");
	  session = session.trim();
	  if(session==null){
		  throw new IOException("Parameter 'session' not found");
	  }
	  boolean foundSession=false;
	  while (sessionEnum.hasMoreElements()) {
		  Session sess = (Session) sessionEnum.nextElement();
		  String str = ""+ sess.getCommandCount();
		  if(session.equals((sess.getName()).trim())){
			foundSession=true;
			user = sess.getUser();
			if(user==null)
				throw new IOException("Parameter 'user' not found in plugin-conf-file");
			password = sess.getPassword();
			
			if(password==null)
				throw new IOException("Parameter 'password' not found in plugin-conf-file");
			Enumeration enumCommands = sess.enumerateCommand();
			while(enumCommands.hasMoreElements()){
				commandVec.add(enumCommands.nextElement());
			}
		  }
	  }
	 

	  if(foundSession==false){
		  throw new IOException("Session '"+session+"' not found in plugin-conf-file.");
	  }

 }

  

  public String doCommand() throws IOException, InterruptedException {
	  Category log = ThreadCategory.getInstance(getClass());
	  if(timeout<0)
	  	   timeout = DEFAULT_TIMEOUT;
  	  timeout = timeout*1000;
	  SshClient ssh = new SshClient();
	  ssh.connect(ipAddress,new IgnoreHostKeyVerification());
	  PasswordAuthenticationClient pwd = new PasswordAuthenticationClient();
	  pwd.setUsername(user);
	  pwd.setPassword(password);
 	  int result = ssh.authenticate(pwd);
	  if(result==AuthenticationProtocolState.FAILED){
		  log.debug("SSH authentication failed.");
		  throw new IOException("SSH authentication failed.");
	  }
	  if(result==AuthenticationProtocolState.PARTIAL){
		  log.debug("SSH authentication succeeded but another authentication is required");
			throw new IOException("SSH authentication succeeded but another authentication is required");
      }
							 
	  if(result==AuthenticationProtocolState.COMPLETE)
		  log.debug("The authentication is complete");
		SessionChannelClient session = ssh.openSessionChannel();
		session.startShell();
		
		ChannelOutputStream out = session.getOutputStream();
		String retStr = null;
		Iterator it = commandVec.iterator();
		ChannelInputStream in = session.getInputStream();
	
		while(it.hasNext()){ 
			String cmd = (String) it.next() +System.getProperty("line.separator");
			log.debug("SSH COMMAND: '"+cmd+"'");
			out.write(cmd.getBytes());
			int read;
			retStr = "";
			while(true) {
				Thread.sleep(timeout);
				if(in.available()<=0)
					break;
				byte buffer[] = new byte[4092];
				long time = System.currentTimeMillis();
				read = in.read(buffer);
				String tmpStr = new String(buffer, 0, read);
				retStr += tmpStr;
			  }
		}
		log.debug(retStr);
		out.write("exit\n".getBytes());
		session.close();
		ssh.disconnect();
		return retStr;
  }

}

