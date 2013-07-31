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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opennms.core.utils.ConfigFileConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.config.collectd.jmx.Attr;
import org.opennms.netmgt.config.collectd.jmx.Attrib;
import org.opennms.netmgt.config.collectd.jmx.CompAttrib;
import org.opennms.netmgt.config.collectd.jmx.CompMember;
import org.opennms.netmgt.config.collectd.jmx.JmxCollection;
import org.opennms.netmgt.config.collectd.jmx.JmxDatacollectionConfig;
import org.opennms.netmgt.config.collectd.jmx.Mbean;
import org.opennms.netmgt.config.collectd.jmx.Mbeans;
import org.opennms.netmgt.model.RrdRepository;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

/**
 * This class is the main repository for JMX data collection configuration
 * information used by the an instance of the JMX service monitor. When this class is loaded it
 * reads the jmx data collection configuration into memory.
 *
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 *
 * @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
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
     * Map of group maps indexed by SNMP collection name.
     */
    private Map<String, Map<String, Mbean>> m_collectionGroupMap;

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
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
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

    private void buildCollectionMap() {
        // Build collection map which is a hash map of Collection
        // objects indexed by collection name...also build
        // collection group map which is a hash map indexed
        // by collection name with a hash map as the value
        // containing a map of the collections's group names
        // to the Group object containing all the information
        // for that group. So the associations are:
        //
        // CollectionMap
        // collectionName -> Collection
        //
        // CollectionGroupMap
        // collectionName -> groupMap
        // 
        // GroupMap
        // groupMapName -> Group
        //
        // This is parsed and built at initialization for
        // faster processing at run-time.
        // 
        m_collectionMap = new HashMap<String, JmxCollection>();
        m_collectionGroupMap = new HashMap<String, Map<String, Mbean>>();
        
        // BOZO isn't the collection name defined in the jmx-datacollection.xml file and
        // global to all the mbeans?
        Collection<JmxCollection> collections = m_config.getJmxCollectionCollection();
        Iterator<JmxCollection> citer = collections.iterator();
        while (citer.hasNext()) {
            JmxCollection collection = citer.next();

            // Build group map for this collection
            Map<String, Mbean> groupMap = new HashMap<String, Mbean>();

            Mbeans mbeans = collection.getMbeans();
            Collection<Mbean> groupList = mbeans.getMbeanCollection();
            Iterator<Mbean> giter = groupList.iterator();
            while (giter.hasNext()) {
                Mbean mbean = giter.next();
                groupMap.put(mbean.getName(), mbean);
            }

            m_collectionGroupMap.put(collection.getName(), groupMap);
            m_collectionMap.put(collection.getName(), collection);
        }
    }

    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
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
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read/loaded
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
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
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
     * @param cName
     *            name of the data collection from which to retrieve oid
     *            information.
     * @param aSysoid
     *            system object id to look up in the collection
     * @param anAddress
     *            IP address to look up in the collection
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
        
        Mbeans beans = collection.getMbeans();
        
        Enumeration<Mbean> en = beans.enumerateMbean();
        while (en.hasMoreElements()) {
            List<Attrib> list = new ArrayList<Attrib>();
            Mbean mbean = en.nextElement();
            Attrib[] attributes = mbean.getAttrib();
            for (int i = 0; i < attributes.length; i++) {
                list.add(attributes[i]);
            }
            
            CompAttrib[] compAttributes = mbean.getCompAttrib();
            for (int i = 0; i < compAttributes.length; i++) {
                CompMember[] compMembers = compAttributes[i].getCompMember();
                for (int j = 0; j < compMembers.length; j++) {
                    Attrib compAttrib = new Attrib();
                    compAttrib.setName(compAttributes[i].getName() + "|" + compMembers[j].getName());
                    compAttrib.setAlias(compMembers[j].getAlias());
                    compAttrib.setType(compMembers[j].getType());
                    list.add(compAttrib);
                }
            }
            attributeMap.put(mbean.getObjectname(), list);            
        }
        return attributeMap;
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
            Mbeans beans = collection.getMbeans();
            Enumeration<Mbean> en = beans.enumerateMbean();
            while (en.hasMoreElements()) {
                BeanInfo beanInfo = new BeanInfo();
                
                Mbean mbean = en.nextElement();
                beanInfo.setMbeanName(mbean.getName());
                beanInfo.setObjectName(mbean.getObjectname());
                beanInfo.setKeyField(mbean.getKeyfield());
                beanInfo.setExcludes(mbean.getExclude());
                beanInfo.setKeyAlias(mbean.getKeyAlias());
                
                Attrib[] attributes = mbean.getAttrib();
                CompAttrib[] compositeAttributes = mbean.getCompAttrib();
                
                List<String> attribNameList = new ArrayList<String>();
                List<String> compAttribNameList = new ArrayList<String>();
                
                for (Object ca : compositeAttributes) {
                    CompAttrib myCa = (CompAttrib)ca;
                    CompMember[] compositeMembers = myCa.getCompMember();
                    for (Object cm : compositeMembers) {
                        CompMember myCm = (CompMember)cm;
                        attribNameList.add(myCa.getName() + "|" + myCm.getName());
                        compAttribNameList.add(myCa.getName() + "|" + myCm.getName());
                    }                    
                }
                
                for (Object a : attributes) {
                    Attrib myA = (Attrib)a;
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
     * <p>getMBeanInfo_save</p>
     *
     * @param cName a {@link java.lang.String} object.
     * @return a {@link java.util.Map} object.
     */
    public Map<String, String[]> getMBeanInfo_save(String cName) {
        Map<String, String[]> map = new HashMap<String, String[]>();
        
        // Retrieve the appropriate Collection object
        // 
        JmxCollection collection = m_collectionMap.get(cName);
        
        Mbeans beans = collection.getMbeans();
        Enumeration<Mbean> en = beans.enumerateMbean();
        while (en.hasMoreElements()) {
            Mbean mbean = en.nextElement();
            int count = mbean.getAttribCount();
            String[] attribs = new String[count];
            Attrib[] attributes = mbean.getAttrib();
            for (int i = 0; i < attributes.length; i++) {
                attribs[i] = attributes[i].getName();
            }
            map.put(mbean.getObjectname(), attribs);
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * Takes a list of castor generated MibObj objects iterates over them
     * creating corresponding MibObject objects and adding them to the supplied
     * MibObject list.
     * 
     * @param objectList
     *            List of MibObject objects parsed from
     *            'datacollection-config.xml'
     * @param mibObjectList
     *            List of MibObject objects currently being built 
     */ 
    static void processObjectList(List<Attrib> objectList, List<Attr> mibObjectList) {
        //TODO: Make mibObjectList a Set
        //TODO: Delete this method, it is not referenced anywhere
        Iterator<Attrib>i = objectList.iterator();
        while (i.hasNext()) {
            Attrib mibObj = i.next();

            // Create a MibObject from the castor MibObj
            Attr aMibObject = new Attr();
            aMibObject.setName(mibObj.getName());
            aMibObject.setAlias(mibObj.getAlias());
            aMibObject.setType(mibObj.getType());
            aMibObject.setMaxval(mibObj.getMaxval());
            aMibObject.setMinval(mibObj.getMinval());

            // Add the MIB object provided it isn't already in the list
            if (!mibObjectList.contains(aMibObject)) {
                mibObjectList.add(aMibObject);
            }
        }
    }

    /**
     * Retrieves configured RRD step size.
     *
     * @param cName
     *            Name of the data collection
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
     * @param cName
     *            Name of the data collection
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
     * @return RRD repository path.
     * @param collectionName a {@link java.lang.String} object.
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
