/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
                String lastFile = Sftp3gppUtils.getLastFilename(getBlobStore(), getServiceName(), resourcePath, url.getPath());
                connection = (Sftp3gppUrlConnection) url.openConnection();
                if (lastFile == null) {
                    lastFile = connection.get3gppFileName();
                    LOG.debug("collect(single): retrieving file from {}{}{} from {}", url.getPath(), File.separatorChar, lastFile, agent.getHostAddress());
                    Document doc = getXmlDocument(urlStr, request);
                    fillCollectionSet(agent, builder, source, doc);
                    Sftp3gppUtils.setLastFilename(getBlobStore(), getServiceName(), resourcePath, url.getPath(), lastFile);
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
                            Sftp3gppUtils.setLastFilename(getBlobStore(), getServiceName(), resourcePath, url.getPath(), fileName);
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
