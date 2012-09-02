/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.LogUtils;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.poller.PollerConfiguration;

/**
 * This is the singleton class used to load the configuration for the OpenNMS
 * Poller service from the poller-configuration xml file.
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
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public PollerConfigFactory(final long currentVersion, final InputStream stream, final String localServer, final boolean verifyServer) throws MarshalException, ValidationException {
        super(stream, localServer, verifyServer);
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

        OpennmsServerConfigFactory.init();
        OpennmsServerConfigFactory onmsSvrConfig = OpennmsServerConfigFactory.getInstance();

        final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONFIG_FILE_NAME);

        LogUtils.debugf(PollerConfigFactory.class, "init: config file path: %s", cfgFile.getPath());

        InputStream stream = null;
        PollerConfigFactory config = null;
        try {
            stream = new FileInputStream(cfgFile);
            config = new PollerConfigFactory(cfgFile.lastModified(), stream, onmsSvrConfig.getServerName(), onmsSvrConfig.verifyServer());
        } finally {
            IOUtils.closeQuietly(stream);
        }

        for (final org.opennms.netmgt.config.poller.Package pollerPackage : config.getConfiguration().getPackageCollection()) {
            for (final org.opennms.netmgt.config.poller.Service service : pollerPackage.getServiceCollection()) {
                for (final org.opennms.netmgt.config.poller.Parameter parm : service.getParameterCollection()) {
                    if (parm.getKey().equals("ds-name")) {
                        if (parm.getValue().length() > ConfigFileConstants.RRD_DS_MAX_SIZE) {
                            throw new ValidationException(
                                String.format("ds-name '%s' in service '%s' (poller package '%s') is greater than %d characters",
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
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static synchronized void reload() throws IOException, MarshalException, ValidationException {
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
    protected void saveXml(final String xml) throws IOException {
        if (xml != null) {
            getWriteLock().lock();
            try {
                final long timestamp = System.currentTimeMillis();
                final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONFIG_FILE_NAME);
                LogUtils.debugf(this, "saveXml: saving config file at %d: %s", timestamp, cfgFile.getPath());
                final Writer fileWriter = new OutputStreamWriter(new FileOutputStream(cfgFile), "UTF-8");
                fileWriter.write(xml);
                fileWriter.flush();
                fileWriter.close();
                LogUtils.debugf(this, "saveXml: finished saving config file: %s", cfgFile.getPath());
            } finally {
                getWriteLock().unlock();
            }
        }
    }

    /**
     * <p>update</p>
     *
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public void update() throws IOException, MarshalException, ValidationException {
        getWriteLock().lock();
        try {
            final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONFIG_FILE_NAME);
            if (cfgFile.lastModified() > m_currentVersion) {
                m_currentVersion = cfgFile.lastModified();
                LogUtils.debugf(this, "init: config file path: %s", cfgFile.getPath());
                InputStream stream = null;
                try {
                    stream = new FileInputStream(cfgFile);
                    m_config = CastorUtils.unmarshal(PollerConfiguration.class, stream);
                } finally {
                    IOUtils.closeQuietly(stream);
                }
                init();
                LogUtils.debugf(this, "init: finished loading config file: %s", cfgFile.getPath());
            }
        } finally {
            getWriteLock().unlock();
        }
    }
}
