//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 12 Nov 2002: Added response time reports to webUI.
//
// Original coda base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.web.response;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.MissingParameterException;
import org.opennms.web.Util;


/**
 * A servlet that creates an XML dump of network performance data
 * using the <a href="http://www.rrdtool.org/">RRDTool</a>.
 *
 * <p>This servlet executes an <em>rrdtool dump</em> command
 * in another process, piping its XML output to the 
 * <code>ServletOutputStream</code>. </p>
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class RRDDumpServlet extends HttpServlet
{
    /** 
     * The working directory where we find the RRD files.
     */
    protected String workDir;

    
    /** 
     * The dump command (minus the actual RRD filename).
     */
    protected String commandPrefix;


    /**
     * Initializes this servlet by reading the rrdtool-graph properties file.
     */
    public void init() throws ServletException {
        ServletConfig config = this.getServletConfig();
        
        this.workDir = config.getInitParameter("rrd-directory");            
        this.commandPrefix = config.getInitParameter("rrd-dump-command");
        
        if( this.workDir == null || this.commandPrefix == null ) {
            throw new ServletException( "Did not get all required init params. rrd-directory and rrd-dump-command are both required.  Please check your web.xml." );
        }
    }


    /**
     * Checks the parameters passed to this servlet, and if all are okay, executes
     * the RRDTool command in another process and pipes its XML output to the 
     * <code>ServletOutputStream</code> back to the requesting web browser.
     */
    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        String rrd = request.getParameter( "rrd" );

        if( rrd == null ) {
            throw new MissingParameterException( "rrd" );
        }

        //build the command
        String command = this.commandPrefix + " " + rrd;
        this.log( command );
        
        //parse the command into an array and fork a process for it
        String[] commandArray = Util.createCommandArray( command, '@' );
        Process process = Runtime.getRuntime().exec( commandArray, null, new File(this.workDir) );
        
        PrintWriter out = response.getWriter();
        BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        if( err.ready() ) {
            //get the error message
            StringWriter tempErr = new StringWriter();            
            Util.streamToStream(err, tempErr);
            String errorMessage = tempErr.toString();
            
            //log the error message
            this.log("Read from stderr: " + errorMessage);
            
            //send the error message to the client
            response.setContentType( "text/plain" );
            Util.streamToStream( new StringReader(errorMessage), out ); 
        }
        else {
            //get the XML output and send it to the client
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            response.setContentType( "text/xml" );                
            Util.streamToStream( in, out );  
        }

        out.close();
    }

}
