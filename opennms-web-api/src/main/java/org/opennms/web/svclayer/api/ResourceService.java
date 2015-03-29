/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.api;

import java.io.File;
import java.util.List;

import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>ResourceService interface.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
@Transactional(readOnly = true)
public interface ResourceService {
    /**
     * <p>getRrdDirectory</p>
     *
     * @return a {@link java.io.File} object.
     */
    public File getRrdDirectory();
    /**
     * <p>findTopLevelResources</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<OnmsResource> findTopLevelResources();
    /**
     * <p>findNodeChildResources</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     * @return a {@link java.util.List} object.
     */
    public List<OnmsResource> findNodeChildResources(OnmsNode node);
    /**
     * <p>findNodeChildResources</p>
     *
     * @param nodeId a int.
     * @return a {@link java.util.List} object.
     */
    public List<OnmsResource> findNodeChildResources(int nodeId);
    /**
     * <p>findDomainChildResources</p>
     *
     * @param domain a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public List<OnmsResource> findDomainChildResources(String domain);
    /**
     * <p>findNodeSourceChildResources</p>
     *
     * @param nodeSource a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public List<OnmsResource> findNodeSourceChildResources(String nodeSource);
    /**
     * <p>findChildResources</p>
     *
     * @param resource a {@link org.opennms.netmgt.model.OnmsResource} object.
     * @param resourceTypeMatches a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public List<OnmsResource> findChildResources(OnmsResource resource, String... resourceTypeMatches);
    /**
     * <p>getResourceById</p>
     *
     * @param id a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsResource} object.
     */
    public OnmsResource getResourceById(String id);
    /**
     * <p>findPrefabGraphsForResource</p>
     *
     * @param resource a {@link org.opennms.netmgt.model.OnmsResource} object.
     * @return an array of {@link org.opennms.netmgt.model.PrefabGraph} objects.
     */
    public PrefabGraph[] findPrefabGraphsForResource(OnmsResource resource);
    /**
     * <p>getPrefabGraph</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.PrefabGraph} object.
     */
    public PrefabGraph getPrefabGraph(String name);
    /**
     * <p>findPrefabGraphsForChildResources</p>
     *
     * @param resource a {@link org.opennms.netmgt.model.OnmsResource} object.
     * @param resourceTypeMatches a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.netmgt.model.PrefabGraph} objects.
     */
    public PrefabGraph[] findPrefabGraphsForChildResources(OnmsResource resource, String... resourceTypeMatches);
    /**
     * <p>promoteGraphAttributesForResource</p>
     *
     * @param resource a {@link org.opennms.netmgt.model.OnmsResource} object.
     */
    public void promoteGraphAttributesForResource(OnmsResource resource);
    /**
     * <p>promoteGraphAttributesForResource</p>
     *
     * @param resourceId a {@link java.lang.String} object.
     */
    public void promoteGraphAttributesForResource(String resourceId);
    /**
     * <p>getResourceListById</p>
     *
     * @param resourceId a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public List<OnmsResource> getResourceListById(String resourceId);
}
