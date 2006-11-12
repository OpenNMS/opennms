package org.opennms.web.svclayer.support;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Category;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.web.graph.AdhocGraphType;
import org.opennms.web.graph.Graph;
import org.opennms.web.graph.GraphDao;
import org.opennms.web.graph.GraphModel;
import org.opennms.web.graph.PrefabGraph;
import org.opennms.web.graph.PrefabGraphType;
import org.opennms.web.performance.GraphAttribute;
import org.opennms.web.performance.GraphResource;
import org.opennms.web.performance.GraphResourceType;
import org.opennms.web.performance.PerformanceModel;
import org.opennms.web.response.ResponseTimeModel;
import org.opennms.web.svclayer.RrdGraphService;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.util.StringUtils;

public class DefaultRrdGraphService implements RrdGraphService {
    private static final String s_missingParamsPath = "/images/rrd/missingparams.png";
    private static final String s_rrdError = "/images/rrd/error.png";
    
    private GraphDao m_prefabGraphDao;

    private PerformanceModel m_performanceModel;

    private ResponseTimeModel m_responseTimeModel;
    private RrdStrategy m_rrdStrategy;

    public InputStream getAdhocGraph(String type,
            String parentResourceType, String parentResource,
            String resourceType, String resource, String title,
            String[] dataSources, String[] aggregateFunctions,
            String[] colors, String[] dataSourceTitles, String[] styles,
            long start, long end) {
        assertPropertiesSet();
        
        if (type == null) {
            throw new IllegalArgumentException("type argument cannot be null");
        }
        if (parentResourceType == null) {
            throw new IllegalArgumentException("parentResourceType argument cannot be null");
        }
        if (parentResource == null) {
            throw new IllegalArgumentException("parentResource argument cannot be null");
        }
        if (resourceType == null) {
            throw new IllegalArgumentException("resourceType argument cannot be null");
        }
        if (resource == null) {
            throw new IllegalArgumentException("resource argument cannot be null");
        }
        if (title == null) {
            throw new IllegalArgumentException("title argument cannot be null");
        }
        if (dataSources == null) {
            throw new IllegalArgumentException("dataSources argument cannot be null");
        }
        if (aggregateFunctions == null) {
            throw new IllegalArgumentException("aggregateFunctions argument cannot be null");
        }
        if (colors == null) {
            throw new IllegalArgumentException("colors argument cannot be null");
        }
        if (dataSourceTitles == null) {
            throw new IllegalArgumentException("dataSourceTitles argument cannot be null");
        }
        if (styles == null) {
            throw new IllegalArgumentException("styles argument cannot be null");
        }
        if (end < start) {
            throw new IllegalArgumentException("end time cannot be before start time");
        }
        
        AdhocGraphType t = m_prefabGraphDao.findAdhocByName(type);
        GraphModel model = findGraphModelByName(t.getName());

        GraphResourceType rt =
            model.getResourceTypeByName(resourceType);

        GraphResource r = findResource(model,
                                       parentResourceType, parentResource,
                                       resourceType, resource);
        
        String command = createAdHocCommand(t,
                                  parentResource,
                                  rt,
                                  r,
                                  start, end,
                                  title,
                                  dataSources,
                                  aggregateFunctions,
                                  colors,
                                  dataSourceTitles,
                                  styles);
        
        return getInputStreamForCommand(rt, command);
    }

    private InputStream getInputStreamForCommand(GraphResourceType resourceType,
            String command) {
        File workDir = resourceType.getRrdDirectory();
        
        boolean debug = true;

        InputStream tempIn = null;
        try {
            log().debug("Executing RRD command in directory '" + workDir
                        + "': " + command);

            tempIn = m_rrdStrategy.createGraph(command, workDir);
        } catch (RrdException e) {
            String message = "RrdException received while creating graph: "
                + e.getMessage(); 
            log().warn(message, e);
            if (debug) {
                throw new DataRetrievalFailureException(message, e);
            } else {
                return returnErrorImage(s_rrdError);
            }
        } catch (IOException e) {
            String message = "IOException received while creating graph: "
                + e.getMessage(); 
            log().warn(message, e);
            if (debug) {
                throw new DataRetrievalFailureException(message, e);
            } else {
                return returnErrorImage(s_rrdError);
            }
        }

        return tempIn;
    }
    
