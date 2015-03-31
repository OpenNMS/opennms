/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.jmx.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;

import org.opennms.netmgt.config.collectd.jmx.Attrib;
import org.opennms.netmgt.config.collectd.jmx.CompAttrib;
import org.opennms.netmgt.config.collectd.jmx.CompMember;
import org.opennms.netmgt.config.collectd.jmx.JmxCollection;
import org.opennms.netmgt.config.collectd.jmx.Mbean;
import org.opennms.netmgt.jmx.JmxCollector;
import org.opennms.netmgt.jmx.JmxCollectorConfig;
import org.opennms.netmgt.jmx.JmxSampleProcessor;
import org.opennms.netmgt.jmx.connection.JmxConnectionManager;
import org.opennms.netmgt.jmx.connection.JmxServerConnectionException;
import org.opennms.netmgt.jmx.connection.JmxServerConnectionWrapper;
import org.opennms.netmgt.jmx.impl.connection.connectors.DefaultConnectionManager;
import org.opennms.netmgt.jmx.samples.JmxAttributeSample;
import org.opennms.netmgt.jmx.samples.JmxCompositeSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A implementation of the JmxCollector.
 * It iterates over all configured MBeans, collects either attributes or composite members and creates a sample accordingly.
 *
 * @see org.opennms.netmgt.jmx.JmxCollector
 */
public class DefaultJmxCollector implements JmxCollector {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void collect(JmxCollectorConfig config, JmxSampleProcessor sampleProcessor) throws JmxServerConnectionException {
        JmxConnectionManager connectionManager = new DefaultConnectionManager(config.getRetries());
        try (JmxServerConnectionWrapper connectionWrapper = connectionManager.connect(config.getConnectionName(), config.getAgentAddress(), config.getServiceProperties(), null)) {
            Objects.requireNonNull(connectionWrapper, "connectionWrapper should never be null");
            Objects.requireNonNull(connectionWrapper.getMBeanServerConnection(), "connectionWrapper.getMBeanServerConnection() should never be null");

            final MBeanServerConnection concreteConnection = connectionWrapper.getMBeanServerConnection();
            collect(concreteConnection, config.getJmxCollection(), sampleProcessor);
        }
    }

    private void collect(MBeanServerConnection concreteConnection, JmxCollection jmxCollection, JmxSampleProcessor sampleProcessor) {
        try {
            for (Mbean eachMbean : jmxCollection.getMbeans()) {
                logger.debug("Collecting MBean (objectname={}, wildcard={})", eachMbean.getObjectname(), isWildcard(eachMbean.getObjectname()));

                final Collection<ObjectName> objectNames = getObjectNames(concreteConnection, eachMbean.getObjectname());
                for (ObjectName eachObjectName : objectNames) {
                    logger.debug("Collecting ObjectName {}", eachObjectName);

                    boolean collect = canBeCollected(concreteConnection, eachObjectName, eachMbean.getKeyfield(), eachMbean.getExclude());
                    if (collect) {
                        List<String> attributeNames = extractAttributeNames(eachMbean);
                        List<Attribute> attributes = getAttributes(concreteConnection, eachObjectName, attributeNames);

                        for (Attribute eachAttribute : attributes) {
                            if (eachAttribute.getValue() instanceof CompositeData) {
                                CompositeData compositeData = (CompositeData) eachAttribute.getValue();
                                for (CompMember eachCompositeMember : getCompositeMembers(eachMbean, eachAttribute.getName())) {
                                    JmxCompositeSample sample = new JmxCompositeSample(eachMbean, eachAttribute, compositeData, eachCompositeMember);
                                    logger.debug("Collected sample {}", sample);
                                    sampleProcessor.process(sample);
                                }
                            } else {
                                JmxAttributeSample sample = new JmxAttributeSample(eachMbean, eachAttribute);
                                logger.debug("Collected sample {}", sample);
                                sampleProcessor.process(sample);
                            }
                        }
                    } else {
                        logger.debug("Skip ObjectName {}", eachObjectName);
                    }
                }
            }
        } catch (JMException e) {
            logger.error("Could not collect data", e);
        } catch (IOException e) {
            logger.error("Could not communicate with MBeanServer", e);
        }
    }

    /**
     * Checks if a given objectName can be collected.
     * It cannot be collected if it is excluded or not registered, otherwise it can be collected.
     *
     * @return if it can be collected.
     * @throws IOException If an error while communicating with the MBeanServer occurs.
     */
    private boolean canBeCollected(MBeanServerConnection connection, ObjectName objectName, String keyField, String excludeList) throws IOException {
        if (isExcluded(objectName, keyField, excludeList)) {
            logger.debug("ObjectName {} with key {} is in excludeList {}.", objectName, keyField, excludeList);
            return false;
        }
        if (!connection.isRegistered(objectName)) {
            logger.debug("ObjectName {} is not registered.", objectName);
            return false;
        }
        return true;
    }

