/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * 2008 Oct 22: Use new ResourceDao method names. - dj@opennms.org
 * 2007 Apr 05: Move properties loading code into ResourceTypeUtils, move
 *              ifSpeed loading to InterfaceSnmpResourceType, reorganize
 *              to use OnmsAttributes to get this data, and use RrdDao
 *              instead of RrdStrategy create graph. - dj@opennms.org
 * 
 * Created: November 12, 2006
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.svclayer.support;


import java.io.File;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.GraphDao;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.dao.RrdDao;
import org.opennms.netmgt.model.AdhocGraphType;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.model.PrefabGraphType;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.web.graph.Graph;
import org.opennms.web.svclayer.RrdGraphService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:cmiskell@opennms.org">Craig Miskell</a>
 */
public class DefaultRrdGraphService implements RrdGraphService, InitializingBean {
//    private static final String s_missingParamsPath = "/images/rrd/missingparams.png";
    private static final String s_rrdError = "/images/rrd/error.png";
    
    private GraphDao m_graphDao;

    private ResourceDao m_resourceDao;
    
    private RrdDao m_rrdDao;

    public InputStream getAdhocGraph(String resourceId, String title,
            String[] dataSources, String[] aggregateFunctions,
            String[] colors, String[] dataSourceTitles, String[] styles,
            long start, long end) {
        Assert.notNull(resourceId, "resourceId argument cannot be null");
        Assert.notNull(title, "title argument cannot be null");
        Assert.notNull(dataSources, "dataSources argument cannot be null");
        Assert.notNull(aggregateFunctions, "aggregateFunctions argument cannot be null");
        Assert.notNull(colors, "colors argument cannot be null");
        Assert.notNull(dataSourceTitles, "dataSourceTitles argument cannot be null");
        Assert.notNull(styles, "styles argument cannot be null");
        Assert.isTrue(end > start, "end time must be after start time");
        
        AdhocGraphType t = m_graphDao.findAdhocByName("performance");

        OnmsResource r = m_resourceDao.loadResourceById(resourceId);
        
        String command = createAdHocCommand(t,
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
        File workDir = m_resourceDao.getRrdDirectory(true);

        InputStream tempIn = null;
        try {
            log().debug("Executing RRD command in directory '" + workDir
                        + "': " + command);

            tempIn = m_rrdDao.createGraph(command, workDir);
        } catch (DataAccessException e) {
            log().warn(e);
            if (debug) {
                throw e;
            } else {
                return returnErrorImage(s_rrdError);
            }
        }

        return tempIn;
    }
    
    public InputStream returnErrorImage(String file) {
        InputStream is =  getClass().getResourceAsStream(file);
        if (is == null) {
            throw new ObjectRetrievalFailureException(InputStream.class, file, "Could not find error image for '" + file + "' or could open", null);
        }
        return is;
    }

    public InputStream getPrefabGraph(String resourceId,
            String report, long start,
            long end) {
        Assert.notNull(resourceId, "resourceId argument cannot be null");
        Assert.notNull(report, "report argument cannot be null");
        Assert.isTrue(end > start, "end time must be after start time");

        PrefabGraphType t = m_graphDao.findByName("performance");
        if (t == null) {
            throw new IllegalArgumentException("graph type \"" + "performance"
                                               + "\" is not valid");
        }
        
        OnmsResource r = m_resourceDao.loadResourceById(resourceId);

        PrefabGraph prefabGraph = m_graphDao.getPrefabGraph(report);
        
        Graph graph = new Graph(prefabGraph, r, new Date(start), new Date(end));

        String command = createPrefabCommand(graph,
                                             t.getCommandPrefix(),
                                             m_resourceDao.getRrdDirectory(true),
                                             report);
        
        return getInputStreamForCommand(command);
    }
    
    protected String createAdHocCommand(AdhocGraphType adhocType,
            OnmsResource resource,
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
        
        String[] rrdFiles = getRrdNames(resource, dsNames);

        List<String> defs = new ArrayList<String>(dsNames.length);
        List<String> lines = new ArrayList<String>(dsNames.length);
        for (int i = 0; i < dsNames.length; i++) {
            String dsAbbrev = "ds" + Integer.toString(i);

            String dsName = dsNames[i];
            String rrd = rrdFiles[i];
            String dsAggregFxn = dsAggregFxns[i];
            String color = colors[i];
            String dsTitle = dsTitles[i];
            String dsStyle = dsStyles[i];

            defs.add(MessageFormat.format(ds, rrd, starttime,
                                            endtime, graphtitle,
                                            dsAbbrev, dsName,
                                            dsAggregFxn, dsStyle,
                                            color, dsTitle));
            
            lines.add(MessageFormat.format(graphline, rrd,
                                            starttime, endtime, graphtitle,
                                            dsAbbrev, dsName, dsAggregFxn,
                                            dsStyle, color, dsTitle));
        }
        
        for (String def : defs) {
            buf.append(" ");
            buf.append(def);
        }
        for (String line : lines) {
            buf.append(" ");
            buf.append(line);
        }

        log().debug("formatting: " + buf + ", bogus-rrd, " + starttime + ", "
                    + endtime + ", " + graphtitle);
        return MessageFormat.format(buf.toString(), "bogus-rrd",
                                    starttime, endtime, graphtitle);
    }

    private String[] getRrdNames(OnmsResource resource, String[] dsNames) {
        String[] rrds = new String[dsNames.length];
        
        Map<String, RrdGraphAttribute> attributes = resource.getRrdGraphAttributes();

        for (int i=0; i < dsNames.length; i++) {
            RrdGraphAttribute attribute = attributes.get(dsNames[i]);
            if (attribute == null) {
                throw new IllegalArgumentException("RRD attribute '" + dsNames[i] + "' is not available on this resource.  Available RRD attributes: " + StringUtils.collectionToDelimitedString(attributes.keySet(), ", "));
            }

            rrds[i] = attribute.getRrdRelativePath(); 
        }

        return rrds;
    }
    
    private Map<String, String> getTranslationsForAttributes(Map<String, String> attributes, String[] requiredAttributes, String type) {
        if (requiredAttributes == null) {
            // XXX Nothing to do; not sure if we need this check
            return new HashMap<String, String>(0);
        }
        
        Map<String, String> translations = new HashMap<String, String>(requiredAttributes.length);
        
        for (String requiredAttribute : requiredAttributes) {
            String attributeValue = attributes.get(requiredAttribute);
            if (attributeValue == null) {
                throw new IllegalArgumentException(type + " '" + requiredAttribute + "' is not available on this resource.  Available " + type + "s: " + StringUtils.collectionToDelimitedString(attributes.keySet(), ", "));
            }

            // Replace any single backslashes in the value with escaped backslashes so other parsing won't barf
            String replacedValue = attributeValue.replace("\\", "\\\\");
            translations.put(RE.simplePatternToFullRegularExpression("{" + requiredAttribute + "}"), replacedValue);
        }

        return translations;
    }

    protected String createPrefabCommand(Graph graph,
            String commandPrefix,
            File workDir, String reportName) {
        PrefabGraph prefabGraph = graph.getPrefabGraph();

        String[] rrds = getRrdNames(graph.getResource(), graph.getPrefabGraph().getColumns());
        
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

        translationMap.putAll(getTranslationsForAttributes(graph.getResource().getExternalValueAttributes(), prefabGraph.getExternalValues(), "external value attribute"));
        translationMap.putAll(getTranslationsForAttributes(graph.getResource().getStringPropertyAttributes(), prefabGraph.getPropertiesValues(), "string property attribute"));
        
        
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

    private Category log() {
        return ThreadCategory.getInstance();
    }

    public void afterPropertiesSet() {
        Assert.state(m_resourceDao != null, "resourceDao property has not been set");
        Assert.state(m_graphDao != null, "graphDao property has not been set");
        Assert.state(m_rrdDao != null, "rrdDao property has not been set");
    }

    public ResourceDao getResourceDao() {
        return m_resourceDao;
    }

    public void setResourceDao(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }

    public GraphDao getGraphDao() {
        return m_graphDao;
    }

    public void setGraphDao(GraphDao graphDao) {
        m_graphDao = graphDao;
    }

    public RrdDao getRrdDao() {
        return m_rrdDao;
    }

    public void setRrdDao(RrdDao rrdDao) {
        m_rrdDao = rrdDao;
    }

}
