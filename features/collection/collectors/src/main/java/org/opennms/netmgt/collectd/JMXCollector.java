/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import java.net.InetAddress;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.management.ObjectName;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.AlphaNumeric;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.collection.api.AbstractRemoteServiceCollector;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.ServiceParameters.ParameterName;
import org.opennms.netmgt.collection.support.NumericAttributeUtils;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.DeferredGenericTypeResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.collection.support.builder.Resource;
import org.opennms.netmgt.config.JMXDataCollectionConfigDao;
import org.opennms.netmgt.config.collectd.jmx.Attrib;
import org.opennms.netmgt.config.collectd.jmx.JmxCollection;
import org.opennms.netmgt.config.jmx.MBeanServer;
import org.opennms.netmgt.dao.jmx.JmxConfigDao;
import org.opennms.netmgt.jmx.JmxCollectorConfig;
import org.opennms.netmgt.jmx.JmxSampleProcessor;
import org.opennms.netmgt.jmx.JmxUtils;
import org.opennms.netmgt.jmx.connection.JmxConnectors;
import org.opennms.netmgt.jmx.impl.DefaultJmxCollector;
import org.opennms.netmgt.jmx.samples.AbstractJmxSample;
import org.opennms.netmgt.jmx.samples.JmxAttributeSample;
import org.opennms.netmgt.jmx.samples.JmxCompositeSample;
import org.opennms.netmgt.rrd.RrdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class performs the collection and storage of data. The derived class
 * manages the connection and configuration. The SNMPCollector class was used
 * as the starting point for this class so anyone familiar with it should be
 * able to easily understand it.
 * <p>
 * The jmx-datacollection-config.xml defines a list of MBeans and attributes
 * that may be monitored. This class retrieves the list of MBeans for the
 * specified service name (currently jboss and jsr160) and queries the remote
 * server for the attributes. The values are then stored in RRD files.
 * </p>
 * <p>
 * Two types of MBeans may be specified in the jmx-datacollection-config.xml
 * file. Standard MBeans which consist of and ObjectName and their attributes,
 * and WildCard MBeans which performs a query to retrieve MBeans based on a
 * criteria. The current implementation looks like: jboss:a=b,c=d,* Future
 * versions may permit enhanced queries. In either case multiple MBeans may be
 * returned and these MBeans would then be queried to obtain their attributes.
 * There are some important issues then using the wild card approach:
 * </p>
 * <p>
 * <ol>
 * <li>Since multiple MBeans will have the same attribute name there needs to
 * be a way to differentiate them. To handle this situation you need to
 * specify which field in the ObjectName should be used. This is defined as
 * the key-field.</li>
 * <li>The version of RRD that is used is limited to 19 characters. If this
 * limit is exceeded then the data will not be saved. The name is defined as:
 * keyField_attributeName.rrd Since the keyfield is defined in the Object Name
 * and may be too long, you may define an alias for it. The key-alias
 * parameter permit you to define a list of names to be substituted. Only
 * exact matches are handled. An example is:
 * <code>key-alias="this-name-is-long|thisIsNot,name-way-2-long,goodName"</code></li>
 * <li>If there are keyfields that you want to exclude (exact matches) you
 * may use a comma separated list like:
 * <code>exclude="name1,name2,name3"</code></li>
 * <li>Unlike the Standard MBeans there is no way (currently) to pre-define
 * graphs for them in the snmp-graph.properties file. The only way you can
 * create graphs is to create a custom graph in the Report section. The wild
 * card approach needs to be carefully considered before using it but it can
 * cut down on the amount of work necessary to define what to save.</li>
 * </ol>
 * </p>
 *
 * @author <a href="mailto:mike@opennms.org">Mike Jamison</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 */
public class JMXCollector extends AbstractRemoteServiceCollector {
    private static final Logger LOG = LoggerFactory.getLogger(JMXCollector.class);

    private static final String JMX_COLLECTION_KEY = "jmxCollection";

    private static final String JMX_MBEAN_SERVER_KEY = "jmxMBeanServer";

