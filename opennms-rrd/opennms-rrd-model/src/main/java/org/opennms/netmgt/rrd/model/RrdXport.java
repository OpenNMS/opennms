/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.rrd.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class XPort (the XML representation of the 'rrdtool xport' command).
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
@XmlRootElement(name="xport")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class RrdXport {

    /** The meta data. */
    private XMeta meta;

    /** The rows. */
    private List<XRow> rows = new ArrayList<>();

    /**
     * Gets the meta data.
     *
     * @return the meta data
     */
    @XmlElement(name="meta")
    public XMeta getMeta() {
        return meta;
    }

    /**
     * Sets the meta data.
     *
     * @param meta the new meta data
     */
    public void setMeta(XMeta meta) {
        this.meta = meta;
    }

    /**
     * Gets the rows.
     *
     * @return the rows
     */
    @XmlElement(name="row")
    @XmlElementWrapper(name="data")
    public List<XRow> getRows() {
        return rows;
    }

    /**
     * Sets the rows.
     *
     * @param rows the new rows
     */
    public void setRows(List<XRow> rows) {
        this.rows = rows;
    }

}
