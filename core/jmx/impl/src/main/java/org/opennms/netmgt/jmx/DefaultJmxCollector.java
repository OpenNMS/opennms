package org.opennms.netmgt.jmx;

import org.opennms.netmgt.config.collectd.jmx.Attrib;
import org.opennms.netmgt.config.collectd.jmx.CompAttrib;
import org.opennms.netmgt.config.collectd.jmx.CompMember;
import org.opennms.netmgt.config.collectd.jmx.JmxCollection;
import org.opennms.netmgt.config.collectd.jmx.Mbean;
import org.opennms.netmgt.jmx.connection.JmxConnectionManager;
import org.opennms.netmgt.jmx.connection.JmxServerConnectionException;
import org.opennms.netmgt.jmx.connection.JmxServerConnectionWrapper;
import org.opennms.netmgt.jmx.connection.connectors.DefaultConnectionManager;
import org.opennms.netmgt.jmx.samples.JmxAttributeSample;
import org.opennms.netmgt.jmx.samples.JmxCompositeSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;

// TODO mvr refactoring der ganzen anderen ConnectionFActories
// TODO mvr JmxCollector sollte nicht mehr das "connect" implementieren.
public class DefaultJmxCollector implements JmxCollector {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void collect(JmxCollectorConfig config, JmxSampleProcessor sampleProcessor) throws JmxServerConnectionException {
        JmxConnectionManager connectionManager = new DefaultConnectionManager(config.getRetries());
        try (JmxServerConnectionWrapper connectionWrapper = connectionManager.connect(config.getConnectionName(), config.getAgentAddress(), config.getServiceProperties(), null)) {
            Assert.notNull(connectionWrapper, "connectionWrapper should never be null");
            Assert.notNull(connectionWrapper.getMBeanServerConnection(), "connectionWrapper.getMBeanServerConnection() should never be null");

            final MBeanServerConnection concreteConnection = connectionWrapper.getMBeanServerConnection();
            collect(concreteConnection, config.getJmxCollection(), sampleProcessor);
        }
    }

    // TODO mvr wir brauchen connectionName (e.g. jsr160, jboss, usw, um zu entscheiden wie die Connection aufgebaut werden soll...)
    private void collect(MBeanServerConnection concreteConnection, JmxCollection jmxCollection, JmxSampleProcessor sampleProcessor) {
        try {
            for (Mbean eachMbean : jmxCollection.getMbeans().getMbeanCollection()) {
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
                                    JmxCompositeSample sample = new JmxCompositeSample(eachMbean, eachObjectName, eachAttribute, compositeData, eachCompositeMember);
                                    logger.debug("Collected sample {}", sample);
                                    sampleProcessor.process(sample);
                                }
                            } else {
                                JmxAttributeSample sample = new JmxAttributeSample(eachMbean, eachObjectName, eachAttribute);
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
        }  catch (IOException e) {
            logger.error("Could not communicate with MBeanServer", e);
        }
    }

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

    // TODO mvr should we really do it this way?
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

    private List<CompMember> getCompositeMembers(Mbean bean, String compAttributeName) {
        for (CompAttrib eachAttrib : bean.getCompAttrib()) {
            if (Objects.equals(compAttributeName, eachAttrib.getName())) {
                List<CompMember> list = Arrays.asList(eachAttrib.getCompMember());
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

        for (Attrib eachAttrib : bean.getAttrib()) {
            attributes.add(eachAttrib.getName());
        }

        for (CompAttrib eachCompAttrib : bean.getCompAttrib()) {
            attributes.add(eachCompAttrib.getName());
        }

        return Collections.unmodifiableList(attributes);
    }

    /**
     * Checks if the given objectName is excluded.
     *
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
     * @param objectName The object Name. May not be null.
     * @return true if objectName contains * otherwise false.
     */
    private boolean isWildcard(String objectName) {
        return objectName.contains("*");
    }

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
