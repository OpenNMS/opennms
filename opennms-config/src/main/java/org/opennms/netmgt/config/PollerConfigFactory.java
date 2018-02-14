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
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.poller.PollerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the singleton class used to load the configuration for the OpenNMS
 * Poller service from the poller-configuration XML file.
 *
 * A mapping of the configured URLs to the iplist they contain is built at
 * init() time so as to avoid numerous file reads.
 *
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 *
 * @author <a href="mailto:jamesz@opennms.com">James Zuo </a>
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class PollerConfigFactory extends PollerConfigManager {
    private static final Logger LOG = LoggerFactory.getLogger(PollerConfigFactory.class);
    /**
     * The singleton instance of this factory
     */
    private static PollerConfig m_singleton = null;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;
    
    /**
     * Loaded version
     */
    private long m_currentVersion = -1L;

    /**
     * <p>Constructor for PollerConfigFactory.</p>
     *
     * @param currentVersion a long.
     * @param stream a {@link java.io.InputStream} object.
     * @param localServer a {@link java.lang.String} object.
     * @param verifyServer a boolean.
     */
    public PollerConfigFactory(final long currentVersion, final InputStream stream, final String localServer, final boolean verifyServer) {
        super(stream, localServer, verifyServer);
        m_currentVersion = currentVersion;
    }

    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @throws java.io.IOException if any.
     */
    public static synchronized void init() throws IOException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        try {
            OpennmsServerConfigFactory.init();
        } catch (final Exception e) {
            LOG.warn("Error while initializing OpennmsServerConfigFactory.", e);
        }
        OpennmsServerConfigFactory onmsSvrConfig = OpennmsServerConfigFactory.getInstance();

        final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONFIG_FILE_NAME);

        LOG.debug("init: config file path: {}", cfgFile.getPath());

        InputStream stream = null;
        PollerConfigFactory config = null;
        try {
            stream = new FileInputStream(cfgFile);
            config = new PollerConfigFactory(cfgFile.lastModified(), stream, onmsSvrConfig.getServerName(), onmsSvrConfig.verifyServer());
        } finally {
            IOUtils.closeQuietly(stream);
        }

        for (final org.opennms.netmgt.config.poller.Package pollerPackage : config.getConfiguration().getPackages()) {
            for (final org.opennms.netmgt.config.poller.Service service : pollerPackage.getServices()) {
                for (final org.opennms.netmgt.config.poller.Parameter parm : service.getParameters()) {
                    if (parm.getKey().equals("ds-name")) {
                        if (parm.getValue().length() > ConfigFileConstants.RRD_DS_MAX_SIZE) {
                            throw new IllegalStateException(String.format("ds-name '%s' in service '%s' (poller package '%s') is greater than %d characters",
                                parm.getValue(), service.getName(), pollerPackage.getName(), ConfigFileConstants.RRD_DS_MAX_SIZE)
                            );
                        }
                    }
                }
            }
        }

        setInstance(config);
    }

    /**
     * Reload the config from the default config file
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read/loaded
     * @throws java.io.IOException if any.
     */
    public static synchronized void reload() throws IOException {
        init();
        getInstance().update();
    }

    /**
     * Return the singleton instance of this factory.
     *
     * @return The current factory instance.
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized PollerConfig getInstance() {
        if (!m_loaded) {
            throw new IllegalStateException("The factory has not been initialized");
        }

        return m_singleton;
    }
    
    /**
     * <p>setInstance</p>
     *
     * @param instance a {@link org.opennms.netmgt.config.PollerConfig} object.
     */
    public static synchronized void setInstance(final PollerConfig instance) {
        m_singleton = instance;
        m_loaded = true;
    }

    /** {@inheritDoc} */
    @Override
    protected void saveXml(final String xml) throws IOException {
        if (xml != null) {
            getWriteLock().lock();
            try {
                final long timestamp = System.currentTimeMillis();
                final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONFIG_FILE_NAME);
                LOG.debug("saveXml: saving config file at {}: {}", timestamp, cfgFile.getPath());
                final Writer fileWriter = new OutputStreamWriter(new FileOutputStream(cfgFile), StandardCharsets.UTF_8);
                fileWriter.write(xml);
                fileWriter.flush();
                fileWriter.close();
                LOG.debug("saveXml: finished saving config file: {}", cfgFile.getPath());
            } finally {
                getWriteLock().unlock();
            }
        }
    }

    /**
     * <p>update</p>
     *
     * @throws java.io.IOException if any.
     */
    @Override
    public void update() throws IOException {
        getWriteLock().lock();
        try {
            final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONFIG_FILE_NAME);
            if (cfgFile.lastModified() > m_currentVersion) {
                m_currentVersion = cfgFile.lastModified();
                LOG.debug("init: config file path: {}", cfgFile.getPath());
                InputStream stream = null;
                InputStreamReader sr = null;
                try {
                    stream = new FileInputStream(cfgFile);
                    sr = new InputStreamReader(stream);
                    m_config = JaxbUtils.unmarshal(PollerConfiguration.class, sr);
                } finally {
                    IOUtils.closeQuietly(sr);
                    IOUtils.closeQuietly(stream);
                }
                init();
                LOG.debug("init: finished loading config file: {}", cfgFile.getPath());
            }
        } finally {
            getWriteLock().unlock();
        }
    }
}
