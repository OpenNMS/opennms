package org.opennms.netmgt.dao.support;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.opennms.core.utils.AlphaNumeric;
import org.opennms.core.utils.LazySet;
import org.opennms.core.utils.SIUtils;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * XXX Note: We should remove any graphs from the list that have external
 * values.  See bug #1703.
 */

public class InterfaceSnmpResourceType implements OnmsResourceType {

    private ResourceDao m_resourceDao;
    private NodeDao m_nodeDao;

    public InterfaceSnmpResourceType(ResourceDao resourceDao, NodeDao nodeDao) {
        m_resourceDao = resourceDao;
        m_nodeDao = nodeDao;
    }

    public String getName() {
        return "interfaceSnmp";
    }
    
    public String getLabel() {
        return "SNMP Interface Data";
    }
    
    public boolean isResourceTypeOnNode(int nodeId) {
        return isResourceTypeOnParentResource(Integer.toString(nodeId));
    }
    
    private boolean isResourceTypeOnParentResource(String parentResource) {
        File parent = getParentResourceDirectory(parentResource, false);
        if (!parent.isDirectory()) {
            return false;
        }
        
        return parent.listFiles(RrdFileConstants.INTERFACE_DIRECTORY_FILTER).length > 0; 
    }
    
    private File getParentResourceDirectory(String parentResource, boolean verify) {
        File snmp = new File(m_resourceDao.getRrdDirectory(verify), DefaultResourceDao.SNMP_DIRECTORY);
        
        File parent = new File(snmp, parentResource);
        if (verify && !parent.isDirectory()) {
            throw new ObjectRetrievalFailureException(File.class, "No parent resource directory exists for " + parentResource + ": " + parent);
        }
        
        return parent;
    }
        
    private File getResourceDirectory(String parentResource, String intf, boolean verify) {
        File parent = getParentResourceDirectory(parentResource, verify);
        
        File intfDir = new File(parent, intf);
        if (verify && !parent.isDirectory()) {
            throw new ObjectRetrievalFailureException(File.class, "No interface directory exists for " + intf + ": " + intfDir);
        }
        
        return intfDir;
    }
    
    public List<OnmsResource> getResourcesForNode(int nodeId) {
        OnmsNode node = m_nodeDao.get(nodeId);
        if (node == null) {
            throw new ObjectRetrievalFailureException(OnmsNode.class, Integer.toString(nodeId), "Could not find node with node ID " + nodeId, null);
        }
            
        ArrayList<OnmsResource> resources =
            new ArrayList<OnmsResource>();

        File parent = getParentResourceDirectory(Integer.toString(nodeId), true);
        File[] intfDirs = parent.listFiles(RrdFileConstants.INTERFACE_DIRECTORY_FILTER);

        Set<OnmsSnmpInterface> snmpInterfaces = node.getSnmpInterfaces();
        Map<String, OnmsSnmpInterface> intfMap = new HashMap<String, OnmsSnmpInterface>();

        for (OnmsSnmpInterface snmpInterface : snmpInterfaces) {
            /*
             * When Cisco Express Forwarding (CEF) or some ATM encapsulations
             * (AAL5) are used on Cisco routers, an additional entry might be 
             * in the ifTable for these sub-interfaces, but there is no
             * performance data available for collection.  This check excludes
             * ifTable entries where ifDescr contains "-cef".  See bug #803.
             */
            if (snmpInterface.getIfDescr() != null) {
                if (Pattern.matches(".*-cef.*", snmpInterface.getIfDescr())) {
                    continue;
                }
            }

            String replacedIfName = AlphaNumeric.parseAndReplace(snmpInterface.getIfName(), '_');
            String replacedIfDescr = AlphaNumeric.parseAndReplace(snmpInterface.getIfDescr(), '_');
            
            String[] keys = new String[] {
                    replacedIfName + "-",
                    replacedIfDescr + "-",
                    replacedIfName + "-" + snmpInterface.getPhysAddr(),
                    replacedIfDescr + "-" + snmpInterface.getPhysAddr()
            };
            
            for (String key : keys) {
                if (!intfMap.containsKey(key)) {
                    intfMap.put(key, snmpInterface);
                }
            }
        }

        for (File intfDir : intfDirs) {
            String name = intfDir.getName();
            
            String desc = name;
            String mac = "";

            // Strip off the MAC address from the end, if there is one
            int dashIndex = name.lastIndexOf("-");

            if (dashIndex >= 0) {
                desc = name.substring(0, dashIndex);
                mac = name.substring(dashIndex + 1, name.length());
            }
            
            String key = desc + "-" + mac; 
            OnmsSnmpInterface snmpInterface = intfMap.get(key);
            
            String label;
            if (snmpInterface == null) {
                label = name + " (Not Currently Updated)";
            } else {
                StringBuffer descr = new StringBuffer();
                StringBuffer parenString = new StringBuffer();

                if (snmpInterface.getIfAlias() != null) {
                    parenString.append(snmpInterface.getIfAlias());
                }
                if ((snmpInterface.getIpAddress() != null) && !snmpInterface.getIpAddress().equals("0.0.0.0")) {
                    String ipaddr = snmpInterface.getIpAddress();
                    if (parenString.length() > 0) {
                        parenString.append(", ");
                    }
                    parenString.append(ipaddr);
                }
                if ((snmpInterface.getIfSpeed() != null) && (snmpInterface.getIfSpeed() != 0)) {
                    long ifSpeed = snmpInterface.getIfSpeed();
                    String speed = SIUtils.getHumanReadableIfSpeed(ifSpeed);
                    if (parenString.length() > 0) {
                        parenString.append(", ");
                    }
                    parenString.append(speed);
                }

                if (snmpInterface.getIfName() != null) {
                    descr.append(snmpInterface.getIfName());
                } else if (snmpInterface.getIfDescr() != null) {
                    descr.append(snmpInterface.getIfDescr());
                } else {
                    /*
                     * Should never reach this point, since ifLabel is based on
                     * the values of ifName and ifDescr but better safe than sorry.
                     */
                    descr.append(name);
                }

                /* Add the extended information in parenthesis after the ifLabel,
                 * if such information was found.
                 */
                if (parenString.length() > 0) {
                    descr.append(" (");
                    descr.append(parenString);
                    descr.append(")");
                }

                label = descr.toString();
            }

            OnmsResource resource = getResourceByNodeAndInterface(nodeId, intfDir.getName(), label);
            resources.add(resource);
        }

        return OnmsResource.sortIntoResourceList(resources);
    }

