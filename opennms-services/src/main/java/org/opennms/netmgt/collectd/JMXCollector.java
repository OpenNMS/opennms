/**
 * *****************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R). If not, see:
 * http://www.gnu.org/licenses/
 *
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 ******************************************************************************
 */
package org.opennms.netmgt.collectd;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import org.apache.commons.lang.StringUtils;
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
import org.opennms.netmgt.config.BeanInfo;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.JMXDataCollectionConfigFactory;
import org.opennms.netmgt.config.collectd.jmx.Attrib;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.protocols.jmx.connectors.ConnectionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class performs the collection and storage of data. The derived class
 * manages the connection and configuration. The SNMPCollector class was used
 * as the starting point for this class so anyone familiar with it should be
 * able to easily understand it.
 * <p>
 * The jmx-datacollection-config.xml defines a list of MBeans and attributes
 * that may be monitored. This class retrieves
 * the list of MBeans for the specified service name (currently jboss and
 * jsr160) and queries the remote server for the
 * attributes. The values are then stored in RRD files.
 * </p>
 * <p>
 * Two types of MBeans may be specified in the jmx-datacollection-config.xml
 * file. Standard MBeans which consist of and
 * ObjectName and their attributes, and WildCard MBeans which performs a query
 * to retrieve MBeans based on a criteria.
 * The current implementation looks like: jboss:a=b,c=d,* Future versions may
 * permit enhanced queries. In either case
 * multiple MBeans may be returned and these MBeans would then be queried to
 * obtain their attributes. There are some
 * important issues then using the wild card approach:
 * </p>
 * <p>
 * <ol>
 * <li>Since multiple MBeans will have the same attribute name there needs to
 * be a way to differentiate them. To handle
 * this situation you need to specify which field in the ObjectName should be
 * used. This is defined as the key-field.</li>
 * <li>The version of RRD that is used is limited to 19 characters. If this
 * limit is exceeded then the data will not be
 * saved. The name is defined as: keyField_attributeName.rrd Since the
 * keyfield is defined in the Object Name and may be
 * too long, you may define an alias for it. The key-alias parameter permit
 * you to define a list of names to be
 * substituted. Only exact matches are handled. An example is:
 * <code>key-alias="this-name-is-long|thisIsNot,name-way-2-long,goodName"</code></li>
 * <li>If there are keyfields that you want to exclude (exact matches) you may
 * use a comma separated list like:
 * <code>exclude="name1,name2,name3"</code></li>
 * <li>Unlike the Standard MBeans there is no way (currently) to pre-define
 * graphs for them in the snmp-graph.properties
 * file. The only way you can create graphs is to create a custom graph in the
 * Report section. The wild card approach
 * needs to be carefully considered before using it but it can cut down on the
 * amount of work necessary to define what
 * to save.</li>
 * </ol>
 * </p>
 *
 * @author <a href="mailto:mike@opennms.org">Mike Jamison</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 */
public abstract class JMXCollector implements ServiceCollector {

    private static final Logger LOG = LoggerFactory.getLogger(JMXCollector.class);

    /**
     * Interface attribute key used to store the map of IfInfo objects which
     * hold data about each interface on a particular node.
     */
    private static final String IF_MAP_KEY = "org.opennms.netmgt.collectd.JBossCollector.ifMap";

    /**
     * In some circumstances there may be many instances of a given service
     * but running on different ports. Rather than using the port as the
     * identifier users may define a more meaningful name.
     */
    private boolean m_useFriendlyName = false;

    /**
     * Interface attribute key used to store a JMXNodeInfo object which holds
     * data about the node being polled.
     */
    protected static final String NODE_INFO_KEY = "org.opennms.netmgt.collectd.JMXCollector.nodeInfo";

    /**
     * The service name is provided by the derived class
     */
    private String m_serviceName = null;

    /**
     * The JMX resource type Map.
     */
    private final HashMap<String, JMXResourceType> m_resourceTypeList = new HashMap<String, JMXResourceType>();

    /**
     * <p>
     * Returns the name of the service that the plug-in collects ("JMX").
     * </p>
     *
     * @return The service that the plug-in collects.
     */
    public String serviceName() {
        return m_serviceName.toUpperCase();
    }

