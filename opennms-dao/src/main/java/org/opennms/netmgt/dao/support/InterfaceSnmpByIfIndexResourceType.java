/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
