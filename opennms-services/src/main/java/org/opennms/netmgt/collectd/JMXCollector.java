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

import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.AlphaNumeric;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionAttributeType;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.collection.api.ServiceParameters.ParameterName;
import org.opennms.netmgt.collection.support.AbstractCollectionAttribute;
import org.opennms.netmgt.collection.support.AbstractCollectionAttributeType;
import org.opennms.netmgt.collection.support.AbstractCollectionResource;
import org.opennms.netmgt.collection.support.SingleResourceCollectionSet;
import org.opennms.netmgt.config.JMXDataCollectionConfigFactory;
import org.opennms.netmgt.config.collectd.jmx.Attrib;
import org.opennms.netmgt.config.collectd.jmx.Mbean;
import org.opennms.netmgt.jmx.JmxCollector;
import org.opennms.netmgt.jmx.JmxCollectorConfig;
import org.opennms.netmgt.jmx.JmxSampleProcessor;
import org.opennms.netmgt.jmx.JmxUtils;
import org.opennms.netmgt.jmx.impl.DefaultJmxCollector;
import org.opennms.netmgt.jmx.samples.JmxAttributeSample;
import org.opennms.netmgt.jmx.samples.JmxCompositeSample;
import org.opennms.netmgt.events.api.EventProxy;
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

    /**
     * {@inheritDoc}
     * <p/>
     * <p>
     * Initialize the service collector.
     * </p>
     * <p>
     * During initialization the JMX collector: - Initializes various
     * configuration factories. - Verifies access to the database - Verifies
     * access to RRD file repository - Verifies access to JNI RRD shared
     * library - Determines if JMX to be stored for only the node's primary
     * interface or for all interfaces.
     * </p>
     *
     * @throws RuntimeException Thrown if an unrecoverable error occurs that prevents
     *                          the plug-in from functioning.
     */
    @Override
    public void initialize(Map<String, String> parameters) {
        // Initialize the JMXDataCollectionConfigFactory
        try {
            // XXX was reload(), which isn't test-friendly
            JMXDataCollectionConfigFactory.init();
        } catch (Throwable e) {
            LOG.error("initialize: Failed to load data collection configuration", e);
            throw new UndeclaredThrowableException(e);
        }

        // Make sure we can connect to the database
        java.sql.Connection ctest = null;
        try {
            ctest = DataSourceFactory.getInstance().getConnection();
        } catch (final Exception e) {
            LOG.error("initialize: failed to get a database connection", e);
            throw new UndeclaredThrowableException(e);
        } finally {
            if (ctest != null) {
                try {
                    ctest.close();
                } catch (final Throwable t) {
                    LOG.debug("initialize: an exception occurred while closing the JDBC connection");
                }
            }
        }

        // Save local reference to singleton instance

        LOG.debug("initialize: successfully instantiated JNI interface to RRD.");
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
        Map<String, List<Attrib>> attrMap = JMXDataCollectionConfigFactory.getInstance().getAttributeMap(collectionName, serviceName, hostAddress);
        nodeInfo.setAttributeMap(attrMap);

        Map<String, JMXDataSource> dsList = buildDataSourceList(collectionName, attrMap);
        nodeInfo.setDsMap(dsList);
        nodeInfo.setMBeans(JMXDataCollectionConfigFactory.getInstance().getMBeanInfo(collectionName));

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
    protected abstract String getConnectionName();

    /**
     * {@inheritDoc}
     * <p/>
     * Perform data collection.
     */
    @Override
    public CollectionSet collect(CollectionAgent agent, EventProxy eproxy, Map<String, Object> map) {
        final Map<String, String> stringMap = JmxUtils.convertToStringMap(map);
        final InetAddress ipaddr = agent.getAddress();
        final JMXNodeInfo nodeInfo = agent.getAttribute(NODE_INFO_KEY);
        final String collectionName = agent.getAttribute("collectionName");
        final String port = ParameterMap.getKeyedString(map, ParameterName.PORT.toString(), null);
        final String friendlyName = ParameterMap.getKeyedString(map, ParameterName.FRIENDLY_NAME.toString(), port);
        final String collDir = JmxUtils.getCollectionDirectory(stringMap, friendlyName, serviceName);
        final int retries = ParameterMap.getKeyedInteger(map, ParameterName.RETRY.toString(), 3);

        // result objects
        final JMXCollectionResource collectionResource = new JMXCollectionResource(agent, collDir);
        final SingleResourceCollectionSet collectionSet = new SingleResourceCollectionSet(collectionResource, new Date());

        LOG.debug("connecting to {} on node ID {}", InetAddressUtils.str(ipaddr), nodeInfo.getNodeId());

        try {
            // create config for JmxCollector
            final JmxCollectorConfig config = new JmxCollectorConfig();
            config.setAgentAddress(InetAddressUtils.str(ipaddr));
            config.setConnectionName(getConnectionName());
            config.setRetries(retries);
            config.setServiceProperties(stringMap);
            config.setJmxCollection(JMXDataCollectionConfigFactory.getInstance().getJmxCollection(collectionName));

            final JmxCollector jmxCollector = new DefaultJmxCollector();
            jmxCollector.collect(config, new JmxSampleProcessor() {

                private final Map<String, AttributeGroupType> groupNameAttributeGroupTypeMap = new HashMap<>();

                @Override
                public void process(JmxAttributeSample attributeSample) {
                    final String objectName = attributeSample.getMbean().getObjectname();
                    final String attributeName = attributeSample.getCollectedAttribute().getName();
                    final AttributeGroupType attribGroupType = getAttributeGroupType(attributeSample.getMbean());

                    JMXDataSource ds = nodeInfo.getDsMap().get(objectName + "|" + attributeName);
                    JMXCollectionAttributeType attribType = new JMXCollectionAttributeType(ds, attribGroupType);
                    collectionResource.setAttributeValue(attribType, attributeSample.getCollectedValueAsString());
                }

                @Override
                public void process(JmxCompositeSample compositeSample) {
                    final String objectName = compositeSample.getMbean().getObjectname();
                    final String attributeName = compositeSample.getCollectedAttribute().getName();
                    final AttributeGroupType attribGroupType = getAttributeGroupType(compositeSample.getMbean());

                    JMXDataSource ds = nodeInfo.getDsMap().get(objectName + "|" + attributeName + "|" + compositeSample.getCompositeKey());
                    JMXCollectionAttributeType attribType = new JMXCollectionAttributeType(ds, attribGroupType);
                    collectionResource.setAttributeValue(attribType, compositeSample.getCollectedValueAsString());
                }

                private AttributeGroupType getAttributeGroupType(Mbean mbean) {
                    //All JMX collected values are per node
                    final String groupName = JmxUtils.getGroupName(stringMap, mbean);
                    if (!groupNameAttributeGroupTypeMap.containsKey(groupName)) {
                        final AttributeGroupType attribGroupType = new AttributeGroupType(fixGroupName(groupName), AttributeGroupType.IF_TYPE_ALL);
                        groupNameAttributeGroupTypeMap.put(groupName, attribGroupType);
                    }
                    return groupNameAttributeGroupTypeMap.get(groupName);
                }
            });
        } catch (final Exception e) {
            LOG.debug("{} Collector.collect: IOException while collecting address: {}", serviceName, agent.getAddress(), e);
        }

        collectionSet.setStatus(ServiceCollector.COLLECTION_SUCCEEDED);
        return collectionSet;
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
                /*
                 * Verify that this object has an appropriate "integer" data
                 * type which can be stored in an RRD database file (must map to
                 * one of the supported RRD data source types: COUNTER or GAUGE).
                 * */
                String ds_type = JMXDataSource.mapType(attr.getType());
                if (ds_type != null) {
                    /*
                     * Passed!! Create new data source instance for this MBean
                     * object.
                     * Assign heartbeat using formula (2 * step) and hard code
                     * min & max values to "U" ("unknown").
                     */
                    JMXDataSource ds = new JMXDataSource();
                    ds.setHeartbeat(2 * JMXDataCollectionConfigFactory.getInstance().getStep(
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

                    /*
                     * Truncate MBean object name/alias if it exceeds 19 char
                     * max for RRD data source names.
                     */
                    String ds_name = JmxUtils.trimAttributeName(attr.getAlias());
                    ds.setName(ds_name);

                    // Map MBean object data type to RRD data type
                    ds.setType(ds_type);

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

    private static class JMXCollectionAttributeType extends AbstractCollectionAttributeType {
        private final JMXDataSource m_dataSource;
        private final String m_name;

        public JMXCollectionAttributeType(JMXDataSource dataSource, AttributeGroupType groupType) {
            super(groupType);
            m_dataSource = dataSource;
            m_name = dataSource.getName();
        }

        @Override
        public void storeAttribute(CollectionAttribute attribute, Persister persister) {
            //Only numeric data comes back from JMX in data collection
            persister.persistNumericAttribute(attribute);
        }

        @Override
        public String getName() {
            return m_name;
        }

        @Override
        public String getType() {
            return m_dataSource.getType();
        }

    }

    private static class JMXCollectionAttribute extends AbstractCollectionAttribute {

        private final String m_value;

        JMXCollectionAttribute(JMXCollectionResource resource, CollectionAttributeType attribType, String value) {
            super(attribType, resource);
            m_value = value;
        }

        @Override
        public String getNumericValue() {
            return m_value;
        }

        @Override
        public String getStringValue() {
            return m_value;
        }

        @Override
        public String toString() {
            return "alias " + getName() + ", value " + m_value + ", resource "
                    + m_resource + ", attributeType " + m_attribType;
        }

        @Override
        public String getMetricIdentifier() {
            String metricId = m_attribType.getGroupType().getName();
            metricId = metricId.replace("_type_", ":type=");
            metricId = metricId.replace("_", ".");
            metricId = metricId.concat(".");
            metricId = metricId.concat(getName());
            return "JMX_".concat(metricId);
        }
    }


    public static class JMXCollectionResource extends AbstractCollectionResource {
        private final String m_resourceName;
        private final int m_nodeId;

        public JMXCollectionResource(CollectionAgent agent, String resourceName) {
            super(agent);
            m_resourceName = resourceName;
            m_nodeId = agent.getNodeId();
        }

        @Override
        public String toString() {
            return "node[" + m_nodeId + ']';
        }

        public void setAttributeValue(CollectionAttributeType type, String value) {
            JMXCollectionAttribute attr = new JMXCollectionAttribute(this, type, value);
            addAttribute(attr);
        }

        @Override
        public File getResourceDir(RrdRepository repository) {
            return new File(repository.getRrdBaseDir(), getParent() + File.separator + m_resourceName);
        }

        @Override
        public String getResourceTypeName() {
            return CollectionResource.RESOURCE_TYPE_NODE; //All node resources for JMX; nothing of interface or "indexed resource" type
        }

        @Override
        public String getInstance() {
            return null; //For node type resources, use the default instance
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RrdRepository getRrdRepository(String collectionName) {
        return JMXDataCollectionConfigFactory.getInstance().getRrdRepository(collectionName);
    }

}
