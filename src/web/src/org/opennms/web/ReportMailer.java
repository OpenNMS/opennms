//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.blast.com/
//

package org.opennms.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

import javax.servlet.ServletException;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.UserFactory;


/**
 * @author <A HREF="mailto:jacinta@opennms.org">Jacinta Remedios</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class ReportMailer extends Object implements Runnable
{
         /**
          * The log4j category used to log debug messsages
          * and statements.
          */
         private static final String LOG4J_CATEGORY = "OpenNMS.Report";

	protected String scriptGenerateReport;
	protected String scriptMailReport;
	protected String finalEmailAddr;
	protected UserFactory userFactory;
	protected String filename;
	protected String commandParms;
	protected String format;
	Category log;
	
	public ReportMailer() throws ServletException
	{
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
                log = ThreadCategory.getInstance(this.getClass());
	}

	public void initialise( String fileName,
			        String userName,
				String generateReport,
				String mailReport,
				String parms,
				String fmt) throws ServletException{
		
		filename = fileName;
		commandParms = parms;
		this.scriptGenerateReport = generateReport;
		this.scriptMailReport = mailReport;	
		this.format = fmt;
			
		if(log.isDebugEnabled())
		{
			log.debug("scriptGenerateReport " + scriptGenerateReport);
			log.debug("parms " + parms);
			log.debug("fmt " + fmt);
		}
		if( this.scriptGenerateReport == null ) {
			throw new ServletException("Missing required init parameter: script.generateReport");
		}
		
		if( this.scriptMailReport == null ) {
			throw new ServletException("Missing required init parameter: script.mailReport");
		}		
				
		try { 				
			UserFactory.init();
			this.userFactory = UserFactory.getInstance();
		}
		catch(Exception e) {
			if(log.isDebugEnabled())
				log.debug("could not initialize the UserFactory", e);
			throw new ServletException("could not initialize the UserFactory", e);
		}
       		
		if(userName == null) {
			//shouldn't happen
			throw new IllegalStateException("OutageReportServlet can't work without authenticating the remote user.");
		}
	
		String emailAddr = null;
	
		try {		
			emailAddr = this.getEmailAddress(userName);
			finalEmailAddr = emailAddr;
			
			if(emailAddr == null || emailAddr.trim().length() == 0) {
				return;
			}
		}
		catch( Exception e ) {
			//if(log.isDebugEnabled())
			//	log.debug("error looking up email address", e);
			throw new ServletException(e);
		}
	}

	public String getEmailAddress()
	{
		return finalEmailAddr;
	}

	public void run() {
		
		if(log.isInfoEnabled())
			log.info("thread to generate outage report started");
		
		try {								
			generateFile(scriptGenerateReport);
			if(log.isInfoEnabled())
				log.info("outage report is generated.  filename is " + filename );
		}				
		catch( InterruptedException e ) {
			if(log.isDebugEnabled())
				log.debug("interrupted while generating report", e);
			return;
		}				
		catch( Exception e ) {
			if(log.isDebugEnabled())
				log.debug("error generating report", e);
			return;
		}
		
		try {								
			mailFileToUser(scriptMailReport, filename, finalEmailAddr);
			if(log.isInfoEnabled())
				log.info("outage report has been mailed to user at " + finalEmailAddr);
		}				
		catch( Exception e ) {
			if(log.isDebugEnabled())
				log.debug("error mailing report", e);
			return;
		}
	}

	/** returns null if no email address is configured for the user */
	protected String getEmailAddress(String username) throws IOException, MarshalException, ValidationException {
		if(username == null) {
			throw new IllegalArgumentException("Cannot take null paramters.");
		}
		
		return this.userFactory.getEmail(username);
	}
	
	
	/** returns the fully-qualified filename of the generated PDF report */
	protected void generateFile(String shellScript) throws IOException, InterruptedException {
		if(shellScript == null ) {
			throw new IllegalArgumentException("Cannot take null parameters.");
		}
		
        	String[] cmdArgs;
		cmdArgs = new String[3];
		int i = 0;
		cmdArgs[i++] = shellScript;
		if(commandParms != null)
		{
			if(!commandParms.equals(""))
				cmdArgs[i++] = commandParms;
		}
		else
			cmdArgs[i++] = "";
		if(null != format)
		{
			if(!format.equals(""))
				cmdArgs[i] = format;
		}
		else
			cmdArgs[i] = "";
		if(log.isDebugEnabled())
		{
			log.debug("Command Line Args " + cmdArgs[0]);
			log.debug("Command Line Args " + cmdArgs[1]);
			log.debug("Command Line Args " + cmdArgs[2]);
		}
        	java.lang.Process process = Runtime.getRuntime().exec( cmdArgs );
        
        	//get the stderr to see if the command failed
        	BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        	if( err.ready() ) {
            		//get the error message
			StringWriter tempErr = new StringWriter();            
            		Util.streamToStream(err, tempErr);
            		String errorMessage = tempErr.toString();
            
            		//log the error message
			if(log.isDebugEnabled())
            			log.debug("Read from stderr: " + errorMessage);
            
            		throw new IOException("Could not generate outage report" );
        	}		

		//wait until the file is completely generated
		process.waitFor();		
	}
	
	protected void mailFileToUser(String mailScript, String filename, String emailAddr) throws IOException {
		if(mailScript == null || filename == null || emailAddr == null) {
			throw new IllegalArgumentException("Cannot take null paramters.");
		}		
				
                String[] cmdArgs = { mailScript, filename, emailAddr };
                java.lang.Process process = Runtime.getRuntime().exec( cmdArgs );
                
                //get the stderr to see if the command failed
                BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        
                if( err.ready() ) {
                    	//get the error message
                    	StringWriter tempErr = new StringWriter();            
                    	Util.streamToStream(err, tempErr);
                    	String errorMessage = tempErr.toString();
                    
                    	//log the error message
			if(log.isDebugEnabled())
	                    	log.debug("Read from stderr: " + errorMessage);
                    
                    	throw new IOException("Could not mail outage report" );
                }				
	}

}
