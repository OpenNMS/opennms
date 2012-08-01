/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.mock;

import java.util.List;

import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;

public class MockResourceType implements OnmsResourceType {
    private String m_name = "nothing but foo";
    private String m_label = "even more foo";
    private String m_link = "http://www.google.com/search?q=opennms";

    public String getLabel() {
        return m_label;
    }

    public String getLinkForResource(OnmsResource resource) {
        return m_link;
    }

    public String getName() {
        return m_name;
    }

    public List<OnmsResource> getResourcesForDomain(String domain) {
        return null;
    }

    public List<OnmsResource> getResourcesForNode(int nodeId) {
        return null;
    }
    
    public List<OnmsResource> getResourcesForNodeSource(String nodeSource, int nodeId) {
        return null;
    }

    public boolean isResourceTypeOnDomain(String domain) {
        return false;
    }

    public boolean isResourceTypeOnNode(int nodeId) {
        return false;
    }

    public void setLink(String link) {
        m_link = link;
    }

    public void setLabel(String label) {
        m_label = label;
    }

    public void setName(String name) {
        m_name = name;
    }

    //@Override
    public boolean isResourceTypeOnNodeSource(String nodeSource, int nodeId) {
        return false;
    }
}
