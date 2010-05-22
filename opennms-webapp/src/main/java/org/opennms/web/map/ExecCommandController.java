//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jul 24: Organize imports. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.web.map;

/*
 * Created on 2-Lug-2007
 *
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.web.WebSecurityUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;


/**
 * @author mmigliore
 * @author antonio
 * 
 * this class provides to create pages for executing ping and traceroute commands
 * 
 */
public class ExecCommandController implements Controller {
	ThreadCategory log;

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
   		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());

        int timeOut = 1;
        int numberOfRequest = 10;
        int packetSize = 56;
        String hopAddress = null;
        
        String command = request.getParameter("command");
        if (command == null) throw new  IllegalArgumentException("Command is required");
        
        String commandToExec = command;

        String address = request.getParameter("address");
        if (address == null) throw new  IllegalArgumentException("Address is required");

        String numericoutput = request.getParameter("numericOutput");
        if (numericoutput != null && numericoutput.equals("true")) {
            commandToExec = commandToExec + " -n ";
        }

        if (command.equals("ping")) {
	        String timeout = request.getParameter("timeOut");
	        if (timeout != null)
	            timeOut = WebSecurityUtils.safeParseInt(timeout);
	        String numberofrequest = request.getParameter("numberOfRequest");
	        if (numberofrequest != null )
	            numberOfRequest = WebSecurityUtils.safeParseInt(numberofrequest);
            String packetsize = request.getParameter("packetSize");
            if (packetsize != null)
                packetSize = WebSecurityUtils.safeParseInt(packetsize);
		    // these are optionals
            String solaris = request.getParameter("solaris");
            if (solaris != null && solaris.equals("true")) {
                commandToExec=commandToExec+" -I "+timeOut+" "+ address +" "+packetSize+" "+numberOfRequest ;
            } else {
                commandToExec=commandToExec+" -c "+numberOfRequest+" -i "+timeOut+" -s "+packetSize + " "+ address; 
            }
            
		} else if(command.equals("traceroute")) {
		    hopAddress = request.getParameter("hopAddress");
		    if (hopAddress != null) {
		        commandToExec = commandToExec + " -g " + hopAddress + " " + address;
		    } else {
                commandToExec = commandToExec + " " + address;		        
		    }
		} else if (command.equals("ipmitool")) {
		    String ipmiCommand = request.getParameter("ipmiCommand");
		    String ipmiUserName = request.getParameter("ipmiUser");
		    String ipmiPassword = request.getParameter("ipmiPassword");
		    String ipmiProtocol = request.getParameter("ipmiProtocol");
		    
		    if(ipmiCommand !=null && ipmiUserName != null &&  ipmiPassword != null ){
		        commandToExec = commandToExec + " -I "+ipmiProtocol+" -U " 
		        + ipmiUserName +" -P " + ipmiPassword + " -H " + address +" " + ipmiCommand;
		    }
		    else
		        throw new IllegalStateException("IPMITool requires Protocol, Command, Usernane and Password");
		       
		} else {
		    throw new IllegalStateException("Command "+ command+" not supported.");   
		}
        
	    log.info("Executing "+commandToExec);
        response.setBufferSize(0);
        response.setContentType("text/html");
        response.setHeader("pragma","no-Cache");
        response.setHeader("Expires","0");
        response.setHeader("Cache-Control","no-Cache");
        final OutputStreamWriter os = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
        os.write("<html>"); 

        try {
			final Command p = new Command(commandToExec);
			String comm = (command.startsWith("ping"))?"Ping":null;
			if(comm==null){
				comm = (command.startsWith("traceroute"))?"Trace Route":"";
			}
			
			os.write("<head><title>"+comm+" "+address+" | OpenNMS Web Console</title>" +
    		"</head>" +
			"<div width='100%' align='right'>" +
			"<input type='button' value='Close' onclick='window.close();'/>" +
			"</div>" +
    		"<h3><font face='courier,arial'>Executing "+comm+" for the IP address "+address+"</h3>");
			new Thread(new Runnable()
			{
			    public void run()
			    {
			        try
			        {
			            String s = null;
			            while((s = p.getBufferedReader().readLine()) != null)
			            {
			               os.write(s);
			               os.write("<br>");
			               os.flush();
			               //log.debug(s);
			            }
			            
			        }
			        catch(IOException io){
			        	log.warn(io.getMessage());
			        }
			    }
			}, this.getClass().getSimpleName()).start();
			try{
				p.waitFor();
			}catch(Exception e){
				log.warn(e.getMessage());
			}

		} catch (Exception e) {
			log.error("An error occourred while executing command.",e);
			os.write("An error occourred.");
		}finally{
			os.write("</font>" +
					"<br>" +
					"</html>");
			os.flush();
			os.close();
		}

		return null;
	}
	
	private class Command
	{
	    private BufferedReader out;
	    private Process p;
	    
	    public Command(String command) throws IOException, IllegalStateException
	    {
	    	if(command.startsWith("traceroute") || command.startsWith("ping") || command.startsWith("ipmitool")){
	    		 p = Runtime.getRuntime().exec(command);
	 	        out = new BufferedReader(new InputStreamReader(p.getInputStream()));
	    	}else{
	    		throw new IllegalStateException("Command "+ command+" not supported.");
	    	}
	    }
	    
	    public BufferedReader getBufferedReader()
	    {
	        return out;
	    }
	    
	    public void waitFor() throws InterruptedException
	    {
	        p.waitFor();
	    }
	}

}
