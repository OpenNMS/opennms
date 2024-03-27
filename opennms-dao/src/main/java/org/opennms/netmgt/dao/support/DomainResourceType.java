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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.core.collections.LazyList;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.springframework.orm.ObjectRetrievalFailureException;

import com.google.common.collect.Sets;

/**
 * Domains are top-level resources stored in paths like:
 *   snmp/${domainName}/${interfaceIpAddr}/ds.rrd
 *
 */
public final class DomainResourceType extends AbstractTopLevelResourceType {
    private static final Set<OnmsAttribute> s_emptyAttributeSet = Collections.unmodifiableSet(new HashSet<OnmsAttribute>());
    private final ResourceDao m_resourceDao;
    private final ResourceStorageDao m_resourceStorageDao;

    /**
     * <p>Constructor for DomainResourceType.</p>
     *
     * @param resourceDao a {@link org.opennms.netmgt.dao.api.ResourceDao} object.
     */
    public DomainResourceType(ResourceDao resourceDao, ResourceStorageDao resourceStorageDao) {
        m_resourceDao = resourceDao;
        m_resourceStorageDao = resourceStorageDao;
    }

    /**
     * <p>getLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getLabel() {
        return "Domain";
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return "domain";
    }

    /** {@inheritDoc} */
    @Override
    public String getLinkForResource(OnmsResource resource) {
        return null;
    }

    @Override
    public List<OnmsResource> getTopLevelResources() {
        return findDomainNames().stream()
                .map(this::createResourceForDomain)
                .collect(Collectors.toList());
    }

    @Override
    public OnmsResource getResourceByName(String domain) {
        if (!m_resourceStorageDao.exists(ResourcePath.get(ResourceTypeUtils.SNMP_DIRECTORY, domain), 1)) {
            throw new ObjectRetrievalFailureException(OnmsResource.class, domain, "Top-level resource of type domain could not be found: " + domain, null);
        }
        return createResourceForDomain(domain);
    }

    private Set<String> findDomainNames() {
        Set<String> domainNames = Sets.newTreeSet();

        // Get all of the non-numeric directory names in the RRD directory; these
        // are the names of the domains that have performance data
        for (ResourcePath child : m_resourceStorageDao.children(ResourcePath.get(ResourceTypeUtils.SNMP_DIRECTORY), 2)) {
            try {
                // if the directory name is an integer
                Integer.parseInt(child.getName());
                continue;
            } catch (NumberFormatException e) {
                domainNames.add(child.getName());
            }
        }

        return domainNames;
    }

    private OnmsResource createResourceForDomain(String domain) {
        final ResourcePath path = new ResourcePath(ResourceTypeUtils.SNMP_DIRECTORY, domain);
        final LazyChildResourceLoader loader = new LazyChildResourceLoader(m_resourceDao);          
        final OnmsResource resource = new OnmsResource(domain, domain, this, s_emptyAttributeSet, new LazyList<OnmsResource>(loader), path);
        loader.setParent(resource);
        return resource;
    }

    public static boolean isDomain(OnmsResource resource) {
        return resource != null && resource.getResourceType() instanceof DomainResourceType;
    }
}