    /**
     * <p>
     * Setter for the field <code>serviceName</code>.
     * </p>
     *
     * @param name
     *             a {@link java.lang.String} object.
     */
    public void setServiceName(final String name) {
        m_serviceName = name;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Initialize the service collector.
     * </p>
     * <p>
     * During initialization the JMX collector: - Initializes various
     * configuration factories. - Verifies access to the
     * database - Verifies access to RRD file repository - Verifies access to
     * JNI RRD shared library - Determines if JMX
     * to be stored for only the node's primary interface or for all
     * interfaces.
     * </p>
     *
     * @exception RuntimeException
     *                             Thrown if an unrecoverable error occurs that prevents
     *                             the plug-in from functioning.
     */
    @Override
    public void initialize(final Map<String, String> parameters) {
        try {
            JMXDataCollectionConfigFactory.init();
        } catch (IOException e) {
            LOG.error("initialize: Failed to load data collection configuration", e);
            throw new UndeclaredThrowableException(e);
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
     * {@inheritDoc} Responsible for performing all necessary initialization
     * for the
     * specified interface in preparation for data collection.
     */
    @Override
    public void initialize(final CollectionAgent agent, final Map<String, Object> parameters) {
        InetAddress ipAddr = agent.getAddress();
        int nodeID = agent.getNodeId();

        // Retrieve the name of the JMX data collector
        final String collectionName = ParameterMap.getKeyedString(parameters, ParameterName.COLLECTION.toString(), m_serviceName);

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
        Map<String, List<Attrib>> attrMap = JMXDataCollectionConfigFactory.getInstance().getAttributeMap(collectionName, m_serviceName, hostAddress);
        nodeInfo.setAttributeMap(attrMap);

        Map<String, JMXDataSource> dsList = buildDataSourceList(collectionName, attrMap);
        nodeInfo.setDsMap(dsList);
        nodeInfo.setMBeans(JMXDataCollectionConfigFactory.getInstance().getMBeanInfo(collectionName));

        // Add the JMXNodeInfo object as an attribute of the interface
        agent.setAttribute(NODE_INFO_KEY, nodeInfo);
        agent.setAttribute("collectionName", collectionName);
    }

    /**
     * {@inheritDoc} Responsible for releasing any resources associated with
     * the specified
     * interface.
     */
    @Override
    public void release(CollectionAgent agent) {
        // Nothing to release...
    }

    /**
     * <p>
     * getMBeanServerConnection
     * </p>
     *
     * @param parameters
     *                a {@link java.util.Map} object.
     * @param address
     *                a {@link java.net.InetAddress} object.
     * @return a
     *         {@link org.opennms.protocols.jmx.connectors.ConnectionWrapper}
     *         object.
     */
    public abstract ConnectionWrapper getMBeanServerConnection(Map<String, Object> parameters, InetAddress address);

    /**
     * {@inheritDoc} Perform data collection.
     * <p>
     * @param parameters
     */
    @Override
    public CollectionSet collect(final CollectionAgent agent, final EventProxy eproxy, final Map<String, Object> parameters) {
        InetAddress ipaddr = agent.getAddress();
        JMXNodeInfo nodeInfo = agent.getAttribute(NODE_INFO_KEY);
        Map<String, BeanInfo> mbeans = nodeInfo.getMBeans();
        String collDir = m_serviceName;

        boolean useMbeanForRrds = ParameterMap.getKeyedBoolean(parameters, ParameterName.USE_MBEAN_NAME_FOR_RRDS.toString(), false);
        String port = ParameterMap.getKeyedString(parameters, ParameterName.PORT.toString(), null);
        String friendlyName = ParameterMap.getKeyedString(parameters, ParameterName.FRIENDLY_NAME.toString(), port);
        if (m_useFriendlyName) {
            collDir = friendlyName;
        }

        JMXCollectionSet collectionSet = new JMXCollectionSet(agent);
        collectionSet.setCollectionTimestamp(new Date());
        collectionSet.setStatus(ServiceCollector.COLLECTION_UNKNOWN);

        ConnectionWrapper connection = null;

        LOG.debug("collecting {} on node ID {}", InetAddressUtils.str(ipaddr), nodeInfo.getNodeId());

        try {
            connection = getMBeanServerConnection(parameters, ipaddr);

            if (connection == null) {
                LOG.debug("unable to get a jmx connection to node={}/addr={}", nodeInfo.getNodeId(), InetAddressUtils.str(ipaddr));
                return collectionSet;
            }

            MBeanServerConnection mbeanServer = connection.getMBeanServer();

            int retry = ParameterMap.getKeyedInteger(parameters, ParameterName.RETRY.toString(), 3);
            for (int attempts = 0; attempts <= retry; attempts++) {
                try {
                    for (BeanInfo beanInfo : mbeans.values()) {
                        String resourceType = beanInfo.getResourceType();
                        if (resourceType == null || "node".equals(resourceType)) {
                            handleSingleInstanceCollection(agent, mbeanServer, collectionSet, beanInfo, useMbeanForRrds, collDir);
                        } else {
                            handleMultiInstanceCollection(agent, mbeanServer, collectionSet, beanInfo, useMbeanForRrds, collDir);
                        }
                    }

                    collectionSet.setStatus(ServiceCollector.COLLECTION_SUCCEEDED);
                    break;
                } catch (final Exception e) {
                    LOG.debug("{} Collector.collect: IOException while collecting address: {}", m_serviceName, agent.getAddress(), e);
                }
            }
        } catch (final Exception e) {
            LOG.error("Error getting MBeanServer", e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        return collectionSet;
    }

    private void handleSingleInstanceCollection(CollectionAgent agent, MBeanServerConnection mbeanServer, JMXCollectionSet collectionSet, BeanInfo beanInfo, boolean useMbeanForRrds, String collDir) {
        JMXNodeInfo nodeInfo = agent.getAttribute(NODE_INFO_KEY);
        String mbeanName = beanInfo.getMbeanName();
        String objectName = beanInfo.getObjectName();

        String obj = useMbeanForRrds ? mbeanName : objectName;
        JMXCollectionResource collectionResource = new JMXSingleInstanceCollectionResource(agent);
        AttributeGroupType attribGroupType = new AttributeGroupType(fixGroupName(obj), "all");

        List<String> attribNames = beanInfo.getAttributeNames();
        Map<String, List<String>> compAttribNames = beanInfo.getCompositeAttributeNames();

        LOG.debug("{} Collector - getAttributes: {}, # attributes: {}, # composite attribute members: {}", m_serviceName, objectName, attribNames.size(), compAttribNames.size());
        try {
            ObjectName oName = new ObjectName(objectName);
            if (mbeanServer.isRegistered(oName)) {
                MBeanInfo jmxMBeanInfo = mbeanServer.getMBeanInfo(oName);

                Map<String, JMXDataSource> dsMap = nodeInfo.getDsMap();
                for (MBeanAttributeInfo mbai : jmxMBeanInfo.getAttributes()) {
                    LOG.debug("objectName: {}, attribute: {}, type: {}", oName, mbai.getName(), mbai.getType());
                    if (attribNames.contains(mbai.getName())) {
                        if ("javax.management.openmbean.CompositeData".equals(mbai.getType())) {
                            LOG.error("MBean {} attribute {} is a CompositeData type", objectName, mbai.getName());
                        } else {
                            try {
                                Object attribute = mbeanServer.getAttribute(oName, mbai.getName());

                                JMXDataSource ds = dsMap.get(objectName + "|" + mbai.getName());
                                if (ds != null) {
                                    JMXCollectionAttributeType attribType = new JMXCollectionAttributeType(ds, null, null, attribGroupType);
                                    collectionResource.setAttributeValue(attribType, attribute.toString());
                                } else {
                                    LOG.debug("Did not find an entry for key '{}|{}' in dsMap", objectName, mbai.getName());
                                }
                            } catch (final InstanceNotFoundException e) {
                                LOG.error("MBean {} attribute {} was not found", objectName, mbai.getName());
                            } catch (final IOException e) {
                                LOG.error("Unable to retrieve mbean {} attribute {}", objectName, mbai.getName());
                            } catch (final AttributeNotFoundException e) {
                                LOG.error("Unable to retrieve mbean {} attribute {}", objectName, mbai.getName());
                            } catch (final MBeanException e) {
                                LOG.error("Unable to retrieve mbean {} attribute {}", objectName, mbai.getName());
                            } catch (final ReflectionException e) {
                                LOG.error("Unable to retrieve mbean {} attribute {}", objectName, mbai.getName());
                            }
                        }
                    } else if (compAttribNames.containsKey(mbai.getName())) {
                        if (!"javax.management.openmbean.CompositeData".equals(mbai.getType())) {
                            LOG.error("MBean {} attribute {} is not a CompositeData type", objectName, mbai.getName());
                        } else {
                            try {
                                CompositeData cd = (CompositeData) mbeanServer.getAttribute(oName, mbai.getName());
                                for (String key : compAttribNames.get(mbai.getName())) {
                                    JMXDataSource ds = dsMap.get(objectName + "|" + mbai.getName() + "|" + key);
                                    if (ds != null) {
                                        JMXCollectionAttributeType attribType = new JMXCollectionAttributeType(ds, null, null, attribGroupType);
                                        collectionResource.setAttributeValue(attribType, cd.get(key).toString());
                                    } else {
                                        LOG.debug("Did not find an entry for key '{}|{}|{}' in dsMap", objectName, mbai.getName(), key);
                                    }
                                }
                            } catch (final InstanceNotFoundException e) {
                                LOG.error("MBean {} attribute {} was not found", objectName, mbai.getName());
                            } catch (final IOException e) {
                                LOG.error("Unable to retrieve mbean {} attribute {}", objectName, mbai.getName());
                            } catch (final AttributeNotFoundException e) {
                                LOG.error("Unable to retrieve mbean {} attribute {}", objectName, mbai.getName());
                            } catch (final MBeanException e) {
                                LOG.error("Unable to retrieve mbean {} attribute {}", objectName, mbai.getName());
                            } catch (final ReflectionException e) {
                                LOG.error("Unable to retrieve mbean {} attribute {}", objectName, mbai.getName());
                            }
                        }
                    }
                }
                collectionSet.getCollectionResources().add(collectionResource);
            }
        } catch (MalformedObjectNameException e) {
            LOG.error("objectname {} is not valid", objectName, e);
        } catch (InstanceNotFoundException e) {
            LOG.error("MBean instance {} was not found.", objectName, e);
        } catch (IntrospectionException e) {
            LOG.error("Error retrieving MBean instance {}.", objectName, e);
        } catch (ReflectionException e) {
            LOG.error("Error retrieving MBean instance {}.", objectName, e);
        } catch (IOException e) {
            LOG.error("Error retrieving MBean instance {}.", objectName, e);
        }
    }

    private void handleMultiInstanceCollection(CollectionAgent agent, MBeanServerConnection mbeanServer, JMXCollectionSet collectionSet, BeanInfo beanInfo, boolean useMbeanForRrds, String collDir) {
        LOG.debug("handleMultiInstanceCollection: collDir: {}", collDir);
        JMXNodeInfo nodeInfo = agent.getAttribute(NODE_INFO_KEY);
        Map<String, JMXDataSource> dsMap = nodeInfo.getDsMap();
        String mbeanName = beanInfo.getMbeanName();
        String objectName = beanInfo.getObjectName();

        String obj = useMbeanForRrds ? mbeanName : objectName;
        JMXResourceType type = getJMXResourceType(agent, beanInfo.getResourceType());
        JMXCollectionResource collectionResource = new JMXMultiInstanceCollectionResource(agent, obj, type);
        AttributeGroupType attribGroupType = new AttributeGroupType(fixGroupName(obj), "all");

        List<String> attribNames = beanInfo.getAttributeNames();
        List<String> excludes = beanInfo.getExcludes();
        Map<String, List<String>> compAttribNames = beanInfo.getCompositeAttributeNames();
        if (beanInfo.getKeyAlias() == null) {
            LOG.debug("{} Collector - handleMultiInstanceCollection: key-alias was null");
            return;
        }

        try {
            Set<ObjectName> mbeanSet = getObjectNames(mbeanServer, objectName);
            for (ObjectName oName : mbeanSet) {
                LOG.debug("{} Collector - handleMultiInstanceCollection: {}, # attributes: {}, alias: {}", m_serviceName, oName, attribNames.size(), beanInfo.getKeyAlias());

                try {
                    if (excludes.isEmpty()) {
                        // XXX: is this the same as handleSingleInstanceCollection()?

                        if (mbeanServer.isRegistered(oName)) {
                            MBeanInfo jmxMBeanInfo = mbeanServer.getMBeanInfo(oName);

                            for (MBeanAttributeInfo mbai : jmxMBeanInfo.getAttributes()) {
                                LOG.debug("handleMultiInstanceCollection: objectName: {}, attribute: {}, type: {}", oName, mbai.getName(), mbai.getType());
                                if (attribNames.contains(mbai.getName())) {
                                    if ("javax.management.openmbean.CompositeData".equals(mbai.getType())) {
                                        LOG.error("MBean {} attribute {} is a CompositeData type", objectName, mbai.getName());
                                    } else {
                                        try {
                                            Object attribute = mbeanServer.getAttribute(oName, mbai.getName());

                                            JMXDataSource ds = dsMap.get(objectName + "|" + mbai.getName());
                                            if (ds != null) {
                                                LOG.debug("ds: {}", ds);
                                                LOG.debug("attributetype: keyfield: null, key-alias: null, attrgrouptype: {}", attribGroupType);
                                                JMXCollectionAttributeType attribType = new JMXCollectionAttributeType(ds, null, null, attribGroupType);
                                                collectionResource.setAttributeValue(attribType, attribute.toString());
                                            } else {
                                                LOG.debug("handleMultiInstanceCollection: Did not find an entry for key '{}|{}' in dsMap", objectName, mbai.getName());
                                            }
                                        } catch (final InstanceNotFoundException e) {
                                            LOG.error("handleMultiInstanceCollection: MBean {} attribute {} was not found", objectName, mbai.getName());
                                        } catch (final IOException e) {
                                            LOG.error("handleMultiInstanceCollection: Unable to retrieve mbean {} attribute {}", objectName, mbai.getName(), e);
                                        } catch (final AttributeNotFoundException e) {
                                            LOG.error("handleMultiInstanceCollection: Unable to retrieve mbean {} attribute {}", objectName, mbai.getName(), e);
                                        } catch (final MBeanException e) {
                                            LOG.error("handleMultiInstanceCollection: Unable to retrieve mbean {} attribute {}", objectName, mbai.getName(), e);
                                        } catch (final ReflectionException e) {
                                            LOG.error("handleMultiInstanceCollection: Unable to retrieve mbean {} attribute {}", objectName, mbai.getName(), e);
                                        }
                                    }
                                } else if (compAttribNames.containsKey(mbai.getName())) {
                                    if (!"javax.management.openmbean.CompositeData".equals(mbai.getType())) {
                                        LOG.error("MBean {} attribute {} is not a CompositeData type", objectName, mbai.getName());
                                    } else {
                                        try {
                                            CompositeData cd = (CompositeData) mbeanServer.getAttribute(oName, mbai.getName());
                                            for (String key : compAttribNames.get(mbai.getName())) {
                                                JMXDataSource ds = dsMap.get(objectName + "|" + mbai.getName() + "|" + key);
                                                if (ds != null) {
                                                    LOG.debug("ds: {}", ds);
                                                    LOG.debug("attributetype: keyfield: null, key-alias: null, attrgrouptype: {}", attribGroupType);

                                                    JMXCollectionAttributeType attribType = new JMXCollectionAttributeType(ds, null, null, attribGroupType);
                                                    collectionResource.setAttributeValue(attribType, cd.get(key).toString());
                                                } else {
                                                    LOG.debug("handleMultiInstanceCollection: Did not find an entry for key '{}|{}|{}' in dsMap", objectName, mbai.getName(), key);
                                                }
                                            }
                                        } catch (final InstanceNotFoundException e) {
                                            LOG.error("handleMultiInstanceCollection: MBean {} attribute {} was not found", objectName, mbai.getName());
                                        } catch (final IOException e) {
                                            LOG.error("handleMultiInstanceCollection: Unable to retrieve CompositeData mbean {} attribute {}", objectName, mbai.getName(), e);
                                        } catch (final AttributeNotFoundException e) {
                                            LOG.error("handleMultiInstanceCollection: Unable to retrieve CompositeData mbean {} attribute {}", objectName, mbai.getName(), e);
                                        } catch (final MBeanException e) {
                                            LOG.error("handleMultiInstanceCollection: Unable to retrieve CompositeData mbean {} attribute {}", objectName, mbai.getName(), e);
                                        } catch (final ReflectionException e) {
                                            LOG.error("handleMultiInstanceCollection: Unable to retrieve CompositeData mbean {} attribute {}", objectName, mbai.getName(), e);
                                        }
                                    }
                                }
                            }
                            collectionSet.getCollectionResources().add(collectionResource);
                        }
                    } else {
                        /*
                         * * filter out calls if the key field
                         * matches an entry in the exclude
                         * list
                         */
                        String keyName = oName.getKeyProperty(beanInfo.getKeyField());

                        if (!excludes.contains(keyName)) {
                            if (mbeanServer.isRegistered(oName)) {
                                MBeanInfo jmxMBeanInfo = mbeanServer.getMBeanInfo(oName);

                                for (MBeanAttributeInfo mbai : jmxMBeanInfo.getAttributes()) {
                                    LOG.debug("handleMultiInstanceCollection: objectName: {}, attribute: {}, type: {}", oName, mbai.getName(), mbai.getType());
                                    if (attribNames.contains(mbai.getName())) {
                                        if ("javax.management.openmbean.CompositeData".equals(mbai.getType())) {
                                            LOG.error("handleMultiInstanceCollection: MBean {} attribute {} is a CompositeData type", objectName, mbai.getName());
                                        } else {
                                            try {
                                                Object attribute = mbeanServer.getAttribute(oName, mbai.getName());

                                                JMXDataSource ds = dsMap.get(objectName + "|" + mbai.getName());
                                                if (ds != null) {
                                                    LOG.debug("ds: {}", ds);
                                                    LOG.debug("attributetype: keyfield: {}, key-alias: {}, attrgrouptype: {}", oName.getKeyProperty(beanInfo.getKeyField()), beanInfo.getKeyAlias(), attribGroupType);
                                                    JMXCollectionAttributeType attribType = new JMXCollectionAttributeType(ds, oName.getKeyProperty(beanInfo.getKeyField()), beanInfo.getKeyAlias(), attribGroupType);
                                                    collectionResource.setAttributeValue(attribType, attribute.toString());
                                                } else {
                                                    LOG.debug("Did not find an entry for key '{}|{}' in dsMap", objectName, mbai.getName());
                                                }
                                            } catch (final InstanceNotFoundException e) {
                                                LOG.error("handleMultiInstanceCollection: MBean {} attribute {} was not found", objectName, mbai.getName(), e);
                                            } catch (final IOException e) {
                                                LOG.error("handleMultiInstanceCollection: Unable to retrieve mbean {} attribute {}", objectName, mbai.getName(), e);
                                            } catch (final AttributeNotFoundException e) {
                                                LOG.error("handleMultiInstanceCollection: Unable to retrieve mbean {} attribute {}", objectName, mbai.getName(), e);
                                            } catch (final MBeanException e) {
                                                LOG.error("handleMultiInstanceCollection: Unable to retrieve mbean {} attribute {}", objectName, mbai.getName(), e);
                                            } catch (final ReflectionException e) {
                                                LOG.error("handleMultiInstanceCollection: Unable to retrieve mbean {} attribute {}", objectName, mbai.getName(), e);
                                            }
                                        }
                                    } else if (compAttribNames.containsKey(mbai.getName())) {
                                        if (!"javax.management.openmbean.CompositeData".equals(mbai.getType())) {
                                            LOG.error("MBean {} attribute {} is not a CompositeData type", objectName, mbai.getName());
                                        } else {
                                            try {
                                                CompositeData cd = (CompositeData) mbeanServer.getAttribute(oName, mbai.getName());
                                                for (String key : compAttribNames.get(mbai.getName())) {
                                                    JMXDataSource ds = dsMap.get(objectName + "|" + mbai.getName() + "|" + key);
                                                    if (ds != null) {
                                                        LOG.debug("ds: {}", ds);
                                                        LOG.debug("attributetype: keyfield: {}, key-alias: {}, attrgrouptype: {}", oName.getKeyProperty(beanInfo.getKeyField()), beanInfo.getKeyAlias(), attribGroupType);
                                                        JMXCollectionAttributeType attribType = new JMXCollectionAttributeType(ds, oName.getKeyProperty(beanInfo.getKeyField()), beanInfo.getKeyAlias(), attribGroupType);
                                                        collectionResource.setAttributeValue(attribType, cd.get(key).toString());
                                                    } else {
                                                        LOG.debug("handleMultiInstanceCollection: Did not find an entry for key '{}|{}|{}' in dsMap", objectName, mbai.getName(), key);
                                                    }
                                                }
                                            } catch (final InstanceNotFoundException e) {
                                                LOG.error("handleMultiInstanceCollection: MBean {} attribute {} was not found", objectName, mbai.getName(), e);
                                            } catch (final IOException e) {
                                                LOG.error("handleMultiInstanceCollection: Unable to retrieve mbean {} attribute {}", objectName, mbai.getName(), e);
                                            } catch (final AttributeNotFoundException e) {
                                                LOG.error("handleMultiInstanceCollection: Unable to retrieve mbean {} attribute {}", objectName, mbai.getName(), e);
                                            } catch (final MBeanException e) {
                                                LOG.error("handleMultiInstanceCollection: Unable to retrieve mbean {} attribute {}", objectName, mbai.getName(), e);
                                            } catch (final ReflectionException e) {
                                                LOG.error("handleMultiInstanceCollection: Unable to retrieve mbean {} attribute {}", objectName, mbai.getName(), e);
                                            }
                                        }
                                    }
                                }
                                collectionSet.getCollectionResources().add(collectionResource);
                            }
                        }
                    }
                } catch (InstanceNotFoundException e) {
                    LOG.error("handleMultiInstanceCollection: Error retrieving attributes for {}", oName, e);
                } catch (IntrospectionException e) {
                    LOG.error("handleMultiInstanceCollection: Error retrieving attributes for {}", oName, e);
                } catch (ReflectionException e) {
                    LOG.error("handleMultiInstanceCollection: Error retrieving attributes for {}", oName, e);
                } catch (IOException e) {
                    LOG.error("handleMultiInstanceCollection: Error retrieving attributes for {}", oName, e);
                }
            }
        } catch (MalformedObjectNameException e) {
            LOG.error("handleMultiInstanceCollection: Invalid objectname {}", objectName, e);
        } catch (IOException e) {
            LOG.error("handleMultiInstanceCollection: Error retrieving mbean {}", objectName, e);
        }
    }

    private static Set<ObjectName> getObjectNames(final MBeanServerConnection mbeanServer, final String objectName) throws IOException, MalformedObjectNameException {
        return mbeanServer.queryNames(new ObjectName(objectName), null);
    }

    private JMXResourceType getJMXResourceType(CollectionAgent agent, String resourceType) {
        if (!m_resourceTypeList.containsKey(resourceType)) {
            ResourceType rt = DataCollectionConfigFactory.getInstance().getConfiguredResourceTypes().get(resourceType);
            if (rt == null) {
                LOG.debug("getJMXResourceType: using default JMX resource type strategy.");
                rt = new ResourceType();
                rt.setName(resourceType);
                rt.setStorageStrategy(new StorageStrategy());
                rt.getStorageStrategy().setClazz(JMXStorageStrategy.class.getName());
                rt.setPersistenceSelectorStrategy(new PersistenceSelectorStrategy());
                rt.getPersistenceSelectorStrategy()
                  .setClazz(PersistAllSelectorStrategy.class.getName());
            }
            JMXResourceType type = new JMXResourceType(agent, rt);

            m_resourceTypeList.put(resourceType, type);
        }
        return m_resourceTypeList.get(resourceType);
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
        return StringUtils.defaultString(AlphaNumeric.parseAndReplace(objectName, '_'), "NULL");
    }

    /**
     * This method is responsible for building a list of RRDDataSource objects
     * from the provided list of MBeanObject objects.
     *
     * @param collectionName
     *                       Collection name
     * @param attributeMap
     * @return list of RRDDataSource objects
     */
    protected static Map<String, JMXDataSource> buildDataSourceList(final String collectionName, final Map<String, List<Attrib>> attributeMap) {
        LOG.debug("buildDataSourceList - ***");

        /*
         * Retrieve the RRD expansion data source list which contains all
         * the expansion data source's. Use this list as a basis
         * for building a data source list for the current interface.
         */
        HashMap<String, JMXDataSource> dsList = new HashMap<String, JMXDataSource>();

        /*
         * Loop through the MBean object list to be collected for this
         * interface and add a corresponding RRD data source object. In this
         * manner each interface will have RRD files create which reflect only
         * the data sources pertinent to it.
         */
        LOG.debug("attributeMap size: {}", attributeMap.size());
        for (Map.Entry<String, List<Attrib>> entry : attributeMap.entrySet()) {
            String objectName = entry.getKey();
            List<Attrib> list = entry.getValue();

            LOG.debug("ObjectName: {}, Attributes: {}", objectName, list.size());

            for (Attrib attr : list) {
                /*
                 * Verify that this object has an appropriate "integer" data
                 * type which can be stored in an RRD database file (must map
                 * to
                 * one of the supported RRD data source types: COUNTER or
                 * GAUGE).
                 */
                String ds_type = JMXDataSource.mapType(attr.getType());
                if (ds_type != null) {
                    /*
                     * Passed!! Create new data source instance for this MBean
                     * object.
                     * Assign heartbeat using formula (2 * step) and hard code
                     * min & max values to "U" ("unknown").
                     */
                    JMXDataSource ds = new JMXDataSource();
                    ds.setHeartbeat(2 * JMXDataCollectionConfigFactory.getInstance().getStep(collectionName));
                    // For completeness, adding a minval option to the variable.
                    String ds_minval = StringUtils.defaultString(attr.getMinval(), "U");
                    ds.setMax(ds_minval);

                    /*
                     * In order to handle counter wraps, we need to set a max
                     * value for the variable.
                     */
                    String ds_maxval = StringUtils.defaultString(attr.getMaxval(), "U");
                    ds.setMax(ds_maxval);
                    ds.setInstance(collectionName);

                    /*
                     * Truncate MBean object name/alias if it exceeds 19 char
                     * max for RRD data source names.
                     */
                    String ds_name = attr.getAlias();
                    if (ds_name.length() > PersistOperationBuilder.MAX_DS_NAME_LENGTH) {
                        LOG.warn("buildDataSourceList: alias '{}' exceeds 19 char maximum for RRD data source names, truncating.", attr.getAlias());
                        char[] temp = ds_name.toCharArray();
                        ds_name = String.copyValueOf(temp, 0, PersistOperationBuilder.MAX_DS_NAME_LENGTH);
                    }
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
                    LOG.warn("buildDataSourceList: Data type '{}' not supported.  Only integer-type data may be stored in RRD.  MBean object '{}' will not be mapped to RRD data source.",
                      attr.getType(), attr.getAlias());
                }
            }
        }

        return dsList;
    }

    /**
     * <p>
     * Setter for the field <code>useFriendlyName</code>.
     * </p>
     *
     * @param useFriendlyName
     *                        a boolean.
     */
    public void setUseFriendlyName(boolean useFriendlyName) {
        this.m_useFriendlyName = useFriendlyName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RrdRepository getRrdRepository(String collectionName) {
        return JMXDataCollectionConfigFactory.getInstance().getRrdRepository(collectionName);
    }

    private void handleCompositeDataAttribute(String objectName, MBeanAttributeInfo mbai, Object attribute, JMXCollectionResource collectionResource, AttributeGroupType attribGroupType) {
    }

}
