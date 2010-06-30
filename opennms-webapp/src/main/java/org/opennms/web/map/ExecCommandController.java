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

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;


/**
 * <p>ExecCommandController class.</p>
 *
 * @author mmigliore
 *
 * this class provides to create pages for executing ping and traceroute commands
 * @version $Id: $
 * @since 1.6.12
 */
public class ExecCommandController implements Controller {
	Category log;

	/** {@inheritDoc} */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		ThreadCategory.setPrefix(MapsConstants.LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());
		
		String command = request.getParameter("command");
		String address = request.getParameter("address");
	    log.info("Executing "+command+ " "+ address+".");
        response.setBufferSize(0);
        response.setContentType("text/html");
        response.setHeader("pragma","no-Chache");
        response.setHeader("Expires","0");
        response.setHeader("Chache-Control","no-Chache");
        final OutputStreamWriter os = new OutputStreamWriter(response.getOutputStream());
        os.write("<html>"); 

        try {
			final Command p = new Command(command, address);
			String comm = (command.startsWith("ping"))?"Ping":null;
			if(comm==null){
				comm = (command.startsWith("traceroute"))?"Trace Route":"";
			}
			
			os.write("<head><title>"+comm+" "+address+" | OpenNMS Web Console</title>" +
    		"</head>" +
			"<div width='100%' align='right'>" +
			"<input type='button' value='Close' onclick='window.close();'/>" +
			"</div>" +
    		"<h3><font face='courier,arial'>Executing "+comm+" for the ip address "+address+"</h3>");
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
			        	log.warn(io);
			        }
			    }
			}).start();
			try{
				p.waitFor();
			}catch(Exception e){
				log.warn(e);
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
	    
	    public Command(String command, String addr) throws IOException, IllegalStateException
	    {
	    	if(command.startsWith("traceroute") || command.startsWith("ping")){
	    		 p = Runtime.getRuntime().exec(command +" "+ addr);
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
