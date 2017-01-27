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

package org.opennms.netmgt.collectd;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.AlphaNumeric;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.collection.api.ServiceParameters.ParameterName;
import org.opennms.netmgt.collection.support.NumericAttributeUtils;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.config.JMXDataCollectionConfigDao;
import org.opennms.netmgt.config.collectd.jmx.Attrib;
import org.opennms.netmgt.dao.jmx.JmxConfigDao;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.jmx.JmxCollector;
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
public abstract class JMXCollector implements ServiceCollector {
    private static final Logger LOG = LoggerFactory.getLogger(JMXCollector.class);

    /**
     * the config dao to be used
     */
    protected JmxConfigDao m_jmxConfigDao = null;

    private JMXDataCollectionConfigDao m_jmxDataCollectionConfigDao;

    /**
     * Interface attribute key used to store a JMXNodeInfo object which holds
     * data about the node being polled.
     */
    private static final String NODE_INFO_KEY =
            "org.opennms.netmgt.collectd.JMXCollector.nodeInfo";

    /**
     * The service name is provided by the derived class
     */
    private String serviceName = null;

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
    public void initialize(Map<String, String> parameters) {
        // Retrieve the configuration DAOs
        if (m_jmxDataCollectionConfigDao == null) {
            m_jmxDataCollectionConfigDao = BeanUtils.getBean("daoContext", "jmxDataCollectionConfigDao", JMXDataCollectionConfigDao.class);
        }
    }

    /**
     * Responsible for freeing up any resources held by the collector.
     */
    @Override
    public void release() {
        // Nothing to release...
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Responsible for performing all necessary initialization for the
     * specified interface in preparation for data collection.
     */
    @Override
    public void initialize(CollectionAgent agent, Map<String, Object> parameters) {
        InetAddress ipAddr = agent.getAddress();
        int nodeID = agent.getNodeId();

        // Retrieve the name of the JMX data collector
        String collectionName = ParameterMap.getKeyedString(parameters, ParameterName.COLLECTION.toString(), serviceName);

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
        Map<String, List<Attrib>> attrMap = m_jmxDataCollectionConfigDao.getAttributeMap(collectionName, serviceName, hostAddress);
        nodeInfo.setAttributeMap(attrMap);

        Map<String, JMXDataSource> dsList = buildDataSourceList(m_jmxDataCollectionConfigDao, collectionName, attrMap);
        nodeInfo.setDsMap(dsList);
        nodeInfo.setMBeans(m_jmxDataCollectionConfigDao.getMBeanInfo(collectionName));

        // Add the JMXNodeInfo object as an attribute of the interface
        agent.setAttribute(NODE_INFO_KEY, nodeInfo);
        agent.setAttribute("collectionName", collectionName);

    }

    /**
     * {@inheritDoc}
     * <p/>
     * Responsible for releasing any resources associated with the specified
     * interface.
     */
    @Override
    public void release(CollectionAgent agent) {
        // Nothing to release...
    }

    // we need this to determine which connection type/manager should be used to connect to the jvm
    protected abstract JmxConnectors getConnectionName();

    /**
     * {@inheritDoc}
     * <p/>
     * Perform data collection.
     */
    @Override
    public CollectionSet collect(CollectionAgent agent, EventProxy eproxy, Map<String, Object> map) {
        final Map<String, String> stringMap = JmxUtils.convertToUnmodifiableStringMap(map);
        final InetAddress ipaddr = agent.getAddress();
        final JMXNodeInfo nodeInfo = agent.getAttribute(NODE_INFO_KEY);
        final String collectionName = agent.getAttribute("collectionName");
        final String port = ParameterMap.getKeyedString(map, ParameterName.PORT.toString(), null);
        final String friendlyName = ParameterMap.getKeyedString(map, ParameterName.FRIENDLY_NAME.toString(), port);
        final String collDir = JmxUtils.getCollectionDirectory(stringMap, friendlyName, serviceName);
        final int retries = ParameterMap.getKeyedInteger(map, ParameterName.RETRY.toString(), 3);

        // Metrics collected from JMX are currently modeled as "interface" resources with
        // the interface name set to the service name
        final NodeLevelResource nodeResource = new NodeLevelResource(agent.getNodeId());
        final InterfaceLevelResource ifResource = new InterfaceLevelResource(nodeResource, collDir);

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
            config.setJmxCollection(m_jmxDataCollectionConfigDao.getJmxCollection(collectionName));

            final JmxCollector jmxCollector = new DefaultJmxCollector();
            ((DefaultJmxCollector) jmxCollector).setJmxConfigDao(m_jmxConfigDao);
            jmxCollector.collect(config, new JmxSampleProcessor() {
                @Override
                public void process(JmxAttributeSample attributeSample) {
                    final String objectName = attributeSample.getMbean().getObjectname();
                    final String attributeName = attributeSample.getCollectedAttribute().getName();

                    final String dsKey = objectName + "|" + attributeName;
                    final JMXDataSource ds = nodeInfo.getDsMap().get(dsKey);
                    if (ds == null) {
                        LOG.info("Could not find datasource for {}. Skipping.", dsKey);
                        return;
                    }
                    addNumericAttributeToCollectionSet(ds, attributeSample);
                }

                @Override
                public void process(JmxCompositeSample compositeSample) {
                    final String objectName = compositeSample.getMbean().getObjectname();
                    final String attributeName = compositeSample.getCollectedAttribute().getName();

                    final String dsKey = objectName + "|" + attributeName + "|" + compositeSample.getCompositeKey();
                    final JMXDataSource ds = nodeInfo.getDsMap().get(dsKey);
                    if (ds == null) {
                        LOG.info("Could not find datasource for {}. Skipping.", dsKey);
                        return;
                    }
                    addNumericAttributeToCollectionSet(ds, compositeSample);
                }

                private void addNumericAttributeToCollectionSet(JMXDataSource ds, AbstractJmxSample sample) {
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

                    collectionSetBuilder.withIdentifiedNumericAttribute(ifResource, groupName, ds.getName(), value, ds.getType(), metricId);
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
    protected static Map<String, JMXDataSource> buildDataSourceList(JMXDataCollectionConfigDao jmxDataCollectionConfigDao, String collectionName, Map<String, List<Attrib>> attributeMap) {
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
                    /*
                     * Passed!! Create new data source instance for this MBean
                     * object.
                     * Assign heartbeat using formula (2 * step) and hard code
                     * min & max values to "U" ("unknown").
                     */
                    JMXDataSource ds = new JMXDataSource();
                    ds.setHeartbeat(2 * jmxDataCollectionConfigDao.getStep(
                            collectionName));
                    // For completeness, adding a minval option to the variable.
                    String ds_minval = attr.getMinval();
                    if (ds_minval == null) {
                        ds_minval = "U";
                    }
                    ds.setMax(ds_minval);

                    /*
                     * In order to handle counter wraps, we need to set a max
                     * value for the variable.
                     */
                    String ds_maxval = attr.getMaxval();
                    if (ds_maxval == null) {
                        ds_maxval = "U";
                    }

                    ds.setMax(ds_maxval);
                    ds.setInstance(collectionName);
                    ds.setName(attr.getAlias());

                    // Map MBean object data type to RRD data type
                    ds.setType(attr.getType());

                    /*
                     * Assign the data source object identifier and instance
                     * ds.setName(attr.getName());
                     */
                    ds.setOid(attr.getName());

                    LOG.debug("buildDataSourceList: ds_name: {} ds_oid: {}.{} ds_max: {} ds_min: {}", ds.getName(), ds.getOid(), ds.getInstance(), ds.getMax(), ds.getMin());

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