    private static final Map<String, Class<?>> TYPE_MAP = Collections.unmodifiableMap(Stream.of(
            new SimpleEntry<>(JMX_COLLECTION_KEY, JmxCollection.class),
            new SimpleEntry<>(JMX_MBEAN_SERVER_KEY, MBeanServer.class))
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));

    private JmxConfigDao m_jmxConfigDao;

    private JMXDataCollectionConfigDao m_jmxDataCollectionConfigDao;

    /**
     * The service name is provided by the derived class
     */
    private String serviceName = null;

    public JMXCollector() {
        super(TYPE_MAP);
    }

    /**
     * <p>
     * Returns the name of the service that the plug-in collects ("JMX").
     * </p>
     *
     * @return The service that the plug-in collects.
     */
    public String serviceName() {
        return serviceName.toUpperCase();
    }

    /**
     * <p>Setter for the field <code>serviceName</code>.</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setServiceName(String name) {
        serviceName = name;
    }

    @Override
    public void initialize() {
        // Retrieve the configuration DAOs
        if (m_jmxDataCollectionConfigDao == null) {
            m_jmxDataCollectionConfigDao = BeanUtils.getBean("daoContext", "jmxDataCollectionConfigDao", JMXDataCollectionConfigDao.class);
        }
        if (m_jmxConfigDao == null) {
            m_jmxConfigDao = BeanUtils.getBean("daoContext", "jmxConfigDao", JmxConfigDao.class);
        }
    }

    @Override
    public Map<String, Object> getRuntimeAttributes(CollectionAgent agent, Map<String, Object> parameters) {
        final Map<String, Object> runtimeAttributes = new HashMap<>();

        // Retrieve the name of the JMX data collector
        final String collectionName = ParameterMap.getKeyedString(parameters, ParameterName.COLLECTION.toString(), serviceName);
        final JmxCollection jmxCollection = m_jmxDataCollectionConfigDao.getJmxCollection(collectionName);
        if (jmxCollection == null) {
            throw new IllegalArgumentException(String.format("JMXCollector: No collection found with name '%s'.", collectionName));
        }
        runtimeAttributes.put(JMX_COLLECTION_KEY, jmxCollection);

        // Retrieve the agent config.
        final Map<String, String> parameterStringMap = new HashMap<String, String>();
        for (Map.Entry<String, Object> eachEntry : parameters.entrySet()) {
            if (eachEntry.getValue() instanceof String) {
                parameterStringMap.put(eachEntry.getKey(), (String) eachEntry.getValue());
            }
        }
        final MBeanServer mBeanServer = JmxUtils.getMBeanServer(m_jmxConfigDao, agent.getHostAddress(), parameterStringMap);
        if (mBeanServer != null) {
            runtimeAttributes.put(JMX_MBEAN_SERVER_KEY, mBeanServer);
        }

        return runtimeAttributes;
    }

    // we need this to determine which connection type/manager should be used to connect to the jvm
    protected JmxConnectors getConnectionName() {
        return JmxConnectors.DEFAULT;
    }

    @Override
    public CollectionSet collect(CollectionAgent agent, Map<String, Object> map) {
        final Map<String, String> stringMap = JmxUtils.convertToUnmodifiableStringMap(map);
        final InetAddress ipaddr = agent.getAddress();
        final JmxCollection jmxCollection = (JmxCollection)map.get(JMX_COLLECTION_KEY);
        final MBeanServer mBeanServer = (MBeanServer)map.get(JMX_MBEAN_SERVER_KEY);
        final String collectionName = ParameterMap.getKeyedString(map, ParameterName.COLLECTION.toString(), serviceName);
        final String port = ParameterMap.getKeyedString(map, ParameterName.PORT.toString(), null);
        final String friendlyName = ParameterMap.getKeyedString(map, ParameterName.FRIENDLY_NAME.toString(), port);
        final String collDir = JmxUtils.getCollectionDirectory(stringMap, friendlyName, serviceName);
        final int retries = ParameterMap.getKeyedInteger(map, ParameterName.RETRY.toString(), 3);

        InetAddress ipAddr = agent.getAddress();
        int nodeID = agent.getNodeId();

        // Retrieve the name of the JMX data collector

        final String hostAddress = InetAddressUtils.str(ipAddr);
        LOG.debug("initialize: InetAddress={}, collectionName={}", hostAddress, collectionName);

        JMXNodeInfo nodeInfo = new JMXNodeInfo(nodeID);
        LOG.debug("nodeInfo: {} {} {}", hostAddress, nodeID, agent);

        /*
         * Retrieve list of MBean objects to be collected from the
         * remote agent which are to be stored in the node-level RRD file.
         * These objects pertain to the node itself not any individual
         * interfaces.
         */
        Map<String, List<Attrib>> attrMap = JMXDataCollectionConfigDao.getAttributeMap(jmxCollection, serviceName(), hostAddress);
        nodeInfo.setAttributeMap(attrMap);

        Map<String, JMXDataSource> dsList = buildDataSourceList(collectionName, attrMap);
        nodeInfo.setDsMap(dsList);
        nodeInfo.setMBeans(JMXDataCollectionConfigDao.getMBeanInfo(jmxCollection));

        // Metrics collected from JMX are currently modeled as node level resources,
        // but live in a sub-directory set to the service name
        final NodeLevelResource nodeResource = new NodeLevelResource(agent.getNodeId(), collDir);
        // This parent resource used for generic resource
        final NodeLevelResource parentResource = new NodeLevelResource(agent.getNodeId());

        // Used to gather the results
        final CollectionSetBuilder collectionSetBuilder = new CollectionSetBuilder(agent);

        LOG.debug("connecting to {} on node ID {}", InetAddressUtils.str(ipaddr), nodeInfo.getNodeId());

        try {
            // create config for JmxCollector
            final JmxCollectorConfig config = new JmxCollectorConfig();
            config.setAgentAddress(InetAddressUtils.str(ipaddr));
            config.setConnectionName(getConnectionName());
            config.setRetries(retries);
            config.setServiceProperties(stringMap);
            config.setJmxCollection(jmxCollection);

            final DefaultJmxCollector jmxCollector = new DefaultJmxCollector();
            jmxCollector.collect(config, mBeanServer, new JmxSampleProcessor() {
                @Override
                public void process(JmxAttributeSample attributeSample, ObjectName objectName) {
                    final String mbeanObjectName = attributeSample.getMbean().getObjectname();
                    final String attributeName = attributeSample.getCollectedAttribute().getName();
                    final String dsKey = mbeanObjectName + "|" + attributeName;
                    final JMXDataSource ds = nodeInfo.getDsMap().get(dsKey);
                    if (ds == null) {
                        LOG.info("Could not find datasource for {}. Skipping.", dsKey);
                        return;
                    }

                    String resourceType = attributeSample.getMbean().getResourceType();
                    if (resourceType != null) {
                        final String parsedObjectName = fixGroupName(objectName.getCanonicalName());
                        final Resource resource = new DeferredGenericTypeResource(parentResource, resourceType,
                                parsedObjectName);
                        addNumericAttributeToCollectionSet(ds, attributeSample, resource);
                        addStringAttributesToCollectionSet(ds, attributeSample, resource, objectName);
                    } else {
                        addNumericAttributeToCollectionSet(ds, attributeSample, nodeResource);
                    }
                }

                @Override
                public void process(JmxCompositeSample compositeSample, ObjectName objectName) {
                    final String mbeanObjectName = compositeSample.getMbean().getObjectname();
                    final String attributeName = compositeSample.getCollectedAttribute().getName();

                    final String dsKey = mbeanObjectName + "|" + attributeName + "|"
                            + compositeSample.getCompositeKey();
                    final JMXDataSource ds = nodeInfo.getDsMap().get(dsKey);
                    if (ds == null) {
                        LOG.info("Could not find datasource for {}. Skipping.", dsKey);
                        return;
                    }
                    String resourceType = compositeSample.getMbean().getResourceType();
                    if (resourceType != null) {
                        final String parsedObjectName = fixGroupName(objectName.getCanonicalName());
                        final Resource resource = new DeferredGenericTypeResource(parentResource, resourceType,
                                parsedObjectName);
                        addNumericAttributeToCollectionSet(ds, compositeSample, resource);
                        addStringAttributesToCollectionSet(ds, compositeSample, resource, objectName);
                    } else {
                        addNumericAttributeToCollectionSet(ds, compositeSample, nodeResource);
                    }

                }

                private void addStringAttributesToCollectionSet(JMXDataSource ds, AbstractJmxSample sample,
                        Resource resource, ObjectName objectName) {

                    final String groupName = fixGroupName(JmxUtils.getGroupName(stringMap, sample.getMbean()));
                    final String domain = objectName.getDomain();
                    final Hashtable<String, String> properties = objectName.getKeyPropertyList();
                    properties.forEach(
                            (key, value) -> collectionSetBuilder.withStringAttribute(resource, groupName, key, value));
                    if (domain != null) {
                        collectionSetBuilder.withStringAttribute(resource, groupName, "domain", objectName.getDomain());
                    }
                }

                private void addNumericAttributeToCollectionSet(JMXDataSource ds, AbstractJmxSample sample,
                        Resource resource) {
                    final String groupName = fixGroupName(JmxUtils.getGroupName(stringMap, sample.getMbean()));

                    // Only numeric data comes back from JMX in data collection
                    final String valueAsString = sample.getCollectedValueAsString();
                    final Double value = NumericAttributeUtils.parseNumericValue(valueAsString);

                    // Construct the metric identifier (used by NRTG)
                    String metricId = groupName;
                    metricId = metricId.replace("_type_", ":type=");
                    metricId = metricId.replace("_", ".");
                    metricId = metricId.concat(".");
                    metricId = metricId.concat(ds.getName());
                    metricId = "JMX_".concat(metricId);

                    collectionSetBuilder.withIdentifiedNumericAttribute(resource, groupName, ds.getName(), value,
                            ds.getType(), metricId);
                }
            });
        } catch (final Exception e) {
            LOG.debug("{} Collector.collect: IOException while collecting address: {}", serviceName, agent.getAddress(), e);
        }

        return collectionSetBuilder.build();
    }

    /**
     * This method removes characters from an object name that are
     * potentially illegal in a file or directory name, returning a
     * name that is appropriate for use with the storeByGroup persistence
     * method.
     *
     * @param objectName
     * @return
     */
    private static String fixGroupName(String objectName) {
        if (objectName == null) {
            return "NULL";
        }
        return AlphaNumeric.parseAndReplace(objectName, '_');
    }

    /**
     * This method is responsible for building a list of RRDDataSource objects
     * from the provided list of MBeanObject objects.
     *
     * @param collectionName Collection name
     * @param attributeMap   List of MBeanObject objects defining the attributes to be collected via JMX.
     * @return list of RRDDataSource objects
     */
    protected static Map<String, JMXDataSource> buildDataSourceList(String collectionName, Map<String, List<Attrib>> attributeMap) {
        LOG.debug("buildDataSourceList - ***");

        /*
         * Retrieve the RRD expansion data source list which contains all
         * the expansion data source's. Use this list as a basis
         * for building a data source list for the current interface.
         */
        HashMap<String, JMXDataSource> dsList = new HashMap<>();

        /*
         * Loop through the MBean object list to be collected for this
         * interface and add a corresponding RRD data source object. In this
         * manner each interface will have RRD files create which reflect only
         * the data sources pertinent to it.
         */

        LOG.debug("attributeMap size: {}", attributeMap.size());
        for (String objectName : attributeMap.keySet()) {
            List<Attrib> list = attributeMap.get(objectName);

            LOG.debug("ObjectName: {}, Attributes: {}", objectName, list.size());

            for (Attrib attr : list) {
                if (attr.getType().isNumeric()) {
                    JMXDataSource ds = new JMXDataSource();
                    ds.setName(attr.getAlias());
                    ds.setType(attr.getType());

                    LOG.debug("buildDataSourceList: ds_name: {} ds_type: {}", ds.getName(), ds.getType());

                    // Add the new data source to the list
                    dsList.put(objectName + "|" + attr.getName(), ds);
                } else {
                    LOG.warn("buildDataSourceList: Data type '{}' not supported.  Only integer-type data may be stored in RRD.  MBean object '{}' will not be mapped to RRD data source.", attr.getType(), attr.getAlias());
                }
            }
        }

        return dsList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RrdRepository getRrdRepository(String collectionName) {
        return m_jmxDataCollectionConfigDao.getRrdRepository(collectionName);
    }

    public void setJmxConfigDao(JmxConfigDao jmxConfigDao) {
        m_jmxConfigDao = Objects.requireNonNull(jmxConfigDao);
    }

    public void setJmxDataCollectionConfigDao(JMXDataCollectionConfigDao jmxDataCollectionConfigDao) {
        m_jmxDataCollectionConfigDao = Objects.requireNonNull(jmxDataCollectionConfigDao);
    }
}
