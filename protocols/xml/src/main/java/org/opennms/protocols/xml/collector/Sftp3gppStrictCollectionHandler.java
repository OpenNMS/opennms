/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.protocols.xml.collector;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.CollectionException;
import org.opennms.netmgt.collectd.ServiceCollector;
import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.netmgt.dao.support.ResourceTypeUtils;
import org.opennms.protocols.sftp.Sftp3gppUrlHandler;
import org.opennms.protocols.xml.config.XmlDataCollection;
import org.opennms.protocols.xml.config.XmlObject;
import org.opennms.protocols.xml.config.XmlSource;

import org.w3c.dom.Document;

/**
 * The custom implementation of the interface XmlCollectionHandler for 3GPP XML Data.
 * <p>This supports the processing of several files strictly ordered by timestamp,
 * and the timestamp between files should be only one granularity period.</p>
 * <p>The state will be persisted on disk by saving the last successfully processed
 * timestamp.</p>
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class Sftp3gppStrictCollectionHandler extends AbstractXmlCollectionHandler {

    /** The Constant XML_LAST_TIMESTAMP. */
    public static final String XML_LAST_TIMESTAMP = "_xmlCollectorLastTimestamp";

    /** The 3GPP Performance Metric Instance Formats. */
    private Properties m_pmGroups;

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.XmlCollectionHandler#collect(org.opennms.netmgt.collectd.CollectionAgent, org.opennms.protocols.xml.config.XmlDataCollection, java.util.Map)
     */
    @Override
    public XmlCollectionSet collect(CollectionAgent agent, XmlDataCollection collection, Map<String, Object> parameters) throws CollectionException {
        // Create a new collection set.
        XmlCollectionSet collectionSet = new XmlCollectionSet(agent);
        collectionSet.setCollectionTimestamp(new Date());
        collectionSet.setStatus(ServiceCollector.COLLECTION_UNKNOWN);

        // TODO We could be careful when handling exceptions because parsing exceptions will be treated different from connection or retrieval exceptions
        try {
            File resourceDir = new File(getRrdRepository().getRrdBaseDir(), Integer.toString(agent.getNodeId()));
            int step = collection.getXmlRrd().getStep() * 1000; // The step should be specified in milliseconds for timestamp calculations
            long lastTs = getLastTimestamp(resourceDir, step);
            long currentTs = getCurrentTimestamp(step);
            // Cycle through the pending files starting from the next file after the last successfully processed.
            for (long ts = lastTs + step; ts <= currentTs; ts += step) {
                for (XmlSource source : collection.getXmlSources()) {
                    String urlStr = parseUrl(source.getUrl(), agent, collection.getXmlRrd().getStep(), ts);
                    log().debug("collect: retrieving data from " + urlStr);
                    Document doc = getXmlDocument(urlStr);
                    fillCollectionSet(agent, collectionSet, source, doc);
                    setLastTimestamp(resourceDir, ts); // collection succeeded
                }
            }
            collectionSet.setStatus(ServiceCollector.COLLECTION_SUCCEEDED);
            return collectionSet;
        } catch (Exception e) {
            collectionSet.setStatus(ServiceCollector.COLLECTION_FAILED);
            throw new CollectionException("Can't collect XML data because " + e.getMessage(), e);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.AbstractXmlCollectionHandler#processXmlResource(org.opennms.protocols.xml.collector.XmlCollectionResource, org.opennms.netmgt.config.collector.AttributeGroupType)
     */
    @Override
    protected void processXmlResource(XmlCollectionResource resource, AttributeGroupType attribGroupType) {
        Map<String,String> properties = get3gppProperties(get3gppFormat(resource.getResourceTypeName()), resource.getInstance());
        for (Entry<String,String> entry : properties.entrySet()) {
            XmlCollectionAttributeType attribType = new XmlCollectionAttributeType(new XmlObject(entry.getKey(), "string"), attribGroupType);
            resource.setAttributeValue(attribType, entry.getValue());
        }
    }

    /**
     * Gets the last timestamp.
     *
     * @param resourceDir the resource directory
     * @param step the collection step (in milliseconds)
     * @return the last timestamp
     * @throws Exception the exception
     */
    private long getLastTimestamp(File resourceDir, Integer step) throws Exception {
        String ts = null;
        try {
            ts = ResourceTypeUtils.getStringProperty(resourceDir, getCacheId());
        } catch (Exception e) {
            log().info("getLastTimestamp: creating a new timestamp tracker on " + resourceDir);
        }
        if (ts == null) {
            Long t = getCurrentTimestamp(step);
            setLastTimestamp(resourceDir, t);
            return t;
        }
        return Long.parseLong(ts);
    }

    /**
     * Sets the last timestamp.
     *
     * @param resourceDir the resource directory
     * @param ts the new timestamp
     * @throws Exception the exception
     */
    private void setLastTimestamp(File resourceDir, Long ts) throws Exception {
        ResourceTypeUtils.updateStringProperty(resourceDir, ts.toString(), getCacheId());
    }

    /**
     * Gets the cache id.
     *
     * @return the cache id
     */
    private String getCacheId() {
        return XML_LAST_TIMESTAMP + '.' + getServiceName();
    }

    /**
     * Gets the current timestamp.
     *
     * @param step the collection step (in milliseconds)
     * @return the current timestamp
     */
    private long getCurrentTimestamp(Integer step) {
        long reference = System.currentTimeMillis();
        return reference - reference  % step; // normalize timestamp
    }

    /**
     * Parses the URL.
     *
     * @param unformattedUrl the unformatted URL
     * @param agent the agent
     * @param collectionStep the collection step (in seconds)
     * @param currentTimestamp the current timestamp
     * @return the string
     */
    protected String parseUrl(String unformattedUrl, CollectionAgent agent, Integer collectionStep, long currentTimestamp) throws IllegalArgumentException {
        if (!unformattedUrl.startsWith(Sftp3gppUrlHandler.PROTOCOL)) {
            throw new IllegalArgumentException("The 3GPP SFTP Collection Handler can only use the protocol " + Sftp3gppUrlHandler.PROTOCOL);
        }
        String baseUrl = parseUrl(unformattedUrl, agent, collectionStep);
        return baseUrl + "&referenceTimestamp=" + currentTimestamp;
    }

    /**
     * Gets the 3GPP resource format.
     *
     * @param resourceType the resource type
     * @return the 3gpp format
     */
    public String get3gppFormat(String resourceType) {
        if (m_pmGroups == null) {
            m_pmGroups = new Properties();
            try {
                m_pmGroups.load(getClass().getResourceAsStream("/3gpp-pmgroups.properties"));
            } catch (IOException e) {
                log().warn("Can't load 3GPP PM Groups formats because " + e.getMessage());
            }
        }
        return m_pmGroups.getProperty(resourceType);
    }

    /**
     * Gets the 3GPP properties based on measInfoId.
     *
     * @param format the format
     * @param measInfoId the measInfoId (the resource instance)
     * @return the properties
     */
    public Map<String,String> get3gppProperties(String format, String measInfoId) {
        Map<String,String> properties = new LinkedHashMap<String,String>();
        if (format != null) {
            String[] groups = format.split("\\|");
            for (String group : groups) {
                String[] subgroups = group.split("/");
                for (String subgroup : subgroups) {
                    String pair[] = subgroup.split("=");
                    if (pair.length > 1) {
                        if (pair[1].matches("^[<].+[>]$")) {
                            // I'm not sure how to deal with separating by | or / and avoiding separating by \/
                            String valueRegex = pair[1].equals("<directory path>") ? "=([^|]+)" : "=([^|/]+)";
                            Matcher m = Pattern.compile(pair[0] + valueRegex).matcher(measInfoId);
                            if (m.find()) {
                                String v = pair[1].equals("<directory path>") ? m.group(1).replaceAll("\\\\/", "/") : m.group(1);
                                properties.put(pair[0], v);
                            }
                        }
                    }
                }
            }
        }
        properties.put("label", properties.toString().replaceAll("[{}]", ""));
        properties.put("instance", measInfoId);
        return properties;
    }
}
