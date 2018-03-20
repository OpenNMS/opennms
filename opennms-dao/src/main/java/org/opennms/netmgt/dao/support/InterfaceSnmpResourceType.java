/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.support;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.opennms.core.collections.LazySet;
import org.opennms.core.utils.AlphaNumeric;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.SIUtils;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.model.ExternalValueAttribute;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Interface SNMP resources are stored in paths like:
 *   snmp/1/${IfName}/ds.rrd
 *
 */
public class InterfaceSnmpResourceType implements OnmsResourceType {

    private static final Logger LOG = LoggerFactory.getLogger(InterfaceSnmpResourceType.class);

    private final ResourceStorageDao m_resourceStorageDao;

    /**
     * <p>Constructor for InterfaceSnmpResourceType.</p>
     *
     * @param resourceStorageDao a {@link org.opennms.netmgt.dao.api.ResourceStorageDao} object.
     */
    public InterfaceSnmpResourceType(ResourceStorageDao resourceStorageDao) {
        m_resourceStorageDao = resourceStorageDao;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return "interfaceSnmp";
    }

    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getLabel() {
        return "SNMP Interface Data";
    }

    /** {@inheritDoc} */
    @Override
    public String getLinkForResource(OnmsResource resource) {
        return null;
    }

    @Override
    public boolean isResourceTypeOnParent(OnmsResource parent) {
        return m_resourceStorageDao.exists(parent.getPath(), 1);
    }

    @Override
    public List<OnmsResource> getResourcesForParent(OnmsResource parent) {
        final Set<String> ifaces = getQueryableInterfaces(parent);
        if (NodeResourceType.isNode(parent)) {
            OnmsNode node = ResourceTypeUtils.getNodeFromResource(parent);
            return getNodeResources(parent.getPath(), ifaces, node);
        } else if (DomainResourceType.isDomain(parent)) {
            return getDomainResources(parent.getPath(), ifaces);
        } else {
            return Collections.emptyList();
        }
    }

    /** {@inheritDoc} */
    @Override
    public OnmsResource getChildByName(final OnmsResource parent, final String name) {
        if (DomainResourceType.isDomain(parent)) {
            // Load all of the resources and search when dealing with domains.
            // This is not efficient, but resources of this type should be sparse.
            for (final OnmsResource resource : getResourcesForParent(parent)) {
                if (resource.getName().equals(name)) {
                    return resource;
                }
            }
            throw new ObjectRetrievalFailureException(OnmsResource.class, "No child with name '" + name + "' found on '" + parent + "'");
        }

        // Grab the node entity
        final OnmsNode node = ResourceTypeUtils.getNodeFromResource(parent);

        // Verify that the requested resource exists
        final ResourcePath resourcePath = new ResourcePath(parent.getPath(), name);
        if (!m_resourceStorageDao.exists(resourcePath, 0)) {
            throw new ObjectRetrievalFailureException(OnmsResource.class, "No resource with name '" + name + "' found.");
        }

        // Leverage the existing function for retrieving the resource list
        final List<OnmsResource> resources = getNodeResources(parent.getPath(), Sets.newHashSet(name), node);
        if (resources.size() != 1) {
            throw new ObjectRetrievalFailureException(OnmsResource.class, "No resource with name '" + name + "' found.");
        }

        final OnmsResource resource = resources.get(0);
        resource.setParent(parent);
        return resource;
    }

    protected static String[] getKeysFor(OnmsSnmpInterface snmpInterface) {
        /*
         * When Cisco Express Forwarding (CEF) or some ATM encapsulations
         * (AAL5) are used on Cisco routers, an additional entry might be
         * in the ifTable for these sub-interfaces, but there is no
         * performance data available for collection.  This check excludes
         * ifTable entries where ifDescr contains "-cef".  See bug #803.
         */
        if (snmpInterface.getIfDescr() != null) {
            if (Pattern.matches(".*-cef.*", snmpInterface.getIfDescr())) {
                return new String[0];
            }
        }

        String replacedIfName = AlphaNumeric.parseAndReplace(snmpInterface.getIfName(), '_');
        String replacedIfDescr = AlphaNumeric.parseAndReplace(snmpInterface.getIfDescr(), '_');

        return new String[] {
                replacedIfName + "-",
                replacedIfDescr + "-",
                replacedIfName + "-" + snmpInterface.getPhysAddr(),
                replacedIfDescr + "-" + snmpInterface.getPhysAddr()
        };
    }

    private static String getKeyFor(String intfName) {
        String desc = intfName;
        String mac = "";

        // Strip off the MAC address from the end, if there is one
        int dashIndex = intfName.lastIndexOf('-');

        if (dashIndex >= 0) {
            desc = intfName.substring(0, dashIndex);
            mac = intfName.substring(dashIndex + 1, intfName.length());
        }

        return desc + "-" + mac;
    }

