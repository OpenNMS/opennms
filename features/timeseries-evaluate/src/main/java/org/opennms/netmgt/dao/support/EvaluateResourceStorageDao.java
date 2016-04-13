/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.ResourcePath;

/**
 * The Class EvaluateResourceStorageDao.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class EvaluateResourceStorageDao implements ResourceStorageDao {

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.ResourceStorageDao#exists(org.opennms.netmgt.model.ResourcePath, int)
     */
    @Override
    public boolean exists(ResourcePath path, int depth) {
        return false;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.ResourceStorageDao#existsWithin(org.opennms.netmgt.model.ResourcePath, int)
     */
    @Override
    public boolean existsWithin(ResourcePath path, int depth) {
        return false;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.ResourceStorageDao#children(org.opennms.netmgt.model.ResourcePath, int)
     */
    @Override
    public Set<ResourcePath> children(ResourcePath path, int depth) {
        return new HashSet<ResourcePath>();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.ResourceStorageDao#delete(org.opennms.netmgt.model.ResourcePath)
     */
    @Override
    public boolean delete(ResourcePath path) {
        return false;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.ResourceStorageDao#getAttributes(org.opennms.netmgt.model.ResourcePath)
     */
    @Override
    public Set<OnmsAttribute> getAttributes(ResourcePath path) {
        return new HashSet<OnmsAttribute>();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.ResourceStorageDao#setStringAttribute(org.opennms.netmgt.model.ResourcePath, java.lang.String, java.lang.String)
     */
    @Override
    public void setStringAttribute(ResourcePath path, String key, String value) {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.ResourceStorageDao#getStringAttribute(org.opennms.netmgt.model.ResourcePath, java.lang.String)
     */
    @Override
    public String getStringAttribute(ResourcePath path, String key) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.ResourceStorageDao#getStringAttributes(org.opennms.netmgt.model.ResourcePath)
     */
    @Override
    public Map<String, String> getStringAttributes(ResourcePath path) {
        return new HashMap<String,String>();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.ResourceStorageDao#updateMetricToResourceMappings(org.opennms.netmgt.model.ResourcePath, java.util.Map)
     */
    @Override
    public void updateMetricToResourceMappings(ResourcePath path, Map<String, String> metricsNameToResourceNames) {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.api.ResourceStorageDao#getMetaData(org.opennms.netmgt.model.ResourcePath)
     */
    @Override
    public Map<String, String> getMetaData(ResourcePath path) {
        return new HashMap<String,String>();
    }

}
