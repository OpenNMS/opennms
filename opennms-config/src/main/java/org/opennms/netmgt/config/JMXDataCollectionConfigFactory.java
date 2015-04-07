/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
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

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.collectd.jmx.Attrib;
import org.opennms.netmgt.config.collectd.jmx.CompAttrib;
import org.opennms.netmgt.config.collectd.jmx.CompMember;
import org.opennms.netmgt.config.collectd.jmx.JmxCollection;
import org.opennms.netmgt.config.collectd.jmx.JmxDatacollectionConfig;
import org.opennms.netmgt.config.collectd.jmx.Mbean;
import org.opennms.netmgt.rrd.RrdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is the main repository for JMX data collection configuration
 * information used by the an instance of the JMX service monitor. When this class is loaded it
 * reads the jmx data collection configuration into memory.
 * <p/>
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 *
 * @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public final class JMXDataCollectionConfigFactory {
    private static final Logger LOG = LoggerFactory.getLogger(JMXDataCollectionConfigFactory.class);
    /**
     * The singleton instance of this factory
     */
    private static JMXDataCollectionConfigFactory m_singleton = null;

    /**
     * The config class loaded from the config file
     */
    private JmxDatacollectionConfig m_config;


    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;

    /**
     * Map of JmxCollection objects indexed by data collection name
     */
    private Map<String, JmxCollection> m_collectionMap;

    /**
     * <p>Constructor for JMXDataCollectionConfigFactory.</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     */
    public JMXDataCollectionConfigFactory(InputStream stream) {
        initialize(new InputStreamResource(stream));
    }

    /**
     * Private constructor
     *
     * @throws java.io.IOException Thrown if the specified config file cannot be read
     */
    private JMXDataCollectionConfigFactory(String configFile) throws IOException {
        initialize(new FileSystemResource(configFile));
    }

    private void initialize(Resource resource) {
        JMXDataCollectionConfigDao dao = new JMXDataCollectionConfigDao();
        dao.setConfigResource(resource);
        dao.afterPropertiesSet();
        m_config = dao.getConfig();
        buildCollectionMap();
    }

    /**
     * Build collection map which is a hash map of Collection
     * objects indexed by collection name...also build
     * collection group map which is a hash map indexed
     * by collection name with a hash map as the value
     * containing a map of the collections's group names
     * to the Group object containing all the information
     * for that group. So the associations are:
     * <p/>
     * CollectionMap
     * collectionName -> Collection
     * <p/>
     * CollectionGroupMap
     * collectionName -> groupMap
     * <p/>
     * GroupMap
     * groupMapName -> Group
     * <p/>
     * This is parsed and built at initialization for
     * faster processing at run-time.
     */
    private void buildCollectionMap() {

        m_collectionMap = new HashMap<>();

        // Map of group maps indexed by SNMP collection name.
        // TODO: This appears to be unused
        Map<String, Map<String, Mbean>> collectionGroupMap = new HashMap<>();

        // BOZO isn't the collection name defined in the jmx-datacollection.xml file and
        // global to all the mbeans?
        for (JmxCollection collection : m_config.getJmxCollectionList()) {

            // Build group map for this collection
            Map<String, Mbean> groupMap = new HashMap<String, Mbean>();
            for (Mbean mbean : collection.getMbeans()) {
                groupMap.put(mbean.getName(), mbean);
            }

            collectionGroupMap.put(collection.getName(), groupMap);
            m_collectionMap.put(collection.getName(), collection);
        }
    }

    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     *
     * @throws java.io.IOException Thrown if the specified config file cannot be read
     */
    public static synchronized void init() throws IOException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }


        try {
            File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.JMX_DATA_COLLECTION_CONF_FILE_NAME);

            LOG.debug("init: config file path: {}", cfgFile.getPath());
            m_singleton = new JMXDataCollectionConfigFactory(cfgFile.getPath());
        } catch (IOException ioe) {
            LOG.error("Unable to open JMX data collection config file", ioe);
            throw ioe;
        }

        m_loaded = true;
    }

    /**
     * Reload the config from the default config file
     *
     * @throws java.io.IOException Thrown if the specified config file cannot be read/loaded
     */
    public static synchronized void reload() throws IOException {
        m_singleton = null;
        m_loaded = false;

        init();
    }

    /**
     * Return the singleton instance of this factory.
     *
     * @return The current factory instance.
     * @throws java.lang.IllegalStateException Thrown if the factory has not yet been initialized.
     */
    public static synchronized JMXDataCollectionConfigFactory getInstance() {
        if (!m_loaded)
            throw new IllegalStateException("The factory has not been initialized");

        return m_singleton;
    }

    /**
     * <p>setInstance</p>
     *
     * @param singleton a {@link org.opennms.netmgt.config.JMXDataCollectionConfigFactory} object.
     */
    public static synchronized void setInstance(JMXDataCollectionConfigFactory singleton) {
        m_singleton = singleton;
        m_loaded = true;
    }


    /**
     * This method returns the list of MIB objects associated with a particular
     * system object id, IP address, and ifType for the specified collection.
     *
     * @param cName     name of the data collection from which to retrieve oid
     *                  information.
     * @param aSysoid   system object id to look up in the collection
     * @param anAddress IP address to look up in the collection
     * @return a list of MIB objects
     */
    public Map<String, List<Attrib>> getAttributeMap(String cName, String aSysoid, String anAddress) {

        Map<String, List<Attrib>> attributeMap = new HashMap<String, List<Attrib>>();


        LOG.debug("getMibObjectList: collection: {} sysoid: {} address: {}", anAddress, cName, aSysoid);

        if (aSysoid == null) {

            LOG.debug("getMibObjectList: aSysoid parameter is NULL...");
            return attributeMap;
        }

        // Retrieve the appropriate Collection object
        // 
        JmxCollection collection = m_collectionMap.get(cName);
        if (collection == null) {
            return attributeMap;
        }

        for(Mbean mbean : collection.getMbeans()) {
            // Make sure to create a new ArrayList because we add to it below
            List<Attrib> list = new ArrayList<Attrib>(mbean.getAttribList());

            for(CompAttrib compAttrib : mbean.getCompAttribList()) {
                for (CompMember compMember : compAttrib.getCompMemberList()) {
                    Attrib attribWrapper = new Attrib();
                    attribWrapper.setName(compAttrib.getName() + "|" + compMember.getName());
                    attribWrapper.setAlias(compMember.getAlias());
                    attribWrapper.setType(compMember.getType());
                    list.add(attribWrapper);
                }
            }
            attributeMap.put(mbean.getObjectname(), list);
        }
        return attributeMap;
    }

    public JmxCollection getJmxCollection(String collectionName) {
        JmxCollection collection = m_collectionMap.get(collectionName);
        if (collection != null) {
            // we clone the collection by marshal/unmarshalling the object :)
            StringWriter out = new StringWriter();
            JaxbUtils.marshal(collection, out);
            StringReader in = new StringReader(out.toString());
            JmxCollection clonedCollection = JaxbUtils.unmarshal(JmxCollection.class, in);
            return clonedCollection;
        } else {
            LOG.warn("No JMX Config for collection '{}' found", collectionName);
        }
        return null;
    }

    /**
     * <p>getMBeanInfo</p>
     *
     * @param cName a {@link java.lang.String} object.
     * @return a {@link java.util.Map} object.
     */
    public Map<String, BeanInfo> getMBeanInfo(String cName) {
        Map<String, BeanInfo> map = new HashMap<String, BeanInfo>();

        // Retrieve the appropriate Collection object
        // 
        JmxCollection collection = m_collectionMap.get(cName);

        if (collection == null) {
            LOG.warn("no collection named '{}' was found", cName);
        } else {
            for (Mbean mbean : collection.getMbeans()) {
                BeanInfo beanInfo = new BeanInfo();
                beanInfo.setMbeanName(mbean.getName());
                beanInfo.setObjectName(mbean.getObjectname());
                beanInfo.setKeyField(mbean.getKeyfield());
                beanInfo.setExcludes(mbean.getExclude());
                beanInfo.setKeyAlias(mbean.getKeyAlias());

                List<String> attribNameList = new ArrayList<String>();
                List<String> compAttribNameList = new ArrayList<String>();

                for (CompAttrib myCa : mbean.getCompAttribList()) {
                    for (CompMember myCm : myCa.getCompMemberList()) {
                        attribNameList.add(myCa.getName() + "|" + myCm.getName());
                        compAttribNameList.add(myCa.getName() + "|" + myCm.getName());
                    }
                }

                for (Attrib myA : mbean.getAttribList()) {
                    attribNameList.add(myA.getName());
                }

                beanInfo.setAttributes(attribNameList);
                beanInfo.setCompositeAttributes(compAttribNameList);
                map.put(mbean.getObjectname(), beanInfo);
            }
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * Retrieves configured RRD step size.
     *
     * @param cName Name of the data collection
     * @return RRD step size for the specified collection
     */
    public int getStep(String cName) {
        JmxCollection collection = m_collectionMap.get(cName);
        if (collection != null)
            return collection.getRrd().getStep();
        else
            return -1;
    }

    /**
     * Retrieves configured list of RoundRobin Archive statements.
     *
     * @param cName Name of the data collection
     * @return list of RRA strings.
     */
    public List<String> getRRAList(String cName) {
        JmxCollection collection = m_collectionMap.get(cName);
        if (collection != null)
            return collection.getRrd().getRraCollection();
        else
            return null;

    }

    /**
     * Retrieves the configured path to the RRD file repository.
     *
     * @param collectionName a {@link java.lang.String} object.
     * @return RRD repository path.
     */
    public RrdRepository getRrdRepository(String collectionName) {
        RrdRepository repo = new RrdRepository();
        repo.setRrdBaseDir(new File(getRrdPath()));
        repo.setRraList(getRRAList(collectionName));
        repo.setStep(getStep(collectionName));
        repo.setHeartBeat((2 * getStep(collectionName)));
        return repo;
        //return m_config.getRrdRepository();
    }

    /**
     * <p>getRrdPath</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRrdPath() {
        String rrdPath = m_config.getRrdRepository();
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
}
