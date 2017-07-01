/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.poller.outages;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Node to which the outage applies.
 */

@XmlRootElement(name="node", namespace="http://xmlns.opennms.org/xsd/config/poller/outages")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("poll-outages.xsd")
public class Node implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name="id", required=true)
    private Integer m_id;

    public Node() {
    }

    public Integer getId() {
        return m_id == null? 0 : m_id;
    }

    public void setId(final Integer id) {
        m_id = ConfigUtils.assertNotNull(id, "id");
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_id);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj )
            return true;

        if (obj instanceof Node) {
            final Node that = (Node)obj;
            return Objects.equals(this.m_id, that.m_id);
        }
        return false;
    }

}