    public InputStream returnErrorImage(String file) {
        InputStream is =
            getClass().getResourceAsStream(file);
        if (is == null) {
            throw new ObjectRetrievalFailureException(InputStream.class, file, "Could not find error image for '" + file + "' or could open", null);
        }
        return is;
    }

    public InputStream getPrefabGraph(String type,
            String parentResourceType, String parentResource,
            String resourceType, String resource, String report, long start,
            long end) {
        assertPropertiesSet();

        if (type == null) {
            throw new IllegalArgumentException("type argument cannot be null");
        }
        if (parentResourceType == null) {
            throw new IllegalArgumentException("parentResourceType argument cannot be null");
        }
        if (parentResource == null) {
            throw new IllegalArgumentException("parentResource argument cannot be null");
        }
        if (resourceType == null) {
            throw new IllegalArgumentException("resourceType argument cannot be null");
        }
        if (resource == null) {
            throw new IllegalArgumentException("resource argument cannot be null");
        }
        if (report == null) {
            throw new IllegalArgumentException("report argument cannot be null");
        }
        if (end < start) {
            throw new IllegalArgumentException("end time cannot be before start time");
        }

        PrefabGraphType t = m_prefabGraphDao.findByName(type);
        if (t == null) {
            throw new IllegalArgumentException("graph type \"" + type
                                               + "\" is not valid");
        }
        
        GraphModel model = findGraphModelByName(t.getName());

        GraphResourceType rt =
            model.getResourceTypeByName(resourceType);
        
        GraphResource r = findResource(model,
                                       parentResourceType, parentResource,
                                       resourceType, resource);
        
        PrefabGraph prefabGraph = rt.getPrefabGraph(report);
        if (prefabGraph == null) {
            throw new ObjectRetrievalFailureException(PrefabGraph.class, report,
                                                      "Unknown report name '" + report + "'",
                                                      null);
        }
        
        Graph graph;
        if ("node".equals(parentResourceType)) {
            int nodeId;
            try {
                nodeId = Integer.parseInt(parentResource);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Could not parse parentResource '"
                                                   + parentResource + "' as an integer node ID: " + e.getMessage(), e);
            }
            graph = new Graph(model, prefabGraph, nodeId, resource, resourceType, new Date(start), new Date(end));
        } else if ("domain".equals(parentResourceType)){
            graph = new Graph(model, prefabGraph, parentResource, resource, resourceType, new Date(start), new Date(end));
        } else {
            throw new IllegalArgumentException("parentResourceType '"
                                               + parentResourceType
                                               + "' is not supported");
            
        }

        String command = getCommandNonAdhoc(t, rt, report, graph);
        
