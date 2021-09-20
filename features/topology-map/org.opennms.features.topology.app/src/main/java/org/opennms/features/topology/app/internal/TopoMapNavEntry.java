/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal;

import javax.servlet.http.HttpServletRequest;

import org.opennms.features.topology.link.TopologyLinkBuilder;
import org.opennms.features.topology.link.TopologyProvider;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.navigate.ConditionalPageNavEntry;
import org.opennms.web.navigate.DisplayStatus;

public class TopoMapNavEntry implements ConditionalPageNavEntry {
    private String m_name;

    @Override
    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = name;
    }

    @Override
    public String getUrl() {
        return new TopologyLinkBuilder()
                .provider(TopologyProvider.ENLINKD)
                .focus("%nodeid%")
                .szl(1).getLink();
    }

    @Override
    public DisplayStatus evaluate(final HttpServletRequest request, final Object target) {
        if (target instanceof OnmsNode) {
            final OnmsNode node = (OnmsNode)target;
            if (node != null && node.getId() != null && node.getId() > 0) {
                return DisplayStatus.DISPLAY_LINK;
            }
        }
        return DisplayStatus.NO_DISPLAY;
    }

    @Override
    public String toString() {
        return "TopoMapNavEntry[url=" + getUrl() + ",name=" + m_name +"]";
    }
}
