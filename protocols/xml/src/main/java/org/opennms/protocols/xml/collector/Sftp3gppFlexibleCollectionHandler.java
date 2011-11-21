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
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.CollectionException;
import org.opennms.netmgt.collectd.ServiceCollector;
import org.opennms.netmgt.dao.support.ResourceTypeUtils;
import org.opennms.protocols.sftp.Sftp3gppUrlConnection;
import org.opennms.protocols.sftp.Sftp3gppUrlHandler;
import org.opennms.protocols.xml.config.XmlDataCollection;
import org.opennms.protocols.xml.config.XmlSource;
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
public class Sftp3gppFlexibleCollectionHandler extends Sftp3gppStrictCollectionHandler {

    /** The Constant XML_LAST_FILENAME. */
    public static final String XML_LAST_FILENAME = "_xmlCollectorLastFilename";

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
            for (XmlSource source : collection.getXmlSources()) {
                if (!source.getUrl().startsWith(Sftp3gppUrlHandler.PROTOCOL)) {
                    throw new CollectionException("The 3GPP SFTP Collection Handler can only use the protocol " + Sftp3gppUrlHandler.PROTOCOL);
                }
                String urlStr = parseUrl(source.getUrl(), agent, collection.getXmlRrd().getStep());
                URL url = UrlFactory.getUrl(urlStr);
                String lastFile = getLastFilename(resourceDir, url.getPath());
                Sftp3gppUrlConnection connection = (Sftp3gppUrlConnection) url.openConnection();
                if (lastFile == null) {
                    lastFile = connection.get3gppFileName();
                    log().debug("collect(single): retrieving file from " + url.getPath() + File.separatorChar + lastFile + " from " + agent.getHostAddress());
                    Document doc = getXmlDocument(urlStr);
                    fillCollectionSet(agent, collectionSet, source, doc);
                    setLastFilename(resourceDir, url.getPath(), lastFile);
                } else {
                    connection.connect();
                    List<String> files = connection.getFileList();
                    long lastTs = connection.getTimeStampFromFile(lastFile);
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    factory.setIgnoringComments(true);
                    boolean collected = false;
                    for (String fileName : files) {
                        long currentTs = connection.getTimeStampFromFile(fileName);
                        if (currentTs > lastTs) {
                            log().debug("collect(multiple): retrieving file " + fileName + " from " + agent.getHostAddress());
                            InputStream is = connection.getFile(fileName);
                            Document doc = builder.parse(is);
                            fillCollectionSet(agent, collectionSet, source, doc);
                            setLastFilename(resourceDir, url.getPath(), fileName);
                            connection.deleteFile(fileName);
                            collected = true;
                        }
                    }
                    if (!collected) {
                        log().warn("collect: could not find any file after " + lastFile + " on " + agent);
                    }
                    connection.disconnect();
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
     * Gets the last filename.
     *
     * @param resourceDir the resource directory
     * @param targetPath the target path
     * @return the last filename
     * @throws Exception the exception
     */
    private String getLastFilename(File resourceDir, String targetPath) throws Exception {
        String filename = null;
        try {
            filename = ResourceTypeUtils.getStringProperty(resourceDir, getCacheId(targetPath));
        } catch (Exception e) {
            log().info("getLastFilename: creating a new filename tracker on " + resourceDir);
        }
        return filename;
    }

    /**
     * Sets the last filename.
     *
     * @param resourceDir the resource directory
     * @param targetPath the target path
     * @param filename the filename
     * @throws Exception the exception
     */
    private void setLastFilename(File resourceDir, String targetPath, String filename) throws Exception {
        ResourceTypeUtils.updateStringProperty(resourceDir, filename, getCacheId(targetPath));
    }

    /**
     * Gets the cache id.
     *
     * @param targetPath the target path
     * @return the cache id
     */
    private String getCacheId(String targetPath) {
        return XML_LAST_FILENAME + '.' + getServiceName() + targetPath.replaceAll("/", "_");
    }
}
