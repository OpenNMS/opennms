/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
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
       return com.google.common.base.Objects.toStringHelper(this)
                 .add("Label", this.label)
                 .add("Resource ID", this.resourceId)
                 .add("Attribute", this.attribute)
                 .add("Datasource", this.datasource)
                 .add("Transient", this.isTransient)
                 .toString();
    }
}
