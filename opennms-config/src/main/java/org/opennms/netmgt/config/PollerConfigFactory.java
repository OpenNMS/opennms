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
import org.opennms.netmgt.filter.FilterDaoFactory;
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
     * Poller config file
     */
    private static File m_pollerConfigFile;

    /**
     * <p>Constructor for PollerConfigFactory.</p>
     *
     * @param currentVersion a long.
     * @param stream a {@link java.io.InputStream} object.
     */
    public PollerConfigFactory(final long currentVersion, final InputStream stream) {
        super(stream);
        m_currentVersion = currentVersion;
    }

    private static File getPollerConfigFile() throws IOException {
        if (m_pollerConfigFile == null) {
            m_pollerConfigFile = ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONFIG_FILE_NAME);
        }
        return m_pollerConfigFile;
    }

    public static void setPollerConfigFile(final File pollerConfigFile) throws IOException {
        m_pollerConfigFile = pollerConfigFile;
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

        final File cfgFile = getPollerConfigFile();

        LOG.debug("init: config file path: {}", cfgFile.getPath());

        InputStream stream = null;
        PollerConfigFactory config = null;
        try {
            stream = new FileInputStream(cfgFile);
            config = new PollerConfigFactory(cfgFile.lastModified(), stream);
        } finally {
            IOUtils.closeQuietly(stream);
        }

        validate(config.getLocalConfiguration());

        setInstance(config);
    }

    private static void validate(PollerConfiguration config) {
        for (final org.opennms.netmgt.config.poller.Package pollerPackage : config.getPackages()) {
            FilterDaoFactory.getInstance().validateRule(pollerPackage.getFilter().getContent());
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
                final File cfgFile = getPollerConfigFile();
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
            final File cfgFile = getPollerConfigFile();
            if (cfgFile.lastModified() > m_currentVersion) {
                m_currentVersion = cfgFile.lastModified();
                LOG.debug("init: config file path: {}", cfgFile.getPath());
                InputStream stream = null;
                InputStreamReader sr = null;
                try {
                    stream = new FileInputStream(cfgFile);
                    sr = new InputStreamReader(stream);
                    final PollerConfiguration config = JaxbUtils.unmarshal(PollerConfiguration.class, sr);

                    validate(config);

                    m_config = config;
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
        super.update();
    }
}
