/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist.requisition;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonRootName;
import org.opennms.core.config.api.JaxbListWrapper;

/**
 * The Class DeployedRequisitionStats.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name="foreign-source")
@JsonRootName("foreign-source")
@XmlAccessorType(XmlAccessType.NONE)
public class DeployedRequisitionStats extends JaxbListWrapper<String> {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new deployed requisition stats.
     */
    public DeployedRequisitionStats() { super(); }

    /** The foreign source. */
    private String foreignSource;

    /**
     * Gets the foreign source.
     *
     * @return the foreign source
     */
    @XmlAttribute(name="name")
    @JsonProperty("name")
    public String getForeignSource() {
        return foreignSource;
    }

    /**
     * Gets the foreign ids.
     *
     * @return the foreign ids
     */
    @XmlElement(name="foreign-id")
    @JsonProperty("foreign-id")
    public List<String> getForeignIds() {
        return getObjects();
    }

    /**
     * Sets the foreign source.
     *
     * @param foreignSource the new foreign source
     */
    public void setForeignSource(String foreignSource) {
        this.foreignSource = foreignSource;
    }

    /**
     * Sets the foreign ids.
     *
     * @param foreignIds the new foreign ids
     */
    public void setForeignIds(List<String> foreignIds) {
        this.clear();
        this.addAll(foreignIds);
    }

}
