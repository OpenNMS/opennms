/*
 * Copyright 2003-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opennms.netmgt.inventory;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.*;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TelnetNotificationHandler;
import org.apache.commons.net.telnet.EchoOptionHandler;
import org.apache.commons.net.telnet.TerminalTypeOptionHandler;
import org.apache.commons.net.telnet.SuppressGAOptionHandler;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.exolab.castor.xml.*;
import java.util.*;
import org.opennms.netmgt.config.inventory.plugin.*;

/***

 * @author maurizio
 ***/
public class TelnetBroker implements TelnetNotificationHandler {
	private TelnetClient tc = null;
	private Vector exchangeVec = new Vector();
    private String command=null; 
    private final int DEFAULT_PORT=23;
    private final int DEFAULT_TIMEOUT=1; 
	/***
	 * Main for the TelnetClientExample.
	 ***/
	public TelnetBroker(String ipAddress, Map parameters, String configFile)
		throws
			IOException,
			MarshalException,
			ValidationException {
		InputStream cfgIn = new FileInputStream(configFile);
		PluginConfiguration telnetConfig = (PluginConfiguration) Unmarshaller.unmarshal(PluginConfiguration.class,	new InputStreamReader(cfgIn));
		Enumeration exchangeEnum = telnetConfig.enumerateExchange();
		
		String strPort = (String)parameters.get("port");
		int port=DEFAULT_PORT;
		if(strPort!=null){
			port = Integer.parseInt(strPort);
		}
		String strTimout = (String)parameters.get("timeout");
		int timeout = DEFAULT_TIMEOUT;
		if(strTimout!=null){
			timeout = Integer.parseInt(strTimout);
		}
		command = (String) parameters.get("command");
		if(command==null){
			throw new IOException("Parameter 'command' not found");
		}
		while (exchangeEnum.hasMoreElements()) {
			Exchange ex = (Exchange) exchangeEnum.nextElement();
			exchangeVec.add(ex);
		}
		boolean foundCommand=false;
		Enumeration enumItemMapping = telnetConfig.enumerateItemMapping();
		while (enumItemMapping.hasMoreElements()) {
					ItemMapping im = (ItemMapping) enumItemMapping.nextElement();
					String tmpCommand = im.getCommand();
					if(tmpCommand.equals(command)){
						foundCommand=true;
						Enumeration enumExchange = im.enumerateExchange();
						while(enumExchange.hasMoreElements()){
							exchangeVec.add(enumExchange.nextElement());
						}
					}
					
				}
		if(foundCommand==false){
			throw new IOException("Command '"+command+"' not found in plugin-conf-file.");
		}
		//System.out.println(exchangeVec);
		tc = new TelnetClient();

		TerminalTypeOptionHandler ttopt =
			new TerminalTypeOptionHandler("VT100", false, false, true, false);
		EchoOptionHandler echoopt =
			new EchoOptionHandler(true, false, true, false);
		SuppressGAOptionHandler gaopt =
			new SuppressGAOptionHandler(true, true, true, true);

		try {
			tc.addOptionHandler(ttopt);
			tc.addOptionHandler(echoopt);
			tc.addOptionHandler(gaopt);
		} catch (InvalidTelnetOptionException e) {
			System.err.println(
				"Error registering option handlers: " + e.getMessage());
		}

		try {
			tc.connect(ipAddress, port);
			tc.registerNotifHandler(this);
		} catch (Exception e) {
			System.err.println("Exception while connecting:" + e.getMessage());
			System.exit(1);
		}

	}

	/***
	 * Callback method called when TelnetClient receives an option
	 * negotiation command.
	 * <p>
	 * @param negotiation_code - type of negotiation command received
	 * (RECEIVED_DO, RECEIVED_DONT, RECEIVED_WILL, RECEIVED_WONT)
	 * <p>
	 * @param option_code - code of the option negotiated
	 * <p>
	 ***/
	public void receivedNegotiation(int negotiation_code, int option_code) {
		String command = null;
		if (negotiation_code == TelnetNotificationHandler.RECEIVED_DO) {
			command = "DO";
		} else if (
			negotiation_code == TelnetNotificationHandler.RECEIVED_DONT) {
			command = "DONT";
		} else if (
			negotiation_code == TelnetNotificationHandler.RECEIVED_WILL) {
			command = "WILL";
		} else if (
			negotiation_code == TelnetNotificationHandler.RECEIVED_WONT) {
			command = "WONT";
		}
		//System.out.println("Received " + command + " for option code " + option_code);
	}

