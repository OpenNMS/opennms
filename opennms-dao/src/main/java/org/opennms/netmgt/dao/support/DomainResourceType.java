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
