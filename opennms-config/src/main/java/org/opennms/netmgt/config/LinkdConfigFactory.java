/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ConfigFileConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.linkd.LinkdConfiguration;

/**
 * This is the singleton class used to load the configuration for the OpenNMS
 * linkd service from the linkd-configuration xml file.
 *
 * A mapping of the configured URLs to the iplist they contain is built at
 * init() time so as to avoid numerous file reads.
 *
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:jamesz@opennms.com">James Zuo </a>
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class LinkdConfigFactory extends LinkdConfigManager {
    private static final Logger LOG = LoggerFactory.getLogger(LinkdConfigFactory.class);
    /**
     * The singleton instance of this factory
     */
    private static LinkdConfig m_singleton = null;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;
    
    /**
     * Loaded version
     */
    private long m_currentVersion = -1L;

    /**
     * <p>Constructor for LinkdConfigFactory.</p>
     *
     * @param currentVersion a long.
     * @param stream a {@link java.io.InputStream} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public LinkdConfigFactory(final long currentVersion, final InputStream stream) throws MarshalException, ValidationException, IOException {
        reloadXML(stream);
        m_currentVersion = currentVersion;
    }

    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static synchronized void init() throws IOException, MarshalException, ValidationException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.LINKD_CONFIG_FILE_NAME);
        LOG.debug("init: config file path: {}", cfgFile.getPath());

        InputStream stream = null;
        try {
            stream = new FileInputStream(cfgFile);
            setInstance(new LinkdConfigFactory(cfgFile.lastModified(), stream));
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected synchronized void saveXml(String xml) throws IOException {
        if (xml != null) {
            long timestamp = System.currentTimeMillis();
            final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.LINKD_CONFIG_FILE_NAME);
            LOG.debug("saveXml: saving config file at {}: {}", timestamp, cfgFile.getPath());
            final Writer fileWriter = new OutputStreamWriter(new FileOutputStream(cfgFile), "UTF-8");
            fileWriter.write(xml);
            fileWriter.flush();
            fileWriter.close();
            LOG.debug("saveXml: finished saving config file: {}", cfgFile.getPath());
        }
    }

    /**
     * Return the singleton instance of this factory.
     *
     * @return The current factory instance.
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized LinkdConfig getInstance() {
        if (!m_loaded) {
            throw new IllegalStateException("The factory has not been initialized");
        }

        return m_singleton;
    }
    
    /**
     * <p>setInstance</p>
     *
     * @param instance a {@link org.opennms.netmgt.config.LinkdConfig} object.
     */
    public static synchronized void setInstance(final LinkdConfig instance) {
        m_singleton = instance;
        m_loaded = true;
    }

    /**
     * <p>reload</p>
     *
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    @Override
    public void reload() throws IOException, MarshalException, ValidationException {
        getWriteLock().lock();
        try {
            final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.LINKD_CONFIG_FILE_NAME);
            if (cfgFile.lastModified() > m_currentVersion) {
                m_currentVersion = cfgFile.lastModified();
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
            }
        } finally {
            getWriteLock().unlock();
        }
    }
    
    /**
     * <p>update</p>
     *
     */
    @Override
    public void update() {
        getWriteLock().lock();
        try {
            updateUrlIpMap();
            updatePackageIpListMap();
        } finally {
            getWriteLock().unlock();
        }
    }
    
    /**
     * <p>reloadXML</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    protected void reloadXML(final InputStream stream) throws MarshalException, ValidationException, IOException {
        getWriteLock().lock();
        try {
            m_config = CastorUtils.unmarshal(LinkdConfiguration.class, stream);
            updateUrlIpMap();
            updatePackageIpListMap();
            updateVlanClassNames();
            updateIpRouteClassNames();
        } finally {
            getWriteLock().unlock();
        }
    }
    /**
     * Saves the current in-memory configuration to disk
     *
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    @Override
    public void save() throws MarshalException, IOException, ValidationException {
        getWriteLock().lock();
        
        try {
            // marshall to a string first, then write the string to the file. This
            // way the original config isn't lost if the xml from the marshall is hosed.
            final StringWriter stringWriter = new StringWriter();
            Marshaller.marshal(m_config, stringWriter);
            saveXml(stringWriter.toString());        
        } finally {
            getWriteLock().unlock();
        }
    }
    
    /**
     * <p>reloadXML</p>
     *
     * @param reader a {@link java.io.Reader} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    @Deprecated
    protected void reloadXML(final Reader reader) throws MarshalException, ValidationException, IOException {
        getWriteLock().lock();
        try {
            m_config = CastorUtils.unmarshal(LinkdConfiguration.class, reader);
            updateUrlIpMap();
            updatePackageIpListMap();
            updateVlanClassNames();
            updateIpRouteClassNames();
        } finally {
            getWriteLock().unlock();
        }
    }

}
