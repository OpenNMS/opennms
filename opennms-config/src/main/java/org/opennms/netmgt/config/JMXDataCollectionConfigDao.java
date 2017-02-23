/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.opennms.core.xml.AbstractMergingJaxbConfigDao;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.collectd.jmx.Attrib;
import org.opennms.netmgt.config.collectd.jmx.CompAttrib;
import org.opennms.netmgt.config.collectd.jmx.CompMember;
import org.opennms.netmgt.config.collectd.jmx.JmxCollection;
import org.opennms.netmgt.config.collectd.jmx.JmxDatacollectionConfig;
import org.opennms.netmgt.config.collectd.jmx.Mbean;
import org.opennms.netmgt.config.collectd.jmx.Mbeans;
import org.opennms.netmgt.rrd.RrdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

/**
 * JAXB Based JMX Data Collection Config DAO
 *
 * @author <a href="mailto:jesse@opennms.org">Jesse White</a>
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class JMXDataCollectionConfigDao extends AbstractMergingJaxbConfigDao<JmxDatacollectionConfig, JmxDatacollectionConfig> {

    public static final Logger LOG = LoggerFactory.getLogger(JMXDataCollectionConfigDao.class);

    /**
     * Map of JmxCollection objects indexed by data collection name
     */
    private final Map<String, JmxCollection> m_collectionMap = new HashMap<>();

    private final ReadWriteLock m_lock = new ReentrantReadWriteLock();

    public JMXDataCollectionConfigDao() {
        super(JmxDatacollectionConfig.class, "JMX Data Collection Configuration",
                Paths.get("etc", "jmx-datacollection-config.xml"),
                Paths.get("etc", "jmx-datacollection-config.d"));
    }

    @Override
    public JmxDatacollectionConfig translateConfig(JmxDatacollectionConfig config) {
        for (JmxCollection collection : config.getJmxCollectionList()) {
            if (collection.hasImportMbeans()) {
                for (String importMbeans : collection.getImportGroupsList()) {
                    final File file = getOpennmsHome().resolve(Paths.get("etc", importMbeans)).toFile();
                    LOG.debug("parseJmxMbeans: parsing {}", file);
                    final Mbeans mbeans = JaxbUtils.unmarshal(Mbeans.class, new FileSystemResource(file));
                    collection.addMbeans(mbeans.getMbeanList());
                }
            }
        }
        return config;
    }

    public JmxDatacollectionConfig getConfig() {
        return getObject();
    }

    @Override
    public JmxDatacollectionConfig mergeConfigs(JmxDatacollectionConfig source, JmxDatacollectionConfig target) {
        if (target == null) {
            target = new JmxDatacollectionConfig();
        }
        return target.merge(source);
    }

    @Override
    public void onConfigUpdated(JmxDatacollectionConfig config) {
        buildCollectionMap(config);
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
    private void buildCollectionMap(JmxDatacollectionConfig config) {
        m_lock.writeLock().lock();
        try {
            m_collectionMap.clear();

            // BOZO isn't the collection name defined in the jmx-datacollection.xml file and
            // global to all the mbeans?
            for (JmxCollection collection : config.getJmxCollectionList()) {

                // Build group map for this collection
                Map<String, Mbean> groupMap = new HashMap<String, Mbean>();
                for (Mbean mbean : collection.getMbeans()) {
                    groupMap.put(mbean.getName(), mbean);
                }
                m_collectionMap.put(collection.getName(), collection);
            }
        } finally {
            m_lock.writeLock().unlock();
        }
    }

    public JmxCollection getJmxCollection(String collectionName) {
        // Try retrieving the object, which will trigger a reload of the cache if it changed
        getObject();

        m_lock.readLock().lock();
        try {
            JmxCollection collection = m_collectionMap.get(collectionName);
            if (collection == null) {
                LOG.warn("No JMX Config for collection '{}' found", collectionName);
            }
            return collection;
        } finally {
            m_lock.readLock().unlock();
        }
    }

    public Map<String, List<Attrib>> getAttributeMap(String cName, String aSysoid, String anAddress) {
        // Try retrieving the object, which will trigger a reload of the cache if it changed
        getObject();

        m_lock.readLock().lock();
        try {
            LOG.debug("getAttributeMap: collection: {} sysoid: {} address: {}", cName, aSysoid, anAddress);
            return getAttributeMap(m_collectionMap.get(cName), aSysoid, anAddress);
        } finally {
            m_lock.readLock().unlock();
        }
    }

    public static Map<String, List<Attrib>> getAttributeMap(JmxCollection collection, String aSysoid, String anAddress) {
        final Map<String, List<Attrib>> attributeMap = new HashMap<String, List<Attrib>>();

        if (aSysoid == null) {
            LOG.debug("getMibObjectList: aSysoid parameter is NULL...");
            return attributeMap;
        }

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

    public Map<String, BeanInfo> getMBeanInfo(String cName) {
        // Try retrieving the object, which will trigger a reload of the cache if it changed
        getObject();

        m_lock.readLock().lock();
        try {
            Map<String, BeanInfo> map = new HashMap<String, BeanInfo>();

            // Retrieve the appropriate Collection object
            // 
            JmxCollection collection = m_collectionMap.get(cName);

            if (collection == null) {
                LOG.warn("no collection named '{}' was found", cName);
            } else {
                return getMBeanInfo(collection);
            }
            return Collections.unmodifiableMap(map);
        } finally {
            m_lock.readLock().unlock();
        }
    }

    public static Map<String, BeanInfo> getMBeanInfo(JmxCollection collection) {
        final Map<String, BeanInfo> map = new HashMap<String, BeanInfo>();

        if (collection == null) {
            return Collections.unmodifiableMap(map);
        }

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

        return Collections.unmodifiableMap(map);
    }

    public RrdRepository getRrdRepository(String collectionName) {
        RrdRepository repo = new RrdRepository();
        repo.setRrdBaseDir(new File(getRrdPath()));
        repo.setRraList(getRRAList(collectionName));
        repo.setStep(getStep(collectionName));
        repo.setHeartBeat((2 * getStep(collectionName)));
        return repo;
    }

    public int getStep(String cName) {
        // Try retrieving the object, which will trigger a reload of the cache if it changed
        getObject();

        m_lock.readLock().lock();
        try {
            JmxCollection collection = m_collectionMap.get(cName);
            if (collection != null)
                return collection.getRrd().getStep();
            else
                return -1;
        } finally {
            m_lock.readLock().unlock();
        }
    }

    /**
     * Retrieves configured list of RoundRobin Archive statements.
     *
     * @param cName Name of the data collection
     * @return list of RRA strings.
     */
    private List<String> getRRAList(String cName) {
        // Try retrieving the object, which will trigger a reload of the cache if it changed
        getObject();

        m_lock.readLock().lock();
        try {
            JmxCollection collection = m_collectionMap.get(cName);
            if (collection != null) {
                return collection.getRrd().getRraCollection();
            } else {
                return null;
            }
        } finally {
            m_lock.readLock().unlock();
        }
    }

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
}
