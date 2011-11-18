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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.CollectionException;
import org.opennms.netmgt.collectd.ServiceCollector;
import org.opennms.netmgt.dao.support.ResourceTypeUtils;
import org.opennms.protocols.sftp.Sftp3gppUrlConnection;
import org.opennms.protocols.sftp.Sftp3gppUrlHandler;
import org.opennms.protocols.xml.config.XmlDataCollection;
import org.opennms.protocols.xml.config.XmlSource;
import org.w3c.dom.Document;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;

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
                    ChannelSftp channel = connection.getChannel();
                    @SuppressWarnings("unchecked")
                    Vector<LsEntry> files = channel.ls(url.getPath());
                    Collections.sort(files, new Comparator<LsEntry>() {
                        @Override
                        public int compare(LsEntry arg0, LsEntry arg1) {
                            return arg0.getFilename().compareTo(arg1.getFilename());
                        }
                    });
                    long lastTs = getTimeStampFromFile(lastFile);
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    factory.setIgnoringComments(true);
                    boolean collected = false;
                    for (LsEntry entry : files) {
                        long currentTs = getTimeStampFromFile(entry.getFilename());
                        if (currentTs > lastTs) {
                            String fileName = url.getPath() + File.separatorChar + entry.getFilename();
                            log().debug("collect(multiple): retrieving file from " + fileName + " from " + agent.getHostAddress());
                            InputStream is = channel.get(fileName);
                            Document doc = builder.parse(is);
                            fillCollectionSet(agent, collectionSet, source, doc);
                            setLastFilename(resourceDir, url.getPath(), entry.getFilename());
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
     * Gets the time stamp from 3GPP XML file name.
     *
     * @param fileName the 3GPP XML file name
     * @return the time stamp from file
     */
    private long getTimeStampFromFile(String fileName) {
        Pattern p = Pattern.compile("\\w(\\d+)\\.(\\d+)-(\\d+)-(\\d+)-(\\d+)_.+");
        Matcher m = p.matcher(fileName);
        if (m.find()) {
            String value = m.group(1) + '-' + m.group(4); // Using end date as a reference
            try {
                DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyyMMdd-HHmm");
                DateTime dateTime = dtf.parseDateTime(value);
                return dateTime.getMillis();
            } catch (Exception e) {
                log().warn("getTimeStampFromFile: malformed 3GPP file " + fileName + ", because " + e.getMessage());
                return 0;
            }
        }
        return 0;
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
