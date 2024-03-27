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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * The Class XRow (XPort Row).
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
@XmlRootElement(name="row")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class XRow extends Row {

    /** The time stamp expressed in seconds since 1970-01-01 UTC. */
    private Long timestamp;

    /**
     * Gets the time stamp.
     * <p>Expressed in seconds since 1970-01-01 UTC</p>
     * 
     * @return the time stamp
     */
    @XmlElement(name="t")
    @XmlJavaTypeAdapter(LongAdapter.class)
    public Long getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the time stamp.
     *
     * @param timestamp the new time stamp
     */
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

}
