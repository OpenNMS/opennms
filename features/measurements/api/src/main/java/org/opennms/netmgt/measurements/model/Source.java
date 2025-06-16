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
package org.opennms.netmgt.measurements.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.google.common.base.Preconditions;

/**
 * Measurement source.
 *
 * @author Jesse White <jesse@opennms.org>
 * @author Dustin Frisch <fooker@lab.sh>
 */
@XmlRootElement(name = "source")
@XmlAccessorType(XmlAccessType.NONE)
public class Source {
    /**
     * Used to identify the values of this source.
     * Should be unique amongst all expressions and other sources.
     */
    private String label;

    /**
     * Resource ID.
     * i.e. node[1].interfaceSnmp[eth0-04013f75f101]
     */
    private String resourceId;

    /**
     * Attribute name.
     * i.e. ifInOctets
     */
    private String attribute;

    /**
     * fallback Attribute name.
     * i.e. ifInOctets
     */
    private String fallbackAttribute;

    /**
     * Data source name. Typically the same as the attribute name, but may differ
     * if an attribute contains multiple data sources.
     *
     * i.e. ping1Micro
     */
    private String datasource;

    /**
     * Aggregation function.
     * Should be one of AVERAGE, MIN, MAX, LAST.
     */
    private String aggregation = "AVERAGE";

    /**
     * Enable to exclude this source from the response, but
     * allow expression to derive values from it.
     */
    private boolean isTransient = false;

    public Source() {
    }

    public Source(final String label,
                  final String resourceId,
                  final String attribute,
                  final String datasource,
                  final boolean isTransient) {
        this.label = Preconditions.checkNotNull(label, "label argument");
        this.resourceId = Preconditions.checkNotNull(resourceId, "resourceId argument");
        this.attribute = Preconditions.checkNotNull(attribute, "attribute argument");
        this.datasource = datasource;
        this.isTransient = isTransient;
    }

    @XmlAttribute(name = "label")
    public String getLabel() {
        return this.label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    @XmlAttribute(name = "resourceId")
    public String getResourceId() {
        return this.resourceId;
    }

    public void setResourceId(final String resourceId) {
        this.resourceId = resourceId;
    }

    @XmlAttribute(name = "attribute")
    public String getAttribute() {
        return this.attribute;
    }

    public void setAttribute(final String attribute) {
        this.attribute = attribute;
    }

    @XmlAttribute(name = "fallback-attribute")
    public String getFallbackAttribute() {
        return this.fallbackAttribute;
    }

    public void setFallbackAttribute(final String fallbackAttribute) {
        this.fallbackAttribute = fallbackAttribute;
    }

    @XmlAttribute(name = "datasource")
    public String getDataSource() {
        return this.datasource;
    }

    public void setDataSource(final String datasource) {
        this.datasource = datasource;
    }

    /**
     * In order to preserve backwards compatibility, we allow the datasource field to be null.
     */
    @XmlTransient
    public String getEffectiveDataSource() {
        return this.datasource != null ? this.datasource : this.attribute;
    }

    @XmlAttribute(name = "aggregation")
    public String getAggregation() {
        return this.aggregation;
    }

    public void setAggregation(final String aggregation) {
        this.aggregation = aggregation;
    }

    @XmlAttribute(name = "transient")
    public boolean getTransient() {
        return isTransient;
    };

    public void setTransient(final boolean isTransient) {
        this.isTransient = isTransient;
    }

    @Override
    public boolean equals(Object obj) {
       if (obj == null) {
          return false;
       }
       if (getClass() != obj.getClass()) {
          return false;
       }
       final Source other = (Source) obj;

       return   com.google.common.base.Objects.equal(this.label, other.label)
             && com.google.common.base.Objects.equal(this.resourceId, other.resourceId)
             && com.google.common.base.Objects.equal(this.attribute, other.attribute)
             && com.google.common.base.Objects.equal(this.datasource, other.datasource)
             && com.google.common.base.Objects.equal(this.isTransient, other.isTransient);
    }

    @Override
    public int hashCode() {
       return com.google.common.base.Objects.hashCode(
                 this.label, this.resourceId, this.attribute, this.datasource, this.isTransient);
    }

    @Override
    public String toString() {
       return com.google.common.base.MoreObjects.toStringHelper(this)
                 .add("Label", this.label)
                 .add("Resource ID", this.resourceId)
                 .add("Attribute", this.attribute)
                 .add("Datasource", this.datasource)
                 .add("Transient", this.isTransient)
                 .toString();
    }
}
