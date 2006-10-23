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
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Category;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.opennms.core.resource.Vault;
import org.opennms.core.utils.StreamUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.utils.RrdFileConstants;
import org.opennms.web.MissingParameterException;
import org.opennms.web.performance.GraphAttribute;
import org.opennms.web.performance.GraphResource;
import org.opennms.web.performance.GraphResourceType;
import org.opennms.web.performance.PerformanceModel;
import org.opennms.web.response.ResponseTimeModel;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

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

    private String s_missingParamsPath = "/images/rrd/missingparams.png";
    private String s_rrdError = "/images/rrd/error.png";
    
    private GraphDao m_prefabGraphDao;

    private PerformanceModel m_performanceModel;

    private ResponseTimeModel m_responseTimeModel;

    /**
     * Initializes this servlet by reading the rrdtool-graph properties file.
     */
    public void init() throws ServletException {
        WebApplicationContext m_webAppContext =
            WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        setPrefabGraphDao((GraphDao) m_webAppContext.getBean("prefabGraphDao", GraphDao.class));
        m_performanceModel = (PerformanceModel) m_webAppContext.getBean("performanceModel", PerformanceModel.class);
        m_responseTimeModel = (ResponseTimeModel) m_webAppContext.getBean("responseTimeModel", ResponseTimeModel.class);

        try {
            RrdUtils.graphicsInitialize();
        } catch (RrdException e) {
            log().warn("Could not inititalize the graphing system", e);
            throw new ServletException("Could not initialize graphing system: " + e.getMessage(), e);
        }
    }

    /**
     * Checks the parameters passed to this servlet, and if all are okay,
     * executes the RRDTool command in another process and pipes its PNG output
     * to the <code>ServletOutputStream</code> back to the requesting web
     * browser.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
        String typeName = request.getParameter("type");
        String resourceTypeName = request.getParameter("resourceType");
        String adhoc = request.getParameter("adhoc");
        
        String[] requiredParameters = {
                "type",
                "resourceType"
        };

        if (typeName == null) {
            throw new MissingParameterException("type", requiredParameters);
//          returnErrorImage(response, s_missingParamsPath);
//            return;
        }
        if (resourceTypeName == null) {
            throw new MissingParameterException("resourceType", requiredParameters);
//          returnErrorImage(response, s_missingParamsPath);
//            return;
        }

        PrefabGraphType type = m_prefabGraphDao.findByName(typeName);
        if (type == null) {
            throw new IllegalArgumentException("graph type \"" + typeName + "\" is not valid");
        }
        
        GraphModel model = findGraphModelByName(type.getName());

        GraphResourceType resourceType =
            model.getResourceTypeByName(resourceTypeName);

	String command;
	if ("true".equals(adhoc)) {
            String[] adhocRequiredParameters = {
                    "node or domain",
                    "resource"
            };
            String nodeIdString = request.getParameter("node");
            String domain = request.getParameter("domain");
            if (nodeIdString == null && domain == null) {
                throw new MissingParameterException("node or domain",
                                                    adhocRequiredParameters);
            }
            
            int nodeId = -1;
            if (nodeIdString != null) {
                nodeId = Integer.parseInt(nodeIdString);
            }

            String resourceName = request.getParameter("resource");
            if (resourceName == null) {
                throw new MissingParameterException("resource",
                                                    adhocRequiredParameters);
            }
            
            AdhocGraphType adhocType = m_prefabGraphDao.findAdhocByName(typeName);
            
            GraphResource resource;
            String resourceParent;
            if (nodeId != -1) {
                resource = model.getResourceForNodeResourceResourceType(nodeId, resourceName, resourceTypeName);
                resourceParent = Integer.toString(nodeId);
            } else {
                resource = model.getResourceForDomainResourceResourceType(domain, resourceName, resourceTypeName);
                resourceParent = domain;
            }
	    command = getCommandAdhoc(adhocType, resourceParent,
                                      resourceType, resource,
                                      request, response);
	} else {
	    command = getCommandNonAdhoc(type, resourceType,
                                         request, response);
	}

	if (command == null) {
	    returnErrorImage(response, s_missingParamsPath);
            return;
        }

        File workDir = resourceType.getRrdDirectory();

        InputStream tempIn = null;
        try {
            log("Executing RRD command in this directory: " + workDir);
            log(command);

            tempIn = RrdUtils.createGraph(command, workDir);
        } catch (RrdException e) {
            log("Read from stderr: " + e.getMessage());
	    returnErrorImage(response, s_rrdError);
	    return;
	}

        if (tempIn != null) {
            response.setContentType(type.getOutputMimeType());

            StreamUtils.streamToStream(tempIn, response.getOutputStream());

            tempIn.close();
        }
    }

    public String getCommandNonAdhoc(PrefabGraphType type,
                                     GraphResourceType resourceType,
                                     HttpServletRequest request,
                                     HttpServletResponse response)
    throws ServletException {
        String report = request.getParameter("report");
        String[] rrds = request.getParameterValues("rrd");
        String propertiesFile = request.getParameter("props");
        String start = request.getParameter("start");
        String end = request.getParameter("end");

//        if (report == null || rrds == null || start == null || end == null
//                || resourceTypeName == null) {
//            return null;
//        }
        
        String[] requiredParameters = {
                "report",
                "rrd",
                "props",
                "start",
                "end",
        };
        
        if (report == null) {
            throw new MissingParameterException("report",
                                                requiredParameters);
        }
        if (rrds == null) {
            throw new MissingParameterException("rrd",
                                                requiredParameters);
        }
        if (start == null) {
            throw new MissingParameterException("start",
                                                requiredParameters);
        }
        if (end == null) {
            throw new MissingParameterException("end",
                                                requiredParameters);
        }
        
        for (int i = 0; i < rrds.length; i++) {
            if (!RrdFileConstants.isValidRRDName(rrds[i])) {
                log("Illegal RRD filename: " + rrds[i]);
                throw new IllegalArgumentException("Illegal RRD filename: "
                        + rrds[i]);
            }
        }
        
        return createPrefabCommand(request,
                                   resourceType,
                                   type.getReportMap(),
                                   type.getCommandPrefix(),
                                   type.getRrdDirectory(), report, rrds,
                                   propertiesFile,
                                   start, end);
    }
    
    private GraphModel findGraphModelByName(String name) {
        if (name.equals("performance")) {
            return m_performanceModel;
        } else if (name.equals("response")) {
            return m_responseTimeModel;
        } else {
            throw new IllegalArgumentException("graph model \"" + name
                                               + "\" is not supported");
        }
    }
    
    protected String createPrefabCommand(HttpServletRequest request,
            GraphResourceType resourceType,
            Map<String, PrefabGraph> reportMap,
            String commandPrefix,
            File workDir, String reportName,
            String[] rrds, String propertiesFile,
            String start, String end)
    throws ServletException {
        PrefabGraph graph = resourceType.getPrefabGraph(reportName);

        if (graph == null) {
            throw new IllegalArgumentException("Unknown report name: "
                    + reportName);
        }
        
        StringBuffer buf = new StringBuffer();
        buf.append(commandPrefix);
        buf.append(" ");
        buf.append(graph.getCommand());
        String command = buf.toString();
        
        long startTime = Long.parseLong(start);
        long endTime = Long.parseLong(end);
        long diffTime = endTime - startTime;
        
        String startTimeString = Long.toString(startTime / 1000);
        String endTimeString = Long.toString(endTime / 1000);
        String diffTimeString = Long.toString(diffTime / 1000);
        
        /*
         // remember rrdtool wants the time in seconds, not milliseconds;
          // java.util.Date.getTime() returns milliseconds, so divide by 1000
           String starttime = Long.toString(Long.parseLong(start) / 1000);
           String endtime = Long.toString(Long.parseLong(end) / 1000);
           */
        
        HashMap<String, String> translationMap = new HashMap<String, String>();
        
        for (int i = 0; i < rrds.length; i++) {
            String key = "{rrd" + (i + 1) + "}";
            translationMap.put(RE.simplePatternToFullRegularExpression(key),
                    rrds[i]);
        }
        
        translationMap.put(RE.simplePatternToFullRegularExpression("{startTime}"), startTimeString);
        translationMap.put(RE.simplePatternToFullRegularExpression("{endTime}"), endTimeString);
        translationMap.put(RE.simplePatternToFullRegularExpression("{diffTime}"), diffTimeString);
        
        Properties externalProperties = loadExternalProperties(workDir, propertiesFile);
        
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
            Iterator<String> iter = translationMap.keySet().iterator();
            
            while (iter.hasNext()) {
                String s1 = iter.next();
                String s2 = translationMap.get(s1);
                
                // replace s1 with s2
                RE re = new RE(s1);
                command = re.subst(command, s2);
            }
        } catch (RESyntaxException e) {
            throw new ServletException("Invalid regular expression syntax, check rrd-properties file", e);
        }
        
        return command;
    }

    public Properties loadExternalProperties(File workDir, String propertiesFile) {
    	Properties externalProperties = new Properties();
    	
    	if (propertiesFile == null) {
    		return externalProperties;
    	}
    	
    	File file = new File(workDir, propertiesFile);
    	if (!file.exists()) {
    		log("loadExternalProperties: Properties file does not exist: " + file.getAbsolutePath());
    		return externalProperties;
    	}
    	
    	FileInputStream fileInputStream = null;
    	try {
    		fileInputStream = new FileInputStream(file);
    	} catch (Exception e) {
    		log("createPrefabGraph: Error opening properties file: "+propertiesFile, e);
    		return externalProperties;
    	}

   		try {
			externalProperties.load(fileInputStream);
		} catch (IOException e) {
    		log("createPrefabGraph: Error loading properties file: "+propertiesFile, e);
		} finally {
            try {
                if (fileInputStream != null) {
                	fileInputStream.close();
                }
            } catch (Exception e) {
                this.log("createPrefabGraph: Error closing properties file: "+propertiesFile, e);
            }      
        }
    		
    	return externalProperties;
    }
    


    public String getCommandAdhoc(AdhocGraphType adhocType,
                                  String resourceParent,
                                  GraphResourceType resourceType,
			          GraphResource resource,
                                  HttpServletRequest request,
			          HttpServletResponse response)
		throws ServletException {
        String start = request.getParameter("start");
        String end = request.getParameter("end");

        if (start == null || end == null) {
            return null;
        }
        
        return createAdHocCommand(request, adhocType,
                                  resourceParent,
                                  resourceType,
                                  resource,
                                  start, end);
    }

    protected String createAdHocCommand(HttpServletRequest request,
					AdhocGraphType adhocType,
                                        String resourceParent,
                                        GraphResourceType resourceType,
					GraphResource resource,
                                        String start, String end) {
        String commandPrefix = adhocType.getCommandPrefix();
        String title = adhocType.getTitleTemplate();
        String ds = adhocType.getDataSourceTemplate();
        String graphline = adhocType.getGraphLineTemplate();

        /*
         * Remember that rrdtool wants the time in seconds, not milliseconds;
         * java.util.Date.getTime() returns milliseconds, so divide by 1000
         */
        String starttime = Long.toString(Long.parseLong(start) / 1000);
        String endtime = Long.toString(Long.parseLong(end) / 1000);

        String graphtitle = request.getParameter("title");

        if (graphtitle == null) {
            return null;
        }

        StringBuffer buf = new StringBuffer();
        buf.append(commandPrefix);
        buf.append(" ");
        buf.append(title);

        String dsNames[] = request.getParameterValues("ds");
        String dsAggregFxns[] = request.getParameterValues("agfunction");
        String colors[] = request.getParameterValues("color");
        String dsTitles[] = request.getParameterValues("dstitle");
        String dsStyles[] = request.getParameterValues("style");

        if (dsNames == null || dsAggregFxns == null || colors == null ||
	    dsTitles == null || dsStyles == null) {
            return null;
        }
        
        Set<String> attributeNames = new HashSet<String>();
        for (GraphAttribute attribute : resource.getAttributes()) {
            attributeNames.add(attribute.getName());
        }
        
        for (String dsName : dsNames) {
            if (!attributeNames.contains(dsName)) {
                throw new IllegalArgumentException("dsName \"" + dsName
                                                   + "\" is not available "
                                                   + "on this resource.  "
                                                   + "Available: "
                                                   + StringUtils.collectionToDelimitedString(attributeNames, ", "));
            }
        }

        for (int i = 0; i < dsNames.length; i++) {
            String dsAbbrev = "ds" + Integer.toString(i);

            String dsName = dsNames[i];
            String rrd = resourceType.getRelativePathForAttribute(resourceParent, resource.getName(), dsName);
            String dsAggregFxn = dsAggregFxns[i];
            String color = colors[i];
            String dsTitle = dsTitles[i];
            String dsStyle = dsStyles[i];

            buf.append(" ");
            buf.append(MessageFormat.format(ds, rrd, starttime,
                                            endtime, graphtitle,
                                            dsAbbrev, dsName,
                                            dsAggregFxn, dsStyle,
                                            color, dsTitle));
        }

        for (int i = 0; i < dsNames.length; i++) {
            String dsAbbrev = "ds" + Integer.toString(i);

            String dsName = dsNames[i];
            String rrd = resourceType.getRelativePathForAttribute(resourceParent, resource.getName(), dsName);
            String dsAggregFxn = dsAggregFxns[i];
            String color = colors[i];
            String dsTitle = dsTitles[i];
            String dsStyle = dsStyles[i];

            buf.append(" ");
            buf.append(MessageFormat.format(graphline, rrd,
                                            starttime, endtime, graphtitle,
                                            dsAbbrev, dsName, dsAggregFxn,
                                            dsStyle, color, dsTitle));
        }

        log().debug("formatting: " + buf + ", bogus-rrd, " + starttime + ", "
                    + endtime + ", " + graphtitle);
        return MessageFormat.format(buf.toString(), "bogus-rrd",
                                    starttime, endtime, graphtitle);
    }

    public boolean isTypeAdHoc(String type) {
	return type.endsWith("-adhoc");
    }

    public void returnErrorImage(HttpServletResponse response, String file)
	    throws IOException {
	response.setContentType("image/png");
	InputStream is =
	    getServletContext().getResourceAsStream(file);
	StreamUtils.streamToStream(is, response.getOutputStream());
    }

    public GraphDao getPrefabGraphDao() {
        return m_prefabGraphDao;
    }

    public void setPrefabGraphDao(GraphDao prefabGraphDao) {
        m_prefabGraphDao = prefabGraphDao;
    }
    
    private Category log() {
        return ThreadCategory.getInstance();
    }

}
