/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.mock;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;

public class MockResourceDao implements ResourceDao {

    @Override
    public File getRrdDirectory() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public File getRrdDirectory(boolean verify) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Collection<OnmsResourceType> getResourceTypes() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsResource getResourceById(String id) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsResource> getResourceListById(String id) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsResource> findDomainResources() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsResource> findTopLevelResources() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsResource getResourceForNode(OnmsNode node) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsResource getResourceForIpInterface(OnmsIpInterface ipInterface) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public OnmsResource getResourceForIpInterface(OnmsIpInterface ipInterface, OnmsLocationMonitor locationMonitor) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

}