        return getInputStreamForCommand(rt, command);
    }
    
    private void assertPropertiesSet() {
        if (m_performanceModel == null) {
            throw new IllegalStateException("performanceModel property has not been set");
        }
        if (m_responseTimeModel == null) {
            throw new IllegalStateException("responseTimeModel property has not been set");
        }
        if (m_prefabGraphDao == null) {
            throw new IllegalStateException("prefabGraphDao property has not been set");
        }
        if (m_rrdStrategy == null) {
            throw new IllegalStateException("rrdStrategy property has not been set");
        }
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
    
    private GraphResource findResource(GraphModel model,
            String parentResourceType, String parentResource,
            String resourceType, String resource) {
        if ("node".equals(parentResourceType)) {
            int nodeId;
            try {
                nodeId = Integer.parseInt(parentResource);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Could not parse parentResource '"
                                                   + parentResource + "' as an integer node ID: " + e.getMessage(), e);
            }
            return model.getResourceForNodeResourceResourceType(nodeId, resource, resourceType);
        } else if ("domain".equals(parentResourceType)){
            return model.getResourceForDomainResourceResourceType(parentResource, resource, resourceType);
        } else {
            throw new IllegalArgumentException("parentResourceType '"
                                               + parentResourceType
                                               + "' is not supported");
            
        }
    }
    
    protected String createAdHocCommand(AdhocGraphType adhocType,
            String parentResource,
            GraphResourceType resourceType,
            GraphResource resource,
            long start, long end,
            String graphtitle,
            String[] dsNames,
            String[] dsAggregFxns,
            String[] colors,
            String[] dsTitles,
            String[] dsStyles) {
        String commandPrefix = adhocType.getCommandPrefix();
        String title = adhocType.getTitleTemplate();
        String ds = adhocType.getDataSourceTemplate();
        String graphline = adhocType.getGraphLineTemplate();

        /*
         * Remember that rrdtool wants the time in seconds, not milliseconds;
         * java.util.Date.getTime() returns milliseconds, so divide by 1000
         */
        String starttime = Long.toString(start / 1000);
        String endtime = Long.toString(end / 1000);

        StringBuffer buf = new StringBuffer();
        buf.append(commandPrefix);
        buf.append(" ");
        buf.append(title);

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
            String rrd = resourceType.getRelativePathForAttribute(parentResource, resource.getName(), dsName);
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
            String rrd = resourceType.getRelativePathForAttribute(parentResource, resource.getName(), dsName);
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


    public String getCommandNonAdhoc(PrefabGraphType type,
                                     GraphResourceType resourceType,
                                     String report,
                                     Graph graph) {
        /*
        String[] rrds = request.getParameterValues("rrd");
        String propertiesFile = request.getParameter("props");
        String start = request.getParameter("start");
        String end = request.getParameter("end");
        
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
        */
        
        /*
        for (int i = 0; i < rrds.length; i++) {
            if (!RrdFileConstants.isValidRRDName(rrds[i])) {
                log("Illegal RRD filename: " + rrds[i]);
                throw new IllegalArgumentException("Illegal RRD filename: "
                        + rrds[i]);
            }
        }
        */
        
        return createPrefabCommand(graph,
                                   type.getCommandPrefix(),
                                   type.getRrdDirectory(),
                                   report);
    }

    protected String createPrefabCommand(Graph graph,
            String commandPrefix,
            File workDir, String reportName) {
        PrefabGraph prefabGraph = graph.getPrefabGraph();

        String[] rrds = graph.getRRDNames();
        String propertiesFile = graph.getParentResource()
            + "/strings.properties";
        
        StringBuffer buf = new StringBuffer();
        buf.append(commandPrefix);
        buf.append(" ");
        buf.append(prefabGraph.getCommand());
        String command = buf.toString();
        
        long startTime = graph.getStart().getTime();
        long endTime = graph.getEnd().getTime();
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

        // XXX WTF does this do?
        /*
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
        */
        
        
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
            throw new IllegalArgumentException("Invalid regular expression "
                                               + "syntax, check "
                                               + "rrd-properties file", e);
        }
        
        return command;
    }


    public PerformanceModel getPerformanceModel() {
        return m_performanceModel;
    }

    public void setPerformanceModel(PerformanceModel performanceModel) {
        m_performanceModel = performanceModel;
    }

    public GraphDao getPrefabGraphDao() {
        return m_prefabGraphDao;
    }

    public void setPrefabGraphDao(GraphDao prefabGraphDao) {
        m_prefabGraphDao = prefabGraphDao;
    }

    public ResponseTimeModel getResponseTimeModel() {
        return m_responseTimeModel;
    }

    public void setResponseTimeModel(ResponseTimeModel responseTimeModel) {
        m_responseTimeModel = responseTimeModel;
    }

    private Category log() {
        return ThreadCategory.getInstance();
    }

    public RrdStrategy getRrdStrategy() {
        return m_rrdStrategy;
    }

    public void setRrdStrategy(RrdStrategy rrdStrategy) {
        m_rrdStrategy = rrdStrategy;
    }

}
