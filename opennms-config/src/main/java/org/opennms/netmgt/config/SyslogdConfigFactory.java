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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.opennms.core.config.api.ConfigReloadContainer;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.syslogd.HideMatch;
import org.opennms.netmgt.config.syslogd.SyslogdConfiguration;
import org.opennms.netmgt.config.syslogd.SyslogdConfigurationGroup;
import org.opennms.netmgt.config.syslogd.UeiMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

/**
 * This is the class used to load the configuration for the OpenNMS
 * Syslogd from syslogd-configuration.xml.
 *
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj </a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class SyslogdConfigFactory implements SyslogdConfig {

    private static final Logger LOG = LoggerFactory.getLogger(SyslogdConfigFactory.class);

    /**
     * The config class loaded from the config file
     */
    private SyslogdConfiguration m_config;

    private ConfigReloadContainer<SyslogdConfigurationGroup> m_extContainer;

    /**
     * Private constructor
     *
     * @throws java.io.IOException Thrown if the specified config file cannot be read
     */
    public SyslogdConfigFactory() throws IOException {
        initExtensions();
        File configFile = ConfigFileConstants.getFile(ConfigFileConstants.SYSLOGD_CONFIG_FILE_NAME);
        m_config = JaxbUtils.unmarshal(SyslogdConfiguration.class, new FileSystemResource(configFile));
        parseIncludedFiles();
    }

    /**
     * <p>Constructor for SyslogdConfigFactory.</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     */
    public SyslogdConfigFactory(InputStream stream) throws IOException {
        initExtensions();
        try (final Reader reader = new InputStreamReader(stream)) {
            m_config = JaxbUtils.unmarshal(SyslogdConfiguration.class, reader);
        }
        parseIncludedFiles();
    }

    /**
     * Reload the config from the default config file
     *
     * @throws java.io.IOException Thrown if the specified config file cannot be
     *                             read/loaded
     */
    public synchronized void reload() throws IOException {
        File configFile = ConfigFileConstants.getFile(ConfigFileConstants.SYSLOGD_CONFIG_FILE_NAME);
        m_config = JaxbUtils.unmarshal(SyslogdConfiguration.class, new FileSystemResource(configFile));
        parseIncludedFiles();
    }

    /**
     * Return the port on which SNMP traps should be received.
     *
     * @return the port on which SNMP traps should be received
     */
    @Override
    public synchronized int getSyslogPort() {
        return m_config.getConfiguration().getSyslogPort();
    }

    /**
     * <p>getListenAddress</p>
     *
     * @return a {@link java.lang.String} object.
     * @since 1.8.1
     */
    @Override
    public synchronized String getListenAddress() {
        return m_config.getConfiguration().getListenAddress().orElse(null);
    }
    
    /**
     * Return whether or not a newSuspect event should be sent when a trap is
     * received from an unknown IP address.
     *
     * @return whether to generate newSuspect events on traps.
     */
    @Override
    public synchronized boolean getNewSuspectOnMessage() {
        return m_config.getConfiguration().getNewSuspectOnMessage();
    }

    /**
     * <p>getForwardingRegexp</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public synchronized String getForwardingRegexp() {
        return m_config.getConfiguration().getForwardingRegexp().orElse(null);
    }

    /**
     * <p>getMatchingGroupHost</p>
     *
     * @return a int.
     */
    @Override
    public synchronized Integer getMatchingGroupHost() {
        return m_config.getConfiguration().getMatchingGroupHost().orElse(null);

    }

    /**
     * <p>getMatchingGroupMessage</p>
     *
     * @return a int.
     */
    @Override
    public synchronized Integer getMatchingGroupMessage() {
        return m_config.getConfiguration().getMatchingGroupMessage().orElse(null);

    }

    /**
     * <p>getParser</p>
     *
     * @return the parser class to use when parsing syslog messages, as a string.
     */
    @Override
    public synchronized String getParser() {
        return m_config.getConfiguration().getParser();
    }

    @Override
    public synchronized List<UeiMatch> getUeiList() {
        return m_config.getUeiMatches();
    }

    @Override
    public synchronized List<HideMatch> getHideMessages() {
        return m_config.getHideMatches();
    }
    
    /**
     * <p>getDiscardUei</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public synchronized String getDiscardUei() {
        return m_config.getConfiguration().getDiscardUei();
    }

    @Override
    public int getNumThreads() {
        if (m_config.getConfiguration().getThreads().isPresent()) {
            return m_config.getConfiguration().getThreads().get();
        } else {
            return Runtime.getRuntime().availableProcessors() * 2;
        }
    }

    @Override
    public int getQueueSize() {
        return m_config.getConfiguration().getQueueSize();
    }

    @Override
    public int getBatchSize() {
        return m_config.getConfiguration().getBatchSize();
    }

    @Override
    public int getBatchIntervalMs() {
        return m_config.getConfiguration().getBatchInterval();
    }

    @Override
    public TimeZone getTimeZone() {
        return m_config.getConfiguration().getTimeZone().orElse(null);
    }
    
    @Override
    public boolean shouldIncludeRawSyslogmessage() {
        return m_config.getConfiguration().shouldIncludeRawSyslogmessage();
    }

    /**
     * Parse import-file tags and add all uei-matchs and hide-messages.
     * 
     * @throws IOException
     */
    private void parseIncludedFiles() throws IOException {
        final File configDir;
        try {
            configDir = ConfigFileConstants.getFile(ConfigFileConstants.SYSLOGD_CONFIG_FILE_NAME).getParentFile();
        } catch (final Throwable t) {
            LOG.warn("Error getting default syslogd configuration location. <import-file> directives will be ignored.  This should really only happen in unit tests.");
            return;
        }
        for (final String fileName : m_config.getImportFiles()) {
            final File configFile = new File(configDir, fileName);
            final SyslogdConfigurationGroup includeCfg = JaxbUtils.unmarshal(SyslogdConfigurationGroup.class, new FileSystemResource(configFile));
            if (includeCfg.getUeiMatches() != null) {
                for (final UeiMatch ueiMatch : includeCfg.getUeiMatches())  {
                    if (m_config.getUeiMatches() == null) {
                        m_config.setUeiMatches(new ArrayList<>());
                    }
                    m_config.addUeiMatch(ueiMatch);
                }
            }
            if (includeCfg.getHideMatches() != null) {
                for (final HideMatch hideMatch : includeCfg.getHideMatches()) {
                    if (m_config.getHideMatches() == null) {
                        m_config.setHideMatches(new ArrayList<>());
                    }
                    m_config.addHideMatch(hideMatch);
                }
            }
        }

        // Insert UEI matches exposed via the service registry
        SyslogdConfigurationGroup extGroup = m_extContainer.getObject();
        if (extGroup != null) {
            m_config.getUeiMatches().addAll(0, extGroup.getUeiMatches());
            m_config.getHideMatches().addAll(0, extGroup.getHideMatches());
            if (LOG.isDebugEnabled()) {
                LOG.debug("UEI matches with the following UEIs are contributed by one or more extensions: {}", extGroup.getUeiMatches().stream()
                        .map(UeiMatch::getUei)
                        .collect(Collectors.joining(",")));
            }
        }
    }

    private void initExtensions() {
        m_extContainer = new ConfigReloadContainer.Builder<>(SyslogdConfigurationGroup.class)
                .withFolder((accumulator, next) -> {
                    accumulator.getUeiMatches().addAll(next.getUeiMatches());
                    accumulator.getHideMatches().addAll(next.getHideMatches());
                })
                .build();
    }
}
