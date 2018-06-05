/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.protocols.xml.collector;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.Resource;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.protocols.sftp.Sftp3gppUrlConnection;
import org.opennms.protocols.sftp.Sftp3gppUrlHandler;
import org.opennms.protocols.xml.config.Request;
import org.opennms.protocols.xml.config.XmlDataCollection;
import org.opennms.protocols.xml.config.XmlSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * The custom implementation of the interface XmlCollectionHandler for 3GPP XML Data.
 * <p>This supports the processing of several files ordered by filename, and the
 * timestamp between files won't be taken in consideration.</p>
 * <p>The state will be persisted on disk by saving the name of the last successfully
 * processed file.</p>
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class Sftp3gppXmlCollectionHandler extends AbstractXmlCollectionHandler {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(Sftp3gppXmlCollectionHandler.class);

    /* (non-Javadoc)
     * @see org.opennms.protocols.xml.collector.XmlCollectionHandler#collect(org.opennms.netmgt.collectd.CollectionAgent, org.opennms.protocols.xml.config.XmlDataCollection, java.util.Map)
     */
    @Override
    public CollectionSet collect(CollectionAgent agent, XmlDataCollection collection, Map<String, Object> parameters) throws CollectionException {
        String status = "finished";

        // Create a new collection set.
        CollectionSetBuilder builder = new CollectionSetBuilder(agent);

        // TODO We could be careful when handling exceptions because parsing exceptions will be treated different from connection or retrieval exceptions
        DateTime startTime = new DateTime();
        Sftp3gppUrlConnection connection = null;
        try {
            ResourcePath resourcePath = ResourcePath.get(ResourceTypeUtils.SNMP_DIRECTORY, Integer.toString(agent.getNodeId()));
            for (XmlSource source : collection.getXmlSources()) {
                if (!source.getUrl().startsWith(Sftp3gppUrlHandler.PROTOCOL)) {
                    throw new CollectionException("The 3GPP SFTP Collection Handler can only use the protocol " + Sftp3gppUrlHandler.PROTOCOL);
                }
                final String urlStr = source.getUrl();
                final Request request = source.getRequest();
                URL url = UrlFactory.getUrl(urlStr, request);
                String lastFile = Sftp3gppUtils.getLastFilename(getResourceStorageDao(), getServiceName(), resourcePath, url.getPath());
                connection = (Sftp3gppUrlConnection) url.openConnection();
                if (lastFile == null) {
                    lastFile = connection.get3gppFileName();
                    LOG.debug("collect(single): retrieving file from {}{}{} from {}", url.getPath(), File.separatorChar, lastFile, agent.getHostAddress());
                    Document doc = getXmlDocument(urlStr, request);
                    fillCollectionSet(agent, builder, source, doc);
                    Sftp3gppUtils.setLastFilename(getResourceStorageDao(), getServiceName(), resourcePath, url.getPath(), lastFile);
                    Sftp3gppUtils.deleteFile(connection, lastFile);
                } else {
                    connection.connect();
                    List<String> files = connection.getFileList();
                    long lastTs = connection.getTimeStampFromFile(lastFile);
                    boolean collected = false;
                    for (String fileName : files) {
                        if (connection.getTimeStampFromFile(fileName) > lastTs) {
                            LOG.debug("collect(multiple): retrieving file {} from {}", fileName, agent.getHostAddress());
                            InputStream is = connection.getFile(fileName);
                            try {
                                Document doc = getXmlDocument(is, request);
                                IOUtils.closeQuietly(is);
                                fillCollectionSet(agent, builder, source, doc);
                            } finally {
                                IOUtils.closeQuietly(is);
                            }
                            Sftp3gppUtils.setLastFilename(getResourceStorageDao(), getServiceName(), resourcePath, url.getPath(), fileName);
                            Sftp3gppUtils.deleteFile(connection, fileName);
                            collected = true;
                        }
                    }
                    if (!collected) {
                        LOG.warn("collect: could not find any file after {} on {}", lastFile, agent);
                    }
                }
            }
            return builder.build();
        } catch (Exception e) {
            status = "failed";
            throw new CollectionException(e.getMessage(), e);
        } finally {
            DateTime endTime = new DateTime();
            LOG.debug("collect: {} collection {}: duration: {} ms", status, collection.getName(), endTime.getMillis()-startTime.getMillis());
            UrlFactory.disconnect(connection);
        }
    }

    @Override
    protected void fillCollectionSet(String urlString, Request request, CollectionAgent agent, CollectionSetBuilder builder, XmlSource source) throws Exception {
        // This handler has a custom implementation of the collect method, so there is no need to do something special here.
    }

    @Override
    protected void processXmlResource(CollectionSetBuilder builder, Resource resource, String resourceTypeName, String group) {
        Sftp3gppUtils.processXmlResource(builder, resource, resourceTypeName, group);
    }

}
