/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 Apr 05: Created this file based on BogusResourceType from
 *              DefaultDistributedStatusServiceTest. - dj@opennms.org
 *
 * Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.dao.support;

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

    public String getRelativePathForAttribute(String resourceParent, String resource, String attribute) {
        return "we don't need no stinkin' relative paths!";
    }

    public List<OnmsResource> getResourcesForDomain(String domain) {
        return null;
    }

    public List<OnmsResource> getResourcesForNode(int nodeId) {
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
}
