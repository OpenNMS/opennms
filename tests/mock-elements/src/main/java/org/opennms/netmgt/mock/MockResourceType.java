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

package org.opennms.netmgt.mock;

import java.util.Collections;
import java.util.List;

import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;

public class MockResourceType implements OnmsResourceType {
    private String m_name = "nothingButFoo";
    private String m_label = "evenMoreFoo";
    private String m_link = "http://www.google.com/search?q=opennms";

    @Override
    public String getLabel() {
        return m_label;
    }

    @Override
    public String getLinkForResource(OnmsResource resource) {
        return m_link;
    }

    @Override
    public String getName() {
        return m_name;
    }

    @Override
    public boolean isResourceTypeOnParent(OnmsResource parent) {
        return false;
    }

    @Override
    public List<OnmsResource> getResourcesForParent(OnmsResource parent) {
        return Collections.emptyList();
    }

    @Override
    public OnmsResource getChildByName(OnmsResource parent, String name) {
        return null;
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
