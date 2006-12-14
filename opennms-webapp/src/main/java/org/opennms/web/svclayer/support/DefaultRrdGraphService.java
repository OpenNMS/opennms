package org.opennms.web.svclayer.support;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Category;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.utils.IfLabel;
import org.opennms.web.graph.AdhocGraphType;
import org.opennms.web.graph.Graph;
import org.opennms.web.graph.GraphDao;
import org.opennms.web.graph.PrefabGraph;
import org.opennms.web.graph.PrefabGraphType;
import org.opennms.web.performance.GraphAttribute;
import org.opennms.web.performance.GraphResource;
import org.opennms.web.performance.GraphResourceType;
import org.opennms.web.performance.PerformanceModel;
import org.opennms.web.svclayer.RrdGraphService;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.util.StringUtils;

public class DefaultRrdGraphService implements RrdGraphService {
    private static final String s_missingParamsPath = "/images/rrd/missingparams.png";
    private static final String s_rrdError = "/images/rrd/error.png";
    
    private GraphDao m_prefabGraphDao;

    private PerformanceModel m_performanceModel;

    private RrdStrategy m_rrdStrategy;

    public InputStream getAdhocGraph(String parentResourceType, String parentResource,
            String resourceType, String resource, String title,
            String[] dataSources, String[] aggregateFunctions,
            String[] colors, String[] dataSourceTitles, String[] styles,
            long start, long end) {
        assertPropertiesSet();
        
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
        
        AdhocGraphType t = m_prefabGraphDao.findAdhocByName("performance");

        GraphResourceType rt =
            m_performanceModel.getResourceTypeByName(resourceType);

        GraphResource r = findResource(parentResourceType, parentResource,
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
        
        return getInputStreamForCommand(command);
    }

    private InputStream getInputStreamForCommand(String command) {
        boolean debug = true;
        File workDir = m_performanceModel.getRrdDirectory(true);

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

    public InputStream getPrefabGraph(
            String parentResourceType, String parentResource,
            String resourceType, String resource, String report, long start,
            long end) {
        assertPropertiesSet();

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

        PrefabGraphType t = m_prefabGraphDao.findByName("performance");
        if (t == null) {
            throw new IllegalArgumentException("graph type \"" + "performance"
                                               + "\" is not valid");
        }
        
        GraphResourceType rt =
            m_performanceModel.getResourceTypeByName(resourceType);
        
        GraphResource r = findResource(parentResourceType, parentResource,
                                       resourceType, resource);
        
        PrefabGraph prefabGraph = m_performanceModel.getQuery(report);
        if (prefabGraph == null) {
            throw new ObjectRetrievalFailureException(PrefabGraph.class, report,
                                                      "Unknown report name '" + report + "'",
                                                      null);
        }
        
        if (!"node".equals(parentResourceType) && !"domain".equals(parentResourceType)) {
            throw new IllegalArgumentException("parentResourceType '"
                                               + parentResourceType
                                               + "' is not supported");
            
        }
        
        // XXX should we verify that the parentResource is an integer if parentResourceType == "node"??
        Graph graph = new Graph(prefabGraph, parentResourceType, parentResource, resourceType, resource, new Date(start), new Date(end));

        String attributePath = rt.getRelativePathForAttribute(parentResource, resource, "bogusAttribute");
        int lastSeparator = attributePath.lastIndexOf(File.separatorChar);
        String relativePropertiesPath = attributePath.substring(0, lastSeparator)
            + File.separator + "strings.properties";
        String command = getCommandNonAdhoc(t, rt, report, graph, 
                                            relativePropertiesPath);
        
        return getInputStreamForCommand(command);
    }
    
    private void assertPropertiesSet() {
        if (m_performanceModel == null) {
            throw new IllegalStateException("performanceModel property has not been set");
        }
        if (m_prefabGraphDao == null) {
            throw new IllegalStateException("prefabGraphDao property has not been set");
        }
        if (m_rrdStrategy == null) {
            throw new IllegalStateException("rrdStrategy property has not been set");
        }
    }
    
    private GraphResource findResource(
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
            return m_performanceModel.getResourceForNodeResourceResourceType(nodeId, resource, resourceType);
        } else if ("domain".equals(parentResourceType)){
            return m_performanceModel.getResourceForDomainResourceResourceType(parentResource, resource, resourceType);
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
                                     Graph graph,
                                     String relativePropertiesPath) {
        return createPrefabCommand(graph,
                                   type.getCommandPrefix(),
                                   m_performanceModel.getRrdDirectory(true),
                                   report,
                                   relativePropertiesPath);
    }
    

    private String[] getRRDNames(Graph graph) {
        String[] columns = graph.getPrefabGraph().getColumns();
        String[] rrds = new String[columns.length];

        for (int i=0; i < columns.length; i++) {
            rrds[i] = m_performanceModel.getRelativePathForAttribute(graph.getResourceType(),
                                                          graph.getParentResource(),
                                                          graph.getResource(),
                                                          columns[i]);
        }

        return rrds;
    }

    protected String createPrefabCommand(Graph graph,
            String commandPrefix,
            File workDir, String reportName,
            String relativePropertiesPath) {
        PrefabGraph prefabGraph = graph.getPrefabGraph();

        String[] rrds = getRRDNames(graph);
        
        
        StringBuffer buf = new StringBuffer();
        buf.append(commandPrefix);
        buf.append(" ");
        buf.append(prefabGraph.getCommand());
        String command = buf.toString();
        
        long startTime = graph.getStart().getTime();
        long endTime = graph.getEnd().getTime();
        long diffTime = endTime - startTime;
        
        /*
         * remember rrdtool wants the time in seconds, not milliseconds;
         * java.util.Date.getTime() returns milliseconds, so divide by 1000
         */
        String startTimeString = Long.toString(startTime / 1000);
        String endTimeString = Long.toString(endTime / 1000);
        String diffTimeString = Long.toString(diffTime / 1000);
        
        HashMap<String, String> translationMap = new HashMap<String, String>();
        
        for (int i = 0; i < rrds.length; i++) {
            String key = "{rrd" + (i + 1) + "}";
            translationMap.put(RE.simplePatternToFullRegularExpression(key),
                    rrds[i]);
        }
        
        translationMap.put(RE.simplePatternToFullRegularExpression("{startTime}"), startTimeString);
        translationMap.put(RE.simplePatternToFullRegularExpression("{endTime}"), endTimeString);
        translationMap.put(RE.simplePatternToFullRegularExpression("{diffTime}"), diffTimeString);

        for (String externalValue : prefabGraph.getExternalValues()) { 
            if ("ifSpeed".equals(externalValue)) {
                if (!"node".equals(graph.getParentResourceType())) {
                    throw new IllegalStateException("Report requires an "
                                                    + "external value of "
                                                    + externalValue
                                                    + ", but this requires "
                                                    + "the parent resource "
                                                    + "to be a node, but "
                                                    + "parent of this "
                                                    + "resource is not a "
                                                    + "node");
                }
                
                // XXX error checking
                int nodeId = Integer.parseInt(graph.getParentResource());
                String speed = getIfSpeed(nodeId, graph.getResource());
                if (speed == null) {
                    throw new IllegalStateException("Report requires an "
                                                    + "external value of "
                                                    + externalValue
                                                    + ", but it is not "
                                                    + "available for this "
                                                    + "resource");
                }
                
                translationMap.put(RE.simplePatternToFullRegularExpression("{" + externalValue + "}"), speed);
            } else {
                throw new IllegalStateException("Unsupported external value name: " + externalValue);
            }                
        }
        
        String[] propertiesValues = prefabGraph.getPropertiesValues();
        if (propertiesValues != null && propertiesValues.length > 0) {
            Properties properties;
            try {
                properties = loadProperties(workDir,
                                            relativePropertiesPath);
            } catch (DataAccessException e) {
                String message = "Could not load properties file but prefab graph has propertiesValues, so the properties file is required.  Chained exception: " + e;
                log().warn(message, e);
                throw new IllegalArgumentException(message, e);
            }

            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                translationMap.put(RE.simplePatternToFullRegularExpression("{" + entry.getKey() + "}"),
                                   entry.getValue().toString());
            }
        }
        
        try {
            for (Map.Entry<String, String> translation : translationMap.entrySet()) {
                // replace s1 with s2
                RE re = new RE(translation.getKey());
                command = re.subst(command, translation.getValue());
            }
        } catch (RESyntaxException e) {
            throw new IllegalArgumentException("Invalid regular expression "
                                               + "syntax, check "
                                               + "rrd-properties file", e);
        }
        
        return command;
    }
    

    private String getIfSpeed(int nodeId, String resource) {
        String speed = null;
        
        Map intfInfo;
        try {
            intfInfo = IfLabel.getInterfaceInfoFromIfLabel(nodeId, resource);
        } catch (SQLException e) {
            SQLErrorCodeSQLExceptionTranslator translator =
                new SQLErrorCodeSQLExceptionTranslator();
            throw translator.translate("Getting interface info for resource '"
                                       + resource + "' on node " + nodeId,
                                       null, e);
        }

        // if the extended information was found correctly
        if (intfInfo != null) {
            speed = (String) intfInfo.get("snmpifspeed");
        }

        return speed;
    }
    
    public Properties loadProperties(File workDir, String propertiesFile) {
        if (workDir == null) {
            throw new IllegalArgumentException("argument workDir cannot e null");
        }
        
        if (propertiesFile == null) {
            throw new IllegalArgumentException("argument propertiesFile cannot e null");
        }
        
        Properties externalProperties = new Properties();
        
        File file = new File(workDir, propertiesFile);
        if (!file.exists()) {
            String message =
                "loadProperties: Properties file does not exist: "
                + file.getAbsolutePath();
            log().warn(message);
            //throw new DataAccessResourceFailureException(message);
        }
        
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
        } catch (IOException e) {
            String message = "loadProperties: Error opening properties file "
                + propertiesFile + ": " + e.getMessage();
            log().warn(message, e);
            throw new DataAccessResourceFailureException(message, e);
        }

        try {
            externalProperties.load(fileInputStream);
        } catch (IOException e) {
            String message = "loadProperties: Error loading properties file "
                + propertiesFile + ": " + e.getMessage();
            log().warn(message, e);
            throw new DataAccessResourceFailureException(message, e);
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                String message = 
                    "loadProperties: Error closing properties file "
                    + propertiesFile + ": " + e.getMessage();
                log().warn(message, e);
                throw new DataAccessResourceFailureException(message, e);
            }
        }
                
        return externalProperties;
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
