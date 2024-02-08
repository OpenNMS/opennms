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
package org.opennms.netmgt.provision.persist.requisition;

import java.util.Date;
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
     * Instantiates a new deployed requisition statistics.
     */
    public DeployedRequisitionStats() { super(); }

    /** The foreign source. */
    private String foreignSource;

    /** The last imported date. */
    private Date lastImported;

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
     * Gets the foreign source.
     *
     * @return the foreign source
     */
    @XmlAttribute(name="last-imported")
    @JsonProperty("last-imported")
    public Date getLastImported() {
        return lastImported;
    }

    /**
     * Gets the foreign IDs.
     *
     * @return the foreign IDs
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
     * Sets the last imported date.
     *
     * @param lastImported the new last imported date
     */
    public void setLastImported(Date lastImported) {
        this.lastImported = lastImported;
    }

    /**
     * Sets the foreign IDs.
     *
     * @param foreignIds the new foreign IDs
     */
    public void setForeignIds(List<String> foreignIds) {
        this.clear();
        this.addAll(foreignIds);
    }

}
