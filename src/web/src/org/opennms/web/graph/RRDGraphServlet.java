//
// Copyright (C) 2001 Oculan Corp.
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
//  Brian Weaver   <weave@opennms.org>
//  http://www.opennms.org/
//
//

package org.opennms.web.graph;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import org.opennms.core.resource.Vault;
import org.opennms.core.utils.BundleLists;
import org.opennms.web.Util;
import org.opennms.web.MissingParameterException;


/**
 * A servlet that creates a graph of network performance data
 * using the <a href="http://www.rrdtool.org/">RRDTool</a>.
 *
 * <p>This servlet executes an <em>rrdtool graph</em> command
 * in another process, piping its PNG file to standard out.  The
 * servlet then reads that PNG file and returns it on the
 * <code>ServletOutputStream</code>. </p>
 *
 * <p>This servlet requires the following parameters:
 * <ul>
 *   <li><em>report</em> The name of the key in the rrdtool-graph properties
 *       file that contains information (including the command line options)
 *       to execute specific graph query.
 *   <li><em>rrd</em> The name of the ".rrd" file to graph.  The file must
 *       exist in the input directory specified in the rrdtool-graph properties file.
 *   <li><em>start</em> The start time.
 *   <li><em>end</em> The end time.
 * </ul>
 * </p>
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class RRDGraphServlet extends HttpServlet
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
     * Holds the graph definitions specified in the rrdtool-graph properties 
     * file.  It maps report names to {@link PrefabGraph PrefabGraph} instances.
     */
    protected Map reportMap;


    /**
     * Initializes this servlet by reading the rrdtool-graph properties file.
     */
    public void init() throws ServletException {
        Properties properties = new Properties();
        
        try {
            String propertiesFilename = Vault.getHomeDir() + this.getServletConfig().getInitParameter("rrd-properties");
            properties.load( new FileInputStream( propertiesFilename ));
        }
        catch( FileNotFoundException e ) {
            throw new ServletException( "Could not find configuration file", e );
        }
        catch( IOException e ) {
            throw new ServletException( "Could not load configuration file", e );
        }

        this.workDir = new File( properties.getProperty( "command.input.dir" ));
        this.commandPrefix = properties.getProperty( "command.prefix" );
        this.mimeType = properties.getProperty( "output.mime" );        
        this.reportMap = PrefabGraph.getPrefabGraphDefinitions(properties);
    }


    /**
     * Checks the parameters passed to this servlet, and if all are okay, executes
     * the RRDTool command in another process and pipes its PNG output to the
     * <code>ServletOutputStream</code> back to the requesting web browser.
     */
    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
        String report = request.getParameter( "report" );
        String[] rrds = request.getParameterValues( "rrd" );
        String start  = request.getParameter( "start" );
        String end    = request.getParameter( "end" );

        if( report == null || rrds == null || start == null || end == null ) {
            response.setContentType( "image/png" );
            Util.streamToStream( this.getServletContext().getResourceAsStream( "/images/rrd/missingparams.png"), response.getOutputStream() );
            return;
        }
        
        for( int i=0; i < rrds.length; i++ ) {
            if( !GraphUtil.isValidRRDName(rrds[i]) ) {
                this.log("Illegal RRD filename: " + rrds[i]);
                throw new IllegalArgumentException("Illegal RRD filename: " + rrds[i]);
            }
        }
        
        String command = this.createPrefabCommand( request, report, rrds, start, end );

        this.log( "Executing RRD command in this directory: " + this.workDir );
        this.log( command );

        String[] commandArray = Util.createCommandArray( command, '@' );
        Process process = Runtime.getRuntime().exec( commandArray, null, this.workDir );

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


    protected String createPrefabCommand( HttpServletRequest request, String reportName, String[] rrds, String start, String end ) throws ServletException {
        PrefabGraph graph = (PrefabGraph)this.reportMap.get(reportName);
        
        if(graph == null) {
            throw new IllegalArgumentException("Unknown report name: " + reportName);
        }
        
        StringBuffer buf = new StringBuffer();
        buf.append( this.commandPrefix );
        buf.append( " " );
        buf.append( graph.getCommand() );
        String command = buf.toString();

        //remember rrdtool wants the time in seconds, not milliseconds;
        //java.util.Date.getTime() returns milliseconds, so divide by 1000
        String starttime = Long.toString( Long.parseLong(start)/1000 );
        String endtime   = Long.toString( Long.parseLong(end)/1000 );

        HashMap translationMap = new HashMap();
        
        for(int i=0; i < rrds.length; i++ ) {
            String key = "{rrd" + (i+1) + "}";
            translationMap.put(RE.simplePatternToFullRegularExpression(key), rrds[i] );
        }
        
        translationMap.put(RE.simplePatternToFullRegularExpression("{startTime}"), starttime );
        translationMap.put(RE.simplePatternToFullRegularExpression("{endTime}"), endtime );        
        
        //names of values specified outside of the RRD data (external values)
        String[] externalValues = graph.getExternalValues();

        if( externalValues != null || externalValues.length > 0 ) {
            for( int i = 0; i < externalValues.length; i++ ) {
                String value = request.getParameter( externalValues[i] );

                if( value == null ) {
                    throw new MissingParameterException( externalValues[i] );
                }
                else {
                    translationMap.put(RE.simplePatternToFullRegularExpression("{" + externalValues[i] + "}"), value);
                }                
            }
        }
        
        try {
            Iterator iter = translationMap.keySet().iterator();
    
            while(iter.hasNext()) {
                String s1 = (String)iter.next();
                String s2 = (String)translationMap.get(s1);
    
                //replace s1 with s2
                RE re = new RE(s1);
                command = re.subst(command, s2);             
            }    
        }
        catch( RESyntaxException e ) {
            throw new ServletException("Invalid regular expression syntax, check rrd-properties file", e);
        }
        
        return command;
    }

}

