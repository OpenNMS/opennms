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
//      http://www.opennms.com/
//

package org.opennms.web.graph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.opennms.core.resource.Vault;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.utils.RrdFileConstants;
import org.opennms.web.MissingParameterException;
import org.opennms.web.Util;

/**
 * A servlet that creates a graph of network performance data using the <a
 * href="http://www.rrdtool.org/">RRDTool </a>.
 * 
 * <p>
 * This servlet executes an <em>rrdtool graph</em> command in another process,
 * piping its PNG file to standard out. The servlet then reads that PNG file and
 * returns it on the <code>ServletOutputStream</code>.
 * </p>
 * 
 * <p>
 * This servlet requires the following parameters:
 * <ul>
 * <li><em>report</em> The name of the key in the rrdtool-graph properties
 * file that contains information (including the command line options) to
 * execute specific graph query.
 * <li><em>rrd</em> The name of the ".rrd" file to graph. The file must exist
 * in the input directory specified in the rrdtool-graph properties file.
 * <li><em>start</em> The start time.
 * <li><em>end</em> The end time.
 * </ul>
 * </p>
 * 
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class RRDGraphServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = 8890231247851529359L;

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
     * file. It maps report names to {@link PrefabGraph PrefabGraph}instances.
     */
    protected Map reportMap;

    /**
     * Initializes this servlet by reading the rrdtool-graph properties file.
     */
    public void init() throws ServletException {
        Properties properties = new Properties();

        FileInputStream fileInputStream = null;
        try {
            String propertiesFilename = Vault.getHomeDir() + this.getServletConfig().getInitParameter("rrd-properties");
            fileInputStream = new FileInputStream(propertiesFilename);
            properties.load(fileInputStream);

            RrdUtils.graphicsInitialize();

        } catch (FileNotFoundException e) {
            log("Could not find configuration file", e);
            throw new ServletException("Could not find configuration file", e);
        } catch (IOException e) {
            log("Could not load configuration file", e);
            throw new ServletException("Could not load configuration file: ", e);
        } catch (RrdException e) {
            log("Could not inititalize the graphing system", e);
            throw new ServletException("Could not initialize graphing system: " + e.getMessage(), e);
        } catch (Throwable e) {
            log("Unexpected exception or error occurred", e);
            throw new ServletException("Unexpected exception or error occured: " + e.getMessage(), e);
        } finally {
            try {
                if (fileInputStream != null) fileInputStream.close();
            } catch (IOException e) {
                this.log("init: Error closing properties file.",e);
            }
        }

        this.workDir = new File(properties.getProperty("command.input.dir"));
        this.commandPrefix = properties.getProperty("command.prefix");
        this.mimeType = properties.getProperty("output.mime");
        this.reportMap = PrefabGraph.getPrefabGraphDefinitions(properties);

    }

    /**
     * Checks the parameters passed to this servlet, and if all are okay,
     * executes the RRDTool command in another process and pipes its PNG output
     * to the <code>ServletOutputStream</code> back to the requesting web
     * browser.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String report = request.getParameter("report");
            String[] rrds = request.getParameterValues("rrd");
	    String propertiesFile = request.getParameter("props");
            String start = request.getParameter("start");
            String end = request.getParameter("end");

            if (report == null || rrds == null || start == null || end == null) {
                response.setContentType("image/png");
                Util.streamToStream(this.getServletContext().getResourceAsStream("/images/rrd/missingparams.png"), response.getOutputStream());
                return;
            }

            for (int i = 0; i < rrds.length; i++) {
                if (!RrdFileConstants.isValidRRDName(rrds[i])) {
                    this.log("Illegal RRD filename: " + rrds[i]);
                    throw new IllegalArgumentException("Illegal RRD filename: " + rrds[i]);
                }
            }

            String command = this.createPrefabCommand(request, report, rrds, propertiesFile, start, end);

            InputStream tempIn = null;
            ServletOutputStream out = response.getOutputStream();
            try {

                this.log("Executing RRD command in this directory: " + workDir);
                this.log(command);

                File workDir = this.workDir;

                tempIn = RrdUtils.createGraph(command, workDir);

            } catch (RrdException e) {
                this.log("Read from stderr: " + e.getMessage());
                response.setContentType("image/png");
                Util.streamToStream(this.getServletContext().getResourceAsStream("/images/rrd/error.png"), out);
            }

            if (tempIn != null) {
                response.setContentType(this.mimeType);
                Util.streamToStream(tempIn, out);

                tempIn.close();
            }
            out.close();
        } catch (Exception e) {
            this.log("Exception occurred: " + e.getMessage(), e);
        }
    }

    protected String createPrefabCommand(HttpServletRequest request, String reportName, String[] rrds, String propertiesFile, String start, String end) throws ServletException {
        PrefabGraph graph = (PrefabGraph) this.reportMap.get(reportName);

        if (graph == null) {
            throw new IllegalArgumentException("Unknown report name: " + reportName);
        }

        StringBuffer buf = new StringBuffer();
        buf.append(this.commandPrefix);
        buf.append(" ");
        buf.append(graph.getCommand());
        String command = buf.toString();

        // remember rrdtool wants the time in seconds, not milliseconds;
        // java.util.Date.getTime() returns milliseconds, so divide by 1000

        long startTime = Long.parseLong(start);
        long endTime = Long.parseLong(end);
        long diffTime = endTime - startTime;
  
        String startTimeString = Long.toString(startTime / 1000);
        String endTimeString = Long.toString(endTime / 1000);
        String diffTimeString = Long.toString(diffTime / 1000);
  
        HashMap translationMap = new HashMap();

        for (int i = 0; i < rrds.length; i++) {
            String key = "{rrd" + (i + 1) + "}";
            translationMap.put(RE.simplePatternToFullRegularExpression(key), rrds[i]);
        }

        translationMap.put(RE.simplePatternToFullRegularExpression("{startTime}"), startTimeString);
        translationMap.put(RE.simplePatternToFullRegularExpression("{endTime}"), endTimeString);
        translationMap.put(RE.simplePatternToFullRegularExpression("{diffTime}"), diffTimeString);

        Properties externalProperties = new Properties();
        if (propertiesFile != null) {
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(this.workDir + File.separator + propertiesFile);
                externalProperties.load(fileInputStream);
            } catch (Exception e1) {
                this.log("createPrefabGraph: Error loading properties file: "+propertiesFile, e1);
            } finally {
                try {
                    if (fileInputStream != null) fileInputStream.close();
                } catch (IOException e) {
                    this.log("createPrefabGraph: Error closing properties file: "+propertiesFile, e);
                }
            }
        }


        // names of values specified outside of the RRD data (external values)
        String[] externalValues = graph.getExternalValues();

        if (externalValues != null || externalValues.length > 0) {
            for (int i = 0; i < externalValues.length; i++) {
                String value = request.getParameter(externalValues[i]);

                if (value == null) {
                    throw new MissingParameterException(externalValues[i]);
                } else {
                    translationMap.put(RE.simplePatternToFullRegularExpression("{" + externalValues[i] + "}"), value);
                }
            }
        }

	//names of values specified that come from properties files
	String[] propertiesValues = graph.getPropertiesValues();
	if (propertiesValues != null || propertiesValues.length > 0) {
		for (int i = 0; i < propertiesValues.length; i++) {
			String value = (externalProperties.getProperty(propertiesValues[i]) == null ? "Unknown" : externalProperties.getProperty(propertiesValues[i]));
			if (value == null) {
				throw new MissingParameterException(propertiesValues[i]);
			} else {
				translationMap.put(
					RE.simplePatternToFullRegularExpression(
						"{" + propertiesValues[i] + "}"),
					value);
			}
		}
	}


        try {
            Iterator iter = translationMap.keySet().iterator();

            while (iter.hasNext()) {
                String s1 = (String) iter.next();
                String s2 = (String) translationMap.get(s1);

                // replace s1 with s2
                RE re = new RE(s1);
                command = re.subst(command, s2);
            }
        } catch (RESyntaxException e) {
            throw new ServletException("Invalid regular expression syntax, check rrd-properties file", e);
        }

        return command;
    }

}