	public String doCommand(int timeoutSec)
		throws IOException {
		InputStream instr = tc.getInputStream();
		String response = null;
		String lastPrompt = null;
		String lastCommand = null;
		try {

			int i = 0;
			String tmpCommand = "";
			boolean nextExit = false;
			for(i=0;i<exchangeVec.size();){
				Exchange ex = (Exchange) exchangeVec.get(i);
				int numBytesRead = 0;
				Thread.sleep(timeoutSec * 1000);
				byte[] buff = new byte[2048];
				String strRecv = null;
				if (nextExit) {
					strRecv = getCommandResponse(instr, lastPrompt);
				} else {
				strRecv = getResponse(instr);
				}
				//System.out.println(strRecv);
				if (nextExit) {
					response = strRecv;
					sendCommand("exit");
					//System.out.println(	"lastprompt:"+ lastPrompt+ " lastcommand:"+ lastCommand);
					break;
				}
				if (strRecv == null) {
					throw new IOException();
				}
				strRecv = strRecv.trim();
			//	System.out.println(strRecv+ "... prompt="+ ex.getPrompt()+ "... "+ strRecv.endsWith(ex.getPrompt()));
				if (strRecv.endsWith(ex.getPrompt())) {
					tmpCommand = ex.getCommand();
					i++;
				} else if (strRecv.equals("") || strRecv.endsWith(ex.getErrorprompt())){
						tc.disconnect();
						throw new IOException();
						}
						    
				lastPrompt = strRecv;
				lastCommand = tmpCommand;
				sendCommand(tmpCommand);
			//	System.out.println("SendCommand "+tmpCommand);
			}
			//System.out.println("LASTPROMPT: "+lastPrompt);
			response = getCommandResponse(instr, lastPrompt); 
			
		} catch (Exception e) {
			throw new IOException("Unable to read/write with target machine.");
		}

		tc.disconnect();
		response = response.replaceAll(lastPrompt, "");
		response = response.replaceAll(lastCommand, "");
		return response;
	}

	private void sendCommand(String command) throws IOException {
		OutputStream outstr = tc.getOutputStream();
		command += "\n";
		byte[] buff = command.getBytes();
		outstr.write(buff, 0, buff.length);
		outstr.flush();
	}

	private String getResponse(InputStream is) throws IOException {
		int ret_read = 0;
		String retStr = "";
		//while((ret_read=is.available())>0){
		//ret_read = 0;
		while ((ret_read = is.available()) > 0) {
			//System.out.println("AVAILABLE: " + ret_read);

			byte[] buff = new byte[2048];
			is.read(buff, 0, ret_read);
			String tmpStr = new String(buff, 0, ret_read);
			retStr += tmpStr;
			//if(retStr.endsWith("end"))
			// return retStr;
		}

		//System.out.println("AVAILABLE: " + ret_read);
		return retStr.equals("") ? null : retStr;
	}

	private String getCommandResponse(InputStream is, String end)
		throws IOException, InterruptedException {
		int ret_read = 0;
		String retStr = "";
		while (true) {
			byte[] buff = new byte[2048];
			ret_read = is.read(buff);
			String tmpStr = new String(buff, 0, ret_read);
			retStr += tmpStr;
			if (retStr.endsWith(end))
				return retStr;
			Thread.sleep(50);
		}
	}

//	public static void main(String[] args) throws Exception {
//		Map m = new HashMap();
//		m.put("port","23");
//		m.put("command","show ver");
//		TelnetBroker tce = new TelnetBroker("10.0.0.251", m, "c://cisco.xml");
//		System.out.println(tce.doCommand(1));
//	}
}
