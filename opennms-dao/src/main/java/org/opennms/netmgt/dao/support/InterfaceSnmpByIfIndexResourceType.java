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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.ResourceTypeUtils;

import com.google.common.collect.Sets;

/**
 * This resource type provides an alias for the {@link InterfaceSnmpResourceType},
 * allowing SNMP interfaces to be queried using the ifIndex instead of the interface
 * name as stored in the metric repository.
 *
 * i.e. interfaceSnmpByIfIndex[2] vs interfaceSnmp[em1-74867ad4b828]
 *
 */
public class InterfaceSnmpByIfIndexResourceType implements OnmsResourceType {

    private final InterfaceSnmpResourceType m_interfaceSnmpResourceType;

    public static final String TYPE_NAME = "interfaceSnmpByIfIndex";

    protected InterfaceSnmpByIfIndexResourceType(InterfaceSnmpResourceType interfaceSnmpResourceType) {
        m_interfaceSnmpResourceType = Objects.requireNonNull(interfaceSnmpResourceType);
    }

    @Override
    public String getName() {
        return TYPE_NAME;
    }

    @Override
    public String getLabel() {
        return "SNMP Interface Data (by ifIndex)";
    }

    /** {@inheritDoc} */
    @Override
    public String getLinkForResource(OnmsResource resource) {
        return m_interfaceSnmpResourceType.getLinkForResource(resource);
    }

    @Override
    public boolean isResourceTypeOnParent(OnmsResource parent) {
        // We're strictly an alias, so we don't want this resource to appear while enumerating
        return false;
    }

    @Override
    public List<OnmsResource> getResourcesForParent(OnmsResource parent) {
        // We're strictly an alias, so we don't want this resource to appear while enumerating
        return Collections.emptyList();
    }

    /** {@inheritDoc} */
    @Override
    public OnmsResource getChildByName(final OnmsResource parent, final String name) {
        // Grab the node entity
        final OnmsNode node = ResourceTypeUtils.getNodeFromResource(parent);

        // Determine the ifIndex from the given name
        final int ifIndex = Integer.parseInt(name);

        // Find the associated SNMP interface
        final OnmsSnmpInterface snmpInterface = node.getSnmpInterfaceWithIfIndex(ifIndex);
        if (snmpInterface == null) {
            return null;
        }

        // Compute the set of possible keys at which the interface resource may be stored
        final Set<String> candidateInterfaceKeys = Sets.newHashSet();
        candidateInterfaceKeys.addAll(Arrays.asList(InterfaceSnmpResourceType.getKeysFor(snmpInterface)));

        // Enumerate all of the available interfaces on the parent, and use the first match
        final Optional<String> path = m_interfaceSnmpResourceType.getQueryableInterfaces(parent).stream()
                .filter(candidateInterfaceKeys::contains)
                .findFirst();
        if (!path.isPresent()) {
            return null;
        }

        // Retrieve the resource directly from the interfaceSnmpResource type as though it was
        // queried directly by name
        return m_interfaceSnmpResourceType.getChildByName(parent, path.get());
    }
}
