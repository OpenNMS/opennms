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

package org.opennms.web.graph;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.resource.Vault;
import org.opennms.web.Util;


/**
 * A servlet that creates a custom graph of network performance data
 * using the <a href="http://www.rrdtool.org/">RRDTool</a>.
 * 
 * <p>This servlet executes an <em>rrdtool graph</em> command
 * in another process, piping its PNG file to standard out.  The
 * servlet then reads that PNG file and returns it on the
 * <code>ServletOutputStream</code>. </p>
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class RRDAdHocGraphServlet extends HttpServlet
{
    /**
     * The working directory as specifed in the rrdtool-graph properties file.
     */
    protected File workDir;

    /**
     * The prefix for the RRDtool command (including the executable's pathname)
     * as specified in the rrdtool-graph properties file.
     */
    protected String commandPrefix;


    /**
     * The mime type of the image we will return.
     */
    protected String mimeType;


    /**
     * Holds the information specified in the rrdtool-graph properties file.
     */
    protected Properties properties;


    /**
     * Initializes this servlet by reading the rrdtool-graph properties file.
     */
    public void init() throws ServletException {
        try {
            String propertiesFilename = Vault.getHomeDir() + this.getServletConfig().getInitParameter("rrd-properties");

            this.properties = new Properties();
            this.properties.load( new FileInputStream( propertiesFilename ));
        }
        catch( FileNotFoundException e ) {
            throw new ServletException( "Could not find configuration file", e );
        }
        catch( IOException e ) {
            throw new ServletException( "Could not load configuration file", e );
        }

        this.workDir = new File( this.properties.getProperty( "command.input.dir" ));
        this.commandPrefix = this.properties.getProperty( "command.prefix" );
        this.mimeType = this.properties.getProperty( "output.mime" );
    }


    /**
     * Checks the parameters passed to this servlet, and if all are okay, executes
     * the RRDTool command in another process and pipes its PNG output to the
     * <code>ServletOutputStream</code> back to the requesting web browser.
     */
    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        String rrdDir = request.getParameter( "rrddir" );
        String start  = request.getParameter( "start" );
        String end    = request.getParameter( "end" );

        if( rrdDir == null || start == null || end == null ) {
            response.setContentType( "image/png" );
            Util.streamToStream( this.getServletContext().getResourceAsStream( "/images/rrd/missingparams.png"), response.getOutputStream() );
            return;
        }
        
        if( !GraphUtil.isValidRRDName(rrdDir) ) {
            this.log("Illegal RRD directory: " + rrdDir);
            throw new IllegalArgumentException("Illegal RRD directory: " + rrdDir);
        }
    
        String command = createAdHocCommand( request, rrdDir, start, end );

        if(command == null) {
            response.setContentType( "image/png" );
            Util.streamToStream( this.getServletContext().getResourceAsStream( "/images/rrd/missingparams.png"), response.getOutputStream() );
            return;
        }

        this.log( command );
        String[] commandArray = Util.createCommandArray( command, '@' );
        Process process = Runtime.getRuntime().exec( commandArray, null, workDir );

        ServletOutputStream out = response.getOutputStream();
        ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
        BufferedInputStream in = new BufferedInputStream( process.getInputStream() );

        Util.streamToStream( in, tempOut );

        in.close();
        tempOut.close();

        BufferedReader err = new BufferedReader( new InputStreamReader( process.getErrorStream() ));
        String line = err.readLine();
        StringBuffer buffer = new StringBuffer();

        while( line != null ) {
            buffer.append( line );
            line = err.readLine();
        }

        if( buffer.length() > 0 ) {
            this.log( "Read from stderr: " + buffer.toString() );
            response.setContentType( "image/png" );
            Util.streamToStream( this.getServletContext().getResourceAsStream( "/images/rrd/error.png"), out );
        }
        else {
            byte[] byteArray = tempOut.toByteArray();
            ByteArrayInputStream tempIn = new ByteArrayInputStream( byteArray );
            response.setContentType( this.mimeType );

            Util.streamToStream( tempIn, out );
        }

        out.close();
    }


    protected String createAdHocCommand( HttpServletRequest request, String rrdDir, String start, String end ) {
        String title = this.properties.getProperty( "adhoc.command.title" );
        String ds = this.properties.getProperty( "adhoc.command.ds" );
        String graphline = this.properties.getProperty( "adhoc.command.graphline" );

        //remember rrdtool wants the time in seconds, not milliseconds;
        //java.util.Date.getTime() returns milliseconds, so divide by 1000
        String starttime = Long.toString( Long.parseLong(start)/1000 );
        String endtime   = Long.toString( Long.parseLong(end)/1000 );

        String graphtitle = request.getParameter( "title" );

        if( graphtitle == null ) {
            return null;
        }

        StringBuffer buf = new StringBuffer();
        buf.append( this.commandPrefix );
        buf.append( " " );
        buf.append( title );

        String dsNames[] = request.getParameterValues( "ds" );
        String dsAggregFxns[] = request.getParameterValues( "agfunction" );
        String colors[] = request.getParameterValues( "color" );
        String dsTitles[] = request.getParameterValues( "dstitle" );
        String dsStyles[] = request.getParameterValues( "style" );

        if( dsNames == null || dsAggregFxns == null || colors == null || dsTitles == null || dsStyles == null ) {
            return null;
        }

        for( int i=0; i < dsNames.length; i++ ){
            String dsAbbrev = "ds" + Integer.toString( i );
 
            String dsName = dsNames[i];
            String rrd = this.workDir + File.separator + rrdDir + File.separator + dsNames[i] + GraphUtil.RRD_SUFFIX;              
            String dsAggregFxn = dsAggregFxns[i];
            String color = colors[i];
            String dsTitle = dsTitles[i];
            String dsStyle = dsStyles[i];

            buf.append( " " );
            buf.append( MessageFormat.format( ds, new String[] {rrd, starttime, endtime, graphtitle, dsAbbrev, dsName, dsAggregFxn, dsStyle, color, dsTitle} ));
        }

        for( int i=0; i < dsNames.length; i++ ){
            String dsAbbrev = "ds" + Integer.toString( i );

            String dsName = dsNames[i];
            String rrd = rrdDir + File.separator + dsNames[i] + GraphUtil.RRD_SUFFIX;              
            String dsAggregFxn = dsAggregFxns[i];
            String color = colors[i];
            String dsTitle = dsTitles[i];
            String dsStyle = dsStyles[i];

            buf.append( " " );
            buf.append( MessageFormat.format( graphline, new String[] {rrd, starttime, endtime, graphtitle, dsAbbrev, dsName, dsAggregFxn, dsStyle, color, dsTitle} ));
        }

        return MessageFormat.format( buf.toString(), new String[] {"bogus-rrd", starttime, endtime, graphtitle } );
    }
}