    /**
     * Get a list of jmx Attributes from the JMX Server.
     * The returned list only contains available attributes.
     * The input list contains all attributes we would like to fetch.
     *
     * @return the list of attributes available at the JMX Server.
     */
    private List<Attribute> getAttributes(MBeanServerConnection concreteConnection, ObjectName eachObjectName, List<String> attributes) throws InstanceNotFoundException, IOException, ReflectionException {
        AttributeList attributeList = concreteConnection.getAttributes(eachObjectName, attributes.toArray(new String[attributes.size()]));
        List<Attribute> newList = new ArrayList<>();
        for (Object eachObject : attributeList) {
            if (eachObject instanceof Attribute) {
                newList.add((Attribute) eachObject);
            }
        }
        return Collections.checkedList(newList, Attribute.class);
    }

    /**
     * Extracts all Composite members from a given composite attribute name.
     *
     * @param bean              The mbean the composite attribute belongs to.
     * @param compAttributeName The composite attribute name.
     * @return A list of all Composite Members of the given composite attribute. May be empty. The list is unmodifiable.
     */
    private List<CompMember> getCompositeMembers(Mbean bean, String compAttributeName) {
        for (CompAttrib eachAttrib : bean.getCompAttribList()) {
            if (Objects.equals(compAttributeName, eachAttrib.getName())) {
                List<CompMember> list = eachAttrib.getCompMemberList();
                return Collections.unmodifiableList(list);
            }
        }
        return Collections.emptyList();
    }

    /**
     * Extracts all Attribute and Composite Attribute Names
     * from the given MBean.
     *
     * @param bean The MBean to extract attributes from.
     * @return An unmodifiable list of Attribute/Composite Attribute Names.
     */
    private List<String> extractAttributeNames(Mbean bean) {
        List<String> attributes = new ArrayList<>();

        for (Attrib eachAttrib : bean.getAttribList()) {
            attributes.add(eachAttrib.getName());
        }

        for (CompAttrib eachCompAttrib : bean.getCompAttribList()) {
            attributes.add(eachCompAttrib.getName());
        }

        return Collections.unmodifiableList(attributes);
    }

    /**
     * Checks if the given objectName is excluded.
     * <p/>
     * An objectName is excluded if the excludeList contains the provided keyField.
     *
     * @param objectName
     * @param keyField
     * @param excludeList A comma-separated list of keys to exclude.
     * @return
     */
    private boolean isExcluded(ObjectName objectName, String keyField, String excludeList) {
        if (excludeList == null || excludeList.isEmpty()) {
            return false; // if no excludeList, cannot be excluded
        }

        // check if the object has that key
        String keyName = objectName.getKeyProperty(keyField);
        if (keyName == null || keyName.isEmpty()) {
            return false;
        }

        // filter out calls if the key field matches
        // an entry in the exclude list
        StringTokenizer st = new StringTokenizer(excludeList, ",");
        while (st.hasMoreTokens()) {
            if (keyName.equals(st.nextToken())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the objectName is a wildcard entry.
     *
     * @param objectName The object Name. May not be null.
     * @return true if objectName contains * otherwise false.
     */
    private boolean isWildcard(String objectName) {
        return objectName.contains("*");
    }

    /**
     * Returns an unmodifiable set of <code>ObjectName</code>s according to the given <code>objectName</code>.
     *
     * @param objectName The objectName to query the server with. May contain wildcards.
     *                   See {@link javax.management.MBeanServer#queryMBeans(javax.management.ObjectName, javax.management.QueryExp)} for details.
     * @return an unmodifiable set of <code>ObjectName</code>s according to the given <code>objectName</code>.
     * @throws MalformedObjectNameException
     * @throws IOException
     */
    private Set<ObjectName> getObjectNames(MBeanServerConnection mbeanServer, String objectName) throws MalformedObjectNameException, IOException {
        Set<ObjectName> objectNames = new HashSet<>();

        // if we have a wildcard in the object Name, we have to query the server for
        // all object names matching that expression
        if (isWildcard(objectName)) {
            Set<ObjectName> retrievedObjectNames = mbeanServer.queryNames(new ObjectName(objectName), null);
            objectNames.addAll(retrievedObjectNames);
        } else {
            // we do not have a wildcard
            objectNames.add(new ObjectName(objectName));
        }
        return Collections.unmodifiableSet(objectNames);
    }
}