    private OnmsResource getResourceByNodeAndInterface(int nodeId,
            String intf, String label) throws DataAccessException {
        Set<OnmsAttribute> set =
            new LazySet<OnmsAttribute>(new AttributeLoader(getResourceDirectory(Integer.toString(nodeId), intf, true)));
        return new OnmsResource(intf, label, this, set);
    }

    public class AttributeLoader implements LazySet.Loader<OnmsAttribute> {
        private File m_intfDir;

        public AttributeLoader(File intfDir) {
            m_intfDir = intfDir;
        }

        public Set<OnmsAttribute> load() {
            List<String> dataSources =
                ResourceTypeUtils.getDataSourcesInDirectory(m_intfDir);
            Set<OnmsAttribute> attributes =
                new HashSet<OnmsAttribute>(dataSources.size());
            
            for (String dataSource : dataSources) {
                attributes.add(new RrdGraphAttribute(dataSource));
            }
            
            return attributes;
        }
        
    }

    public String getRelativePathForAttribute(String resourceParent, String resource, String attribute) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(DefaultResourceDao.SNMP_DIRECTORY);
        buffer.append(File.separator);
        buffer.append(resourceParent);
        buffer.append(File.separator);
        buffer.append(resource);
        buffer.append(File.separator);
        buffer.append(attribute);
        buffer.append(RrdFileConstants.getRrdSuffix());
        return buffer.toString();
    }

    /**
     * This resource type is never available for domains.
     * Only the interface resource type is available for domains.
     */
    public boolean isResourceTypeOnDomain(String domain) {
        return getQueryableInterfacesForDomain(domain).size() > 0;
    }
    
    public List<OnmsResource> getResourcesForDomain(String domain) {
        ArrayList<OnmsResource> resources =
            new ArrayList<OnmsResource>();

        List<String> ifaces = getQueryableInterfacesForDomain(domain);
        for (String iface : ifaces) {
            resources.add(getResourceByDomainAndInterface(domain, iface));
        }

        return resources;
    }
    
    private List<String> getQueryableInterfacesForDomain(String domain) {
        if (domain == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        ArrayList<String> intfs = new ArrayList<String>();
        File snmp = new File(m_resourceDao.getRrdDirectory(), DefaultResourceDao.SNMP_DIRECTORY);
        File domainDir = new File(snmp, domain);

        if (!domainDir.exists() || !domainDir.isDirectory()) {
            throw new IllegalArgumentException("No such directory: " + domainDir);
        }

        File[] intfDirs = domainDir.listFiles(RrdFileConstants.INTERFACE_DIRECTORY_FILTER);

        if (intfDirs != null && intfDirs.length > 0) {
            intfs.ensureCapacity(intfDirs.length);
            for (int i = 0; i < intfDirs.length; i++) {
                intfs.add(intfDirs[i].getName());
            }
        }

        return intfs;
    }

    private OnmsResource getResourceByDomainAndInterface(String domain, String intf) {
        Set<OnmsAttribute> set =
            new LazySet<OnmsAttribute>(new AttributeLoader(getResourceDirectory(domain, intf, true)));
        return new OnmsResource(intf, intf, this, set);
    }

    public String getLinkForResource(OnmsResource resource) {
        return null;
    }

}
