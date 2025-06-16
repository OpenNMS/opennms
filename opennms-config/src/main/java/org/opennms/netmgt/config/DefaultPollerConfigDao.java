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
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * <p>DefaultPollerConfigDao class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class DefaultPollerConfigDao implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultPollerConfigDao.class);
    private Resource m_configResource;

    private PollerConfig m_pollerConfig;

    /**
     * <p>Constructor for DefaultPollerConfigDao.</p>
     */
    public DefaultPollerConfigDao() {
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_configResource != null, "property configResource must be set to a non-null value");

        loadConfig();
    }

    private void loadConfig() throws Exception {
        InputStream stream = null;
        long lastModified;

        File file = null;
        try {
            file = getConfigResource().getFile();
        } catch (IOException e) {
            LOG.info("Resource '{}' does not seem to have an underlying File object; using input stream", getConfigResource());
        }

        try {
            if (file != null) {
                lastModified = file.lastModified();
                stream = new FileInputStream(file);
                LOG.debug("loadConfig: creating new PollerConfigFactory from file path: {}", file.getPath());
            } else {
                lastModified = System.currentTimeMillis();
                stream = getConfigResource().getInputStream();
                LOG.debug("loadConfig: creating new PollerConfigFactory from input stream");
            }

            setPollerConfig(new PollerConfigFactory(lastModified, stream));
        } finally {
            if (stream != null) stream.close();
        }
    }

    /**
     * <p>getPollerConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.PollerConfig} object.
     */
    public PollerConfig getPollerConfig() {
        return m_pollerConfig;
    }

    private void setPollerConfig(PollerConfig pollerConfig) {
        m_pollerConfig = pollerConfig;
    }

    /**
     * <p>getConfigResource</p>
     *
     * @return a {@link org.springframework.core.io.Resource} object.
     */
    public Resource getConfigResource() {
        return m_configResource;
    }

    /**
     * <p>setConfigResource</p>
     *
     * @param configResource a {@link org.springframework.core.io.Resource} object.
     */
    public void setConfigResource(Resource configResource) {
        m_configResource = configResource;
    }

}