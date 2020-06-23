/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.category;

import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;
import org.opennms.core.config.api.JaxbListWrapper;

@XmlRootElement(name="nodes")
@JsonRootName("nodes")
public class NodeList extends JaxbListWrapper<AvailabilityNode> {
    private static final long serialVersionUID = 1L;

    public NodeList() { super(); }
    public NodeList(final Collection<? extends AvailabilityNode> nodes) {
        super(nodes);
    }

    public static NodeList forNodes(final Collection<? extends org.opennms.netmgt.xml.rtc.Node> nodes) {
        final NodeList nl = new NodeList();
        for (final org.opennms.netmgt.xml.rtc.Node n : nodes) {
            nl.add(new AvailabilityNode(n));
        }
        return nl;
    }

    @XmlElement(name="node")
    @JsonProperty("node")
    public List<AvailabilityNode> getObjects() {
        return super.getObjects();
    }
}
