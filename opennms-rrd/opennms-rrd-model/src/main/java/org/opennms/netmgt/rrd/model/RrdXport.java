/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
