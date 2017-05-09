/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.threshd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Grouping of related threshold definitions
 */
@XmlRootElement(name = "group")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("thresholding.xsd")
public class Group implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * Group name
     */
    @XmlAttribute(name = "name", required = true)
    private String m_name;

    /**
     * Full path to the RRD repository where the data is stored
     *  
     */
    @XmlAttribute(name = "rrdRepository", required = true)
    private String m_rrdRepository;

    /**
     * Threshold definition
     */
    @XmlElement(name = "threshold")
    private List<Threshold> m_thresholds = new ArrayList<>();

    /**
     * Expression definition
     */
    @XmlElement(name = "expression")
    private List<Expression> m_expressions = new ArrayList<>();

    public Group() { }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public String getRrdRepository() {
        return m_rrdRepository;
    }

    public void setRrdRepository(final String rrdRepository) {
        m_rrdRepository = ConfigUtils.assertNotEmpty(rrdRepository, "rrdRepository");
    }

    public List<Threshold> getThresholds() {
        return m_thresholds;
    }

    public void setThresholds(final List<Threshold> thresholds) {
        if (thresholds == m_thresholds) return;
        m_thresholds.clear();
        if (thresholds != null) m_thresholds.addAll(thresholds);
    }

    public void addThreshold(final Threshold threshold) {
        m_thresholds.add(threshold);
    }

    public boolean removeThreshold(final Threshold threshold) {
        return m_thresholds.remove(threshold);
    }

    public List<Expression> getExpressions() {
        return m_expressions;
    }

    public void setExpressions(final List<Expression> expressions) {
        if (expressions == m_expressions) return;
        m_expressions.clear();
        if (expressions != null) m_expressions.addAll(expressions);
    }

    public void addExpression(final Expression expression) {
        m_expressions.add(expression);
    }

    public boolean removeExpression(final Expression expression) {
        return m_expressions.remove(expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, 
                            m_rrdRepository, 
                            m_thresholds, 
                            m_expressions);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Group) {
            final Group that = (Group)obj;
            return Objects.equals(this.m_name, that.m_name)
                    && Objects.equals(this.m_rrdRepository, that.m_rrdRepository)
                    && Objects.equals(this.m_thresholds, that.m_thresholds)
                    && Objects.equals(this.m_expressions, that.m_expressions);
        }
        return false;
    }

}
