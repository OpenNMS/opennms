//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Apr 10: Store the entity and a guess at a link - dj@opennms.org
// 2007 Apr 05: Remove getRelativePathForAttribute, move attribute loading to
//              ResourceTypeUtils.getAttributesAtRelativePath, and add ifSpeed
//              and ifSpeedFriendly as external values. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.dao.support;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.opennms.core.utils.AlphaNumeric;
import org.opennms.core.utils.LazySet;
import org.opennms.core.utils.SIUtils;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.model.ExternalValueAttribute;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.OnmsSnmpInterface;
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
    
    public List<OnmsResource> getResourcesForNode(int nodeId) {
        OnmsNode node = m_nodeDao.get(nodeId);
        if (node == null) {
            throw new ObjectRetrievalFailureException(OnmsNode.class, Integer.toString(nodeId), "Could not find node with node ID " + nodeId, null);
        }
            
        ArrayList<OnmsResource> resources = new ArrayList<OnmsResource>();

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
            Long ifSpeed = null;
            String ifSpeedFriendly = null;
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
                    ifSpeed = snmpInterface.getIfSpeed();
                    ifSpeedFriendly = SIUtils.getHumanReadableIfSpeed(ifSpeed);
                    if (parenString.length() > 0) {
                        parenString.append(", ");
                    }
                    parenString.append(ifSpeedFriendly);
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

            OnmsResource resource = getResourceByNodeAndInterface(nodeId, intfDir.getName(), label, ifSpeed, ifSpeedFriendly);
            if (snmpInterface != null) {
                Set<OnmsIpInterface> ipInterfaces = snmpInterface.getIpInterfaces();
                if (ipInterfaces.size() > 0) {
                    int id = ipInterfaces.iterator().next().getId();
                    resource.setLink("element/interface.jsp?ipinterfaceid=" + id);
                } else {
                    int ifIndex = snmpInterface.getIfIndex();
                    if(ifIndex > -1) {
                        resource.setLink("element/snmpinterface.jsp?node=" + nodeId + "&ifindex=" + ifIndex);
                    }
                }

                resource.setEntity(snmpInterface);
            }
            resources.add(resource);
        }

        return OnmsResource.sortIntoResourceList(resources);
    }

    private OnmsResource getResourceByNodeAndInterface(int nodeId, String intf, String label, Long ifSpeed, String ifSpeedFriendly) throws DataAccessException {
        Set<OnmsAttribute> set = new LazySet<OnmsAttribute>(new AttributeLoader(Integer.toString(nodeId), intf, ifSpeed, ifSpeedFriendly));
        return new OnmsResource(intf, label, this, set);
    }

    public class AttributeLoader implements LazySet.Loader<OnmsAttribute> {
        private String m_parent;
        private String m_resource;
        private Long m_ifSpeed;
        private String m_ifSpeedFriendly;

        public AttributeLoader(String parent, String resource, Long ifSpeed, String ifSpeedFriendly) {
            m_parent = parent;
            m_resource = resource;
            m_ifSpeed = ifSpeed;
            m_ifSpeedFriendly = ifSpeedFriendly;
        }

        public Set<OnmsAttribute> load() {
            Set<OnmsAttribute> attributes = ResourceTypeUtils.getAttributesAtRelativePath(m_resourceDao.getRrdDirectory(), getRelativePathForResource(m_parent, m_resource));
            if (m_ifSpeed != null) {
                attributes.add(new ExternalValueAttribute("ifSpeed", m_ifSpeed.toString()));
            }
            if (m_ifSpeedFriendly != null) {
                attributes.add(new ExternalValueAttribute("ifSpeedFriendly", m_ifSpeedFriendly));
            }
            return attributes;
        }
        
    }
    
    private String getRelativePathForResource(String parent, String resource) {
        return DefaultResourceDao.SNMP_DIRECTORY
            + File.separator + parent 
            + File.separator + resource;
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
            OnmsResource resource = getResourceByDomainAndInterface(domain, iface); 
            try {
                resource.setLink("element/nodeList.htm?listInterfaces=true&snmpParm=ifAlias&snmpParmMatchType=contains&snmpParmValue=" + URLEncoder.encode(iface, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("URLEncoder.encode complained about UTF-8. " + e, e);
            }
            resources.add(resource);
        }

        return OnmsResource.sortIntoResourceList(resources);
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
        Set<OnmsAttribute> set = new LazySet<OnmsAttribute>(new AttributeLoader(domain, intf, null, null));
        return new OnmsResource(intf, intf, this, set);
    }

    public String getLinkForResource(OnmsResource resource) {
        return null;
    }

}
