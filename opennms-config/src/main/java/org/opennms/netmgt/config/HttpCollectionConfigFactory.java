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
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.httpdatacollection.HttpCollection;
import org.opennms.netmgt.config.httpdatacollection.HttpDatacollectionConfig;
import org.opennms.netmgt.dao.api.HttpCollectionConfigDao;
import org.opennms.netmgt.rrd.RrdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Access to the HTTP collector configuration.
 *
 * <p>The configuration is stored in Config Management and read through the
 * {@link HttpCollectionConfigDao} of the core's DAO context, which is looked up lazily on first use.
 * Changes made through Config Management are picked up by the DAO, so there is nothing to reload here.</p>
 *
 * <p>Only the core side resolves collections. On Minion, the {@code HttpCollector} receives the
 * already resolved {@link HttpCollection} as a runtime attribute and never calls this factory.</p>
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class HttpCollectionConfigFactory {
    private static final Logger LOG = LoggerFactory.getLogger(HttpCollectionConfigFactory.class);

    /** The singleton instance. */
    private static HttpCollectionConfigFactory m_instance;

    /** The DAO the configuration is read from; resolved from the DAO context unless set explicitly. */
    private static volatile HttpCollectionConfigDao s_configDao;

    /** Boolean indicating if the init() method has been called. */
    protected boolean initialized = false;

    protected HttpCollectionConfigFactory() {
    }

    /**
     * Creates a factory backed by the configuration in the given file instead of Config Management.
     * Used by tests only.
     *
     * @param configFile path to an http-datacollection-config.xml file
     * @throws java.io.IOException if the file cannot be read
     */
    public HttpCollectionConfigFactory(final String configFile) throws IOException {
        try (InputStream is = new FileInputStream(configFile)) {
            initialize(is);
        }
    }

    /**
     * Creates a factory backed by the configuration in the given stream instead of Config Management.
     * Used by tests only.
     *
     * @param stream an http-datacollection-config.xml document
     * @throws IOException if the stream cannot be read
     */
    public HttpCollectionConfigFactory(final InputStream stream) throws IOException {
        initialize(stream);
    }

    private void initialize(final InputStream stream) throws IOException {
        LOG.debug("initialize: initializing http collection config factory from a stream.");
        try (InputStreamReader isr = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            setConfig(JaxbUtils.unmarshal(HttpDatacollectionConfig.class, isr));
        }
    }

    /**
     * Be sure to call this method before calling getInstance(). It does not access the configuration,
     * so it is safe to call where Config Management is not available (e.g. on Minion).
     */
    public static synchronized void init() {
        if (m_instance == null) {
            m_instance = new HttpCollectionConfigFactory();
        }
    }

    /**
     * Singleton static call to get the only instance that should exist
     *
     * @return the single factory instance
     * @throws java.lang.IllegalStateException
     *             if init has not been called
     */
    public static synchronized HttpCollectionConfigFactory getInstance() {
        if (m_instance == null) {
            throw new IllegalStateException("You must call HttpCollectionConfigFactory.init() before calling getInstance().");
        }
        return m_instance;
    }

    /**
     * <p>setInstance</p>
     *
     * @param instance a {@link org.opennms.netmgt.config.HttpCollectionConfigFactory} object.
     */
    public static synchronized void setInstance(final HttpCollectionConfigFactory instance) {
        m_instance = instance;
    }

    /**
     * Sets the DAO the configuration is read from. Passing {@code null} makes the factory look up the
     * DAO from the DAO context again on next use.
     */
    public static void setConfigDao(final HttpCollectionConfigDao configDao) {
        s_configDao = configDao;
    }

    private static HttpCollectionConfigDao getConfigDao() {
        if (s_configDao == null) {
            synchronized (HttpCollectionConfigFactory.class) {
                if (s_configDao == null) {
                    s_configDao = BeanUtils.getBean("daoContext", "httpCollectionConfigDao", HttpCollectionConfigDao.class);
                }
            }
        }
        return s_configDao;
    }

    /**
     * <p>getConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.httpdatacollection.HttpDatacollectionConfig} object.
     */
    public static HttpDatacollectionConfig getConfig() {
        return getConfigDao().getConfig();
    }

    /**
     * Replaces the configuration with the given one, held in memory only. Used by tests only.
     *
     * @param config a {@link org.opennms.netmgt.config.httpdatacollection.HttpDatacollectionConfig} object.
     */
    public static void setConfig(final HttpDatacollectionConfig config) {
        setConfigDao(new InMemoryHttpCollectionConfigDao(config));
    }

    /**
     * <p>getHttpCollection</p>
     *
     * @param collectionName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.httpdatacollection.HttpCollection} object.
     */
    public HttpCollection getHttpCollection(final String collectionName) {
        for (final HttpCollection coll : getConfig().getHttpCollection()) {
            if (coll.getName().equalsIgnoreCase(collectionName)) {
                return coll;
            }
        }
        throw new IllegalArgumentException("getHttpCollection: collection name: "
                + collectionName + " specified in collectd configuration not found in http collection configuration.");
    }

    /**
     * <p>getRrdRepository</p>
     *
     * @param collectionName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.rrd.RrdRepository} object.
     */
    public RrdRepository getRrdRepository(final String collectionName) {
        final HttpCollection collection = getHttpCollection(collectionName);
        final RrdRepository repo = new RrdRepository();
        repo.setRrdBaseDir(new File(getRrdPath()));
        repo.setRraList(collection.getRrd().getRra());
        repo.setStep(collection.getRrd().getStep());
        repo.setHeartBeat((2 * collection.getRrd().getStep()));
        return repo;
    }

    /**
     * <p>getStep</p>
     *
     * @param cName a {@link java.lang.String} object.
     * @return a int.
     */
    public int getStep(final String cName) {
        return getHttpCollection(cName).getRrd().getStep();
    }

    /**
     * <p>getRRAList</p>
     *
     * @param cName a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public List<String> getRRAList(final String cName) {
        return getHttpCollection(cName).getRrd().getRra();
    }

    /**
     * <p>getRrdPath</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRrdPath() {
        String rrdPath = getConfig().getRrdRepository();
        if (rrdPath == null) {
            throw new RuntimeException("Configuration error, failed to "
                    + "retrieve path to RRD repository.");
        }

        /*
         * TODO: make a path utils class that has the below in it strip the
         * File.separator char off of the end of the path.
         */
        if (rrdPath.endsWith(File.separator)) {
            rrdPath = rrdPath.substring(0, (rrdPath.length() - File.separator.length()));
        }

        return rrdPath;
    }

    private static final class InMemoryHttpCollectionConfigDao implements HttpCollectionConfigDao {
        private volatile HttpDatacollectionConfig config;

        private InMemoryHttpCollectionConfigDao(final HttpDatacollectionConfig config) {
            this.config = Objects.requireNonNull(config);
        }

        @Override
        public HttpDatacollectionConfig getConfig() {
            return config;
        }

        @Override
        public void updateConfig(final HttpDatacollectionConfig config) {
            this.config = Objects.requireNonNull(config);
        }

        @Override
        public void replaceConfig(final HttpDatacollectionConfig config) {
            this.config = Objects.requireNonNull(config);
        }
    }
}
