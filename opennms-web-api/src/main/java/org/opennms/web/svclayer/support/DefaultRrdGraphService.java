/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.svclayer.support;

import java.io.File;
import java.io.InputStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.opennms.netmgt.dao.api.GraphDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.dao.api.RrdDao;
import org.opennms.netmgt.model.AdhocGraphType;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.model.PrefabGraphType;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.rrd.RrdFileConstants;
import org.opennms.web.svclayer.RrdGraphService;
import org.opennms.web.svclayer.model.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * <p>DefaultRrdGraphService class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:cmiskell@opennms.org">Craig Miskell</a>
 */
public class DefaultRrdGraphService implements RrdGraphService, InitializingBean {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultRrdGraphService.class);

//    private static final String s_missingParamsPath = "/images/rrd/missingparams.png";
    private static final String s_rrdError = "/images/rrd/error.png";

    private GraphDao m_graphDao;

    private ResourceDao m_resourceDao;

    private RrdDao m_rrdDao;

    /** {@inheritDoc} */
    @Override
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

        AdhocGraphType t = m_graphDao.findAdhocGraphTypeByName("performance");

        OnmsResource r = m_resourceDao.getResourceById(resourceId);
        Assert.notNull(r, "resource \"" + resourceId + "\" could not be located");

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
            LOG.debug("Executing RRD command in directory '{}': {}", workDir, command);
            tempIn = m_rrdDao.createGraph(command, workDir);
        } catch (final DataAccessException e) {
        	LOG.warn("Exception while creating graph.", e);
            if (debug) {
                throw e;
            } else {
                return returnErrorImage(s_rrdError);
            }
        }

        return tempIn;
    }

    /**
     * <p>returnErrorImage</p>
     *
     * @param file a {@link java.lang.String} object.
     * @return a {@link java.io.InputStream} object.
     */
    public InputStream returnErrorImage(String file) {
        InputStream is =  getClass().getResourceAsStream(file);
        if (is == null) {
            throw new ObjectRetrievalFailureException(InputStream.class, file, "Could not find error image for '" + file + "' or could open", null);
        }
        return is;
    }

    /** {@inheritDoc} */
    @Override
    public InputStream getPrefabGraph(String resourceId, String report, long start, long end, Integer width, Integer height) {
        Assert.notNull(resourceId, "resourceId argument cannot be null");
        Assert.notNull(report, "report argument cannot be null");
        Assert.isTrue(end > start, "end time " + end + " must be after start time" + start);

        PrefabGraphType t = m_graphDao.findPrefabGraphTypeByName("performance");
        if (t == null) {
            throw new IllegalArgumentException("graph type \"" + "performance"
                                               + "\" is not valid");
        }

        OnmsResource r = m_resourceDao.getResourceById(resourceId);
        Assert.notNull(r, "resource could not be located");

        PrefabGraph prefabGraph = m_graphDao.getPrefabGraph(report);

        Graph graph = new Graph(prefabGraph, r, new Date(start), new Date(end));

        String command = createPrefabCommand(graph,
                                             t.getCommandPrefix(),
                                             m_resourceDao.getRrdDirectory(true),
                                             report,
                                             width,
                                             height);

        return getInputStreamForCommand(command);
    }

    /**
     * <p>createAdHocCommand</p>
     *
     * @param adhocType a {@link org.opennms.netmgt.model.AdhocGraphType} object.
     * @param resource a {@link org.opennms.netmgt.model.OnmsResource} object.
     * @param start a long.
     * @param end a long.
     * @param graphtitle a {@link java.lang.String} object.
     * @param dsNames an array of {@link java.lang.String} objects.
     * @param dsAggregFxns an array of {@link java.lang.String} objects.
     * @param colors an array of {@link java.lang.String} objects.
     * @param dsTitles an array of {@link java.lang.String} objects.
     * @param dsStyles an array of {@link java.lang.String} objects.
     * @return a {@link java.lang.String} object.
     */
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

        LOG.debug("formatting: {}, bogus-rrd, {}, {}, {}", buf, starttime, endtime, graphtitle);
        return MessageFormat.format(buf.toString(), "bogus-rrd", starttime, endtime, graphtitle);
    }

    private static String[] getRrdNames(OnmsResource resource, String[] dsNames) {
        String[] rrds = new String[dsNames.length];

        Map<String, RrdGraphAttribute> attributes = resource.getRrdGraphAttributes();

        for (int i=0; i < dsNames.length; i++) {
            RrdGraphAttribute attribute = attributes.get(dsNames[i]);
            if (attribute == null) {
                throw new IllegalArgumentException("RRD attribute '" + dsNames[i] + "' is not available on resource '" + resource.getId() + "'.  Available RRD attributes: " + StringUtils.collectionToDelimitedString(attributes.keySet(), ", "));
            }

            rrds[i] = RrdFileConstants.escapeForGraphing(attribute.getRrdRelativePath());
        }

        return rrds;
    }

    private static Map<String, String> getTranslationsForAttributes(Map<String, String> attributes, String[] requiredAttributes, String type) {
        if (requiredAttributes == null) {
            // XXX Nothing to do; not sure if we need this check
            return new HashMap<String, String>(0);
        }

        Map<String, String> translations = new HashMap<String, String>(requiredAttributes.length);

        for (String requiredAttribute : requiredAttributes) {
            String attributeValue = attributes.get(requiredAttribute);
            if (attributeValue == null) {
                throw new IllegalArgumentException(type + " '" + requiredAttribute + "' is not available in the list of provided attributes.  Available " + type + "s: " + StringUtils.collectionToDelimitedString(attributes.keySet(), ", "));
            }

            // Replace any single backslashes in the value with escaped backslashes so other parsing won't barf
            String replacedValue = attributeValue.replace("\\", "\\\\");
            translations.put("{" + requiredAttribute + "}", replacedValue);
        }

        return translations;
    }

    /**
     * <p>createPrefabCommand</p>
     *
     * @param graph a {@link org.opennms.web.svclayer.model.Graph} object.
     * @param commandPrefix a {@link java.lang.String} object.
     * @param workDir a {@link java.io.File} object.
     * @param reportName a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */

    protected String createPrefabCommand(Graph graph, String commandPrefix, File workDir, String reportName, Integer width, Integer height) {
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
            translationMap.put(key, rrds[i]);
        }

        translationMap.put("{startTime}", startTimeString);
        translationMap.put("{endTime}", endTimeString);
        translationMap.put("{diffTime}", diffTimeString);

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        translationMap.put("{startTimeDate}", fmt.format(new Date(startTime)).replace(":", "\\:"));
        translationMap.put("{endTimeDate}", fmt.format(new Date(endTime)).replace(":", "\\:"));

        // Handle a start time with a format.
        Matcher stm = Pattern.compile("\\{startTime:(.+?)\\}").matcher(command);
        boolean matchFail = false;
        while(stm.find() && !matchFail) {
            String sdfPattern = stm.group(0);
            if (sdfPattern == null) {
                matchFail = true;
            } else {
                try {
                    fmt = new SimpleDateFormat(sdfPattern);
                    translationMap.put("{startTime:"+sdfPattern+"}", fmt.format(new Date(startTime)).replace(":", "\\:"));
                } catch (IllegalArgumentException e) {
                    LOG.error("Cannot parse date format '{}' for graph {}.", sdfPattern, reportName);
                }
            }
        }

        // Handle an end time with a format
        Matcher etm = Pattern.compile("\\{endTime:(.+?)\\}").matcher(command);
        matchFail = false;
        while (etm.find() && !matchFail) {
            String sdfPattern = etm.group(0);
            if (sdfPattern == null) {
              matchFail = true;
            } else {
                try {
                    fmt = new SimpleDateFormat(sdfPattern);
                    translationMap.put("{endTime:"+sdfPattern+"}", fmt.format(new Date(endTime)).replace(":", "\\:"));
                } catch (IllegalArgumentException e) {
                    LOG.error("Cannot parse date format '{}' for graph {}.", sdfPattern, reportName);
                }
            }
        }

        try {
            translationMap.putAll(getTranslationsForAttributes(graph.getResource().getExternalValueAttributes(), prefabGraph.getExternalValues(), "external value attribute"));
            translationMap.putAll(getTranslationsForAttributes(graph.getResource().getStringPropertyAttributes(), prefabGraph.getPropertiesValues(), "string property attribute"));
        } catch (RuntimeException e) {
            LOG.error("Invalid attributes were found on resource '{}'", graph.getResource().getId());
            throw e;
        }


        try {
            for (Map.Entry<String, String> translation : translationMap.entrySet()) {
                // replace s1 with s2
                final Matcher matcher = Pattern.compile(translation.getKey(), Pattern.LITERAL).matcher(command);
                command = matcher.replaceAll(Matcher.quoteReplacement(translation.getValue()));
            }
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Invalid regular expression syntax, check rrd-properties file", e);
        }


        if (width != null) {
            final Pattern re = Pattern.compile("(--width|-w)(\\w+|=)(\\d+)");

            final Matcher matcher = re.matcher(command);
            if (matcher.matches()) {
                matcher.replaceFirst("--width " + width);
            } else {
                command = command + " --width " + width;
            }
        }

        if (height != null) {
            final Pattern re = Pattern.compile("(--height|-h)(\\w+|=)(\\d+)");

            final Matcher matcher = re.matcher(command);
            if (matcher.matches()) {
                matcher.replaceFirst("--height " + height);
            } else {
                command = command + " --height " + height;
            }
        }

        return command;
    }

    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        Assert.state(m_resourceDao != null, "resourceDao property has not been set");
        Assert.state(m_graphDao != null, "graphDao property has not been set");
        Assert.state(m_rrdDao != null, "rrdDao property has not been set");
    }

    /**
     * <p>getResourceDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.ResourceDao} object.
     */
    public ResourceDao getResourceDao() {
        return m_resourceDao;
    }

    /**
     * <p>setResourceDao</p>
     *
     * @param resourceDao a {@link org.opennms.netmgt.dao.api.ResourceDao} object.
     */
    public void setResourceDao(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }

    /**
     * <p>getGraphDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.GraphDao} object.
     */
    public GraphDao getGraphDao() {
        return m_graphDao;
    }

    /**
     * <p>setGraphDao</p>
     *
     * @param graphDao a {@link org.opennms.netmgt.dao.api.GraphDao} object.
     */
    public void setGraphDao(GraphDao graphDao) {
        m_graphDao = graphDao;
    }

    /**
     * <p>getRrdDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.RrdDao} object.
     */
    public RrdDao getRrdDao() {
        return m_rrdDao;
    }

    /**
     * <p>setRrdDao</p>
     *
     * @param rrdDao a {@link org.opennms.netmgt.dao.api.RrdDao} object.
     */
    public void setRrdDao(RrdDao rrdDao) {
        m_rrdDao = rrdDao;
    }

}
