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
            try (final InputStream stream = new FileInputStream(cfgFile)){
                try(final Reader reader = new InputStreamReader(stream)) {
                    m_config = JaxbUtils.unmarshal(EnlinkdConfiguration.class, reader);
                } finally {
                    IOUtils.close(stream);
                }
            }
            LOG.debug("init: finished loading config file: {}", cfgFile.getPath());
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