    private List<OnmsResource> getNodeResources(ResourcePath parent, Set<String> intfNames, OnmsNode node) {
            
        ArrayList<OnmsResource> resources = new ArrayList<>();

        Set<OnmsSnmpInterface> snmpInterfaces = node.getSnmpInterfaces();
        Map<String, OnmsSnmpInterface> intfMap = new HashMap<String, OnmsSnmpInterface>();

        for (OnmsSnmpInterface snmpInterface : snmpInterfaces) {
            for (String key : getKeysFor(snmpInterface)) {
                if (!intfMap.containsKey(key)) {
                    intfMap.put(key, snmpInterface);
                }
            }
        }

        for (String intfName : intfNames) {
            String key = getKeyFor(intfName);
            OnmsSnmpInterface snmpInterface = intfMap.get(key);
            
            String label;
            Long ifSpeed = null;
            String ifSpeedFriendly = null;
            if (snmpInterface == null) {
                label = intfName + " (*)";
            } else {
                final StringBuilder descr = new StringBuilder();
                final StringBuilder parenString = new StringBuilder();

                if (snmpInterface.getIfAlias() != null) {
                    parenString.append(snmpInterface.getIfAlias());
                }
                // Append all of the IP addresses on this ifindex
                for (OnmsIpInterface ipif : snmpInterface.getIpInterfaces()) {
                    String ipaddr = InetAddressUtils.str(ipif.getIpAddress());
                    if (!"0.0.0.0".equals(ipaddr)) {
                        if (parenString.length() > 0) {
                            parenString.append(", ");
                        }
                        parenString.append(ipaddr);
                    }
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
                    descr.append(intfName);
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

            OnmsResource resource = getResourceByParentPathAndInterface(parent, intfName, label, snmpInterface);
            if (snmpInterface != null) {
                Set<OnmsIpInterface> ipInterfaces = snmpInterface.getIpInterfaces();
                if (ipInterfaces.size() > 0) {
                    int id = ipInterfaces.iterator().next().getId();
                    resource.setLink("element/interface.jsp?ipinterfaceid=" + id);
                } else {
                    int ifIndex = snmpInterface.getIfIndex();
                    if(ifIndex > -1) {
                        resource.setLink("element/snmpinterface.jsp?node=" + node.getNodeId() + "&ifindex=" + ifIndex);
                    }
                }

                resource.setEntity(snmpInterface);
            } else {
                LOG.debug("populateResourceList: snmpInterface is null");
            }
            LOG.debug("populateResourceList: adding resource toString {}", resource.toString());
            resources.add(resource);
        }
        
        return resources; 
    }

    private List<OnmsResource> getDomainResources(ResourcePath parent, Set<String> intfNames) {
        final List<OnmsResource> resources = Lists.newLinkedList();
        for (String intfName : intfNames) {
            OnmsResource resource = getResourceByParentPathAndInterface(parent, intfName); 
            try {
                resource.setLink("element/nodeList.htm?listInterfaces=true&snmpParm=ifAlias&snmpParmMatchType=contains&snmpParmValue=" + URLEncoder.encode(intfName, StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("URLEncoder.encode complained about UTF-8. " + e, e);
            }
            resources.add(resource);
        }
        return resources;
    }

    protected Set<String> getQueryableInterfaces(OnmsResource parent) {
        if (!NodeResourceType.isNode(parent) && !DomainResourceType.isDomain(parent)) {
            return Collections.emptySet();
        }

        return m_resourceStorageDao.children(parent.getPath(), 1).stream()
                .map(ResourcePath::getName)
                .collect(Collectors.toSet());
    }

    private OnmsResource getResourceByParentPathAndInterface(ResourcePath parent, String intf) {
        final ResourcePath path = ResourcePath.get(parent, intf);
        final LazyResourceAttributeLoader loader = new LazyResourceAttributeLoader(m_resourceStorageDao, path);
        final Set<OnmsAttribute> set = new LazySet<OnmsAttribute>(loader);
        return new OnmsResource(intf, intf, this, set, path);
    }

    private OnmsResource getResourceByParentPathAndInterface(ResourcePath parent, String intf, String label, OnmsSnmpInterface snmpInterface) throws DataAccessException {
        final ResourcePath path = ResourcePath.get(parent, intf);
        final AttributeLoader loader = new AttributeLoader(m_resourceStorageDao, path, snmpInterface);
        final Set<OnmsAttribute> set = new LazySet<OnmsAttribute>(loader);
        return new OnmsResource(intf, label, this, set, path);
    }

    private static class AttributeLoader implements LazySet.Loader<OnmsAttribute> {
        private final ResourceStorageDao m_resourceStorageDao;
        private final ResourcePath m_path;
        private final OnmsSnmpInterface m_snmpInterface;

        public AttributeLoader(ResourceStorageDao resourceStorageDao, ResourcePath path,
                OnmsSnmpInterface snmpInterface) {
            m_resourceStorageDao = resourceStorageDao;
            m_path = path;
            m_snmpInterface = snmpInterface;
        }

        @Override
        public Set<OnmsAttribute> load() {
            Set<OnmsAttribute> attributes = m_resourceStorageDao.getAttributes(m_path);
            if (m_snmpInterface != null) {
                attributes.add(new ExternalValueAttribute("nodeId", m_snmpInterface.getNodeId().toString()));
                attributes.add(new ExternalValueAttribute("ifIndex", m_snmpInterface.getIfIndex().toString()));
                if (m_snmpInterface.getIfSpeed() != null) {
                    String ifSpeedFriendly = SIUtils.getHumanReadableIfSpeed(m_snmpInterface.getIfSpeed());
                    attributes.add(new ExternalValueAttribute("ifSpeed", m_snmpInterface.getIfSpeed().toString()));
                    attributes.add(new ExternalValueAttribute("ifSpeedFriendly", ifSpeedFriendly));
                }
            }
            return attributes;
        }
    }
}
