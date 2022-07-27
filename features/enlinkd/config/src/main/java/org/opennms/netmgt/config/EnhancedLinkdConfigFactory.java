/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.enlinkd.EnlinkdConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the singleton class used to load the configuration for the OpenNMS
 * enhanced linkd service from the enlinkd-configuration xml file.
 *
 *
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class EnhancedLinkdConfigFactory extends EnhancedLinkdConfigManager {
    private static final Logger LOG = LoggerFactory.getLogger(EnhancedLinkdConfigFactory.class);
    
    public EnhancedLinkdConfigFactory() throws IOException {
        reload();
    }

    /**
     * <p>Constructor for LinkdConfigFactory.</p>
     *
     * @param currentVersion a long.
     * @param stream a {@link java.io.InputStream} object.
     * @throws java.io.IOException if any.
     */
    public EnhancedLinkdConfigFactory(final InputStream stream) throws IOException {
        reloadXML(stream);
    }

    /** {@inheritDoc} */
    protected synchronized void saveXml(String xml) throws IOException {
        if (xml != null) {
            long timestamp = System.currentTimeMillis();
            final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.ENLINKD_CONFIG_FILE_NAME);
            LOG.debug("saveXml: saving config file at {}: {}", timestamp, cfgFile.getPath());
            final Writer fileWriter = new OutputStreamWriter(new FileOutputStream(cfgFile), StandardCharsets.UTF_8);
            fileWriter.write(xml);
            fileWriter.flush();
            fileWriter.close();
            LOG.debug("saveXml: finished saving config file: {}", cfgFile.getPath());
        }
    }

    /**
     * <p>reload</p>
     *
     * @throws java.io.IOException if any.
     */
    public void reload() throws IOException {
        getWriteLock().lock();
        try {
            final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.ENLINKD_CONFIG_FILE_NAME);
           LOG.debug("init: config file path: {}", cfgFile.getPath());
            InputStream stream = null;
            try {
                stream = new FileInputStream(cfgFile);
                reloadXML(stream);
            } finally {
                if (stream != null) {
                    IOUtils.closeQuietly(stream);
                }
            }
            LOG.debug("init: finished loading config file: {}", cfgFile.getPath());
        } finally {
            getWriteLock().unlock();
        }
    }
        
    /**
     * <p>reloadXML</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @throws java.io.IOException if any.
     */
    protected void reloadXML(final InputStream stream) throws IOException {
        getWriteLock().lock();
        try(final Reader reader = new InputStreamReader(stream)) {
            m_config = JaxbUtils.unmarshal(EnlinkdConfiguration.class, reader);
        } finally {
            getWriteLock().unlock();
        }
    }

    /**
     * Saves the current in-memory configuration to disk
     *
     * @throws java.io.IOException if any.
     */
    public void save() throws IOException {
        getWriteLock().lock();
        
        try {
            // marshall to a string first, then write the string to the file. This
            // way the original config isn't lost if the xml from the marshall is hosed.
            final StringWriter stringWriter = new StringWriter();
            JaxbUtils.marshal(m_config, stringWriter);
            saveXml(stringWriter.toString());        
        } finally {
            getWriteLock().unlock();
        }
    }
}
