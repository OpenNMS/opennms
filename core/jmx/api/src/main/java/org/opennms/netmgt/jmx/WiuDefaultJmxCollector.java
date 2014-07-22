package org.opennms.netmgt.jmx;

import org.opennms.netmgt.config.BeanInfo;
import org.opennms.netmgt.config.service.Attribute;
import org.opennms.netmgt.provision.support.jmx.connectors.ConnectionWrapper;

import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class WiuDefaultJmxCollector implements WiuJmxCollector {

//    private static Logger LOG = LoggerFactory.getLogger(WiuDefaultJmxCollector.class);


    public static class WiuJmxSample {

    }

    public void wiu(WiuJmxConfig wiuConfig, WiuCallback wiuCallback) {
        try (ConnectionWrapper connection = connect(wiuConfig.getServiceProperties(), wiuConfig.getAgentAddress())) {
            doCollect(connection, wiuConfig, wiuCallback);
        }
    }

    private ConnectionWrapper connect(Map<String, Object> serviceProperties, InetAddress agentAddress) {
        return null;
    }


    private void doCollect(ConnectionWrapper connection, WiuJmxConfig wiuConfig, WiuCallback wiuCallback) {
            MBeanServerConnection mbeanServer = connection.getMBeanServer();

            for (int attempts = 0; attempts <= wiuConfig.getRetries(); attempts++) {
//                try {
                    for (BeanInfo beanInfo : wiuConfig.getMbeans().values()) {
                        flattenCompAttributes(beanInfo);

                        String mbeanName = beanInfo.getMbeanName();
                        String objectName = beanInfo.getObjectName();
                        String excludeList = beanInfo.getExcludes();

                        //All JMX collected values are per node
                        String obj = wiuConfig.isUseMbeanForRrds() ? mbeanName : objectName;
                        String groupName = fixGroupName(obj);

                        //LOG.debug(" JMXCollector: processed the following attributes: {}", attribNames);
                        //LOG.debug(" JMXCollector: processed the following Composite Attributes: {}", compAttribNames);

//                        String[] attrNames = attribNames.toArray(new String[attribNames.size()]);

                        if (!isWildcard(objectName)) {
//                            LOG.debug("{} Collector - getAttributes: {}, # attributes: {}, # composite attribute members: {}", serviceName, objectName, attrNames.length, compAttribNames.size());
                            try {
                                ObjectName oName = new ObjectName(objectName);
                                if (mbeanServer.isRegistered(oName)) {
                                    AttributeList attrList = mbeanServer.getAttributes(oName, attrNames);
                                    for (Object attribute : attrList) {
                                        List<String> compositeMemberKeys = new ArrayList<String>();
                                        Boolean isComposite = false;
                                        Attribute attrib = (Attribute) attribute;
                                        // TODO mvr this is magic... we had this before...
                                        // I guess this is the revert magic which was done in getAttributeMap in JmxConfigFactory....
                                        for (String compAttrName : compAttribNames) {
                                            String[] attribKeys = compAttrName.split("\\|", -1);
                                            if (attrib.getName().equals(attribKeys[0])) {
                                                compositeMemberKeys.add(attribKeys[1]);
                                                isComposite = true;
                                            }
                                        }
                                        if (isComposite) {
                                            try {
                                                CompositeData cd = (CompositeData) attrib.getValue();
                                                for (String key : compositeMemberKeys) {
                                                    JMXDataSource ds = wiuConfig.getDataSourceMap().get(objectName + "|" + attrib.getName() + "|" + key);
                                                    JMXCollectionAttributeType attribType = new JMXCollectionAttributeType(ds, null, null, attribGroupType);
                                                    wiuCallback.addAttribute(attribType, cd.get(key).toString());
                                                }
                                            } catch (final ClassCastException cce) {
                                                LOG.debug("{} Collection - getAttributes (try CompositeData) - ERROR: Failed to cast attribute value to type CompositeData!", serviceName, cce);
                                            }
                                        } else {
                                            // this is a normal attribute, so fallback to default handler
                                            JMXDataSource ds = wiuConfig.getDataSourceMap().get(objectName + "|" + attrib.getName());
                                            JMXCollectionAttributeType attribType = new JMXCollectionAttributeType(ds, null, null, attribGroupType);
                                            wiuCallback.addAttribute(attribType, attrib.getValue().toString());
                                        }
                                    }
                                }
                            } catch (final InstanceNotFoundException e) {
//                                LOG.error("Unable to retrieve attributes from {}", objectName, e);
                            }
                        } else {
                            /*
                             * This section is for ObjectNames that use the
                             * '*' wildcard
                             */
                            Set<ObjectName> mbeanSet = getObjectNames(mbeanServer, objectName);
                            for (Iterator<ObjectName> objectNameIter = mbeanSet.iterator(); objectNameIter.hasNext(); ) {
                                ObjectName oName = objectNameIter.next();
//                                LOG.debug("{} Collector - getAttributesWC: {}, # attributes: {}, alias: {}", serviceName, oName, attrNames.length, beanInfo.getKeyAlias());

                                if (!isExcluded(oName, beanInfo, excludeList)) {
                                    // the exclude list doesn't apply
                                    if (mbeanServer.isRegistered(oName)) {
                                        AttributeList attrList = mbeanServer.getAttributes(oName, attrNames);
                                        for (Object attribute : attrList) {
                                            Attribute attrib = (Attribute) attribute;
                                            JMXDataSource ds = wiuConfig.getDataSourceMap().get(objectName + "|" + attrib.getName());
                                            JMXCollectionAttributeType attribType =
                                                    new JMXCollectionAttributeType(ds,
                                                            oName.getKeyProperty(beanInfo.getKeyField()),
                                                            beanInfo.getKeyAlias(),
                                                            attribGroupType);

                                            wiuCallback.addAttribute(attribType, attrib.getValue().toString());
                                        }

                                    }
                                }
//                                } catch (final InstanceNotFoundException e) {
//                                    LOG.error("Error retrieving attributes for {}", oName, e);
//                                }
//                            }
                        }
                    }
//                    break;
//                } catch (final Exception e) {
//                    LOG.debug("{} Collector.collect: IOException while collecting address: {}, {}", serviceName, wiuConfig.getAgentAddress(), e);
//                }
//            }
        }

//        collectionSet.setStatus(ServiceCollector.COLLECTION_SUCCEEDED);
//        return collectionSet;
    }

    private boolean isExcluded(ObjectName objectName, BeanInfo beanInfo, String excludeList) {
        if (excludeList == null) {
            return false; // if no excludeList, cannot be excluded
        }

        /*
        * filter out calls if the key field
        * matches an entry in the exclude
        * list
        */
        String keyName = objectName.getKeyProperty(beanInfo.getKeyField());
        StringTokenizer st = new StringTokenizer(excludeList, ",");
        while (st.hasMoreTokens()) {
            if (keyName.equals(st.nextToken())) {
                return true;
            }
        }
        return false;
    }

    private boolean isWildcard(String objectName) {
        return objectName.contains("*");
    }

    private void flattenCompAttributes(BeanInfo beanInfo) {
        List<String> attribNames = beanInfo.getAttributeNames();
        List<String> compAttribNames = beanInfo.getCompositeAttributeNames();

        for (String compAttribName : compAttribNames) {
            if (attribNames.contains(compAttribName)) {
                attribNames.remove(compAttribName);
                String[] ac = compAttribName.split("\\|", -1);
                String attrName = ac[0];
                if (!attribNames.contains(attrName)) {
                    attribNames.add(attrName);
                }
            }
        }
    }

    private static class MBeanServerConnectionException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    private Set<ObjectName> getObjectNames(MBeanServerConnection mbeanServer, String objectName) throws IOException, MalformedObjectNameException {
        return mbeanServer.queryNames(new ObjectName(objectName), null);
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
    private String fixGroupName(String objectName) {
        if (objectName == null) {
            return "NULL";
        }
        return org.opennms.core.utils.AlphaNumeric.parseAndReplace(objectName, '_');
    }
}
