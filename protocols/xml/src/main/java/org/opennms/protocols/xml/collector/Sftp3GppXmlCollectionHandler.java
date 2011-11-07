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

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.CollectionException;
import org.opennms.netmgt.collectd.ServiceCollector;
import org.opennms.protocols.sftp.Sftp3gppUrlHandler;
import org.opennms.protocols.xml.config.XmlDataCollection;
import org.opennms.protocols.xml.config.XmlSource;

import org.w3c.dom.Document;

/**
 * The custom implementation of the interface XmlCollectionHandler for 3GPP XML Data.
 * <p>This supports the processing of several files.</p>
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class Sftp3gppXmlCollectionHandler extends DefaultXmlCollectionHandler {

    /** The Cache to hold last successfully processed timestamp for each node. */
    // FIXME: The cache should be persisted on disk
    protected static Map<Integer,Long> m_cache = new ConcurrentHashMap<Integer,Long>();

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.XmlCollectionHandler#collect(org.opennms.netmgt.collectd.CollectionAgent, org.opennms.protocols.xml.config.XmlDataCollection, java.util.Map)
     */
    @Override
    public XmlCollectionSet collect(CollectionAgent agent, XmlDataCollection collection, Map<String, Object> parameters) throws CollectionException {
        // Create a new collection set.
        XmlCollectionSet collectionSet = new XmlCollectionSet(agent);
        collectionSet.setCollectionTimestamp(new Date());
        collectionSet.setStatus(ServiceCollector.COLLECTION_UNKNOWN);

        // Load the attribute group types.
        loadAttributes(collection);

        // TODO We could be careful when handling exceptions because parsing exceptions will be treated different from connection or retrieval exceptions
        try {
            int step = collection.getXmlRrd().getStep() * 1000; // The step should be specified in milliseconds for timestamp calculations
            long lastTs = getLastTimestamp(agent.getNodeId(), step);
            long currentTs = getCurrentTimestamp(step);
            // Cycle through the pending files starting from the next file after the last successfully processed.
            for (long ts = lastTs + step; ts <= currentTs; ts += step) {
                for (XmlSource source : collection.getXmlSources()) {
                    String urlStr = parseUrl(source.getUrl(), agent, collection.getXmlRrd().getStep(), ts);
                    log().debug("collect: retrieving data from " + urlStr);
                    Document doc = getXmlDocument(agent, urlStr);
                    fillCollectionSet(agent, collectionSet, source, doc);
                    setLastTimestamp(agent.getNodeId(), ts); // collection succeeded
                }
            }
            collectionSet.setStatus(ServiceCollector.COLLECTION_SUCCEEDED);
            return collectionSet;
        } catch (Exception e) {
            collectionSet.setStatus(ServiceCollector.COLLECTION_FAILED);
            throw new CollectionException("Can't collect XML data because " + e.getMessage(), e);
        }
    }

    /**
     * Gets the last timestamp.
     *
     * @param nodeId the node id
     * @param step the collection step (in milliseconds)
     * @return the last timestamp
     */
    private long getLastTimestamp(Integer nodeId, Integer step) {
        if (!m_cache.containsKey(nodeId))
            m_cache.put(nodeId, getCurrentTimestamp(step));
        return m_cache.get(nodeId);
    }

    /**
     * Sets the last timestamp.
     *
     * @param nodeId the node id
     * @param ts the timestamp
     */
    private void setLastTimestamp(Integer nodeId, Long ts) {
        m_cache.put(nodeId, ts);
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

}
